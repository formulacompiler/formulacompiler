package sej.internal.expressions;

import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;

import sej.describable.DescriptionBuilder;

public class ExpressionNodeForFold1st extends AbstractExpressionNodeForFold
{
	private final String firstName;

	private ExpressionNodeForFold1st(String _firstName, String _accumulatorName, String _elementName)
	{
		super( _accumulatorName, _elementName );
		this.firstName = _firstName;
	}

	public ExpressionNodeForFold1st(String _firstName, ExpressionNode _firstValue, String _accumulatorName,
			String _elementName, ExpressionNode _accumulatingStep, ExpressionNode _emptyValue, ExpressionNode... _elements)
	{
		this( _firstName, _accumulatorName, _elementName );
		addArgument( _emptyValue );
		addArgument( _accumulatingStep );
		addArgument( _firstValue );
		addArguments( _elements );
	}

	public ExpressionNodeForFold1st(String _firstName, ExpressionNode _firstValue, String _accumulatorName,
			String _elementName, ExpressionNode _accumulatingStep, ExpressionNode _emptyValue,
			Collection<ExpressionNode> _elements)
	{
		this( _firstName, _accumulatorName, _elementName );
		addArgument( _emptyValue );
		addArgument( _accumulatingStep );
		addArgument( _firstValue );
		arguments().addAll( _elements );
	}


	public final String firstName()
	{
		return this.firstName;
	}

	public final ExpressionNode firstValue()
	{
		return argument( 2 );
	}

	public final ExpressionNode emptyValue()
	{
		return initialAccumulatorValue();
	}


	@Override
	protected void skipToElements( Iterator<ExpressionNode> _iterator )
	{
		super.skipToElements( _iterator );
		_iterator.next();
	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForFold1st( this.firstName, accumulatorName(), elementName() );
	}

	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_FOLDL_1ST( " ).append( firstName() ).append( ": " );
		firstValue().describeTo( _to, _cfg );
		_to.append( "; " ).append( accumulatorName() ).append( ' ' ).append( elementName() ).append( ": " );
		accumulatingStep().describeTo( _to, _cfg );
		_to.append( "; " );
		emptyValue().describeTo( _to, _cfg );
		_to.append( "; " );
		describeElements( _to, _cfg );
	}

}
