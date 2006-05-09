package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;

public class EvalAnd extends EvalShortCircuitedBooleanOperator
{

	public EvalAnd(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	protected Object eval( final boolean _constFirstArg )
	{
		if (_constFirstArg) {
			return evaluateSecondArgAsBoolean();
		}
		else {
			return false;
		}
	}


}
