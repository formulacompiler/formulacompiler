/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.spreadsheet.internal.excel.xlsx.saver;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template.Stylesheet;


/**
 * @author Igor Didyuk
 */
class WorksheetWriter extends ContentXmlWriter
{

	private static final String CONTENT_TYPE = "worksheet+xml";
	private static final String RELATIONSHIP_TYPE = "worksheet";

	private final TimeZone timeZone;
	private final SharedStringsWriter.StringList sharedStrings;
	private final Stylesheet template;

	WorksheetWriter( ZipOutputStream _outputStream, String _path, TimeZone _timeZone,
			SharedStringsWriter.StringList _sharedStrings,
			Stylesheet _template ) throws XMLStreamException, IOException
	{
		super( _outputStream, _path, XMLConstants.Main.XMLNS );
		this.timeZone = _timeZone;
		this.sharedStrings = _sharedStrings;
		this.template = _template;
	}

	private double dateToNum( Date _date )
	{
		return RuntimeDouble_v2.dateToNum( _date, this.timeZone, ComputationMode.EXCEL );
	}

	private void writeCellValue( Object _value ) throws XMLStreamException
	{
		if (_value instanceof Boolean) {
			writeText( ((Boolean) _value).booleanValue() ? XMLConstants.TRUE : XMLConstants.FALSE );
		}
		else if (_value instanceof Date) {
			writeText( String.valueOf( dateToNum( (Date) _value ) ) );
		}
		else if (_value instanceof Number) {
			writeText( String.valueOf( ((Number) _value).doubleValue() ) );
		}
		else if (_value instanceof String) {
			writeText( String.valueOf( this.sharedStrings.add( (String) _value ) ) );
		}
	}

	/**
	 * @param date - Date-Time value to test
	 * @return true if the date contains fractional part with time not less than one second
	 */
	private static boolean dateContainsTime( double date )
	{
		date -= Math.round( date );
		return Math.abs( date ) > 1 / 24 / 60 / 60;
	}

	private void writeCellStyle( Object _value, String _styleName ) throws XMLStreamException
	{
		boolean isDate = _value != null && (_value instanceof Date || _value instanceof LocalDate);
		boolean isTime = _value != null && _value instanceof Duration;
		if (_value != null) {
			if (_value instanceof Duration) {
				isDate = false;
				isTime = true;
			}
			else if (_value instanceof Date) {
				final double date = dateToNum( (Date) _value );
				isDate = true;
				isTime = dateContainsTime( date );
			}
			else if (_value instanceof LocalDate) {
				isDate = true;
				isTime = dateContainsTime( ((LocalDate) _value).doubleValue() );
			}
		}
		final int styleIndex = this.template.getStyleIndex( _styleName, isDate, isTime );

		String cellType = null;
		if (_value != null) {
			if (_value instanceof Boolean) {
				cellType = XMLConstants.CELL_TYPE_BOOLEAN;
			}
			else if (_value instanceof Number) {
				cellType = XMLConstants.CELL_TYPE_NUMBER;
			}
			else if (_value instanceof String) {
				cellType = XMLConstants.CELL_TYPE_SHARED_STRING;
			}
		}
		if (cellType != null)
			writeAttribute( XMLConstants.Main.CELL_TYPE, cellType );

		if (styleIndex != -1) {
			writeAttribute( XMLConstants.Main.CELL_STYLE, Integer.toString( styleIndex ) );
		}
	}

	private void writeRowStyle( String _styleName ) throws XMLStreamException
	{
		if (_styleName == null) return;
		final Stylesheet.RowStyle rowStyle = this.template.getRowStyle( _styleName );
		if (rowStyle == null) return;

		final String height = rowStyle.getHeight();
		if (height != null) {
			writeAttribute( XMLConstants.Main.ROW_HEIGHT, height );
			writeAttribute( XMLConstants.Main.ROW_CUSTOM_HEIGHT, XMLConstants.TRUE );
		}
	}

	void write( Spreadsheet.Sheet _sheet ) throws XMLStreamException, SpreadsheetException
	{
		writeStartElement( XMLConstants.Main.WORKSHEET );
		if (this.template != null) {
			Stylesheet.SheetStyle sheetStyle = this.template.getSheetStyle();
			if (sheetStyle != null) {
				writeStartElement( XMLConstants.Main.SHEET_FORMAT_PROPERTIES );
				if (sheetStyle.getDefaultColWidth() != null)
					writeAttribute( XMLConstants.Main.SHEET_FORMAT_DEFAULT_COL_WIDTH, sheetStyle.getDefaultColWidth() );
				if (sheetStyle.getDefaultRowHeight() != null)
					writeAttribute( XMLConstants.Main.SHEET_FORMAT_DEFAULT_ROW_HEIGHT, sheetStyle.getDefaultRowHeight() );
				writeEndElement( XMLConstants.Main.SHEET_FORMAT_PROPERTIES );
			}

			List<Stylesheet.ColumnStyle> columns = this.template.getColumns();
			if (!columns.isEmpty()) {
				writeStartElement( XMLConstants.Main.COLS );
				for (Stylesheet.ColumnStyle column : columns) {
					writeStartElement( XMLConstants.Main.COL );
					writeAttribute( XMLConstants.Main.COL_MIN, column.getMin() );
					writeAttribute( XMLConstants.Main.COL_MAX, column.getMax() );
					if (column.getStyle() != null)
						writeAttribute( XMLConstants.Main.COL_STYLE, column.getStyle() );
					if (column.getBestFit() != null)
						writeAttribute( XMLConstants.Main.COL_BEST_FIT, column.getBestFit() );
					if (column.getWidth() != null)
						writeAttribute( XMLConstants.Main.COL_WIDTH, column.getWidth() );
					if (column.getCustomWidth() != null)
						writeAttribute( XMLConstants.Main.COL_CUSTOM_WIDTH, column.getCustomWidth() );
					writeEndElement( XMLConstants.Main.COL );
				}
				writeEndElement( XMLConstants.Main.COLS );
			}
		}
		writeStartElement( XMLConstants.Main.SHEET_DATA );
		if (_sheet != null) {
			final List<RowImpl> rows = ((SheetImpl) _sheet).getRowList();
			for (int ri = 0; ri != rows.size(); ri++) {
				final RowImpl row = rows.get( ri );
				writeStartElement( XMLConstants.Main.ROW );
				writeAttribute( XMLConstants.Main.ROW_INDEX, String.valueOf( ri + 1 ) );

				if (row != null) {
					if (this.template != null)
						writeRowStyle( row.getStyleName() );

					final List<CellInstance> cells = row.getCellList();
					for (int ci = 0; ci != cells.size(); ci++) {
						final CellInstance cell = cells.get( ci );
						writeStartElement( XMLConstants.Main.CELL );
						final StringBuilder sb = new StringBuilder();
						CellIndex.appendNameA1ForCellIndex( sb, ci, false, ri, false );
						writeAttribute( XMLConstants.Main.CELL_REFERENCE, sb.toString() );

						final Object value = cell == null ? null : cell.getValue();
						final ExpressionNode expression;
						if (cell != null && cell instanceof CellWithExpression)
							expression = ((CellWithExpression) cell).getExpression();
						else
							expression = null;

						if (cell != null)
							writeCellStyle( value, cell.getStyleName() );

						if (expression != null) {
							writeStartElement( XMLConstants.Main.CELL_FORMULA );
							writeText( ExpressionFormatter.format( expression, cell.getCellIndex() ) );
							writeEndElement( XMLConstants.Main.CELL_FORMULA );
						}

						if (value != null) {
							writeStartElement( XMLConstants.Main.CELL_VALUE );
							writeCellValue( value );
							writeEndElement( XMLConstants.Main.CELL_VALUE );
						}
						writeEndElement( XMLConstants.Main.CELL );
					}
				}

				writeEndElement( XMLConstants.Main.ROW );
			}
		}
		writeEndElement( XMLConstants.Main.SHEET_DATA );
		writeEndElement( XMLConstants.Main.WORKSHEET );
	}

	public Metadata getMetadata()
	{
		return new Metadata( getPath(), CONTENT_TYPE, XMLConstants.DocumentRelationships.XMLNS, RELATIONSHIP_TYPE );
	}
}
