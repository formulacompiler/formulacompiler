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

package org.formulacompiler.tests.reference.base;

import java.util.Collection;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;

public class RowSetupDbAgg extends RowSetup
{

	public static final class Builder extends RowSetup.Builder
	{
		@Override
		RowSetup newInstance( Context _cx )
		{
			return new RowSetupDbAgg( _cx );
		}
	}

	private final int startingRow;
	private final int startingCol;

	private RowSetupDbAgg( Context _parent )
	{
		super( _parent );

		final CellIndex testStart = (CellIndex) cx().getSpreadsheet().getRange( "TestHeader" ).getTopLeft();
		this.startingRow = testStart.getRowIndex() + 1;
		this.startingCol = testStart.getColumnIndex();
	}


	// DO NOT REFORMAT BELOW THIS LINE
	@Override protected int startingRow() { return this.startingRow; }
	@Override	protected int expectedCol() { return 0 + this.startingCol; }
	@Override	protected int actualCol() { return 1 + this.startingCol; }
	@Override	protected int nameCol() { return 5 + this.startingCol; }
	@Override	protected int highlightCol() { return 6 + this.startingCol; }
	@Override	protected int excelSaysCol() { return 7 + this.startingCol; }
	@Override	protected int skipIfCol() { return 8 + this.startingCol; }
	// DO NOT REFORMAT ABOVE THIS LINE

	@Override
	protected int documentedColCount()
	{
		final CellInstance rowInputCell = cx().getRowCell( this.startingCol + 2 );
		if (null == rowInputCell || null == rowInputCell.getValue()) return 2;
		return 3;
	}


	@Override
	public RowSetup makeInput()
	{
		int n = 0;
		n += countInputsFromRangeNamedInCol( this.startingCol + 3 );
		n += countInputsFromRangeNamedInCol( this.startingCol + 4 );

		final Context cx = cx();
		final CellInstance rowInputCell = cx.getRowCell( this.startingCol + 2 );
		if (null != rowInputCell && null != rowInputCell.getValue()) {
			n++;
		}

		cx.setInputCellCount( n );
		return makeExpected();
	}

	private int countInputsFromRangeNamedInCol( int _nameCellCol )
	{
		final Context cx = cx();
		final CellInstance nameCell = cx.getRowCell( _nameCellCol );
		if (null != nameCell && null != nameCell.getValue()) {
			final String name = (String) nameCell.getValue();
			final Range range = cx.getSpreadsheet().getRange( name );
			Cell tl = range.getTopLeft();
			Cell br = range.getBottomRight();
			return (br.getColumnIndex() - tl.getColumnIndex() + 1)
					* (br.getRow().getRowIndex() - tl.getRow().getRowIndex() + 1);
		}
		return 0;
	}


	@Override
	public RowSetup setupValues()
	{
		final Collection<CellIndex> cells = New.collection();
		extractInputsFromRangeNamedInCol( this.startingCol + 3, cells );
		extractInputsFromRangeNamedInCol( this.startingCol + 4, cells );

		final Context cx = cx();
		final CellInstance rowInputCell = cx.getRowCell( this.startingCol + 2 );
		if (null != rowInputCell && null != rowInputCell.getValue()) {
			cells.add( rowInputCell.getCellIndex() );
		}

		final CellIndex[] cellArray = cells.toArray( new CellIndex[ cells.size() ] );
		cx.setInputCells( cellArray );
		cx.setInputs( new Inputs( cx, cellArray ) );
		return super.setupValues();
	}

	private void extractInputsFromRangeNamedInCol( int _nameCellCol, Collection<CellIndex> _cells )
	{
		final Context cx = cx();
		final CellInstance nameCell = cx.getRowCell( _nameCellCol );
		if (null != nameCell && null != nameCell.getValue()) {
			final String name = (String) nameCell.getValue();
			final Range range = cx.getSpreadsheet().getRange( name );
			for (Cell cell : range.cells()) {
				_cells.add( (CellIndex) cell );
			}
		}
	}

}
