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

import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpreterException;

public class EvalSubstitution extends EvalShadow<ExpressionNodeForSubstitution>
{

	public EvalSubstitution( ExpressionNodeForSubstitution _node )
	{
		super( _node, null );
	}


	@Override
	protected EvalShadow unsubstituted()
	{
		if (cardinality() == 1) {
			return unsubstitutedArgument( 0 );
		}
		else {
			return this;
		}
	}


	@Override
	protected TypedResult evaluateToConst( TypedResult... _args ) throws InterpreterException
	{
		if (_args.length == 1) {
			// Skip the substitution node right away.
			return _args[ 0 ];
		}
		else {
			return super.evaluateToNode( _args );
		}
	}

	@Override
	protected TypedResult evaluateToNode( TypedResult... _args ) throws InterpreterException
	{
		if (_args.length == 1) {
			// Skip the substitution node right away.
			return _args[ 0 ];
		}
		else {
			return super.evaluateToNode( _args );
		}
	}

}
