package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.AbstractExpressionNodeForFold;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.model.ExpressionNodeForSubstitution;
import sej.internal.model.util.InterpretedNumericType;

abstract class EvalAbstractFold extends EvalShadow
{
	private final String accName;
	private final String eltName;

	public EvalAbstractFold(AbstractExpressionNodeForFold _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.accName = _node.accumulatorName();
		this.eltName = _node.elementName();
	}


	@Override
	protected final Object eval()
	{
		final Object[] args = new Object[ node().arguments().size() ];
		final int i0 = evalFixedArgs( args, 0 );
		for (int i = i0; i < args.length; i++) {
			args[ i ] = evaluateArgument( i );
		}

		Object acc = initial( args );
		if (isConstant( acc )) {
			for (int i = args.length - 1; i >= i0; i--) {
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
			if (node().arguments().size() == i0) {
				return acc;
			}
			else {
				insertPartialFold( acc );
				return node();
			}
		}
		else {
			return nodeWithConstantArgsFixed( args );
		}
	}


	protected int evalFixedArgs( final Object[] _args, final int _i0 )
	{
		int i0 = _i0;
		_args[ i0++ ] = evaluateArgument( 0 ); // initial
		_args[ i0++ ] = node().argument( 1 ); // fold
		return i0;
	}

	
	protected Object initial( final Object[] _args )
	{
		return _args[0];
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

	
	protected Object fold( final Object _acc, final Object _val )
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

	
	protected abstract void insertPartialFold( Object _acc );


	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "EvalFold.evaluateToConst() should never be called" );
	}

}
