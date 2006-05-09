package sej.engine.compiler.model.optimizer.expreval;

import sej.engine.compiler.model.ExpressionNodeForRangeValue;
import sej.engine.compiler.model.RangeValue;
import sej.engine.compiler.model.util.InterpretedNumericType;
import sej.expressions.Aggregator;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForAggregator;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.expressions.ExpressionNodeForFunction;
import sej.expressions.ExpressionNodeForIf;
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Function;
import sej.expressions.Operator;
import junit.framework.TestCase;

public abstract class AbstractExpressionInterpreterTest extends TestCase
{


	public void testConstants()
	{
		assertEval( "123.45", cst( "123.45" ) );
	}


	public void testOperators()
	{
		assertEval( "357.95", new ExpressionNodeForOperator( Operator.PLUS, cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "-111.05", new ExpressionNodeForOperator( Operator.MINUS, cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "-123.45", new ExpressionNodeForOperator( Operator.MINUS, cst( "123.45" ) ) );
		assertEval( "28949.025", new ExpressionNodeForOperator( Operator.TIMES, cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "24.69", new ExpressionNodeForOperator( Operator.DIV, cst( "123.45" ), cst( "5" ) ) );
		assertEval( "15239.9025", new ExpressionNodeForOperator( Operator.EXP, cst( "123.45" ), cst( "2" ) ) );
		// computing a root is not supported by BigDecimal, so the corresponding test is only in
		// DoubleInterpreterTest
		assertEval( "1.2345", new ExpressionNodeForOperator( Operator.PERCENT, cst( "123.45" ) ) );

		assertEval( "123.45", new ExpressionNodeForOperator( Operator.MIN, cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "123.45", new ExpressionNodeForOperator( Operator.MIN, cst( "123.45" ), cst( null ) ) );
		assertEval( "123.45", new ExpressionNodeForOperator( Operator.MIN, cst( null ), cst( "123.45" ) ) );
		assertEval( null, new ExpressionNodeForOperator( Operator.MIN, cst( null ), cst( null ) ) );
		assertEval( "234.5", new ExpressionNodeForOperator( Operator.MAX, cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "-123.45", new ExpressionNodeForOperator( Operator.MAX, cst( "-123.45" ), cst( null ) ) );
		assertEval( "-123.45", new ExpressionNodeForOperator( Operator.MAX, cst( null ), cst( "-123.45" ) ) );
		assertEval( null, new ExpressionNodeForOperator( Operator.MAX, cst( null ), cst( null ) ) );

		assertEval( "123.45234.5", new ExpressionNodeForOperator( Operator.CONCAT, cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "0", new ExpressionNodeForOperator( Operator.CONCAT, cst( null ), cst( "0" ) ) );
	}


	public void testComparisons()
	{
		assertEval( "true", new ExpressionNodeForOperator( Operator.EQUAL, cst( "123.45" ), cst( "123.45" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.EQUAL, cst( "123.45" ), cst( "123.46" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.EQUAL, cst( null ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.EQUAL, cst( null ), cst( "0" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.EQUAL, cst( null ), cst( "1" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.EQUAL, cst( "0" ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.EQUAL, cst( "1" ), cst( null ) ) );

		assertEval( "false", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( "123.45" ), cst( "123.45" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( "123.45" ), cst( "123.46" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( null ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( null ), cst( "0" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( null ), cst( "1" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( "0" ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.NOTEQUAL, cst( "1" ), cst( null ) ) );

		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( "123.45" ), cst( "123.45" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( "123.45" ), cst( "123.46" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATER, cst( "123.45" ), cst( "123.44" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( null ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( null ), cst( "0" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( null ), cst( "1" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATER, cst( null ), cst( "-1" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( "0" ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATER, cst( "1" ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATER, cst( "-1" ), cst( null ) ) );

		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( "123.45" ), cst( "123.45" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( "123.45" ), cst( "123.46" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( "123.45" ), cst( "123.44" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( null ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( null ), cst( "0" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( null ), cst( "1" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( null ), cst( "-1" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( "0" ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( "1" ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.GREATEROREQUAL, cst( "-1" ), cst( null ) ) );

		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( "123.45" ), cst( "123.45" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESS, cst( "123.45" ), cst( "123.46" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( "123.45" ), cst( "123.44" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( null ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( null ), cst( "0" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESS, cst( null ), cst( "1" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( null ), cst( "-1" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( "0" ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESS, cst( "1" ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESS, cst( "-1" ), cst( null ) ) );

		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( "123.45" ), cst( "123.45" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( "123.45" ), cst( "123.46" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( "123.45" ), cst( "123.44" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( null ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( null ), cst( "0" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( null ), cst( "1" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( null ), cst( "-1" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( "0" ), cst( null ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( "1" ), cst( null ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.LESSOREQUAL, cst( "-1" ), cst( null ) ) );
	}


	public void testBooleanLogic()
	{
		assertEval( "true", new ExpressionNodeForOperator( Operator.AND, cst( "1" ), cst( "2" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.AND, cst( "1" ), cst( "0" ) ) );
		assertEval( "true", new ExpressionNodeForOperator( Operator.OR, cst( "1" ), cst( "0" ) ) );
		assertEval( "false", new ExpressionNodeForOperator( Operator.OR, cst( "0" ), cst( "0" ) ) );

		assertEval( "true", new ExpressionNodeForFunction( Function.NOT, cst( "0" ) ) );
		assertEval( "false", new ExpressionNodeForFunction( Function.NOT, cst( "1" ) ) );
		assertEval( "true", new ExpressionNodeForFunction( Function.NOT, cst( null ) ) );

	}


	public void testIf()
	{
		assertEval( "123.45", new ExpressionNodeForIf( cst( "1" ), cst( "123.45" ), cst( "234.5" ) ) );
		assertEval( "234.5", new ExpressionNodeForIf( cst( "0" ), cst( "123.45" ), cst( "234.5" ) ) );
	}


	public void testRound()
	{
		assertEval( "1", new ExpressionNodeForFunction( Function.ROUND, cst( "1.4" ), cst( "0" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.ROUND, cst( "1.5" ), cst( "0" ) ) );
		assertEval( "0", new ExpressionNodeForFunction( Function.ROUND, cst( null ), cst( "0" ) ) );
		assertEval( "0", new ExpressionNodeForFunction( Function.ROUND, cst( null ), cst( null ) ) );
		assertEval( "1", new ExpressionNodeForFunction( Function.ROUND, cst( "1.2" ), cst( null ) ) );
	}


	public void testMatch()
	{
		RangeValue asc = new RangeValue( 1, 1, 3 );
		asc.add( valueFromString( "1" ) );
		asc.add( valueFromString( "2" ) );
		asc.add( valueFromString( "4" ) );
		ExpressionNodeForRangeValue cstAsc = new ExpressionNodeForRangeValue( asc );

		assertEval( "0", new ExpressionNodeForFunction( Function.MATCH, cst( "0" ), cstAsc, cst( "1" ) ) );
		assertEval( "1", new ExpressionNodeForFunction( Function.MATCH, cst( "1" ), cstAsc, cst( "1" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.MATCH, cst( "2" ), cstAsc, cst( "1" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.MATCH, cst( "3" ), cstAsc, cst( "1" ) ) );
		assertEval( "3", new ExpressionNodeForFunction( Function.MATCH, cst( "4" ), cstAsc, cst( "1" ) ) );
		assertEval( "3", new ExpressionNodeForFunction( Function.MATCH, cst( "5" ), cstAsc, cst( "1" ) ) );

		assertEval( "0", new ExpressionNodeForFunction( Function.MATCH, cst( "0" ), cstAsc, cst( "0" ) ) );
		assertEval( "1", new ExpressionNodeForFunction( Function.MATCH, cst( "1" ), cstAsc, cst( "0" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.MATCH, cst( "2" ), cstAsc, cst( "0" ) ) );
		assertEval( "0", new ExpressionNodeForFunction( Function.MATCH, cst( "3" ), cstAsc, cst( "0" ) ) );
		assertEval( "3", new ExpressionNodeForFunction( Function.MATCH, cst( "4" ), cstAsc, cst( "0" ) ) );
		assertEval( "0", new ExpressionNodeForFunction( Function.MATCH, cst( "5" ), cstAsc, cst( "0" ) ) );

		RangeValue desc = new RangeValue( 1, 1, 3 );
		desc.add( valueFromString( "4" ) );
		desc.add( valueFromString( "2" ) );
		desc.add( valueFromString( "1" ) );
		ExpressionNodeForRangeValue cstDesc = new ExpressionNodeForRangeValue( desc );

		assertEval( "0", new ExpressionNodeForFunction( Function.MATCH, cst( "5" ), cstDesc, cst( "-1" ) ) );
		assertEval( "1", new ExpressionNodeForFunction( Function.MATCH, cst( "4" ), cstDesc, cst( "-1" ) ) );
		assertEval( "1", new ExpressionNodeForFunction( Function.MATCH, cst( "3" ), cstDesc, cst( "-1" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.MATCH, cst( "2" ), cstDesc, cst( "-1" ) ) );
		assertEval( "3", new ExpressionNodeForFunction( Function.MATCH, cst( "1" ), cstDesc, cst( "-1" ) ) );
		assertEval( "3", new ExpressionNodeForFunction( Function.MATCH, cst( "0" ), cstDesc, cst( "-1" ) ) );
	}


	public void testIndex2D()
	{
		RangeValue rng = new RangeValue( 1, 1, 3 );
		rng.add( valueFromString( "1" ) );
		rng.add( valueFromString( "2" ) );
		rng.add( valueFromString( "4" ) );
		ExpressionNodeForRangeValue cstRng = new ExpressionNodeForRangeValue( rng );

		assertEval( "1", new ExpressionNodeForFunction( Function.INDEX, cstRng, cst( "1" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.INDEX, cstRng, cst( "2" ) ) );
		assertEval( "4", new ExpressionNodeForFunction( Function.INDEX, cstRng, cst( "3" ) ) );
	}


	public void testIndex3D()
	{
		RangeValue rng = new RangeValue( 1, 2, 3 );

		rng.add( valueFromString( "1" ) );
		rng.add( valueFromString( "2" ) );
		rng.add( valueFromString( "4" ) );

		rng.add( valueFromString( "-1" ) );
		rng.add( valueFromString( "-2" ) );
		rng.add( valueFromString( "-4" ) );

		rng.add( valueFromString( "5" ) );
		rng.add( valueFromString( "6" ) );
		rng.add( valueFromString( "7" ) );

		ExpressionNodeForRangeValue cstRng = new ExpressionNodeForRangeValue( rng );

		assertEval( "1", new ExpressionNodeForFunction( Function.INDEX, cstRng, cst( "1" ), cst( "1" ) ) );
		assertEval( "-1", new ExpressionNodeForFunction( Function.INDEX, cstRng, cst( "2" ), cst( "1" ) ) );
		assertEval( "2", new ExpressionNodeForFunction( Function.INDEX, cstRng, cst( "1" ), cst( "2" ) ) );
	}


	public void testSum()
	{
		assertEval( "0", new ExpressionNodeForAggregator( Aggregator.SUM ) );
		assertEval( "392.51", new ExpressionNodeForAggregator( Aggregator.SUM, cst( "123.45" ), cst( "234.5" ),
				cst( "34.56" ) ) );
		assertEval( "158.01", new ExpressionNodeForAggregator( Aggregator.SUM, cst( "123.45" ), cst( null ),
				cst( "34.56" ) ) );
	}


	public void testProduct()
	{
		assertEval( "0", new ExpressionNodeForAggregator( Aggregator.PRODUCT ) );
		assertEval( "184.5", new ExpressionNodeForAggregator( Aggregator.PRODUCT, cst( "123" ), cst( "0.5" ),
				cst( "3" ) ) );
		assertEval( "370.35", new ExpressionNodeForAggregator( Aggregator.PRODUCT, cst( "123.45" ), cst( null ),
				cst( "3" ) ) );
	}


	public void testAverage()
	{
		assertEval( "1.3", new ExpressionNodeForAggregator( Aggregator.AVERAGE, cst( "1.2" ), cst( "1.3" ),
				cst( "1.4" ) ) );
	}


	protected abstract InterpretedNumericType getType();
	protected abstract Object valueFromString( String _string );

	protected ExpressionNodeForConstantValue cst( String _asString )
	{
		if (null == _asString) {
			return new ExpressionNodeForConstantValue( null );
		}
		else {
			return new ExpressionNodeForConstantValue( valueFromString( _asString ) );
		}
	}

	protected void assertEval( String _expected, ExpressionNode _expr )
	{
		Object result = EvalShadow.evaluate( _expr, getType() );
		if (null == _expected) {
			assertNull( _expr.toString(), result );
		}
		else {
			String actual = (result == null) ? "null" : result.toString();
			if (actual.endsWith( ".0" )) actual = actual.substring( 0, actual.length() - 2 );
			assertEquals( _expr.toString(), _expected, actual );
		}
	}

}
