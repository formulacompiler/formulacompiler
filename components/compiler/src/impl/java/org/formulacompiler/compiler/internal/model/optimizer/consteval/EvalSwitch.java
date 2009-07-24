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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;

final class EvalSwitch extends EvalShadow
{

	public EvalSwitch( ExpressionNodeForSwitch _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected TypedResult eval() throws CompilerException
	{
		final ExpressionNodeForSwitch switchNode = (ExpressionNodeForSwitch) node();
		final TypedResult valueArg = evaluateArgument( switchNode.offsetOfValueInArguments() );
		if (valueArg.hasConstantValue()) {
			final int value = type().toInt( valueArg.getConstantValue(), -1 );
			if (value >= 0) {
				final Iterable<ExpressionNodeForSwitchCase> cases = switchNode.cases();
				int iCase = 0;
				for (ExpressionNodeForSwitchCase caze : cases) {
					if (value == caze.caseValue()) {
						final EvalSwitchCase caseEval = (EvalSwitchCase) arguments().get(
								iCase + switchNode.offsetOfCasesInArguments() );
						final TypedResult caseResult = caseEval.evaluateArgument( 0, context() );
						return caseResult;
					}
					iCase++;
				}
			}
			return evaluateArgument( switchNode.offsetOfDefaultInArguments() );
		}
		return super.eval();
	}


	@Override
	protected TypedResult evaluateToConst( TypedResult... _args ) throws CompilerException
	{
		return evaluateToNode( _args );
	}

}
