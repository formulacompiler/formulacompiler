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
			assertCellValuesSame( _want, _want.getValue(), (null == _have)? null : _have.getValue() );
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

	protected void assertCellValuesSame( CellInstance _where, Object _want, Object _have ) throws Exception
	{
		if (!areCellValuesEqual( _want, _have )) {
			assertEquals( _where.toString(), _want, _have );
		}
	}

	protected boolean areCellValuesEqual( Object _want, Object _have ) throws Exception
	{
		return _want == null? _have == null : _want.equals( _have );
	}


}
