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

import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public final class RowSetupDefault extends RowSetup
{

	public static final class Builder extends RowSetup.Builder
	{
		@Override
		RowSetup newInstance( Context _cx )
		{
			return new RowSetupDefault( _cx );
		}
	}

	private RowSetupDefault( Context _cx )
	{
		super( _cx );
	}

	// DO NOT REFORMAT BELOW THIS LINE
	@Override	protected int startingRow() { return 1; }
	@Override	protected int expectedCol() { return 0; }
	@Override	protected int actualCol() { return 1; }
	protected int inputStartCol() { return 2; }
	protected int inputCountCol() { return 9; }
	@Override	protected int nameCol() { return 10; }
	@Override	protected int highlightCol() { return 11; }
	@Override	protected int excelSaysCol() { return 12; }
	@Override	protected int skipIfCol() { return 13; }
	// DO NOT REFORMAT ABOVE THIS LINE

	@Override
	protected int documentedColCount()
	{
		final CellInstance inputCountCell = cx().getRowCell( inputCountCol() );
		if (null == inputCountCell || null == inputCountCell.getValue()) return 2;
		return 2 + ((Number) inputCountCell.getValue()).intValue();
	}

	@Override
	public int checkingCol()
	{
		return 15;
	}


	@Override
	public RowSetup makeInput()
	{
		final Context cx = cx();
		final CellInstance countCell = cx.getRowCell( inputCountCol() );
		final Number countValue = (countCell == null) ? null : (Number) countCell.getValue();
		final int n = (countValue == null) ? 0 : countValue.intValue();
		cx.setInputCellCount( n );
		return makeExpected();
	}


	@Override
	public RowSetup setupValues()
	{
		final Context cx = cx();
		final int n = cx.getInputCellCount();
		final CellIndex[] inputCells = new CellIndex[ n ];
		final SpreadsheetImpl ss = cx.getSpreadsheet();
		final int r = cx.getRowIndex();
		int c = inputStartCol();
		for (int i = 0; i < n; i++)
			inputCells[ i ] = new CellIndex( ss, 0, c++, r );
		cx.setInputCells( inputCells );
		return super.setupValues();
	}

}
