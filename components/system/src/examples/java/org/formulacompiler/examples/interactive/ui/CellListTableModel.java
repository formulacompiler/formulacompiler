/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.examples.interactive.ui;


import org.formulacompiler.examples.interactive.controller.MainWindowController.CellListEntry;
import org.formulacompiler.examples.interactive.controller.MainWindowController.CellListModel;

public class CellListTableModel extends AbstractTableModel
{
	private static final int CELL_COL = 0;
	private static final int VALUE_COL = 1;
	private final CellListModel cellList;


	public CellListTableModel(CellListModel _list)
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
