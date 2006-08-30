package sej.internal.model.optimizer.consteval;

import sej.Operator;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.model.util.InterpretedNumericType;

public class EvalMapReduceOr extends EvalMapReduce
{

	public EvalMapReduceOr(ExpressionNodeForAggregator _node, InterpretedNumericType _type)
	{
		super( _node, Operator.OR, _type );
	}

	
	@Override
	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args )
	{
		for (Object arg : _args) {
			if (!(arg instanceof ExpressionNode)) {
				if (type().toBoolean( arg )) return Boolean.TRUE;
			}
		}
		return super.evaluateToConstOrExprWithConstantArgsFixed( _args );
	}
	
	
}
