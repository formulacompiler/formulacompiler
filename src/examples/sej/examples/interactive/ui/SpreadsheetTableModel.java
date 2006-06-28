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
package sej.examples.interactive.ui;

import sej.Spreadsheet;
import sej.Spreadsheet.Cell;
import sej.Spreadsheet.Row;
import sej.Spreadsheet.Sheet;
import sej.examples.interactive.controller.MainWindowController.SpreadsheetModel;

public class SpreadsheetTableModel extends AbstractTableModel
{
	private final SpreadsheetModel spreadsheet;


	public SpreadsheetTableModel(SpreadsheetModel _spreadsheet)
	{
		assert null != _spreadsheet;
		this.spreadsheet = _spreadsheet;
		listenToController( _spreadsheet.getListeners() );
	}


	public Spreadsheet getSpreadsheetModel()
	{
		return this.spreadsheet.getSpreadsheet();
	}


	public Sheet getSheet()
	{
		return (null == getSpreadsheetModel()) ? null : getSpreadsheetModel().getSheets()[ 0 ];
	}


	public int getRowCount()
	{
		final Sheet sheet = getSheet();
		if (null == sheet) return 1;
		return sheet.getRows().length;
	}


	public int getColumnCount()
	{
		final Sheet sheet = getSheet();
		if (null == sheet) return 1;
		int result = 0;
		for (Row row : sheet.getRows()) {
			int len = row.getCells().length;
			if (len > result) result = len;
		}
		return result;
	}


	public String getColumnName( int _columnIndex )
	{
		return "Col??";
		//return Sheet.getCanonicalNameForColumnIndex( _columnIndex );
	}


	public Class<?> getColumnClass( int _columnIndex )
	{
		return Object.class;
	}


	public boolean isCellEditable( int _rowIndex, int _columnIndex )
	{
		return false;
	}


	public Object getValueAt( int _rowIndex, int _columnIndex )
	{
		if (null == getSheet()) {
			return "Click <Open spreadsheet file> to load a spreadsheet";
		}
		else {
			Row row = getSheet().getRows()[ _rowIndex ];
			if (null != row) {
				final Cell[] cells = row.getCells();
				Cell cell = _columnIndex < cells.length ? cells[ _columnIndex ] : null;
				if (null != cell) {
					return cell.getConstantValue();
				}
			}
			return null;
		}
	}


	public void setValueAt( Object _value, int _rowIndex, int _columnIndex )
	{
		// Not implemented
	}


}
