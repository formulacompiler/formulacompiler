/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.bytecode.compiler;

import java.math.BigDecimal;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.CallFrameImpl;
import org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.optimizer.IntermediateResultsInliner;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.tests.utils.AbstractIOTestCase;
import org.formulacompiler.tests.utils.Inputs;
import org.formulacompiler.tests.utils.Outputs;


public class ByteCodeCompilerOnEngineModelTest extends AbstractIOTestCase
{


	public void testIllegalSymbolsInReadableCode() throws Exception
	{
		testReadableCode( "cell 1", "cell$2", "cell!3" );
	}

	public void testSameNamesInReadableCode() throws Exception
	{
		testReadableCode( "cell", "cell", "cell" );
	}

	private void testReadableCode( final String _name1, final String _name2, final String _name3 ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, _name1 );
		final CellModel b = new CellModel( rootModel, _name2 );
		final CellModel r = new CellModel( rootModel, _name3 );
		r.setExpression( new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleB" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getResult" ) ) );

		engineModel.traverse( new IntermediateResultsInliner() );
		engineModel.traverse( new TypeAnnotator() );
		final ByteCodeEngineCompiler.Config config = new ByteCodeEngineCompiler.Config();
		config.model = engineModel;
		config.numericType = FormulaCompiler.DOUBLE;
		config.compileToReadableCode = true;
		final ByteCodeEngineCompiler compiler = new ByteCodeEngineCompiler( config );
		final SaveableEngine engine = compiler.compile();

		checkEngine( engine );

		final ComputationFactory factory = engine.getComputationFactory();
		final Outputs outputs = (Outputs) factory.newComputation( new Inputs() );
		final double d = outputs.getResult();
		assertEquals( 103.34, d, 0.000001 );
	}


	public void testOperators() throws Exception
	{
		final double a = 100.34;
		final double b = 3.0;
		assertOperator( Operator.PLUS, a + b );
		assertOperator( Operator.MINUS, a - b );
		assertOperator( Operator.TIMES, a * b );
		assertOperator( Operator.DIV, a / b );
		assertOperator( Operator.EXP, Math.pow( a, b ) );
		assertOperator( Operator.INTERNAL_MIN, b );
		assertOperator( Operator.INTERNAL_MAX, a );
		assertUnaryOperator( Operator.MINUS, -a );
		assertUnaryOperator( Operator.PERCENT, a / 100 );
	}


	public void testArraysInTemplates() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel c = new CellModel( rootModel, "c" );
		final CellModel d = new CellModel( rootModel, "d" );
		final CellModel r = new CellModel( rootModel, "r" );
		final ArrayDescriptor desc = new ArrayDescriptor( 0, 0, 0, 1, 2, 2 );
		r.setExpression( new ExpressionNodeForFunction( Function.MDETERM, new ExpressionNodeForArrayReference( desc,
				new ExpressionNodeForCellModel( a ), new ExpressionNodeForCellModel( b ),
				new ExpressionNodeForCellModel( c ), new ExpressionNodeForCellModel( d ) ),
				new ExpressionNodeForConstantValue( 2, DataType.NUMERIC ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleB" ) ) );
		c.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleC" ) ) );
		d.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleD" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( 4586.6908, engineModel, "MDETERM" );
	}


	public void testRefToCellEqualToConst() throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForFunction( Function.AND, //
				new ExpressionNodeForCellModel( a ), //
				new ExpressionNodeForCellModel( b ) ) );
		a.setExpression( new ExpressionNodeForConstantValue( true, DataType.NUMERIC ) );
		b.setExpression( new ExpressionNodeForConstantValue( 1 ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( 1.0, engineModel, "AND(=true, =1)" );
	}


	private void assertOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		assertDoubleOperator( _operator, _expectedResult );
		assertBigDecimalOperator( _operator, _expectedResult );
		assertScaledLongOperator( _operator, _expectedResult );
	}


	private void assertUnaryOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		assertDoubleUnaryOperator( _operator, _expectedResult );
		assertBigDecimalUnaryOperator( _operator, _expectedResult );
		assertScaledLongUnaryOperator( _operator, _expectedResult );
	}


	private void assertDoubleOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleA" ) ) );
		b.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleB" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( _expectedResult, engineModel, _operator.toString() );
	}


	private void assertDoubleUnaryOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getDoubleA" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getResult" ) ) );

		assertDoubleResult( _expectedResult, engineModel, "Unary_" + _operator.toString() );
	}


	private void assertDoubleResult( final double _expectedResult, final ComputationModel _engineModel, final String _id )
			throws Exception
	{
		final Outputs outputs = newOutputs( _engineModel, FormulaCompiler.DOUBLE, _id + "_double" );
		final double d = outputs.getResult();
		assertEquals( _expectedResult, d, 0.000001 );
	}


	private void assertBigDecimalOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getBigDecimalA" ) ) );
		b.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getBigDecimalB" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getBigDecimalA" ) ) );

		assertBigDecimalResult( _expectedResult, engineModel, _operator.toString(), FormulaCompiler.BIGDECIMAL_SCALE8,
				"_big" );
		assertBigDecimalResult( _expectedResult, engineModel, _operator.toString(), FormulaCompiler.BIGDECIMAL64, "_bigp" );
	}


	private void assertBigDecimalUnaryOperator( final Operator _operator, final double _expectedResult )
			throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getBigDecimalA" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getBigDecimalA" ) ) );

		assertBigDecimalResult( _expectedResult, engineModel, "Unary_" + _operator.toString(),
				FormulaCompiler.BIGDECIMAL_SCALE8, "_big" );
		assertBigDecimalResult( _expectedResult, engineModel, "Unary_" + _operator.toString(),
				FormulaCompiler.BIGDECIMAL64, "_bigp" );
	}


	private void assertBigDecimalResult( final double _expectedResult, final ComputationModel _engineModel,
			final String _id, NumericType _numericType, String _suffix ) throws Exception
	{
		final Outputs outputs = newOutputs( _engineModel, _numericType, _id + _suffix );
		final BigDecimal v = outputs.getBigDecimalA();
		final double d = v.doubleValue();
		assertEquals( _expectedResult, d, 0.000001 );
	}


	private void assertScaledLongOperator( final Operator _operator, final double _expectedResult ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel b = new CellModel( rootModel, "b" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForCellModel( b ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getScaledLongA" ) ) );
		b.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getScaledLongB" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getScaledLongA" ) ) );

		assertScaledLongResult( _expectedResult, engineModel, _operator.toString() );
	}


	private void assertScaledLongUnaryOperator( final Operator _operator, final double _expectedResult )
			throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( Inputs.class, Outputs.class );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "a" );
		final CellModel r = new CellModel( rootModel, "r" );
		r.setExpression( new ExpressionNodeForOperator( _operator, new ExpressionNodeForCellModel( a ) ) );

		a.makeInput( new CallFrameImpl( Inputs.class.getMethod( "getScaledLongA" ) ) );
		r.makeOutput( new CallFrameImpl( Outputs.class.getMethod( "getScaledLongA" ) ) );

		assertScaledLongResult( _expectedResult, engineModel, "Unary_" + _operator.toString() );
	}


	private void assertScaledLongResult( final double _expectedResult, final ComputationModel _engineModel,
			final String _id ) throws Exception
	{
		final Outputs outputs = newOutputs( _engineModel, FormulaCompiler.LONG_SCALE4, _id + "_long4" );
		final long actual = outputs.getScaledLongA();
		final long expected = FormulaCompiler.LONG_SCALE4.valueOf( _expectedResult ).longValue();
		final long diff = actual - expected;
		if (diff > 1 || diff < -1) {
			// accept difference in the last decimal due to rounding problems with division
			assertEquals( expected, actual );
		}
	}


	private Outputs newOutputs( ComputationModel _engineModel, NumericType _numericType, String _id ) throws Exception
	{
		_engineModel.traverse( new IntermediateResultsInliner() );
		_engineModel.traverse( new TypeAnnotator() );
		final ByteCodeEngineCompiler.Config config = new ByteCodeEngineCompiler.Config();
		config.model = _engineModel;
		config.numericType = _numericType;
		final ByteCodeEngineCompiler compiler = new ByteCodeEngineCompiler( config );
		final SaveableEngine engine = compiler.compile();

		checkEngine( engine, _id );

		final ComputationFactory factory = engine.getComputationFactory();
		return (Outputs) factory.newComputation( new Inputs() );
	}

}
