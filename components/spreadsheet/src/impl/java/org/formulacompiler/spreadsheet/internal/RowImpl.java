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

package org.formulacompiler.spreadsheet.internal;

import java.util.List;

import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;

public final class RowImpl extends AbstractStyledElement implements Spreadsheet.Row
{
	private final SheetImpl sheet;
	private final int rowIndex;
	private final List<CellInstance> cells;


	private RowImpl( final SheetImpl _sheet, final List<CellInstance> _cells )
	{
		this.sheet = _sheet;
		this.rowIndex = _sheet.getRowList().size();
		this.cells = _cells;
		_sheet.getRowList().add( this );
	}

	public RowImpl( SheetImpl _sheet )
	{
		this( _sheet, New.<CellInstance>list() );
	}

	public RowImpl( RowImpl _sameRow )
	{
		this( _sameRow.sheet, _sameRow.cells );
	}


	public SheetImpl getSheet()
	{
		return this.sheet;
	}


	public Cell[] getCells()
	{
		final SpreadsheetImpl spreadsheet = getSheet().getSpreadsheet();
		final int sheetIndex = getSheet().getSheetIndex();
		final int rowIndex = getRowIndex();

		final Cell[] result = new Cell[ this.cells.size() ];
		for (int i = 0; i < this.cells.size(); i++) {
			final CellInstance cellInst = this.cells.get( i );
			if (cellInst == null) {
				result[ i ] = new CellIndex( spreadsheet, sheetIndex, i, rowIndex );
			}
			else {
				result[ i ] = cellInst.getCellIndex();
			}
		}
		return result;
	}


	public int getRowIndex()
	{
		return this.rowIndex;
	}


	public List<CellInstance> getCellList()
	{
		return this.cells;
	}


	public CellInstance getCellOrNull( int _columnIndex )
	{
		if (_columnIndex < getCellList().size()) return getCellList().get( _columnIndex );
		else return null;
	}


	public CellIndex getCellIndex( int _columnIndex )
	{
		return new CellIndex( getSheet().getSpreadsheet(), getSheet().getSheetIndex(), _columnIndex, getRowIndex() );
	}


	public void trim()
	{
		boolean canRemove = true;
		for (int i = getCellList().size() - 1; i >= 0; i--) {
			CellInstance cell = getCellList().get( i );
			if (canRemove) {
				if (cell == null) {
					getCellList().remove( i );
				}
				else canRemove = false;
			}
		}
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.ln( "cells" ).l( getCellList() );
	}


}
