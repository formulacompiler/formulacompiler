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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


public class EvalIf extends EvalFunction
{

	public EvalIf( ExpressionNodeForFunction _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected TypedResult eval( EvalShadowContext _context ) throws CompilerException
	{
		final int card = cardinality();
		if (card > 0) {
			final TypedResult firstArg = evaluateArgument( 0, _context );
			if (firstArg.hasConstantValue()) {
				final boolean constFirstArg = type().toBoolean( firstArg.getConstantValue() );
				switch (card) {
					case 2:
						return (constFirstArg) ? evaluateArgument( 1, _context ) : null;
					case 3:
						return (constFirstArg) ? evaluateArgument( 1, _context ) : evaluateArgument( 2, _context );
				}
			}
		}
		return super.eval( _context );
	}

}
