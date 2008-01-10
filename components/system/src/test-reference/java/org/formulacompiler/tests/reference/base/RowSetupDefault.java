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
			inputCells[ i ] = new CellIndex(ss, 0, c++, r );
		cx.setInputCells( inputCells );
		return super.setupValues();
	}

}
