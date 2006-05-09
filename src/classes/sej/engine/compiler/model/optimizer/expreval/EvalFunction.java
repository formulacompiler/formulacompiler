package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForFunction;
import sej.expressions.Function;

public class EvalFunction extends EvalShadow
{

	EvalFunction(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		final Function function = ((ExpressionNodeForFunction) getNode()).getFunction();
		return getType().compute( function, _args );
	}

}
