package sej.engine.compiler.model.optimizer.expreval;

import sej.expressions.ExpressionNode;

public class EvalPassthrough extends EvalShadow
{

	public EvalPassthrough(ExpressionNode _node)
	{
		super( _node, null );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		return _args[0];
	}

}
