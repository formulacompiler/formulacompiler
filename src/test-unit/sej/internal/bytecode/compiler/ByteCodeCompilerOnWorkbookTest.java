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
package sej.internal.bytecode.compiler;

import java.math.BigDecimal;

import sej.Aggregator;
import sej.CallFrame;
import sej.CompilerError;
import sej.NumericType;
import sej.Operator;
import sej.SEJ;
import sej.SpreadsheetBinder;
import sej.SpreadsheetCompiler;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellWithConstant;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.ExpressionNodeForCell;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;
import sej.runtime.Engine;
import sej.runtime.EngineError;
import sej.tests.utils.AbstractTestBase;
import sej.tests.utils.InputInterface;
import sej.tests.utils.Inputs;
import sej.tests.utils.OutputInterface;
import sej.tests.utils.Outputs;
import sej.tests.utils.WorksheetBuilderWithBands;


public class ByteCodeCompilerOnWorkbookTest extends AbstractTestBase
{
	protected SpreadsheetImpl workbook;
	protected SheetImpl sheet;
	protected RowImpl row;
	protected CellWithLazilyParsedExpression formula;
	protected SpreadsheetBinder binder;
	protected SpreadsheetBinder.Section root;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		this.workbook = new SpreadsheetImpl();
		this.sheet = new SheetImpl( this.workbook );
		this.row = new RowImpl( this.sheet );
		this.formula = new CellWithLazilyParsedExpression( this.row );
		makeBinderFor( Inputs.class, Outputs.class );
	}


	private void makeBinderFor( Class _inputClass, Class _outputClass )
	{
		this.binder = SEJ.newSpreadsheetBinder( this.workbook, _inputClass, _outputClass );
		this.root = this.binder.getRoot();
	}


	public void testInputIsOutput() throws Exception
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		assertResult( 123.0 );
		assertResult( 456.0, new CellInstance[] { this.formula }, new double[] { 456.0 } );
	}


	public void testParametrizedInput() throws Exception
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getPlusOne",
				Double.TYPE ), 100.0 ) );

		assertResult( 101.0 );
	}


	public void testChainedInput() throws Exception
	{
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getInner",
				Double.TYPE ), 100.0 ).chain( Inputs.Inner.class.getMethod( "getTimesTwo" ) ) );

		assertResult( 200.0 );
	}


	public void testInputInterface() throws Exception
	{
		makeBinderFor( InputInterface.class, OutputsWithDefault.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root
				.defineInputCell( this.formula.getCellIndex(), new CallFrame( InputInterface.class.getMethod( "getOne" ) ) );

		assertResult( 1.0, new Inputs( new double[] { 1.0 } ) );
	}

	public void testOutputInterface() throws Exception
	{
		makeBinderFor( InputInterface.class, OutputInterface.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		final CellIndex res = this.formula.getCellIndex();
		this.root.defineInputCell( res, new CallFrame( InputInterface.class.getMethod( "getOne" ) ) );
		this.root.defineOutputCell( res, new CallFrame( OutputInterface.class.getMethod( "getResult" ) ) );
		assertEngineResult( 1.0, newEngine(), new Inputs( new double[] { 1.0 } ) );
	}

	public void testDefaultOutputReferencingInput() throws Exception
	{
		makeBinderFor( InputInterface.class, OutputsWithDefault.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );

		assertEngineResult( 12346.67, newEngine(), new Inputs( new double[] { 1.0 } ) );
	}


	public static abstract class OutputsWithDefault extends Outputs
	{
		private final InputInterface input;

		public OutputsWithDefault(InputInterface _input)
		{
			super();
			this.input = _input;
		}

		@Override
		public double getResult()
		{
			return this.input.getOne() + 12345.67;
		}

	}


	public void testInputThrowingDeclaredException() throws Exception
	{
		makeBinderFor( ThrowingInput.class, ThrowingOutput.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root.defineInputCell( this.formula.getCellIndex(),
				new CallFrame( ThrowingInput.class.getMethod( "getFails" ) ) );
		this.root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( ThrowingOutput.class
				.getMethod( "getFails" ) ) );

		Engine engine = newEngine();
		ThrowingOutput outputs = (ThrowingOutput) engine.getComputationFactory().newInstance( new ThrowingInput() );

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
		makeBinderFor( ThrowingInput.class, ThrowingOutput.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root.defineInputCell( this.formula.getCellIndex(),
				new CallFrame( ThrowingInput.class.getMethod( "getFails" ) ) );
		this.root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( ThrowingOutput.class
				.getMethod( "getShouldNotFail" ) ) );

		Engine engine = newEngine();
		ThrowingOutput outputs = (ThrowingOutput) engine.getComputationFactory().newInstance( new ThrowingInput() );

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


	public static class ThrowingInput extends Inputs
	{
		public double getFails() throws Failure
		{
			throw new Failure();
		}
	}


	public static class ThrowingOutput extends Outputs
	{
		@SuppressWarnings("unused")
		public double getFails() throws Failure
		{
			throw new AbstractMethodError( "" );
		};

		public double getShouldNotFail()
		{
			throw new AbstractMethodError( "" );
		};
	}


	public static class Failure extends Throwable
	{
		// NOP
	}


	public void testUnsupportedInputType() throws Exception
	{
		makeBinderFor( ThrowingInput.class, ThrowingOutput.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root
				.defineInputCell( this.formula.getCellIndex(), new CallFrame( Inputs.class.getMethod( "getUnsupported" ) ) );
		this.root.defineOutputCell( this.formula.getCellIndex(), new CallFrame( Outputs.class.getMethod( "getResult" ) ) );
		try {
			newEngine();
			fail();
		}
		catch (CompilerError.UnsupportedDataType e) {
			// expected
		}
	}


	public void testUnsupportedOutputType() throws Exception
	{
		makeBinderFor( ThrowingInput.class, ThrowingOutput.class );
		this.formula.setExpression( new ExpressionNodeForConstantValue( 123.0 ) );
		this.root.defineOutputCell( this.formula.getCellIndex(),
				new CallFrame( Outputs.class.getMethod( "getUnsupported" ) ) );
		try {
			newEngine();
			fail();
		}
		catch (CompilerError.UnsupportedDataType e) {
			// expected
		}
	}


	public void testAddingConstants() throws Exception
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertResult( 579.0, null, null );
		assertResult( 345.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertResult( 3.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
	}


	public void testSums() throws Exception
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForAggregator( Aggregator.SUM, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertResult( 579.0, null, null );
		assertResult( 345.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertResult( 3.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
	}


	public void testMins() throws Exception
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


	public void testMaxs() throws Exception
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


	public void testBigDecimal() throws Exception
	{
		CellInstance a = new CellWithConstant( this.row, 123.0 );
		CellInstance b = new CellWithConstant( this.row, 456.0 );
		this.formula.setExpression( new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( b ) ) );

		assertBigResult( 579.0, null, null );
		assertBigResult( 345.0, new CellInstance[] { b }, new double[] { 222.0 } );
		assertBigResult( 3.0, new CellInstance[] { a, b }, new double[] { 1.0, 2.0 } );
	}


	/**
	 * <pre>
	 *             a = 1 
	 *             b = 2 
	 *             c = 3 
	 *             d = 1 + 3 = 4 
	 *             e = c + d = 7 
	 *             f = (a + b) + e = 10 
	 *             r = a * f = 10
	 * </pre>
	 */
	public void testSubExprs() throws Exception
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


	public void testEmptyCells() throws Exception
	{
		CellInstance a = new CellWithConstant( this.row, 1.0 );
		this.formula.setExpression( new ExpressionNodeForOperator( Operator.PLUS, new ExpressionNodeForCell( a ),
				new ExpressionNodeForCell( new CellIndex( this.workbook, 0, 5, 5 ) ) ) );

		assertResult( 1.0, null, null );
		assertResult( 2.0, new CellInstance[] { a }, new double[] { 2.0 } );
	}


	/**
	 * Construct a sheet with dynamic ranges: {@link WorksheetBuilderWithBands}.
	 * 
	 * Then provide input values for the range A2:B3 (the fixed numbers) which extend it by one row:
	 * 
	 * <pre>
	 *             SUM(C2:C3) 0.5
	 *             4.0 5.0 SUM(A2:B2)*B$1 
	 *             6.0 7.0 SUM(A3:B3)*B$1 
	 *             8.0 9.0 SUM(A4:B4)*B$1
	 * </pre>
	 * 
	 * @throws CompilerError
	 */
	// TODO xtest
	public void xtestSections() throws Exception
	{
		/*
		 * 
		 * WorksheetBuilderWithBands dyn = new WorksheetBuilderWithBands( this.sheet );
		 * 
		 * Compiler compiler = dyn.newCompiler(); Engine engine = compiler.compileNewEngine();
		 * 
		 * /-* Define a computation where the dynamic input range is populated with more rows than are
		 * in the original spreadsheet definition. SEJ will have to extend the range, copy the row
		 * summing formula, and extend the scope of the totals sum. -/ Inputs inputs = new Inputs();
		 * inputs.getDetails().add( new Inputs( 4, 5, 0 ) ); inputs.getDetails().add( new Inputs( 6,
		 * 7, 0 ) ); inputs.getDetails().add( new Inputs( 8, 9, 0 ) ); Outputs outputs = (Outputs)
		 * engine.newComputation( inputs ); double result = outputs.getResult();
		 * 
		 * assertEquals( 19.5, result, 0.001 );
		 * 
		 */
	}


	private Engine newEngine() throws CompilerError, EngineError
	{
		return newEngine( SEJ.DEFAULT_NUMERIC_TYPE );
	}

	private Engine newEngine( NumericType _type ) throws CompilerError, EngineError
	{
		SpreadsheetCompiler.Config cfg = new SpreadsheetCompiler.Config();
		cfg.binding = this.binder.getBinding();
		cfg.numericType = _type;
		SpreadsheetCompiler compiler = SEJ.newSpreadsheetCompiler( cfg );
		return compiler.compile();
	}

	private void assertResult( double _expected ) throws Exception
	{
		this.root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );
		assertEngineResult( _expected, newEngine(), (double[]) null );
	}

	private void assertResult( double _expected, InputInterface _inputs ) throws Exception
	{
		this.root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );
		assertEngineResult( _expected, newEngine(), _inputs );
	}

	private void assertResult( double _expected, CellInstance[] _inputs, double[] _values ) throws Exception
	{
		makeBinderFor( Inputs.class, Outputs.class );
		setupBinder( _inputs );
		assertEngineResult( _expected, newEngine(), _values );
	}

	private void setupBinder( CellInstance[] _inputs ) throws Exception
	{
		this.root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getResult" ) );
		int index = 0;
		if (null != _inputs) {
			for (CellInstance input : _inputs) {
				this.root.defineInputCell( input.getCellIndex(),
						getInput( new String[] { "getOne", "getTwo", "getThree" }[ index++ ] ) );
			}
		}
	}

	private void assertEngineResult( double _expected, Engine _engine, double[] _values )
	{
		assertEngineResult( _expected, _engine, new Inputs( _values ) );
	}

	private void assertEngineResult( double _expected, Engine _engine, InputInterface _inputs )
	{
		OutputInterface outputs = (OutputInterface) _engine.getComputationFactory().newInstance( _inputs );
		double result = outputs.getResult();
		assertEquals( outputs.getClass().getName(), _expected, result );
	}


	private void assertBigResult( double _expected, CellInstance[] _inputs, double[] _values ) throws Exception
	{
		makeBinderFor( Inputs.class, Outputs.class );
		setupBigBinder( _inputs );
		assertBigEngineResult( _expected, newEngine( SEJ.BIGDECIMAL8 ), _values );
	}

	private void setupBigBinder( CellInstance[] _inputs ) throws Exception
	{
		this.root.defineOutputCell( this.formula.getCellIndex(), getOutput( "getBigResult" ) );
		int index = 0;
		if (null != _inputs) {
			for (CellInstance input : _inputs) {
				this.root.defineInputCell( input.getCellIndex(), getInput( new String[] { "getBigOne", "getBigTwo",
						"getBigThree" }[ index++ ] ) );
			}
		}
	}

	private void assertBigEngineResult( double _expected, Engine _engine, double[] _values )
	{
		BigDecimal[] values = null;
		if (null != _values) {
			values = new BigDecimal[ _values.length ];
			for (int i = 0; i < _values.length; i++) {
				values[ i ] = BigDecimal.valueOf( _values[ i ] );
			}
		}
		Outputs outputs = (Outputs) _engine.getComputationFactory().newInstance( new Inputs( values ) );
		BigDecimal result = outputs.getBigResult();
		assertTrue( 0 == result.compareTo( BigDecimal.valueOf( _expected ) ) );
	}

}
