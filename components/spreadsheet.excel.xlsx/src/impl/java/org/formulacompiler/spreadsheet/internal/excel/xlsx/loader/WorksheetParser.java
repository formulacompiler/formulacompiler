/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.parser.CellRefFormat;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRefParser;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithError;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.CellWithSharedExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.StylesheetParser.Style;
import org.formulacompiler.spreadsheet.internal.parser.LazySpreadsheetExpressionParser;


/**
 * @author Igor Didyuk
 */
final class WorksheetParser extends XmlParser
{

	private final StylesheetParser stylesheet;
	private final SharedStringsParser sharedStrings;
	private final Map<String, CellWithExpression> sharedFormulas = New.hashMap();
	private final SpreadsheetLoader.Config config;

	WorksheetParser( final PackageLoader _loader, final String _entryPath,
			final StylesheetParser _stylesheet, final SharedStringsParser _sharedStrings,
			final SpreadsheetLoader.Config _config ) throws XMLStreamException
	{
		super( _loader.getEntry( _entryPath ) );
		this.stylesheet = _stylesheet;
		this.sharedStrings = _sharedStrings;
		this.config = _config;
	}

	void parse( final SheetImpl _sheet ) throws XMLStreamException, SpreadsheetException
	{
		StartElement se;
		while ((se = find( XMLConstants.ROW_PATH )) != null) {
			final Attribute rowIndex = se.getAttributeByName( XMLConstants.Main.ROW_INDEX );
			if (rowIndex != null) {
				final int ri = Integer.parseInt( rowIndex.getValue() );
				final int rc = _sheet.getRowList().size();
				for (int i = rc; i != ri - 1; i++)
					new RowImpl( _sheet );
			}
			final RowImpl row = new RowImpl( _sheet );
			if (rowIndex != null)
				if (row.getRowIndex() != Integer.parseInt( rowIndex.getValue() ) - 1)
					throw new SpreadsheetException.LoadError( "Row with index = " + rowIndex.getValue() + " was not found." );

			final int rowContext = getContext();
			while ((se = find( XMLConstants.Main.CELL, rowContext )) != null)
				parseCell( row, se );
		}
	}

	private Number parseNumberCellValue( Attribute _styleIndex, String _value )
	{
		final double value = Double.parseDouble( _value );

		if (_styleIndex == null)
			return value;

		final Style cellStyle = this.stylesheet.getStyle( Integer.parseInt( _styleIndex.getValue() ) );
		if (cellStyle != null) {
			if (!cellStyle.isDate() && cellStyle.isTime())
				return value < 365.0 ? new Duration( value ) : new LocalDate( value );

			if (cellStyle.isDate())
				//LATER: need to check for global time format & global timezone
				return value < 1.0 ? new Duration( value ) : new LocalDate( value );
		}
		return value;
	}

	private Object parseCellValue( Attribute _dataType, Attribute _styleIndex, String _value ) throws XMLStreamException, SpreadsheetException
	{
		if (_dataType == null) // number format is defined in the cell style
			return parseNumberCellValue( _styleIndex, _value );
		final String dataTypeId = _dataType.getValue();

		if (dataTypeId.equals( XMLConstants.CELL_TYPE_NUMBER ))
			return parseNumberCellValue( _styleIndex, _value );

		if (dataTypeId.equals( XMLConstants.CELL_TYPE_BOOLEAN ))
			return _value.equals( XMLConstants.TRUE );

		if (dataTypeId.equals( XMLConstants.CELL_TYPE_SHARED_STRING ))
			return this.sharedStrings.getString( Integer.parseInt( _value ) );

		if (dataTypeId.equals( XMLConstants.CELL_TYPE_INLINE_STRING ))
			return _value;

		if (dataTypeId.equals( XMLConstants.CELL_TYPE_ERROR ))
			return _value;

		throw new SpreadsheetException.LoadError( "Cells of type \"" + _dataType + "\" are not supported!" );
	}

	private void parseCell( final RowImpl _row, final StartElement _cell ) throws XMLStreamException, SpreadsheetException
	{
		final Attribute reference = _cell.getAttributeByName( XMLConstants.Main.CELL_REFERENCE );
		final Attribute dataType = _cell.getAttributeByName( XMLConstants.Main.CELL_TYPE );
		final Attribute styleIndex = _cell.getAttributeByName( XMLConstants.Main.CELL_STYLE );

		final SheetImpl sheet = _row.getSheet();
		final CellIndex upperLeftCellIndex = new CellIndex( sheet.getSpreadsheet(), sheet.getSheetIndex(), 0, 0 );
		final CellIndex cellIndex = CellRefParser.A1.parseCellA1( reference.getValue(), upperLeftCellIndex );

		String formula = null;
		String formulaIndex = null;

		String value = null;

		final int cellContext = getContext();
		StartElement se;
		while ((se = findAny( cellContext )) != null) {
			final QName name = se.getName();
			if (name.equals( XMLConstants.Main.CELL_FORMULA )) {
				final Attribute formulaType = se.getAttributeByName( XMLConstants.Main.CELL_FORMULA_TYPE );
				if (formulaType != null && "shared".equals( formulaType.getValue() ))
					formulaIndex = se.getAttributeByName( XMLConstants.Main.CELL_FORMULA_SHARED_INDEX ).getValue();
				formula = getText();
			}
			else if (name.equals( XMLConstants.Main.CELL_VALUE )) {
				value = getText();
			}
		}

		if (formula == null && formulaIndex == null && value == null)
			return;

		final int columnIndex = cellIndex.getColumnIndex();
		while (columnIndex != _row.getCellList().size())
			_row.getCellList().add( null );

		if (formula != null) {
			final CellWithLazilyParsedExpression exprCell = new CellWithLazilyParsedExpression(
					_row, new LazySpreadsheetExpressionParser( formula, CellRefFormat.A1_OOXML ) );
			if (formulaIndex != null) {
				sharedFormulas.put( formulaIndex, exprCell );
			}

			if (this.config.loadAllCellValues)
				exprCell.setValue( parseCellValue( dataType, styleIndex, value ) );

			return;
		}

		if (formulaIndex != null) {
			final CellWithExpression expression = sharedFormulas.get( formulaIndex );
			final CellWithExpression exprCell = new CellWithSharedExpression( _row, expression );

			if (this.config.loadAllCellValues)
				exprCell.setValue( parseCellValue( dataType, styleIndex, value ) );

			return;
		}

		// Cell with constant
		if (dataType != null && XMLConstants.CELL_TYPE_ERROR.equals( dataType.getValue() ))
			new CellWithError( _row, value );
		else
			new CellWithConstant( _row, parseCellValue( dataType, styleIndex, value ) );
	}
}
