package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Operator;

public class EvalOperator extends EvalShadow
{
	
	EvalOperator(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}
	
	
	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		final Operator operator = ((ExpressionNodeForOperator) getNode()).getOperator();
		return getType().compute( operator, _args );
	}

}
