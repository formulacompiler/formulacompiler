package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.ExpressionNodeForRangeValue;
import sej.engine.compiler.model.RangeValue;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;

public class EvalRangeValue extends EvalShadow
{

	public EvalRangeValue(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		ExpressionNodeForRangeValue rangeNode = (ExpressionNodeForRangeValue) getNode();
		RangeValue result = (RangeValue) rangeNode.getRangeValue().clone();
		for (Object arg : _args) {
			result.add( arg );
		}
		return result;
	}

}
