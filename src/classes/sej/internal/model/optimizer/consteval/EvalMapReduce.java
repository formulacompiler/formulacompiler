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
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.model.Aggregation;
import sej.internal.model.util.InterpretedNumericType;

public class EvalMapReduce extends EvalAggregator
{
	private final Operator reductor;


	public EvalMapReduce(ExpressionNodeForAggregator _node, Operator _reductor, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.reductor = _reductor;
	}


	@Override
	protected Aggregation newAggregation()
	{
		final Aggregation agg = new Aggregation();
		return agg;
	}


	@Override
	protected void aggregate( Aggregation _agg, Object _value )
	{
		final Object value = (null == _value) ? type().zero() : _value;
		if (null == _agg.accumulator) {
			_agg.accumulator = value;
		}
		else {
			_agg.accumulator = type().compute( this.reductor, _agg.accumulator, value );
		}
	}


	@Override
	protected Object resultOf( Aggregation _agg )
	{
		return _agg.accumulator;
	}

}
