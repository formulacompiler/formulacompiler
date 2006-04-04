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
import java.util.Collection;

import sej.describable.DescriptionBuilder;
import sej.engine.expressions.Aggregator.Aggregation;

public class ExpressionNodeForAggregator extends ExpressionNode
{
	private Aggregator aggregator;
	private Aggregation partialAggregation;


	public ExpressionNodeForAggregator(Aggregator _aggregator, ExpressionNode... _args)
	{
		super( _args );
		setAggregator( _aggregator );
	}


	public ExpressionNodeForAggregator(Aggregator _aggregator, Collection _args)
	{
		super( _args );
		setAggregator( _aggregator );
	}


	public ExpressionNodeForAggregator(Aggregator _aggregator, Aggregation _partialAggregation)
	{
		super();
		setAggregator( _aggregator );
		setPartialAggregation( _partialAggregation );
	}


	public Aggregator getAggregator()
	{
		return this.aggregator;
	}


	public void setAggregator( Aggregator _aggregator )
	{
		this.aggregator = _aggregator;
	}


	public Aggregation getPartialAggregation()
	{
		return this.partialAggregation;
	}


	public void setPartialAggregation( Aggregation _partialAggregation )
	{
		this.partialAggregation = _partialAggregation;
	}


	@Override
	public Object evaluate( EvaluationContext _context ) throws EvaluationFailed, InvocationTargetException
	{
		Aggregation aggregation = newAggregation();
		for (ExpressionNode arg : getArguments()) {
			arg.aggregateInto( _context, aggregation );
		}
		return aggregation.getResult();
	}


	protected Aggregation newAggregation()
	{
		Aggregation aggregation = this.aggregator.newAggregation();
		if (null != this.partialAggregation) {
			aggregation.initializeFrom( this.partialAggregation );
		}
		return aggregation;
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForAggregator( this.aggregator, this.partialAggregation );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( this.aggregator.getName() );
		if (null != this.partialAggregation) {
			_to.append( '{' );
			this.partialAggregation.describeTo( _to );
			_to.append( '}' );
		}
		describeArgumentListTo( _to );
	}


}
