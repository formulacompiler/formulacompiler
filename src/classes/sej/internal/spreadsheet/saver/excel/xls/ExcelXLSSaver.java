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
import java.io.OutputStream;
import java.util.Date;
import java.util.Locale;
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

import jxl.JXLException;
import jxl.WorkbookSettings;
import jxl.write.WritableCell;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

public final class ExcelXLSSaver implements SpreadsheetSaver
{
	private final Spreadsheet model;
	private final OutputStream outputStream;
	private final ExcelXLSExpressionFormatter formatter = new ExcelXLSExpressionFormatter();


	public ExcelXLSSaver(Config _config)
	{
		super();
		this.model = _config.spreadsheet;
		this.outputStream = _config.outputStream;
	}


	public void save() throws IOException, SpreadsheetException
	{
		final SpreadsheetImpl wb = (SpreadsheetImpl) this.model;
		final WorkbookSettings xset = new jxl.WorkbookSettings();
		xset.setLocale( new Locale( "en", "EN" ) );
		final jxl.write.WritableWorkbook xwb = jxl.Workbook.createWorkbook( this.outputStream, xset );
		try {
			saveWorkbook( wb, xwb );
			xwb.write();
			xwb.close();
		}
		catch (JXLException e) {
			throw new SpreadsheetException.SaveError( e );
		}
	}

	private void saveWorkbook( SpreadsheetImpl _wb, WritableWorkbook _xwb ) throws JXLException, SpreadsheetException
	{
		for (final SheetImpl s : _wb.getSheetList()) {
			final jxl.write.WritableSheet xs = _xwb.createSheet( s.getName(), s.getSheetIndex() );
			saveSheet( s, _xwb, xs );
		}
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
	}


	private void saveCell( CellInstance _c, WritableWorkbook _xwb, WritableSheet _xs, int _row ) throws JXLException,
			SpreadsheetException
	{
		final int col = _c.getColumnIndex();
		final jxl.write.WritableCell xc = createCell( _c, _xwb, col, _row );
		_xs.addCell( xc );
	}


	private WritableCell createCell( CellInstance _c, WritableWorkbook _xwb, int _col, int _row )
			throws SpreadsheetException
	{
		final ExpressionNode expr = _c.getExpression();
		if (null != expr) {
			return new jxl.write.Formula( _col, _row, this.formatter.format( expr ) );
		}
		else {
			final Object val = _c.getValue();
			if (val instanceof String) {
				return new jxl.write.Label( _col, _row, ((String) val) );
			}
			if (val instanceof Date) {
				return new jxl.write.DateTime( _col, _row, ((Date) val) );
			}
			if (val instanceof Boolean) {
				return new jxl.write.Boolean( _col, _row, ((Boolean) val) );
			}
			if (val instanceof Number) {
				return new jxl.write.Number( _col, _row, ((Number) val).doubleValue() );
			}
		}
		return new jxl.write.Blank( _col, _row );
	}


}
