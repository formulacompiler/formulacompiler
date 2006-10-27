package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.model.ExpressionNodeForSubstitution;
import sej.internal.model.util.InterpretedNumericType;

final class EvalFold extends EvalShadow
{
	private final String accName;
	private final String eltName;

	public EvalFold(ExpressionNodeForFold _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.accName = _node.accumulatorName();
		this.eltName = _node.elementName();
	}


	@Override
	protected Object eval()
	{
		final Object[] args = new Object[ node().arguments().size() ];
		args[ 0 ] = evaluateArgument( 0 ); // initial
		args[ 1 ] = node().argument( 1 ); // fold
		for (int i = 2; i < args.length; i++) {
			args[ i ] = evaluateArgument( i );
		}

		Object acc = args[ 0 ];
		if (isConstant( acc )) {
			for (int i = args.length - 1; i >= 2; i--) {
				final Object xi = args[ i ];
				if (xi instanceof ExpressionNodeForSubstitution) {
					node().arguments().remove( i );
					acc = fold( acc, ((ExpressionNodeForSubstitution) xi).arguments() );
				}
				else if (isConstant( xi )) {
					acc = fold( acc, xi );
					node().arguments().remove( i );
				}
			}
			if (node().arguments().size() == 2) {
				return acc;
			}
			else {
				if (fixArg( node().arguments(), 0, acc )) {
					// We set the partial fold as the new initial value.
					((ExpressionNodeForFold) node()).neverInlineFirst();
				}
				return node();
			}
		}
		else {
			return nodeWithConstantArgsFixed( args );
		}
	}


	private final Object fold( final Object _acc, final Iterable<ExpressionNode> _nodes )
	{
		Object acc = _acc;
		for (final ExpressionNode node : _nodes) {
			if (node instanceof ExpressionNodeForConstantValue) {
				final ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) node;
				acc = fold( acc, constNode.getValue() );
			}
			else if (node instanceof ExpressionNodeForSubstitution) {
				acc = fold( acc, node.arguments() );
			}
			else {
				this.node().addArgument( node );
			}
		}
		return acc;
	}

	
	private final Object fold( final Object _acc, final Object _val )
	{
		letDict().let( this.accName, null, _acc );
		letDict().let( this.eltName, null, _val );
		try {
			return evaluateArgument( 1 ); // fold
		}
		finally {
			letDict().unlet( this.eltName );
			letDict().unlet( this.accName );
		}
	}

	
	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "EvalFold.evaluateToConst() should never be called" );
	}

}
