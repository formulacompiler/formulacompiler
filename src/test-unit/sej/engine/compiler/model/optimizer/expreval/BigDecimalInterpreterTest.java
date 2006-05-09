package sej.engine.compiler.model.optimizer.expreval;

import java.math.BigDecimal;

import sej.NumericType;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNodeForAggregator;

public class BigDecimalInterpreterTest extends AbstractExpressionInterpreterTest
{

	@Override
	protected Object valueFromString( String _string )
	{
		return new BigDecimal( _string );
	}

	@Override
	protected InterpretedNumericType getType()
	{
		return InterpretedNumericType.typeFor( NumericType.BIGDECIMAL8 );
	}
	
	@Override
	public void testAverage()
	{
		super.testAverage();
		
		// The following test fails for doubles due to rounding errors.
		assertEval( "1.3", new ExpressionNodeForAggregator( Aggregator.AVERAGE, cst( "1.2" ), cst( null ),
				cst( "1.4" ) ) );
	}

}
