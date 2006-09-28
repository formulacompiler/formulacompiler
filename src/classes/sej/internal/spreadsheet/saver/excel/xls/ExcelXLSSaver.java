/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package sej.internal.spreadsheet.saver.excel.xls;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import sej.Spreadsheet;
import sej.SpreadsheetException;
import sej.SpreadsheetSaver;
import sej.internal.expressions.ExpressionNode;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.Reference;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;

import jxl.Cell;
import jxl.CellView;
import jxl.JXLException;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.DisplayFormat;
import jxl.format.Border;
import jxl.format.CellFormat;
import jxl.read.biff.BiffException;
import jxl.write.Blank;
import jxl.write.DateFormats;
import jxl.write.Formula;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public final class ExcelXLSSaver implements SpreadsheetSaver
{
	private final Spreadsheet model;
	private final OutputStream outputStream;
	private final ExcelXLSExpressionFormatter formatter = new ExcelXLSExpressionFormatter();
	private final Workbook template;
	private final Sheet templateSheet;


	public ExcelXLSSaver(Config _config) throws IOException, SpreadsheetException
	{
		super();
		this.model = _config.spreadsheet;
		this.outputStream = _config.outputStream;
		this.template = (null == _config.templateInputStream) ? null : loadTemplate( _config.templateInputStream );
		this.templateSheet = (null == this.template) ? null : this.template.getSheet( 0 );
	}


	private static Workbook loadTemplate( InputStream _stream ) throws IOException, SpreadsheetException
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


	public void save() throws IOException, SpreadsheetException
	{
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

	private WritableWorkbook createWorkbook() throws IOException
	{
		final WorkbookSettings xset = new WorkbookSettings();
		xset.setLocale( new Locale( "en", "EN" ) );
		if (null == this.template) {
			return Workbook.createWorkbook( this.outputStream, xset );
		}
		else {
			final WritableWorkbook xwb = Workbook.createWorkbook( this.outputStream, this.template, xset );
			for (int i = xwb.getSheets().length - 1; i >= 0; i--) {
				xwb.removeSheet( i );
			}
			xwb.removeAllAreaNames();
			return xwb;
		}
	}


	private void saveWorkbook( SpreadsheetImpl _wb, WritableWorkbook _xwb ) throws JXLException, SpreadsheetException
	{
		saveSheets( _wb, _xwb );
		saveNames( _wb, _xwb );
	}


	private void saveNames( SpreadsheetImpl _wb, WritableWorkbook _xwb )
	{
		for (final Entry<String, Reference> nd : _wb.getNameMap().entrySet()) {
			final String name = nd.getKey();
			final Reference ref = nd.getValue();
			if (ref instanceof CellIndex) {
				final CellIndex cell = (CellIndex) ref;
				_xwb.addNameArea( name, _xwb.getSheet( cell.sheetIndex ), cell.columnIndex, cell.rowIndex,
						cell.columnIndex, cell.rowIndex );
			}
			else if (ref instanceof CellRange) {
				final CellRange range = (CellRange) ref;
				final CellIndex from = range.getFrom();
				final CellIndex to = range.getTo();
				if (from.sheetIndex == to.sheetIndex) {
					_xwb.addNameArea( name, _xwb.getSheet( from.sheetIndex ), from.columnIndex, from.rowIndex,
							to.columnIndex, to.rowIndex );
				}
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
				return new jxl.write.DateTime( _col, _row, ((Date) val), jxl.write.DateTime.GMT );
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


	private final Map<String, CellView> colStyles = new HashMap<String, CellView>();
	private final Map<String, CellView> rowStyles = new HashMap<String, CellView>();

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


	private final Map<String, CellFormat> cellStyles = new HashMap<String, CellFormat>();

	private CellFormat getCellStyle( String _styleName ) throws JXLException
	{
		final Map<String, CellFormat> styles = this.cellStyles;
		if (styles.containsKey( _styleName )) {
			return styles.get( _styleName );
		}
		else {
			final Cell styleCell = getTemplateCell( _styleName );
			if (null != styleCell) {
				final CellFormat styleFormat = styleCell.getCellFormat();
				final WritableCellFormat targetFormat = new WritableCellFormat();

				copyCellAttributes( styleFormat, targetFormat );

				styles.put( _styleName, targetFormat );
				return targetFormat;
			}
			else {
				styles.put( _styleName, null );
				return null;
			}
		}
	}

	private static final Border[] BORDERS = new Border[] { Border.TOP, Border.BOTTOM, Border.LEFT, Border.RIGHT };

	private void copyCellAttributes( CellFormat _source, WritableCellFormat _target ) throws JXLException
	{
		_target.setAlignment( _source.getAlignment() );
		_target.setBackground( _source.getBackgroundColour(), _source.getPattern() );
		for (Border b : BORDERS) {
			_target.setBorder( b, _source.getBorderLine( b ), _source.getBorderColour( b ) );
		}
		_target.setFont( new WritableFont( _source.getFont() ) );
		_target.setIndentation( _source.getIndentation() );
		_target.setLocked( _source.isLocked() );
		_target.setOrientation( _source.getOrientation() );
		_target.setShrinkToFit( _source.isShrinkToFit() );
		_target.setVerticalAlignment( _source.getVerticalAlignment() );
		_target.setWrap( _source.getWrap() );
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
			throws JXLException
	{
		final CellFormat style = getCellStyle( _styleName );
		final DisplayFormat displayFormat = getCellDisplayFormat( _xc );
		if (null != style) {
			if (null != displayFormat) {
				final WritableCellFormat custom = new WritableCellFormat( displayFormat );
				copyCellAttributes( style, custom );
				_xc.setCellFormat( custom );
			}
			else {
				_xc.setCellFormat( style );
			}
		}
		else if (null != displayFormat) {
			_xc.setCellFormat( getCellFormatFor( displayFormat ) );
		}
	}

	private DisplayFormat getCellDisplayFormat( WritableCell _xc )
	{
		if (_xc instanceof jxl.write.DateTime) {
			return DateFormats.DEFAULT;
		}
		return null;
	}

	private CellFormat getCellFormatFor( DisplayFormat _displayFormat )
	{
		CellFormat result = this.cellFormats.get( _displayFormat );
		if (null == result) {
			result = new WritableCellFormat( _displayFormat );
			this.cellFormats.put( _displayFormat, result );
		}
		return result;
	}

	private final Map<DisplayFormat, CellFormat> cellFormats = new HashMap<DisplayFormat, CellFormat>();


}
