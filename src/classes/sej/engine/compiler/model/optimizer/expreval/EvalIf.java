package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNode;

public class EvalIf extends EvalFunction
{

	public EvalIf(ExpressionNode _node, InterpretedNumericType _type)
	{
		super( _node, _type );
	}


	@Override
	public Object eval()
	{
		final int card = cardinality();
		if (card > 0) {
			final Object firstArg = evaluateArgument( 0 );
			if (isConstant( firstArg )) {
				final boolean constFirstArg = getType().toBoolean( firstArg );
				switch (card) {
					case 2:
						return (constFirstArg) ? evaluateArgument( 1 ) : null;
					case 3:
						return (constFirstArg) ? evaluateArgument( 1 ) : evaluateArgument( 2 );
				}
			}
		}
		return super.eval();
	}

}
