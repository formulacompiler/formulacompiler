package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.model.util.InterpretedNumericType;

final class EvalLet extends EvalShadow
{
	private final String varName;

	public EvalLet(ExpressionNodeForLet _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.varName = _node.varName();
	}


	@Override
	protected Object eval()
	{
		final Object val = evaluateArgument( 0 );
		letDict().let( this.varName, null, val );
		try {
			final Object result = evaluateArgument( 1 );
			if (isConstant( result )) {
				return result;
			}
			return nodeWithConstantArgsFixed( new Object[] { val, result } );
		}
		finally {
			letDict().unlet( this.varName );
		}
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "EvalLet.evaluateToConst() should never be called" );
	}

}
