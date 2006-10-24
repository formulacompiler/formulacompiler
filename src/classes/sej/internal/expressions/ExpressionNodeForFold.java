package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForFold extends ExpressionNode
{
	private final String accumulatorName;
	private final String elementName;

	private ExpressionNodeForFold(String _accumulatorName, String _elementName)
	{
		super();
		this.accumulatorName = _accumulatorName;
		this.elementName = _elementName;
	}

	public ExpressionNodeForFold(String _accumulatorName, ExpressionNode _initialAccumulatorValue, String _elementName,
			ExpressionNode _accumulatingStep, ExpressionNode... _elements)
	{
		this( _accumulatorName, _elementName );
		arguments().add( _initialAccumulatorValue );
		arguments().add( _accumulatingStep );
		for (ExpressionNode element : _elements)
			addArgument( element );
	}

	public ExpressionNodeForFold(String _acc, ExpressionNode _init, String _x, ExpressionNode _fold,
			Collection<ExpressionNode> _args)
	{
		this( _acc, _init, _x, _fold, _args.toArray( new ExpressionNode[ _args.size() ] ) );
	}


	public final String accumulatorName()
	{
		return this.accumulatorName;
	}

	public final ExpressionNode initialAccumulatorValue()
	{
		return argument( 0 );
	}

	public final String elementName()
	{
		return this.elementName;
	}

	public final ExpressionNode accumulatingStep()
	{
		return argument( 1 );
	}

	public final Iterable<ExpressionNode> elements()
	{
		return new Iterable<ExpressionNode>()
		{

			public Iterator<ExpressionNode> iterator()
			{
				Iterator<ExpressionNode> result = arguments().iterator();
				result.next();
				result.next();
				return result;
			}

		};
	}

	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForFold( this.accumulatorName, this.elementName );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_FOLD( " ).append( accumulatorName() ).append( ": " );
		initialAccumulatorValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		boolean first = true;
		for (final ExpressionNode element : elements()) {
			if (first) first = false;
			else _to.append( ", " );
			element.describeTo( _to, _cfg );
		}
		_to.append( " )" );
	}

}
