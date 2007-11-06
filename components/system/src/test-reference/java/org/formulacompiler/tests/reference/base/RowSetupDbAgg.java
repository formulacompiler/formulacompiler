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

import java.util.Collection;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.internal.CellIndex;

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
		this.startingRow = testStart.rowIndex + 1;
		this.startingCol = testStart.columnIndex;
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
		final Cell rowInputCell = cx().getRowCell( this.startingCol + 2 );
		if (null == rowInputCell || null == rowInputCell.getConstantValue()) return 2;
		return 3;
	}


	@Override
	public RowSetup makeInput()
	{
		final Collection<Cell> cells = New.collection();
		extractInputsFromRangeNamedInCol( this.startingCol + 3, cells );
		extractInputsFromRangeNamedInCol( this.startingCol + 4, cells );

		final Context cx = cx();
		final Cell rowInputCell = cx.getRowCell( this.startingCol + 2 );
		if (null != rowInputCell && null != rowInputCell.getConstantValue()) {
			cells.add( rowInputCell );
		}

		final Cell[] cellArray = cells.toArray( new Cell[ cells.size() ] );
		cx.setInputCells( cellArray );
		cx.setInputs( new Inputs( cx, cellArray ) );
		return makeExpected();
	}

	private void extractInputsFromRangeNamedInCol( int _nameCellCol, Collection<Cell> _cells )
	{
		final Context cx = cx();
		final Cell nameCell = cx.getRowCell( _nameCellCol );
		if (null != nameCell && null != nameCell.getConstantValue()) {
			final String name = (String) nameCell.getConstantValue();
			final Range range = cx.getSpreadsheet().getRange( name );
			for (Cell cell : range.cells()) {
				_cells.add( cell );
			}
		}
	}

	@Override
	public String getInputIsBoundString()
	{
		final boolean[] flags = cx().getInputIsBound();
		if (null == flags) return "none";
		int bitset = 0;
		for (int i = 0; i < flags.length; i++) {
			if (flags[ i ]) {
				bitset |= 1 << i;
			}
		}
		return "bits \"" + Integer.toBinaryString( bitset ) + "\"";
	}

}
