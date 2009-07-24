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

package org.formulacompiler.examples.interactive.ui;


import org.formulacompiler.examples.interactive.controller.MainWindowController.CellListEntry;
import org.formulacompiler.examples.interactive.controller.MainWindowController.CellListModel;

public class CellListTableModel extends AbstractTableModel
{
	private static final int CELL_COL = 0;
	private static final int VALUE_COL = 1;
	private final CellListModel cellList;


	public CellListTableModel( CellListModel _list )
	{
		assert null != _list;
		this.cellList = _list;
		listenToController( _list.getListeners() );
	}


	public CellListModel getCellList()
	{
		return this.cellList;
	}


	public int getRowCount()
	{
		return getCellList().getCells().size();
	}


	public int getColumnCount()
	{
		return 2;
	}


	public String getColumnName( int _columnIndex )
	{
		switch (_columnIndex) {
			case CELL_COL:
				return "Cell";
			case VALUE_COL:
				return "Value";
			default:
				return "ERROR";
		}
	}


	public Class<?> getColumnClass( int _columnIndex )
	{
		switch (_columnIndex) {
			case VALUE_COL:
				return Double.class;
			default:
				return String.class;
		}
	}


	public boolean isCellEditable( int _rowIndex, int _columnIndex )
	{
		return (VALUE_COL == _columnIndex);
	}


	public Object getValueAt( int _rowIndex, int _columnIndex )
	{
		final CellListEntry e = getCellList().getCells().get( _rowIndex );
		switch (_columnIndex) {
			case CELL_COL:
				return "Cell??";
				// return Sheet.getCanonicalNameForCellIndex( e.index.columnIndex, e.index.rowIndex );
			case VALUE_COL:
				return e.value;
			default:
				return "ERROR";
		}
	}


	public void setValueAt( Object _value, int _rowIndex, int _columnIndex )
	{
		final CellListEntry e = getCellList().getCells().get( _rowIndex );
		switch (_columnIndex) {
			case VALUE_COL:
				e.value = (Double) _value;
				break;
			default:
				break;
		}
	}

}
