package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForFold extends AbstractExpressionNodeForFold
{

	private ExpressionNodeForFold(String _accumulatorName, String _elementName)
	{
		super( _accumulatorName, _elementName );
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, ExpressionNode... _elements)
	{
		this( _accumulatorName, _elementName );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		addArguments( _elements );
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, Collection<ExpressionNode> _elements)
	{
		this( _accumulatorName, _elementName );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		arguments().addAll( _elements );
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForFold( accumulatorName(), elementName() );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_FOLDL( " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
