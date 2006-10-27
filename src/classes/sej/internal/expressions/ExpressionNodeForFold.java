package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForFold extends AbstractExpressionNodeForFold
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

	
	public final void neverInlineFirst()
	{
		setCanInlineFirst( false );
	}
	

	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForFold( accumulatorName(), elementName(), canInlineFirst() );
	}


	// FIXME Rename FOLDL to FOLD because it does not have a predetermined order of evaluation of its arguments.
	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( canInlineFirst() ? "_FOLD_1STOK( " : "_FOLD( " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
