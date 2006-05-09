package sej.engine.compiler.model;

import java.io.IOException;

import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;

public class Aggregation extends AbstractDescribable
{
	public Object accumulator;
	
	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( this.accumulator );
	}

	
	public static class NonNullCountingAggregation extends Aggregation
	{
		public int numberOfNonNullArguments;

		@Override
		public void describeTo( DescriptionBuilder _to ) throws IOException
		{
			super.describeTo( _to );
			_to.append( "; n=" );
			_to.append( this.numberOfNonNullArguments );
		}
	}
	
}