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

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.List;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.LocalExcelDate;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;

final class ODSRowVerificationTestCase extends AbstractVariantRowVerificationTestCase
{

	public static class Factory implements AbstractVariantRowVerificationTestCase.Factory
	{
		public static final Factory INSTANCE = new Factory();

		public AbstractVariantRowVerificationTestCase newInstance( Context _cx, Context _variant )
		{
			return new ODSRowVerificationTestCase( _cx, _variant );
		}
	}

	protected ODSRowVerificationTestCase( Context _cx, Context _variant )
	{
		super( _cx, _variant );
	}


	@Override
	protected boolean areCellValuesEqual( Object _want, Object _have ) throws Exception
	{
		if (super.areCellValuesEqual( _want, _have )) return true;
		if (_want instanceof Double) {
			if (!(_have instanceof Double)) {
				return false;
			}
			return areDoublesEqual( (Double) _want, (Double) _have );
		}
		else if (_want instanceof LocalExcelDate) {
			if (!(_have instanceof LocalExcelDate)) {
				return false;
			}
			return areDoublesEqual( ((LocalExcelDate) _want).doubleValue(), ((LocalExcelDate) _have).doubleValue() );
		}
		return false;
	}

	private boolean areDoublesEqual( double _wantDbl, double _haveDbl )
	{
		/*
		 * OOo saves double values with a maximum precision of 15.
		 */
		final double wantAbs = Math.abs( _wantDbl );
		final double haveAbs = Math.abs( _haveDbl );
		if (wantAbs < ZERO_EPSILON) {
			return haveAbs < ZERO_EPSILON;
		}
		else {
			return Math.abs( _wantDbl - _haveDbl ) < EPSILON * Math.min( wantAbs, haveAbs );
		}
	}
	private static final double EPSILON = 0.00000000000001;
	private static final double ZERO_EPSILON = 1e-306;


	@Override
	protected void assertCellExpressionsSame( CellWithLazilyParsedExpression _want, CellWithLazilyParsedExpression _have )
			throws Exception
	{
		final ExpressionNode wantExpr = insertDefaultParamsInto( _want.getExpression() );
		final ExpressionNode haveExpr = _have.getExpression();
		final String wantText = wantExpr.toString();
		final String haveText = haveExpr.toString();
		if (!wantText.equals( haveText )) {
			assertEquals( _want.toString(), wantText, haveText );
		}
	}

	private ExpressionNode insertDefaultParamsInto( ExpressionNode _expr )
	{
		ExpressionNode res = _expr;
		int i = 0;
		for (ExpressionNode srcArg : _expr.arguments()) {
			if (srcArg != null) {
				ExpressionNode resArg = insertDefaultParamsInto( srcArg );
				if (resArg != srcArg) {
					if (res == _expr) {
						res = shallowClone( _expr );
					}
					res.arguments().set( i, resArg );
				}
			}
			i++;
		}
		if (res instanceof ExpressionNodeForFunction) {
			final ExpressionNodeForFunction funNode = (ExpressionNodeForFunction) res;
			final Function fun = funNode.getFunction();
			final int c = funNode.arguments().size();
			switch (fun) {
				case LOG:
					if (c == 1) res = insert( _expr, res, _, 10.0 );
					break;
				case FV:
				case PMT:
				case PV: {
					res = replaceNull( _expr, res, 3, 0.0 );
					break;
				}
				case RATE: {
					res = replaceNull( _expr, res, 3, 0.0 );
					res = replaceNull( _expr, res, 4, 0.0 );
					break;
				}
			}
		}
		return res;
	}

	private ExpressionNode replaceNull( ExpressionNode _expr, ExpressionNode _currentRes, int _pos, Object _cst )
	{
		final List<ExpressionNode> args = _currentRes.arguments();
		if (args.size() > _pos && args.get( _pos ) == null) {
			return replace( _expr, _currentRes, _pos, _cst );
		}
		return _currentRes;
	}

	private ExpressionNode replace( ExpressionNode _expr, ExpressionNode _currentRes, int _pos, Object _cst )
	{
		final ExpressionNode res = (_expr == _currentRes) ? shallowClone( _currentRes ) : _currentRes;
		res.arguments().set( _pos, cst( _cst ) );
		return res;
	}

	private ExpressionNode insert( ExpressionNode _expr, ExpressionNode _currentRes, Object... _args )
	{
		final ExpressionNode res = (_expr == _currentRes) ? shallowClone( _currentRes ) : _currentRes;
		for (int i = 0; i < _args.length; i++) {
			Object arg = _args[ i ];
			if (arg != _) {
				res.arguments().add( i, cst( arg ) );
			}
		}
		return res;
	}
	private static final Object _ = new Object();

	private ExpressionNode shallowClone( ExpressionNode _expr )
	{
		final ExpressionNode res = _expr.cloneWithoutArguments();
		res.arguments().addAll( _expr.arguments() );
		return res;
	}

}
