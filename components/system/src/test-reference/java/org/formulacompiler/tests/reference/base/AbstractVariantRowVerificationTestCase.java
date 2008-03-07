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
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;

abstract class AbstractVariantRowVerificationTestCase extends AbstractContextTestCase
{
	public static interface Factory
	{
		public AbstractVariantRowVerificationTestCase newInstance( Context _cx, Context _variant );
	}

	private final Context variant;

	protected AbstractVariantRowVerificationTestCase( Context _cx, Context _variant )
	{
		super( _cx );
		this.variant = _variant;
	}

	@Override
	protected String getOwnName()
	{
		return "Check row " + (cx().getRowIndex() + 1) + " against " + this.variant.getSpreadsheetFile().getName();
	}

	@Override
	protected void runTest() throws Throwable
	{
		final Context want = cx();
		final Context have = this.variant;
		have.setRow( want.getRowIndex() );
		assertInputsSame( want, have );
		assertOutputSame( want, have );
	}

	protected void assertInputsSame( Context _want, Context _have ) throws Exception
	{
		assertCellsSame( _want.getInputCells(), _have );
		assertCellsSame( _want.getExpectedCell(), _have );
	}

	protected void assertOutputSame( Context _want, Context _have ) throws Exception
	{
		assertCellsSame( _want.getOutputCell(), _have );
	}

	protected void assertCellsSame( CellIndex[] _want, Context _have ) throws Exception
	{
		for (int i = 0; i < _want.length; i++) {
			final CellIndex want = _want[ i ];
			assertCellsSame( want, _have );
		}
	}

	protected void assertCellsSame( CellIndex _want, Context _have ) throws Exception
	{
		assertCellsSame( _want.getCell(), _have.getSheetRow( _want.rowIndex ).getCellOrNull( _want.columnIndex ) );
	}

	protected void assertCellsSame( CellInstance _want, CellInstance _have ) throws Exception
	{
		if (_want == null) {
			assertNull( _have );
		}
		else if (_want instanceof CellWithLazilyParsedExpression) {
			CellWithLazilyParsedExpression wantExpr = (CellWithLazilyParsedExpression) _want;
			CellWithLazilyParsedExpression haveExpr = (CellWithLazilyParsedExpression) _have;
			assertCellExpressionsSame( wantExpr, haveExpr );
		}
		else {
			assertCellValuesSame( _want, _want.getValue(), (null == _have) ? null : _have.getValue() );
		}
	}

	protected void assertCellExpressionsSame( CellWithLazilyParsedExpression _want, CellWithLazilyParsedExpression _have )
			throws Exception
	{
		final String wantExpr = _want.getExpression().toString();
		final String haveExpr = _have.getExpression().toString();
		if (!wantExpr.equals( haveExpr )) {
			assertEquals( _want.toString(), wantExpr, haveExpr );
		}
	}

	private void assertCellValuesSame( CellInstance _where, Object _want, Object _have ) throws Exception
	{
		if (!areCellValuesEqual( _want, _have )) {
			final CellIndex cellIndex = _where.getCellIndex();
			assertEquals( cellIndex.toString(), _want, _have );
		}
	}

	protected boolean areCellValuesEqual( Object _want, Object _have ) throws Exception
	{
		return _want == null ? _have == null : _want.equals( _have );
	}


}
