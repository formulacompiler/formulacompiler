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

package org.formulacompiler.spreadsheet.internal.excel.xls.saver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.spreadsheet.internal.BaseRow;
import org.formulacompiler.spreadsheet.internal.BaseSheet;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.saver.SpreadsheetSaverDispatcher;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Name;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

public final class ExcelXLSSaver implements SpreadsheetSaver
{

	private final Spreadsheet model;
	private final OutputStream outputStream;
	private final InputStream templateInputStream;
	private Workbook template;
	private final TimeZone timeZone;

	private final static String TIME_FORMAT = "h:mm:ss";
	private final static String DATE_TIME_FORMAT = "m/d/yy h:mm";
	private final static String DATE_FORMAT = "m/d/yy";
	private CellStyle TIME_STYLE;
	private CellStyle DATE_TIME_STYLE;
	private CellStyle DATE_STYLE;

	private ExcelXLSSaver( Config _config )
	{
		super();
		this.model = _config.spreadsheet;
		this.outputStream = _config.outputStream;
		this.templateInputStream = _config.templateInputStream;
		this.timeZone = (_config.timeZone != null) ? _config.timeZone : TimeZone.getDefault();
	}


	public static final class Factory implements SpreadsheetSaverDispatcher.Factory
	{
		public SpreadsheetSaver newInstance( Config _config )
		{
			return new ExcelXLSSaver( _config );
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".xls" );
		}
	}

	private void initDateTimeStyles( Workbook wb )
	{
		this.DATE_TIME_STYLE = wb.createCellStyle();
		this.DATE_TIME_STYLE.setDataFormat( HSSFDataFormat.getBuiltinFormat( DATE_TIME_FORMAT ) );

		this.DATE_STYLE = wb.createCellStyle();
		this.DATE_STYLE.setDataFormat( HSSFDataFormat.getBuiltinFormat( DATE_FORMAT ) );

		this.TIME_STYLE = wb.createCellStyle();
		this.TIME_STYLE.setDataFormat( HSSFDataFormat.getBuiltinFormat( TIME_FORMAT ) );
	}

	public void save() throws IOException, SpreadsheetException
	{
		this.template = (null == this.templateInputStream) ? null : loadTemplate( this.templateInputStream );
		final BaseSpreadsheet wb = (BaseSpreadsheet) this.model;
		final Workbook xwb = createWorkbook();
		initDateTimeStyles( xwb );
		saveWorkbook( wb, xwb );
		xwb.write( this.outputStream );
		this.outputStream.close();
	}

	private Workbook loadTemplate( InputStream _stream ) throws IOException
	{
		return new HSSFWorkbook( _stream );
	}

	private Workbook createWorkbook()
	{
		if (null == this.template) {
			return new HSSFWorkbook();
		}
		else {
			final Workbook xwb = this.template;
			extractCellFormatsFrom( xwb );
			removeDataFrom( xwb );
			return xwb;
		}
	}

	private void removeDataFrom( final Workbook _xwb )
	{
		for (int i = _xwb.getNumberOfSheets() - 1; i >= 0; i--) {
			_xwb.removeSheetAt( i );
		}
		for (int i = 0; i < _xwb.getNumberOfNames(); i++) {
			_xwb.removeName( i );
		}
	}


	private void saveWorkbook( BaseSpreadsheet _wb, Workbook _xwb ) throws SpreadsheetException
	{
		saveSheets( _wb, _xwb );
		saveNames( _wb, _xwb );
	}


	private void saveNames( BaseSpreadsheet _wb, Workbook _xwb )
	{
		for (final Entry<String, CellRange> nd : _wb.getModelRangeNames().entrySet()) {
			final String name = nd.getKey();
			final CellRange ref = nd.getValue();
			final CellIndex from = ref.getFrom();
			final CellIndex to = ref.getTo();
			if (from.getSheetIndex() == to.getSheetIndex()) {
				final Name namedCel = _xwb.createName();
				namedCel.setNameName( name );
				namedCel.setRefersToFormula( ref.toString() );
			}
		}
	}


	private void saveSheets( BaseSpreadsheet _wb, Workbook _xwb ) throws SpreadsheetException
	{
		for (final BaseSheet s : _wb.getSheetList()) {
			final Sheet xs = _xwb.createSheet( s.getName() );
			saveSheet( s, xs );
		}
	}


	private void saveSheet( BaseSheet _s, Sheet _xs ) throws SpreadsheetException
	{
		for (final BaseRow r : _s.getRowList()) {
			saveRow( r, _xs );
		}
	}


	private void saveRow( BaseRow _r, Sheet _xs ) throws SpreadsheetException
	{
		if (_r != null) {
			final int rowIdx = _r.getRowIndex();
			final Row row = _xs.createRow( rowIdx );
			for (final CellInstance c : _r.getCellList()) {
				saveCell( c, row );
			}
		}
	}


	private void saveCell( CellInstance _c, Row _row ) throws SpreadsheetException
	{
		if (_c != null) {
			final int colIdx = _c.getColumnIndex();
			final Cell xc = createCell( _c, colIdx, _row );
			styleCell( _c.getStyleName(), xc );
		}
	}


	private Cell createCell( CellInstance _c, int _colIdx, Row _row )
			throws SpreadsheetException
	{
		final Cell cell = _row.createCell( _colIdx );
		if (_c instanceof CellWithExpression) {
			final ExpressionNode expr = ((CellWithExpression) _c).getExpression();
			cell.setCellFormula( ExpressionFormatter.format( expr, _c.getCellIndex() ) );
		}
		else {
			final Object val = _c.getValue();
			if (val instanceof String) {
				cell.setCellValue( (String) val );
			}
			else if (val instanceof Date) {
				final GregorianCalendar calendar = new GregorianCalendar( this.timeZone );
				calendar.setTime( (Date) val );
				cell.setCellValue( calendar );
			}
			else if (val instanceof Boolean) {
				cell.setCellValue( (Boolean) val );
			}
			else if (val instanceof Number) {
				final double value = ((Number) val).doubleValue();
				cell.setCellValue( value );
				if (val instanceof LocalDate) {
					if (value % 1 == 0) {
						cell.setCellStyle( this.DATE_STYLE );
					}
					else {
						cell.setCellStyle( this.DATE_TIME_STYLE );
					}
				}
				else if (val instanceof Duration) {
					cell.setCellStyle( this.TIME_STYLE );
				}
			}
		}
		return cell;
	}

	private final Map<String, HSSFCellStyle> cellStyles = New.map();

	private void extractCellFormatsFrom( Workbook _xwb )
	{
		final short styleCount = _xwb.getNumCellStyles();
		for (short idx = 0; idx < styleCount; idx++) {
			final HSSFCellStyle cellStyle = (HSSFCellStyle) _xwb.getCellStyleAt( idx );
			try {
				final String styleName = cellStyle.getParentStyle().getUserStyleName();
				if (styleName != null) {
					this.cellStyles.put( styleName, cellStyle );
				}
			}
			catch (Exception e) {
				// Do nothing, we use only styles with parents
			}
		}
	}

	private HSSFCellStyle getCellStyle( String _styleName )
	{
		return this.cellStyles.get( _styleName );
	}

	private void styleCell( String _styleName, Cell _xc )
	{
		final HSSFCellStyle style = getCellStyle( _styleName );
		if (null != style) {
			_xc.setCellStyle( style );
		}
	}
}
