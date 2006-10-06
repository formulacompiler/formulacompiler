package sej.internal.expressions;

import java.io.IOException;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForLetVar extends ExpressionNode
{
	private final String varName;
	
	public ExpressionNodeForLetVar( String _varName )
	{
		super();
		this.varName = _varName;
	}
	
	
	public final String varName()
	{
		return this.varName;
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForLetVar( varName() );
	}

	
	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( varName() );
	}

}
