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

package org.formulacompiler.spreadsheet.internal;

import java.util.List;

import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;

public abstract class BaseRow extends AbstractStyledElement implements Spreadsheet.Row
{
	private final BaseSheet sheet;
	private final int rowIndex;

	public BaseRow( final BaseSheet _sheet, int _rowIndex )
	{
		this.sheet = _sheet;
		this.rowIndex = _rowIndex;
	}

	public BaseSheet getSheet()
	{
		return this.sheet;
	}


	public Cell[] getCells()
	{
		final BaseSpreadsheet spreadsheet = getSheet().getSpreadsheet();
		final int sheetIndex = getSheet().getSheetIndex();
		final int rowIndex = getRowIndex();

		final List<? extends CellInstance> cellList = getCellList();
		final Spreadsheet.Cell[] result = new Spreadsheet.Cell[cellList.size()];
		for (int i = 0; i < cellList.size(); i++) {
			final CellInstance cellInst = cellList.get( i );
			result[ i ] = new CellIndex( spreadsheet, sheetIndex, i, rowIndex );
			assert cellInst == null || result[ i ].equals( cellInst.getCellIndex() )
					: "Expected " + result[ i ] + " but was " + cellInst.getCellIndex();
		}
		return result;
	}


	public int getRowIndex()
	{
		return this.rowIndex;
	}

	public abstract List<? extends CellInstance> getCellList();

	public CellInstance getCellOrNull( int _columnIndex )
	{
		if (_columnIndex < getCellList().size()) return getCellList().get( _columnIndex );
		else return null;
	}


	public CellIndex getCellIndex( int _columnIndex )
	{
		return new CellIndex( getSheet().getSpreadsheet(), getSheet().getSheetIndex(), _columnIndex, getRowIndex() );
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.ln( "cells" ).l( getCellList() );
	}


}
