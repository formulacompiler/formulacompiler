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

package org.formulacompiler.tutorials;

import java.io.File;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.NotAvailableException;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@SuppressWarnings( "unqualified-field-access" )
@RunWith( MultiFormat.class )
public final class Exceptions
{
	private final String spreadsheetExtension;

	public Exceptions( final String _spreadsheetExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}



	// ------------------------------------------------ Errors


	@Test
	public void testNUM() throws Throwable
	{
		// ---- NUM
		setupOutputCell( "B3" );
		setupInputCell( "C3" );
		// This works:
		assertRunsWithInputReturning( 1.0, Math.asin( 1.0 ) );
		// Invalid argument to ASIN() fails with a special return value for doubles:
		assertRunsWithInputReturning( 2.0, /**/Double.NaN/**/ );
		// But with an exception for BigDecimals:
		useNumericType( FormulaCompiler./**/BIGDECIMAL128/**/ );
		assertFailsWhenRunWithInput( 2.0, /**/FormulaException.class/**/, "#NUM! (value is NaN)" );
		// ---- NUM
	}

	@Test
	public void testNUMFolding() throws Throwable
	{
		// ---- NUM_fold
		setupOutputCell( "B3" );
		assertRunsWithInputReturning( 2.0, /**/Double.NaN/**/ );
		// The exception for BigDecimals is raised at *runtime*:
		useNumericType( FormulaCompiler.BIGDECIMAL128 );
		Outputs computation = newOutputs( null ); // Compilation succeeds.
		try {
			double have = computation.result(); // Execution fails.
			fail( "Exception expected, but got " + have );
		}
		catch (FormulaException err) {
			assertEquals( "#NUM! (value is NaN)", err.getMessage() );
		}
		// ---- NUM_fold
	}

	@Test
	public void testNUMCaching() throws Throwable
	{
		// ---- NUM_cached
		setupOutputCell( "B3" );
		setupInputCell( "C3" );
		useNumericType( FormulaCompiler.BIGDECIMAL128 );
		b./**/setFullCaching( true )/**/;
		Outputs computation = newOutputs( new ValueInput( 2.0 ) );
		for (int i = 0; i < 2; i++) {
			try {
				double have = computation.result(); // Execution fails repeatedly.
				fail( "Exception expected, but got " + have );
			}
			catch (FormulaException err) {
				assertEquals( "#NUM! (value is NaN)", err.getMessage() );
			}
		}
		// ---- NUM_cached
	}

	@Test
	public void testVALUE() throws Throwable
	{
		// ---- VAL
		setupOutputCell( "B4" );
		setupInputCell( "C4" );
		assertRunsWithInputReturning( 1.0, 12.0 );
		assertFailsWhenRunWithInput( 0.0, FormulaException.class, "#VALUE! because index to CHOOSE is out of range" );
		// ---- VAL
	}

	@Test
	public void testDIV0() throws Throwable
	{
		// ---- DIV
		setupOutputCell( "B5" );
		setupInputCell( "C5" );
		assertRunsWithInputReturning( 1.0, 1.0 );
		// Division by zero fails with a special return value for doubles:
		assertRunsWithInputReturning( 0.0, /**/Double.POSITIVE_INFINITY/**/ );
		// But with an exception for BigDecimals:
		useNumericType( FormulaCompiler./**/BIGDECIMAL128/**/ );
		assertFailsWhenRunWithInput( 0.0, ArithmeticException.class, null );
		// ---- DIV
	}

	@Test
	public void testErrorPropagation() throws Throwable
	{
		// ---- errProp
		setupOutputCell( /**/"B6"/**/ );
		setupInputCell( "C3" );
		assertRunsWithInputReturning( 1.0, /**/1.0 + /**/Math.asin( 1.0 ) );
		assertRunsWithInputReturning( 2.0, /**/Double.NaN/**/ );
		useNumericType( FormulaCompiler./**/BIGDECIMAL128/**/ );
		assertFailsWhenRunWithInput( 2.0, /**/FormulaException.class/**/, "#NUM! (value is NaN)" );
		// ---- errProp
	}

	@Test
	public void testIsErr() throws Throwable
	{
		// ---- isErr
		setupOutputCell( "B7" );
		setupInputCell( "C3" );
		assertRunsWithInputReturning( 1.0, 1.0 + Math.asin( 1.0 ) );
		assertRunsWithInputReturning( /**/2.0/**/, /**/4712/**/ );

		// ---- isErr
		FormulaDecompiler.decompile( e ).saveTo( new File( "temp/test/decompiled/exceptions/iserr" ) );
		// ---- isErr
		// The above failed when accessing B3 directly.
		useNumericType( FormulaCompiler./**/BIGDECIMAL128/**/ );
		assertRunsWithInputReturning( /**/2.0/**/, /**/4712/**/ );
		// ---- isErr

		setupOutputCell( "B7" );
		setupInputCell( "C3" );
		b.setFullCaching( true );
		assertRunsWithInputReturning( 1.0, 1.0 + Math.asin( 1.0 ) );
		assertRunsWithInputReturning( /**/2.0/**/, /**/4712/**/ );
	}

	@Test
	public void testThrowError() throws Throwable
	{
		// ---- throwErr
		setupOutputCell( "B3" );
		setupInputCell( "C3" );
		assertFailsWhenRunWithInput( /**/new ThrowingInput()/**/, FormulaException.class, /**/"My error"/**/ );
		// ---- throwErr
	}

	// DO NOT REFORMAT BELOW THIS LINE
	// ---- throwingInput
	public static final class /**/ThrowingInput/**/ implements Inputs {
		public double value() {
			throw new /**/FormulaException/**/( /**/"My error"/**/ );
		}
	}
	// ---- throwingInput
	// DO NOT REFORMAT ABOVE THIS LINE


	// ------------------------------------------------ N/A


	@Test
	public void testNA() throws Throwable
	{
		// ---- NA
		for (int row = 9; row <= 10; row++) {
			setupOutputCell( "B" + row ); // B9, B10
			Outputs computation = newOutputs( null ); // Compilation succeeds.
			try {
				double have = computation.result(); // Execution fails.
				fail( "Exception expected, but got " + have );
			}
			catch (NotAvailableException err) {
				assertEquals( "#N/A", err.getMessage() );
			}
		}
		// ---- NA
	}

	@Test
	public void testThrowNA() throws Throwable
	{
		// ---- throwNA
		setupOutputCell( "B11" );
		setupInputCell( "C11" );
		assertFailsWhenRunWithInput( /**/new InputNotAvailable()/**/, NotAvailableException.class, /**/"My message"/**/ );
		// ---- throwNA
	}

	// DO NOT REFORMAT BELOW THIS LINE
	// ---- inputNA
	public static final class /**/InputNotAvailable/**/ implements Inputs {
		public double value() {
			throw new /**/NotAvailableException/**/( /**/"My message"/**/ );
		}
	}
	// ---- inputNA
	// DO NOT REFORMAT ABOVE THIS LINE

	@Test
	public void testIsNA() throws Throwable
	{
		// ---- isNA
		setupOutputCell( "B13" );
		setupInputCell( "C12" );
		assertRunsWithInputReturning( 2.0, 3.0 );
		assertRunsWithInputReturning( 1.0, 4712 );
		// The above failed when accessing B11 directly.
		// ---- isNA
		FormulaDecompiler.decompile( e ).saveTo( new File( "temp/test/decompiled/exceptions/isna" ) );
	}


	// ------------------------------------------------ Interplay


	@Test
	public void testIsError() throws Throwable
	{
		// ---- isError
		for (int row = 18; row <= 19; row++) {
			setupOutputCell( "B" + row ); // B18, B19
			assertRunsWithInputReturning( null, 1.0 ); // 1.0 means true
			useNumericType( FormulaCompiler.BIGDECIMAL128 );
			assertRunsWithInputReturning( null, 1.0 ); // 1.0 means true
		}
		// ---- isError
	}

	@Test
	public void testIsXonY() throws Throwable
	{
		// ---- isXonY
		setupOutputCell( "B20" );
		setupInputCell( "C12" );
		assertRunsWithInputReturning( 1.0, 0.0 ); // 0.0 means false

		setupOutputCell( "B21" );
		setupInputCell( "C3" );
		assertRunsWithInputReturning( 2.0, 0.0 ); // 0.0 means false
		// ---- isXonY
	}

	@Test
	public void testCount() throws Throwable
	{
		// ---- count
		for (int row = 23; row <= 24; row++) {
			setupOutputCell( "B" + row ); // B23, B24
			assertRunsWithInputReturning( null, 3.0 );
		}
		// ---- count
	}


	// ------------------------------------------------ Support methods


	private EngineBuilder b;
	private SaveableEngine e;
	private Factory f;

	private void init() throws Throwable
	{
		f = null;
		e = null;
		b = SpreadsheetCompiler.newEngineBuilder();
		b.loadSpreadsheet( new File( "src/test/data/org/formulacompiler/tutorials/Exceptions" + getSpreadsheetExtension() ) );
		b.setFactoryClass( Factory.class );
	}

	private void useNumericType( NumericType _type ) throws Throwable
	{
		f = null;
		e = null;
		b.setNumericType( _type );
	}

	private void setupOutputCell( String _cellName ) throws Throwable
	{
		init();
		Spreadsheet s = b.getSpreadsheet();
		Section r = b.getRootBinder();
		r.defineOutputCell( s.getCellA1( _cellName ), Outputs.class.getMethod( "result" ) );
	}

	private void setupInputCell( String _cellName ) throws Throwable
	{
		Spreadsheet s = b.getSpreadsheet();
		Section r = b.getRootBinder();
		r.defineInputCell( s.getCellA1( _cellName ), "value" );
	}

	private void assertRunsWithInputReturning( double _inputValue, double _expectedResult ) throws Throwable
	{
		assertRunsWithInputReturning( new ValueInput( _inputValue ), _expectedResult );
	}

	private void assertRunsWithInputReturning( Inputs _input, double _expectedResult ) throws Throwable
	{
		double have = runWith( _input );
		if (!Double.isNaN( _expectedResult ) || !Double.isNaN( have )) {
			assertEquals( _expectedResult, have, 0.001 );
		}
	}

	private void assertFailsWhenRunWithInput( Inputs _input ) throws Throwable
	{
		double have = runWith( _input );
		fail( "Exception expected, but got " + have );
	}

	private void assertFailsWhenRunWithInput( double _inputValue, Class _errorClass, String _errorMessage )
			throws Throwable
	{
		assertFailsWhenRunWithInput( new ValueInput( _inputValue ), _errorClass, _errorMessage );
	}

	private void assertFailsWhenRunWithInput( Inputs _input, Class _errorClass, String _errorMessage ) throws Throwable
	{
		try {
			assertFailsWhenRunWithInput( _input );
		}
		catch (Throwable err) {
			if (err.getClass() == _errorClass) {
				if (null != _errorMessage) {
					assertEquals( _errorMessage, err.getMessage() );
				}
			}
			else throw err;
		}
	}

	private Outputs newOutputs( Inputs _inputs ) throws CompilerException, EngineException
	{
		if (null == e) e = b.compile();
		if (null == f) f = (Factory) e.getComputationFactory();
		return f.newInstance( _inputs );
	}

	private double runWith( Inputs _input ) throws Throwable
	{
		return newOutputs( _input ).result();
	}


	public static interface Inputs
	{
		double value();
	}

	public static final class ValueInput implements Inputs
	{
		private final double value;

		public ValueInput( double _value )
		{
			this.value = _value;
		}

		public double value()
		{
			return this.value;
		}
	}

	public static interface Outputs
	{
		double result();
	}

	public static interface Factory
	{
		Outputs newInstance( Inputs _in );
	}

}
