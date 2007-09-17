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
package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;

final class EvalSwitch extends EvalShadow
{

	public EvalSwitch( ExpressionNodeForSwitch _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected Object eval() throws CompilerException
	{
		final ExpressionNodeForSwitch switchNode = (ExpressionNodeForSwitch) node();
		final Object valueArg = evaluateArgument( switchNode.offsetOfValueInArguments() );
		if (isConstant( valueArg )) {
			final int value = type().toInt( valueArg, -1 );
			if (value >= 0) {
				final Iterable<ExpressionNodeForSwitchCase> cases = switchNode.cases();
				int iCase = 0;
				for (ExpressionNodeForSwitchCase caze : cases) {
					if (value == caze.caseValue()) {
						final EvalSwitchCase caseEval = (EvalSwitchCase) arguments().get(
								iCase + switchNode.offsetOfCasesInArguments() );
						final Object caseResult = caseEval.evaluateArgument( 0, context() );
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
	protected Object evaluateToConst( Object... _args ) throws CompilerException
	{
		return evaluateToNode( _args );
	}

}
