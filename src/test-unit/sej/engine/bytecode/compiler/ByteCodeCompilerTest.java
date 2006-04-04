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

import sej.CallFrame;
import sej.Compiler;
import sej.CompilerFactory;
import sej.Engine;
import sej.ModelError;
import sej.Compiler.Section;
import sej.engine.expressions.Aggregator;
import sej.engine.expressions.ExpressionNodeForAggregator;
import sej.engine.expressions.ExpressionNodeForConstantValue;
import sej.engine.expressions.ExpressionNodeForOperator;
import sej.engine.expressions.Operator;
import sej.model.CellIndex;
import sej.model.CellInstance;
import sej.model.CellWithConstant;
import sej.model.CellWithLazilyParsedExpression;
import sej.model.ExpressionNodeForCell;
import sej.model.Row;
import sej.model.Sheet;
import sej.model.Workbook;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.InputInterface;
import sej.tests.utils.Inputs;
import sej.tests.utils.Outputs;
import sej.tests.utils.WorksheetBuilderWithBands;


public class ByteCodeCompilerTest extends AbstractTestBase
{
	protected Workbook workbook;
	protected Sheet sheet;
	protected Row row;
	protected CellWithLazilyParsedExpression formula;


	static {
		ByteCodeCompiler.registerAsDefault();
	}


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.workbook = new Workbook();
		this.sheet = new Sheet( this.workbook );
		this.row = new Row( this.sheet );
		this.formula = new CellWithLazilyParsedExpression( this.row );
	}


	public void testInputIsOutput() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		assertResult( 123.0, null, null );
		assertResult( 456.0, new CellInstance[] { this.formula }, new double[] { 456.0 } );
	}


	public void testParametrizedInput() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, Inputs.class, Outputs.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getPlusOne",
				Double.TYPE ), 100.0 ) );
		root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );

		Engine engine = compiler.compileNewEngine();
		Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		assertEquals( 101.0, outputs.getResult(), 0.01 );
	}


	public void testChainedInput() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, Inputs.class, Outputs.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class
				.getMethod( "getInner", Double.TYPE ), 100.0 ).chain( Inputs.Inner.class.getMethod( "getTimesTwo" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );

		Engine engine = compiler.compileNewEngine();
		Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		assertEquals( 200.0, outputs.getResult(), 0.01 );
	}


	public void testInputInterface() throws SecurityException, ModelError, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, InputInterface.class, Outputs.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( InputInterface.class.getMethod( "getOne" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );

		Engine engine = compiler.compileNewEngine();
		Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		assertEquals( 1.0, outputs.getResult(), 0.01 );
	}


	public void testDefaultOuputInPackage() throws ModelError
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, Inputs.class, OutputsInPackage.class );

		Engine engine = compiler.compileNewEngine();
		OutputsInPackage outputs = (OutputsInPackage) engine.newComputation( new Inputs() );

		assertEquals( 12345.67, outputs.getResult(), 0.01 );
	}


	public void testDefaultOuputReferencingInput() throws ModelError
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, Inputs.class, OutputsWithDefault.class );

		Engine engine = compiler.compileNewEngine();
		OutputsWithDefault outputs = (OutputsWithDefault) engine.newComputation( new Inputs() );

		assertEquals( 12346.67, outputs.getResult(), 0.01 );
	}


	public static abstract class OutputsWithDefault
	{
		private final Inputs input;

		public OutputsWithDefault(Inputs _input)
		{
			super();
			this.input = _input;
		}

		public double getResult()
		{
			return this.input.getOne() + 12345.67;
		}

	}


	public void testInputThrowingDeclaredException() throws SecurityException, ModelError, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( ThrowingInput.class.getMethod( "getFails" ) ) );
		root
				.defineOutputCell( this.formula.getCellIndex(),
						new CallFrame( ThrowingOutput.class.getMethod( "getFails" ) ) );

		Engine engine = compiler.compileNewEngine();
		ThrowingOutput outputs = (ThrowingOutput) engine.newComputation( new ThrowingInput() );

		try {
			outputs.getFails();
			fail();
		}
		catch (Failure e) {
			// ok
		}
	}


	public void testInputThrowingUndeclaredException() throws Throwable
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( ThrowingInput.class.getMethod( "getFails" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( ThrowingOutput.class
				.getMethod( "getShouldNotFail" ) ) );

		Engine engine = compiler.compileNewEngine();
		ThrowingOutput outputs = (ThrowingOutput) engine.newComputation( new ThrowingInput() );

		try {
			outputs.getShouldNotFail();
			fail();
		}
		catch (Throwable t) {
			if (t instanceof Failure)
			/* ok */;
			else throw t;
		}
	}


	public static class ThrowingInput
	{
		public double getFails() throws Failure
		{
			throw new Failure();
		}
	}


	public static abstract class ThrowingOutput
	{
		public abstract double getFails() throws Failure;

		public abstract double getShouldNotFail();
	}


	public static class Failure extends Throwable
	{
		// NOP
	}


	public void testOutputIsInterface() throws ModelError, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, null, OutputInterface.class );
		Section root = compiler.getRoot();

		root.defineOutputCell( this.formula.getCellIndex(),
				new CallFrame( OutputInterface.class.getMethod( "getResult" ) ) );

		Engine engine = compiler.compileNewEngine();
		OutputInterface output = (OutputInterface) engine.newComputation( null );

		double result = output.getResult();

		assertEquals( 123.0, result, 0.0001 );
	}


	public static interface OutputInterface
	{
		double getResult();
	}



	// TODO xtest
	public void xtestDoubleObj() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getDoubleObj" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getResult" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getDoubleObj" ) ) );

		Engine engine = compiler.compileNewEngine();
		Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		assertEquals( 123.45, outputs.getResult(), 0.01 );
		assertEquals( 123.45, outputs.getDoubleObj(), 0.01 );
	}


	// TODO xtest
	public void xtestDoubleObjToDoubleObj() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getDoubleObj" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getDoubleObj" ) ) );

		Engine engine = compiler.compileNewEngine();
		Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		assertNull( outputs.getDoubleObj() );
	}


	// TODO xtest
	public void xtestDoubleNull() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getDoubleNull" ) ) );
		root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getResult" ) ) );

		Engine engine = compiler.compileNewEngine();
		Outputs outputs = (Outputs) engine.newComputation( new Inputs() );

		assertEquals( 0, outputs.getResult(), 0.01 );
	}


	public void testUnsupportedInputType() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getUnsupported" ) ) );
		try {
			compiler.compileNewEngine();
			fail();
		}
		catch (ModelError.UnsupportedDataType e) {
			// expected
		}
	}


	public void testUnsupportedOutputType() throws ModelError, SecurityException, NoSuchMethodException
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, ThrowingInput.class, ThrowingOutput.class );
		Section root = compiler.getRoot();

		root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getUnsupported" ) ) );
		try {
			compiler.compileNewEngine();
			fail();
		}
		catch (ModelError.UnsupportedDataType e) {
			// expected
		}
	}


	public void testAddingConstants() throws ModelError, SecurityException, NoSuchMethodException
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertResult( 579.0, null, null );
		assertResult( 345.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertResult( 3.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
	}


	public void testSums() throws ModelError, SecurityException, NoSuchMethodException
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForAggregator( Aggregator.SUM, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertResult( 579.0, null, null );
		assertResult( 345.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertResult( 3.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
	}


	public void testMins() throws ModelError, SecurityException, NoSuchMethodException
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForAggregator( Aggregator.MIN, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertResult( 123.0, null, null );
		assertResult( 123.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertResult( 111.0, new CellInstance[] { b }, new double[] { 111.0 } );
		assertResult( 1.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
		assertResult( 2.0, new CellInstance[] { a, b }, new double[] { 3.0, 2.0 } );
	}


	public void testMaxs() throws ModelError, SecurityException, NoSuchMethodException
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForAggregator( Aggregator.MAX, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertResult( 456.0, null, null );
		assertResult( 222.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertResult( 123.0, new CellInstance[] { b }, new double[] { 111.0 } );
		assertResult( 2.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
		assertResult( 3.0, new CellInstance[] { a, b }, new double[] { 3.0, 2.0 } );
	}


	/**
	 * <pre>
	 *      a = 1 
	 *      b = 2 
	 *      c = 3 
	 *      d = 1 + 3 = 4 
	 *      e = c + d = 7 
	 *      f = (a + b) + e = 10 
	 *      r = a * f = 10
	 * </pre>
	 */
	public void testSubExprs() throws ModelError, SecurityException, NoSuchMethodException
	{
		CellInstance a = new CellWithConstant( this.row, 1.0 );
		CellInstance b = new CellWithConstant( this.row, 2.0 );
		CellInstance c = new CellWithConstant( this.row, 3.0 );
		CellInstance d = new CellWithLazilyParsedExpression( this.row, new ExpressionNodeForOperator( Operator.PLUS,
				new ExpressionNodeForConstantValue( 1.0 ), new ExpressionNodeForConstantValue( 3.0 ) ) );
		CellInstance e = new CellWithLazilyParsedExpression( this.row, new ExpressionNodeForOperator( Operator.PLUS,
				new ExpressionNodeForCell( c ), new ExpressionNodeForCell( d ) ) );
		CellInstance f = new CellWithLazilyParsedExpression( this.row,
				new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForOperator( Operator.PLUS,
						new ExpressionNodeForCell( a ), new ExpressionNodeForCell( b ) ), new ExpressionNodeForCell( e ) ) );

		this.formula.setExpression( new ExpressionNodeForOperator( Operator.TIMES, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( f ) ) );

		assertResult( 10.0, null, null );
		assertResult( 22.0, new CellInstance[] { a }, new double[] { 2.0 } );
		assertResult( 2.0, new CellInstance[] { f }, new double[] { 2.0 } );
	}


	public void testEmptyCells() throws ModelError, SecurityException, NoSuchMethodException
	{
		CellInstance a = new CellWithConstant( this.row, 1.0 );
		this.formula.setExpression( new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( new CellIndex( 0, 5, 5 ) ) ) );

		assertResult( 1.0, null, null );
		assertResult( 2.0, new CellInstance[] { a }, new double[] { 2.0 } );
	}


	/**
	 * Construct a sheet with dynamic ranges: {@link WorksheetBuilderWithBands}.
	 * 
	 * Then provide input values for the range A2:B3 (the fixed numbers) which extend it by one row:
	 * 
	 * <pre>
	 *      SUM(C2:C3) 0.5
	 *      4.0 5.0 SUM(A2:B2)*B$1 
	 *      6.0 7.0 SUM(A3:B3)*B$1 
	 *      8.0 9.0 SUM(A4:B4)*B$1
	 * </pre>
	 * 
	 * @throws ModelError
	 */
	// TODO xtest
	public void xtestSections() throws ModelError
	{
		WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );

		Compiler compiler = dyn.newCompiler();
		Engine engine = compiler.compileNewEngine();

		/*
		 * Define a computation where the dynamic input range is populated with more rows than are in
		 * the original spreadsheet definition. SEJ will have to extend the range, copy the row
		 * summing formula, and extend the scope of the totals sum.
		 */
		Inputs inputs = new Inputs();
		inputs.getDetails().add( new Inputs( 4, 5, 0 ) );
		inputs.getDetails().add( new Inputs( 6, 7, 0 ) );
		inputs.getDetails().add( new Inputs( 8, 9, 0 ) );
		Outputs outputs = (Outputs) engine.newComputation( inputs );
		double result = outputs.getResult();

		assertEquals( 19.5, result, 0.001 );
	}


	private void assertResult( double _expected, CellInstance[] _inputs, double[] _values ) throws ModelError,
			SecurityException, NoSuchMethodException
	{
		Compiler compiler = CompilerFactory.newDefaultCompiler( this.workbook, Inputs.class, Outputs.class );
		setupCompiler( compiler.getRoot(), _inputs );
		Engine engine = compiler.compileNewEngine();
		assertEngineResult( _expected, engine, _values );
	}


	private void setupCompiler( Compiler.Section _root, CellInstance[] _inputs ) throws ModelError, SecurityException,
			NoSuchMethodException
	{
		_root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );
		int index = 0;
		if (null != _inputs) {
			for (CellInstance input : _inputs) {
				_root.defineInputCell( input.getCellIndex(),
						getInput( new String[] { "getOne", "getTwo", "getThree" }[ index++ ] ) );
			}
		}
	}


	private void assertEngineResult( double _expected, Engine _engine, double[] _values )
	{
		Outputs outputs = (Outputs) _engine.newComputation( new Inputs( _values ) );
		double result = outputs.getResult();
		assertEquals( outputs.getClass().getName(), _expected, result );
	}

}
