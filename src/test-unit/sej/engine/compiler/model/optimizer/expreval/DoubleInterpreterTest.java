package sej.engine.compiler.model.optimizer.expreval;

import sej.NumericType;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Operator;

public class DoubleInterpreterTest extends AbstractExpressionInterpreterTest
{

	@Override
	protected Object valueFromString( String _string )
	{
		return Double.valueOf( _string );
	}
	
	@Override
	protected InterpretedNumericType getType()
	{
		return InterpretedNumericType.typeFor( NumericType.DOUBLE );
	}

	
	public void testDoubleOnlyOperators()
	{
		assertEval( "5", new ExpressionNodeForOperator( Operator.EXP, cst("25"), cst("0.5") ) );
	}

}
