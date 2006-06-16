/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNode;
import sej.internal.model.util.InterpretedNumericType;

public abstract class EvalShortCircuitedBooleanOperator extends EvalOperator
{

	public EvalShortCircuitedBooleanOperator(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}
	
	
	@Override
	public Object eval()
	{
		final int card = cardinality();
		switch (card) {
			case 2:
				final Object firstArg = evaluateArgument( 0 );
				if (isConstant( firstArg )) {
					final boolean constFirstArg = getType().toBoolean( firstArg );
					return eval( constFirstArg );
				}
		}
		return super.eval();
	}


	protected abstract Object eval( final boolean _constFirstArg );


	protected Object evaluateSecondArgAsBoolean()
	{
		Object secondArg = evaluateArgument( 1 );
		if (isConstant( secondArg )) {
			return getType().toBoolean( secondArg );
		}
		else {
			return secondArg;
		}
	}


}
