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
package sej.engine.expressions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import sej.describable.DescriptionBuilder;
import sej.engine.expressions.Aggregator.Aggregation;

public class ExpressionNodeForRangeValue extends ExpressionNode
{
	private RangeValue rangeValue;


	public ExpressionNodeForRangeValue(RangeValue _rangeValue)
	{
		super();
		this.rangeValue = _rangeValue;
	}


	private RangeValue getRangeValue()
	{
		return this.rangeValue;
	}


	@Override
	public Object evaluate( EvaluationContext _context ) throws EvaluationFailed, InvocationTargetException
	{
		RangeValue result = (RangeValue) getRangeValue().clone();
		for (ExpressionNode arg : getArguments()) {
			result.add( arg.evaluate( _context ) );
		}
		return result;
	}


	@Override
	public void aggregateInto( EvaluationContext _context, Aggregation _aggregation ) throws EvaluationFailed, InvocationTargetException
	{
		for (ExpressionNode arg : getArguments()) {
			arg.aggregateInto( _context, _aggregation );
		}
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForRangeValue( (RangeValue) getRangeValue().clone() );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		if (getArguments().size() == 1) {
			getArguments().get( 0 ).describeTo( _to );
		}
		else {
			_to.append( '{' );
			boolean isFirst = true;
			for (ExpressionNode arg : getArguments()) {
				if (isFirst) isFirst = false;
				else _to.append( ',' );
				arg.describeTo( _to );
			}
			_to.append( '}' );
		}
	}

}
