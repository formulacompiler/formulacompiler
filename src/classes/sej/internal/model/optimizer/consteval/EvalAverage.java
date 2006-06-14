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

import sej.expressions.ExpressionNode;
import sej.expressions.Operator;
import sej.internal.model.Aggregation;
import sej.internal.model.Aggregation.NonNullCountingAggregation;
import sej.internal.model.util.InterpretedNumericType;

public class EvalAverage extends EvalAggregator
{

	public EvalAverage(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Aggregation newAggregation()
	{
		final Aggregation.NonNullCountingAggregation result = new Aggregation.NonNullCountingAggregation();
		result.accumulator = getType().adjustConstantValue( 0 );
		return result;
	}

	@Override
	protected void aggregate( Aggregation _agg, Object _value )
	{
		final NonNullCountingAggregation agg = (NonNullCountingAggregation) _agg;
		if (null != _value) {
			agg.accumulator = getType().compute( Operator.PLUS, agg.accumulator, _value );
			agg.numberOfNonNullArguments++;
		}
	}

	@Override
	protected Object resultOf( Aggregation _agg )
	{
		final NonNullCountingAggregation agg = (NonNullCountingAggregation) _agg;
		return getType().compute( Operator.DIV, agg.accumulator,
				getType().adjustConstantValue( agg.numberOfNonNullArguments ) );
	}

}
