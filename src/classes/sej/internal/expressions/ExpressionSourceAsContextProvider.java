package sej.internal.expressions;

import java.io.IOException;

import sej.describable.DescriptionBuilder;

public class ExpressionSourceAsContextProvider implements ExpressionContextProvider
{
	private final ExpressionNode expr;

	public ExpressionSourceAsContextProvider(ExpressionNode _expr)
	{
		super();
		this.expr = _expr.getOrigin();
	}

	public void buildContext( DescriptionBuilder _result, ExpressionNode _focusedNode )
	{
		_result.append( "\nIn expression " );
		
		try {
			final ExpressionNode focus = (_focusedNode == null)? null : _focusedNode.getOrigin();
			this.expr.describeTo( _result, new ExpressionDescriptionConfig( focus, " >> ", " << ") );
		}
		catch (IOException e) {
			_result.append( " >> ERROR describing expression:" ).append( e.getMessage() );
		}
		
		_result.append( "; error location indicated by >>..<<." );
	}

}
