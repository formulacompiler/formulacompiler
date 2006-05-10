/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are prohibited, unless you have been explicitly granted 
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS 
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE 
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS 
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND 
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR 
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE 
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
import sej.expressions.ExpressionNodeForOperator;
import sej.expressions.Operator;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;
import junit.framework.TestCase;


public class ByteCodeCompilerOnEngineModelTest extends TestCase
{


	public void testOperators() throws ModelError, NoSuchMethodException
	{
		final double a = 100.34;
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
		final ByteCodeCompiler compiler = new ByteCodeCompiler( null, Inputs.class, Outputs.class, NumericType.BIGDECIMAL8 );
		final Engine engine = compiler.compileNewEngine( _engineModel );
		final Outputs outputs = (Outputs) engine.newComputation( new Inputs() );
		final BigDecimal v = outputs.getBigDecimalA();
		final double d = v.doubleValue();
		assertEquals( _expectedResult, d, 0.000001 );
	}

}
