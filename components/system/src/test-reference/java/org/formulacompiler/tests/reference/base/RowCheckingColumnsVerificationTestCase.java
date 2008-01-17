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

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.internal.CellInstance;

public class RowCheckingColumnsVerificationTestCase extends AbstractContextTestCase
{
	private final int checkingCol;

	protected RowCheckingColumnsVerificationTestCase( Context _cx, int _checkingCol )
	{
		super( _cx );
		this.checkingCol = _checkingCol;
	}

	@Override
	protected String getOwnName()
	{
		return "Check setup of row " + (cx().getRowIndex() + 1);
	}

	@Override
	protected void runTest() throws Throwable
	{
		final Context cx = cx();
		final CellInstance check1 = cx.getRowCell( this.checkingCol );
		final CellInstance check2 = cx.getRowCell( this.checkingCol + 1 );

		assertCheck(
				"OR( ISBLANK( Bn ), IF( ISERROR( Bn ), (ERRORTYPE( Bn ) = IF( ISBLANK( Mn ), ERRORTYPE( An ), ERRORTYPE( Mn ) )), IF( ISBLANK( Mn ), AND( NOT( ISBLANK( An ) ), (An = Bn) ), (Bn = Mn) ) ) )",
				check1 );

		assertCheck( "IF( ISBLANK( On ), IF( ISERROR( Pn ), false, Pn ), On )", check2 );

		if (!cx.getSpreadsheetFileBaseName().startsWith( "Bad" )) {
			assertTrue( (Boolean) check2.getValue() );
		}
	}

	private void assertCheck( String _wantExpr, CellInstance _cell ) throws Exception
	{
		final ExpressionNode expr = _cell.getExpression();
		final String exprText = expr.toString();
		final String normalizedText = exprText.replace( String.valueOf( cx().getRowIndex() + 1 ), "n" );
		assertEquals( _wantExpr, normalizedText );
	}

}
