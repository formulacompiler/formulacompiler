package sej.model;

import java.io.IOException;

import sej.describable.DescriptionBuilder;
import sej.engine.expressions.ExpressionNode;


public class ExpressionNodeForRangeIntersection extends ExpressionNode
{

	public ExpressionNodeForRangeIntersection(ExpressionNode _firstArg)
	{
		super();
		addArgument( _firstArg );
	}


	public ExpressionNodeForRangeIntersection()
	{
		super();
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForRangeIntersection();
	}

	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		describeArgumentTo( _to, 0 );
		for (int iArg = 1; iArg < getArguments().size(); iArg++) {
			_to.append( " " );
			describeArgumentTo( _to, iArg );
		}
	}

}
