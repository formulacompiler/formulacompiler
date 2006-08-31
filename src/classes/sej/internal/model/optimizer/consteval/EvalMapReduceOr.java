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

import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.model.util.InterpretedNumericType;

public class EvalMapReduceOr extends EvalMapReduce
{

	public EvalMapReduceOr(ExpressionNodeForAggregator _node, InterpretedNumericType _type)
	{
		super( _node, Operator.OR, _type );
	}

	
	@Override
	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args )
	{
		for (Object arg : _args) {
			if (!(arg instanceof ExpressionNode)) {
				if (type().toBoolean( arg )) return Boolean.TRUE;
			}
		}
		return super.evaluateToConstOrExprWithConstantArgsFixed( _args );
	}
	
	
}
