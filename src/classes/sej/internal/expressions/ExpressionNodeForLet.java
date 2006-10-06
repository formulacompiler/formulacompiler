package sej.internal.expressions;

import java.io.IOException;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForLet extends ExpressionNode
{
	private final String varName;

	public ExpressionNodeForLet(String _varName, ExpressionNode _value, ExpressionNode _in)
	{
		super( _value, _in );
		this.varName = _varName;
	}

	private ExpressionNodeForLet(String _varName)
	{
		super();
		this.varName = _varName;
	}


	public final String varName()
	{
		return this.varName;
	}

	public final ExpressionNode value()
	{
		return argument( 0 );
	}

	public final ExpressionNode in()
	{
		return argument( 1 );
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForLet( this.varName );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "°LET( " ).append( varName() ).append( ": " );
		value().describeTo( _to, _cfg );
		_to.append( ", " );
		in().describeTo( _to, _cfg );
		_to.append( " )" );
	}

}
