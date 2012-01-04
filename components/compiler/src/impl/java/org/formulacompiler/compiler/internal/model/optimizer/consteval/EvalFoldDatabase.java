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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDatabase;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class EvalFoldDatabase extends EvalShadow<ExpressionNodeForFoldDatabase>
{
	private final String[] colNames;


	public EvalFoldDatabase( ExpressionNodeForFoldDatabase _node, InterpretedNumericType _type )
	{
		super( _node, _type );
		this.colNames = _node.filterColumnNames();
	}


	@Override
	protected TypedResult eval() throws CompilerException
	{
		final int card = cardinality();
		final TypedResult[] argValues = new TypedResult[ card ];
		for (int iArg = 0; iArg < card; iArg++) {
			argValues[ iArg ] = (iArg == 1) ? evalFilter() : evaluateArgument( iArg );
		}
		return evaluateToConstOrExprWithConstantArgsFixed( argValues );
	}


	@Override
	protected TypedResult evaluateToConst( TypedResult... _args ) throws CompilerException
	{
		return evaluateToNode( _args );
	}


	private TypedResult evalFilter() throws CompilerException
	{
		for (final String colName : this.colNames) {
			letDict().let( colName, null, EvalLetVar.UNDEF );
		}
		try {
			return evaluateArgument( 1 ); // filter
		}
		finally {
			letDict().unlet( this.colNames.length );
		}
	}

}
