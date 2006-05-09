package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;

public abstract class EvalShortCircuitedBooleanOperator extends EvalOperator
{

	public EvalShortCircuitedBooleanOperator(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}
	
	
	@Override
	public Object eval()
	{
		final int card = cardinality();
		switch (card) {
			case 2:
				final Object firstArg = evaluateArgument( 0 );
				if (isConstant( firstArg )) {
					final boolean constFirstArg = getType().toBoolean( firstArg );
					return eval( constFirstArg );
				}
		}
		return super.eval();
	}


	protected abstract Object eval( final boolean _constFirstArg );


	protected Object evaluateSecondArgAsBoolean()
	{
		Object secondArg = evaluateArgument( 1 );
		if (isConstant( secondArg )) {
			return getType().toBoolean( secondArg );
		}
		else {
			return secondArg;
		}
	}


}
