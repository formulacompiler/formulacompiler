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

package org.formulacompiler.examples.interactive.ui;

import org.formulacompiler.examples.interactive.controller.MainWindowController.SpreadsheetModel;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;
import org.formulacompiler.spreadsheet.Spreadsheet.Sheet;


public class SpreadsheetTableModel extends AbstractTableModel
{
	private final SpreadsheetModel spreadsheet;


	public SpreadsheetTableModel( SpreadsheetModel _spreadsheet )
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
		// return Sheet.getCanonicalNameForColumnIndex( _columnIndex );
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
