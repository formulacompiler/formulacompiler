package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.model.util.InterpretedNumericType;

final class EvalLetVar extends EvalShadow
{
	private final String varName;

	public EvalLetVar(ExpressionNodeForLetVar _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.varName = _node.varName();
	}


	@Override
	protected Object eval()
	{
		return letDict().lookup( this.varName );
	}


	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "EvalLetVar.evaluateToConst() should never be called" );
	}

}
