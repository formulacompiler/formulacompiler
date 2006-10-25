package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import sej.describable.DescriptionBuilder;

public abstract class AbstractExpressionNodeForFold extends ExpressionNode
{
	private final String accumulatorName;
	private final String elementName;

	protected AbstractExpressionNodeForFold(String _accumulatorName, String _elementName)
	{
		super();
		this.accumulatorName = _accumulatorName;
		this.elementName = _elementName;
	}

	
	protected final void addArguments( ExpressionNode... _elements )
	{
		for (ExpressionNode element : _elements)
			addArgument( element );
	}

	protected static final ExpressionNode[] elementsToArray( Collection<ExpressionNode> _args )
	{
		return _args.toArray( new ExpressionNode[ _args.size() ] );
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
				skipToElements( result );
				return result;
			}

		};
	}
	
	protected void skipToElements( Iterator<ExpressionNode> _iterator )
	{
		_iterator.next();
		_iterator.next();
	}

	
	protected final void describeElements( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		boolean first = true;
		for (final ExpressionNode element : elements()) {
			if (first) first = false;
			else _to.append( ", " );
			element.describeTo( _to, _cfg );
		}
		_to.append( " )" );
	}

}
