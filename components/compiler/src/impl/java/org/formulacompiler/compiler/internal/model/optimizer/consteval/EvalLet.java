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

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class EvalLet extends EvalShadow<ExpressionNodeForLet>
{
	private final String varName;
	private final boolean mayFold;

	public EvalLet( ExpressionNodeForLet _node, InterpretedNumericType _type )
	{
		super( _node, _type );
		this.varName = _node.varName();
		this.mayFold = (_node.type() == ExpressionNodeForLet.Type.BYVAL);
	}


	@Override
	protected TypedResult eval( EvalShadowContext _context ) throws CompilerException
	{
		if (this.mayFold) {
			final TypedResult val = evaluateArgument( 0, _context );
			_context.letDict.let( this.varName, null, val );
			try {
				final TypedResult result = evaluateArgument( 1, _context );
				if (result.isConstant()) {
					return result;
				}
				return evaluateToNode( val, result );
			}
			finally {
				_context.letDict.unlet( this.varName );
			}
		}
		else {
			_context.letDict.let( this.varName, null, EvalLetVar.UNDEF );
			try {
				final TypedResult result = evaluateArgument( 1, _context );
				if (result.isConstant()) {
					return result;
				}
				final TypedResult val = evaluateArgument( 0, _context );
				return evaluateToNode( val, result );
			}
			finally {
				_context.letDict.unlet( this.varName );
			}
		}
	}

	@Override
	protected TypedResult evaluateToConst( TypedResult... _args )
	{
		throw new IllegalStateException( "EvalLet.evaluateToConst() should never be called" );
	}

}
