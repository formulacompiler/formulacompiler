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

import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;

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
		final CellIndex andingCell = cx.getRowCellIndex( this.checkingCol + 1 );
		final CellIndex indicatorCell = cx.getRowCellIndex( rowSetup.expectedCol() );

		assertCheck( "AND( Q2:Q10000 )", andingCell );
		assertCheck( "IF( Q1, \"Expected\", \"FAILED!\" )", indicatorCell );

		if (!cx.getSpreadsheetFileBaseName().startsWith( "Bad" )) {
			assertTrue( "Sheet contains invalid data.", (Boolean) andingCell.getValue() );
		}
	}

	private void assertCheck( String _wantExpr, CellIndex _cellIndex ) throws Exception
	{
		final CellInstance cell = _cellIndex.getCell();
		assertNotNull( _cellIndex + " is empty.", cell );
		assertTrue( _cellIndex + " does not contain expression.", cell instanceof CellWithExpression);
		assertEquals( "" + _cellIndex, _wantExpr, ((CellWithExpression) cell).getExpression().toString() );
	}

}
