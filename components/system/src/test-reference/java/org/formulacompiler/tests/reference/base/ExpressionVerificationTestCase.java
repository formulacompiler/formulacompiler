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

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;

public class ExpressionVerificationTestCase extends AbstractContextTestCase
{
	private final int column;
	private final String wantExpr;

	protected ExpressionVerificationTestCase( Context _cx, int _col, final String _expectedExpression )
	{
		super( _cx );
		this.column = _col;
		this.wantExpr = _expectedExpression;
	}

	@Override
	protected String getOwnName()
	{
		return "Check expression in " + cx().getRowCellIndex( this.column );
	}

	@Override
	protected void runTest() throws Throwable
	{
		final CellWithExpression cell = (CellWithExpression) cx().getRowCell( this.column );
		assertCheck( this.wantExpr, cell );
	}

	private void assertCheck( String _wantExpr, CellWithExpression _cell ) throws Exception
	{
		final ExpressionNode expr = _cell.getExpression();
		final String exprText = expr.toString();
		final String normalizedText = exprText.replaceAll( "\\b([A-Z])" + (String.valueOf( cx().getRowIndex() + 1 ) + "\\b"), "$1n" );
		assertEquals( _wantExpr, normalizedText );
	}

}
