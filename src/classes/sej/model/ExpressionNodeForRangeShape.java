package sej.model;

import java.io.IOException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;
import sej.expressions.ExpressionNode;

public class ExpressionNodeForRangeShape extends ExpressionNode
{

	public ExpressionNodeForRangeShape()
	{
		super();
	}

	public ExpressionNodeForRangeShape(Collection _args)
	{
		super( _args );
	}

	public ExpressionNodeForRangeShape(ExpressionNode... _args)
	{
		super( _args );
	}

	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForRangeShape();
	}

	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		describeArgumentOrArgumentListTo( _to );
	}

}
