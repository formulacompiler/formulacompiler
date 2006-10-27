package sej.internal.model.optimizer.consteval;

import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.model.util.InterpretedNumericType;

final class EvalFold extends EvalAbstractFold
{

	public EvalFold(ExpressionNodeForFold _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	protected void insertPartialFold( Object _acc )
	{
		if (fixArg( node().arguments(), 0, _acc )) {
			((ExpressionNodeForFold) node()).neverInlineFirst();
		}
	}

}
