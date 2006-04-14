package sej.engine.bytecode.compiler;

import java.math.BigDecimal;

import sej.CallFrame;
import sej.Engine;
import sej.ModelError;
import sej.NumericType;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.EngineModel;
import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.SectionModel;
import sej.engine.expressions.ExpressionNodeForOperator;
import sej.engine.expressions.Operator;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;
import junit.framework.TestCase;


public class ByteCodeCompilerOnEngineModelTest extends TestCase
{


	public void testOperators() throws ModelError, NoSuchMethodException
	{
		final double a = 370.35;
		final double b = 3.0;
		assertOperator( Operator.PLUS, a + b );
		assertOperator( Operator.MINUS, a - b );
		assertOperator( Operator.TIMES, a * b );
		assertOperator( Operator.DIV, a / b );
		assertOperator( Operator.EXP, Math.pow( a, b ) );
		assertOperator( Operator.MIN, b );
		assertOperator( Operator.MAX, a );
		assertUnaryOperator( Operator.MINUS, -a );
		assertUnaryOperator( Operator.PERCENT, a / 100 );
	}


	private void assertOperator( final Operator _operator, final double _expectedResult ) throws NoSuchMethodException,
			ModelError
	{
		assertDoubleOperator( _operator, _expectedResult );
		assertBigDecimalOperator( _operator, _expectedResult );
	}


	private void assertUnaryOperator( final Operator _operator, final double _expectedResult )
			throws NoSuchMethodException, ModelError
	{
		assertDoubleUnaryOperator( _operator, _expectedResult );
		assertBigDecimalUnaryOperator( _operator, _expectedResult );
	}


	private void assertDoubleOperator( final Operator _operator, final double _expectedResult )
			throws NoSuchMethodException, ModelError
	{
		final EngineModel engineModel = new EngineModel();
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleB" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( _expectedResult, engineModel );
	}


	private void assertDoubleUnaryOperator( final Operator _operator, final double _expectedResult )
			throws NoSuchMethodException, ModelError
	{
		final EngineModel engineModel = new EngineModel();
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getDoubleA" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( _expectedResult, engineModel );
	}


	private void assertDoubleResult( final double _expectedResult, final EngineModel _engineModel ) throws ModelError
	{
		final ByteCodeCompiler compiler = new ByteCodeCompiler( null, Inputs.class, Outputs.class, NumericType.DOUBLE );
		final Engine engine = compiler.compileNewEngine( _engineModel );
		final Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		final double d = outputs.getResult();
		assertEquals( _expectedResult, d, 0.000001 );
	}


	private void assertBigDecimalOperator( final Operator _operator, final double _expectedResult )
			throws NoSuchMethodException, ModelError
	{
		final EngineModel engineModel = new EngineModel();
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getBigDecimalA" ) ) );
		b.makeInput( new CallFrame( Inputs.class.getMethod( "getBigDecimalB" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getBigDecimalA" ) ) );

		assertBigDecimalResult( _expectedResult, engineModel );
	}


	private void assertBigDecimalUnaryOperator( final Operator _operator, final double _expectedResult )
			throws NoSuchMethodException, ModelError
	{
		final EngineModel engineModel = new EngineModel();
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrame( Inputs.class.getMethod( "getBigDecimalA" ) ) );
		r.makeOutput( new CallFrame( Outputs.class.getMethod( "getBigDecimalA" ) ) );

		assertBigDecimalResult( _expectedResult, engineModel );
	}


	private void assertBigDecimalResult( final double _expectedResult, final EngineModel _engineModel )
			throws ModelError
	{
		final ByteCodeCompiler compiler = new ByteCodeCompiler( null, Inputs.class, Outputs.class, NumericType.BIGDECIMAL );
		final Engine engine = compiler.compileNewEngine( _engineModel );
		final Outputs outputs = (Outputs) engine.newComputation( new Inputs() );
		final BigDecimal v = outputs.getBigDecimalA();
		final double d = v.doubleValue();
		assertEquals( _expectedResult, d, 0.000001 );
	}

}
