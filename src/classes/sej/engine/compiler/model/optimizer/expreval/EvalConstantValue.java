package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForConstantValue;

public class EvalConstantValue extends EvalShadow
{

	EvalConstantValue(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		return ((ExpressionNodeForConstantValue) getNode()).getValue();
	}

}
