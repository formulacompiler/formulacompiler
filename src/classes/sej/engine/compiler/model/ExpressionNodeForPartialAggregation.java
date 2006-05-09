package sej.engine.compiler.model;

import java.io.IOException;

import sej.describable.DescriptionBuilder;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForAggregator;

public class ExpressionNodeForPartialAggregation extends ExpressionNodeForAggregator
{
	private final Aggregation partialAggregation;

	public ExpressionNodeForPartialAggregation(Aggregator _aggregator, Aggregation _partialAggregation)
	{
		super( _aggregator );
		this.partialAggregation = _partialAggregation;
	}
	
	
	public Aggregation getPartialAggregation()
	{
		return this.partialAggregation;
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForPartialAggregation( getAggregator(), this.partialAggregation );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( getAggregator().getName() );
		if (null != this.partialAggregation) {
			_to.append( '{' );
			this.partialAggregation.describeTo( _to );
			_to.append( '}' );
		}
		describeArgumentListTo( _to );
	}


}
