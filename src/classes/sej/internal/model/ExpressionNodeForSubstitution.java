package sej.internal.model;

import java.io.IOException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;
import sej.internal.expressions.ExpressionDescriptionConfig;
import sej.internal.expressions.ExpressionNode;

public final class ExpressionNodeForSubstitution extends ExpressionNode
{

	public ExpressionNodeForSubstitution()
	{
		super();
	}

	public ExpressionNodeForSubstitution(ExpressionNode... _args)
	{
		super( _args );
	}

	public ExpressionNodeForSubstitution(Collection _args)
	{
		super( _args );
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForSubstitution();
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		describeArgumentListTo( _to, _cfg );
	}

}
