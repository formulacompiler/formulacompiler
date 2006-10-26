package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;

public class ExpressionNodeForFold extends AbstractExpressionNodeForFold
{

	private ExpressionNodeForFold(String _accumulatorName, String _elementName, boolean _canInlineFirst)
	{
		super( _accumulatorName, _elementName, _canInlineFirst );
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, boolean _canInlineFirst, ExpressionNode... _elements)
	{
		this( _accumulatorName, _elementName, _canInlineFirst );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		addArguments( _elements );
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, boolean _canInlineFirst, Collection<ExpressionNode> _elements)
	{
		this( _accumulatorName, _elementName, _canInlineFirst );
		addArgument( _initialAccumulatorValue );
		addArgument( _accumulatingStep );
		arguments().addAll( _elements );
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForFold( accumulatorName(), elementName(), canInlineFirst() );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( canInlineFirst() ? "_FOLDL_1ST_OK( " : "_FOLDL( " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
