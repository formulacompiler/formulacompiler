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

package org.formulacompiler.spreadsheet.internal.saver.excel.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TimeZone;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Runtime_v2;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.saver.SpreadsheetSaverDispatcher;

import jxl.Cell;
import jxl.CellView;
import jxl.JXLException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Blank;
import jxl.write.Formula;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public final class ExcelXLSSaver implements SpreadsheetSaver
{
	private final Spreadsheet model;
	private final OutputStream outputStream;
	private final ExcelXLSExpressionFormatter formatter = new ExcelXLSExpressionFormatter();
	private final InputStream templateInputStream;
	private final TimeZone timeZone;
	private Workbook template;
	private Sheet templateSheet;


	public ExcelXLSSaver( Config _config )
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


	public void save() throws IOException, SpreadsheetException
	{
		this.template = (null == this.templateInputStream) ? null : loadTemplate( this.templateInputStream );
		this.templateSheet = (null == this.template) ? null : this.template.getSheet( 0 );

		final SpreadsheetImpl wb = (SpreadsheetImpl) this.model;
		final WritableWorkbook xwb = createWorkbook();
		try {
			saveWorkbook( wb, xwb );
			xwb.write();
			xwb.close();
		}
		catch (JXLException e) {
			throw new SpreadsheetException.SaveError( e );
		}
	}

	private Workbook loadTemplate( InputStream _stream ) throws IOException, SpreadsheetException
	{
		try {
			return Workbook.getWorkbook( _stream );
		}
		catch (BiffException e) {
			throw new SpreadsheetException.LoadError( e );
		}
	}

	private Cell getTemplateCell( String _styleName )
	{
		return (null == this.template) ? null : this.template.findCellByName( _styleName );
	}

	private WritableWorkbook createWorkbook() throws IOException
	{
		final WorkbookSettings xset = new WorkbookSettings();
		xset.setLocale( Locale.ENGLISH );
		if (null == this.template) {
			return Workbook.createWorkbook( this.outputStream, xset );
		}
		else {
			final WritableWorkbook xwb = Workbook.createWorkbook( this.outputStream, this.template, xset );
			extractCellFormatsFrom( xwb );
			removeDataFrom( xwb );
			return xwb;
		}
	}

	private void removeDataFrom( final WritableWorkbook _xwb )
	{
		for (int i = _xwb.getSheets().length - 1; i >= 0; i--) {
			_xwb.removeSheet( i );
		}
		for (String name : _xwb.getRangeNames()) {
			_xwb.removeRangeName( name );
		}
	}


	private void saveWorkbook( SpreadsheetImpl _wb, WritableWorkbook _xwb ) throws JXLException, SpreadsheetException
	{
		saveSheets( _wb, _xwb );
		saveNames( _wb, _xwb );
	}


	private void saveNames( SpreadsheetImpl _wb, WritableWorkbook _xwb )
	{
		for (final Entry<String, CellRange> nd : _wb.getModelRangeNames().entrySet()) {
			final String name = nd.getKey();
			final CellRange ref = nd.getValue();
			final CellIndex from = ref.getFrom();
			final CellIndex to = ref.getTo();
			if (from.sheetIndex == to.sheetIndex) {
				_xwb.addNameArea( name, _xwb.getSheet( from.sheetIndex ), from.columnIndex, from.rowIndex, to.columnIndex,
						to.rowIndex );
			}
		}
	}


	private void saveSheets( SpreadsheetImpl _wb, WritableWorkbook _xwb ) throws JXLException, SpreadsheetException
	{
		for (final SheetImpl s : _wb.getSheetList()) {
			final WritableSheet xs = _xwb.createSheet( s.getName(), s.getSheetIndex() );
			saveSheet( s, _xwb, xs );
		}
	}


	private void saveSheet( SheetImpl _s, WritableWorkbook _xwb, WritableSheet _xs ) throws JXLException,
			SpreadsheetException
	{
		for (final RowImpl r : _s.getRowList()) {
			saveRow( r, _xwb, _xs );
		}
	}


	private void saveRow( RowImpl _r, WritableWorkbook _xwb, WritableSheet _xs ) throws JXLException,
			SpreadsheetException
	{
		final int row = _r.getRowIndex();
		for (final CellInstance c : _r.getCellList()) {
			saveCell( c, _xwb, _xs, row );
		}
		styleRow( _r.getStyleName(), _xwb, _xs, row );
	}


	private void saveCell( CellInstance _c, WritableWorkbook _xwb, WritableSheet _xs, int _row ) throws JXLException,
			SpreadsheetException
	{
		final int col = _c.getColumnIndex();
		final WritableCell xc = createCell( _c, _xwb, col, _row );
		styleCell( _c.getStyleName(), _xwb, _xs, xc );
		_xs.addCell( xc );
		styleColumn( _c.getStyleName(), _xwb, _xs, col );
	}


	private WritableCell createCell( CellInstance _c, WritableWorkbook _xwb, int _col, int _row )
			throws SpreadsheetException
	{
		final ExpressionNode expr = _c.getExpression();
		if (null != expr) {
			return new Formula( _col, _row, this.formatter.format( expr ) );
		}
		else {
			final Object val = _c.getValue();
			if (val instanceof String) {
				return new jxl.write.Label( _col, _row, ((String) val) );
			}
			if (val instanceof Date) {
				final Date date = (Date) val;
				final long msSinceLocal1970 = Runtime_v2.dateToMsSinceLocal1970( date, this.timeZone );
				return new jxl.write.DateTime( _col, _row, new Date( msSinceLocal1970 ), jxl.write.DateTime.GMT );
			}
			if (val instanceof Boolean) {
				return new jxl.write.Boolean( _col, _row, ((Boolean) val) );
			}
			if (val instanceof Number) {
				return new jxl.write.Number( _col, _row, ((Number) val).doubleValue() );
			}
		}
		return new Blank( _col, _row );
	}


	private final Map<String, CellView> colStyles = New.map();
	private final Map<String, CellView> rowStyles = New.map();

	private CellView getRowStyle( String _styleName )
	{
		return getRowOrColStyle( _styleName, this.rowStyles, true );
	}

	private CellView getColumnStyle( String _styleName )
	{
		return getRowOrColStyle( _styleName, this.colStyles, false );
	}

	private CellView getRowOrColStyle( String _styleName, Map<String, CellView> _styles, boolean _isRow )
	{
		if (_styles.containsKey( _styleName )) {
			return _styles.get( _styleName );
		}
		else {
			final Cell styleCell = getTemplateCell( _styleName );
			if (null != styleCell) {
				final CellView styleFormat = _isRow ? this.templateSheet.getRowView( styleCell.getRow() )
						: this.templateSheet.getColumnView( styleCell.getColumn() );
				final CellView targetFormat = new CellView();

				copyRowOrColAttributes( styleFormat, targetFormat );

				_styles.put( _styleName, targetFormat );
				return targetFormat;
			}
			else {
				_styles.put( _styleName, null );
				return null;
			}
		}
	}

	private void copyRowOrColAttributes( CellView _source, CellView _target )
	{
		_target.setSize( _source.getSize() );
	}


	private final Map<String, CellFormat> cellStyles = New.map();

	private void extractCellFormatsFrom( WritableWorkbook _xwb )
	{
		for (final String name : _xwb.getRangeNames()) {
			final WritableCell cell = _xwb.findCellByName( name );
			this.cellStyles.put( name, cell.getCellFormat() );
		}
	}

	private CellFormat getCellStyle( String _styleName )
	{
		return this.cellStyles.get( _styleName );
	}


	private void styleRow( String _styleName, WritableWorkbook _xwb, WritableSheet _xs, int _row ) throws JXLException
	{
		final CellView style = getRowStyle( _styleName );
		if (null != style) {
			_xs.setRowView( _row, style.getSize() );
		}
	}

	private void styleColumn( String _styleName, WritableWorkbook _xwb, WritableSheet _xs, int _col )
	{
		final CellView style = getColumnStyle( _styleName );
		if (null != style) {
			_xs.setColumnView( _col, style );
		}
	}

	private void styleCell( String _styleName, WritableWorkbook _xwb, WritableSheet _xs, WritableCell _xc )
	{
		final CellFormat style = getCellStyle( _styleName );
		if (null != style) {
			_xc.setCellFormat( style );
		}
	}


}
