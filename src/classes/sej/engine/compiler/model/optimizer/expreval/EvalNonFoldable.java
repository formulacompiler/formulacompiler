package sej.engine.compiler.model.optimizer.expreval;

import sej.expressions.ExpressionNode;

public class EvalNonFoldable extends EvalShadow
{

	public EvalNonFoldable(ExpressionNode _node)
	{
		super( _node, null );
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		return nodeWithConstantArgsFixed( _args );
	}

}
