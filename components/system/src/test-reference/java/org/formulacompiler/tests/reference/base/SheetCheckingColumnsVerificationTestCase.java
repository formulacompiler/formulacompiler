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

import org.formulacompiler.spreadsheet.internal.CellInstance;

public class SheetCheckingColumnsVerificationTestCase extends AbstractContextTestCase
{
	private final int checkingCol;

	protected SheetCheckingColumnsVerificationTestCase( Context _cx, int _checkingCol )
	{
		super( _cx );
		this.checkingCol = _checkingCol;
	}

	@Override
	protected String getOwnName()
	{
		return "Check setup of sheet";
	}

	@Override
	protected void runTest() throws Throwable
	{
		final Context cx = new Context( cx() );
		final RowSetup rowSetup = cx.getRowSetup();
		cx.setRow( rowSetup.startingRow() - 1 );
		final CellInstance andingCell = cx.getRowCell( this.checkingCol + 1 );
		final CellInstance indicatorCell = cx.getRowCell( rowSetup.expectedCol() );

		assertCheck( "AND( Q2:Q10000 )", andingCell );
		assertCheck( "IF( Q1, \"Expected\", \"FAILED!\" )", indicatorCell );

		if (!cx.getSpreadsheetFileBaseName().startsWith( "Bad" )) {
			assertTrue( (Boolean) andingCell.getValue() );
		}
	}

	private void assertCheck( String _wantExpr, CellInstance _cell ) throws Exception
	{
		assertEquals( _wantExpr, _cell.getExpression().toString() );
	}

}
