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

import sej.examples.interactive.controller.MainWindowController.SpreadsheetModel;
import sej.expressions.ExpressionNode;
import sej.model.CellInstance;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;

public class SpreadsheetTableModel extends AbstractTableModel
{
	private final SpreadsheetModel spreadsheet;


	public SpreadsheetTableModel(SpreadsheetModel _spreadsheet)
	{
		assert null != _spreadsheet;
		this.spreadsheet = _spreadsheet;
		listenToController( _spreadsheet.getListeners() );
	}


	public Workbook getWorkbook()
	{
		return this.spreadsheet.getWorkbook();
	}


	public Sheet getSheet()
	{
		return (null == getWorkbook()) ? null : getWorkbook().getSheets().get( 0 );
	}


	public int getRowCount()
	{
		if (null == getSheet()) return 1;
		return getSheet().getRows().size();
	}


	public int getColumnCount()
	{
		if (null == getSheet()) return 1;
		return getSheet().getMaxColumnCount();
	}


	public String getColumnName( int _columnIndex )
	{
		return Sheet.getCanonicalNameForColumnIndex( _columnIndex );
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
			Row row = getSheet().getRows().get( _rowIndex );
			if (null != row) {
				CellInstance cell = _columnIndex < row.getCells().size() ? row.getCells().get( _columnIndex )
						: null;
				if (null != cell) {
					final ExpressionNode expression = cell.getExpression();
					if (null == expression) {
						return cell.getValue();
					}
					else {
						return expression.toString();
					}
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
