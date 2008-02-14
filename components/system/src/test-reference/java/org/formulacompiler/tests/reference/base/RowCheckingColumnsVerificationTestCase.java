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
