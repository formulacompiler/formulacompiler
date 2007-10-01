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
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


public class EvalIndex extends EvalFunction
{

	public EvalIndex( ExpressionNode _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected Object eval() throws CompilerException
	{
		final int card = cardinality();
		switch (card) {

			case 2: { // one-dimensional lookup
				final Object indexArg = evaluateArgument( 1 );
				if (isConstant( indexArg )) {
					final int index = type().toInt( indexArg, 0 ) - 1;
					if (index < 0) {
						return null;
					}
					final EvalRangeValue range = (EvalRangeValue) unsubstitutedArgument( 0 );
					if (index >= range.arguments().size()) {
						return null;
					}
					return range.evaluateArgument( index );
				}
				break;
			}

		}

		return super.eval();
	}

}
