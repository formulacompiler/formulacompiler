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
package sej.tutorials;

import java.lang.reflect.Method;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.SEJ;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;
import sej.runtime.ComputationFactory;
import junit.framework.TestCase;


public class BindingCells extends TestCase
{

	public void testBindingCells() throws Exception
	{
		final String path = "src/test-system/testdata/sej/tutorials/BindingCells.xls";

		// ---- setupBuilder
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		/* -in- */builder.setInputClass( Input.class );/* -in- */
		/* -out- */builder.setOutputClass( Output.class );/* -out- */
		// ---- setupBuilder
		builder.createCellNamesFromRowTitles();
		// ---- getBinder
		Spreadsheet spreadsheet = builder./**/getSpreadsheet()/**/;
		SpreadsheetBinder.Section binder = builder./**/getRootBinder/**/();
		// ---- getBinder

		Method method, chainedMethod;
		Spreadsheet.Cell cell;

		// ---- bindPlainInputs
		cell = spreadsheet./**/getCell/**/( "SOME_VALUE" );
		method = Input.class./**/getMethod/**/( "getSomeValue" );
		binder./**/defineInputCell/**/( cell, new CallFrame( method ) );

		cell = spreadsheet.getCell( "OTHER_VALUE" );
		method = Input.class.getMethod( "getAnotherValue" );
		binder.defineInputCell( cell, new CallFrame( method ) );
		// ---- bindPlainInputs

		// ---- bindParamInputs
		cell = spreadsheet.getCell( "YEAR_1994" );
		method = Input.class.getMethod( "getValueForYear", /**/Integer.TYPE/**/ );
		binder.defineInputCell( cell, new CallFrame( method, /**/1994/**/ ) );
		// ---- bindParamInputs

		// ---- bindChainedInputs
		cell = spreadsheet.getCell( "NAME_LENGTH" );
		method = Input.class.getMethod( "getName" );
		chainedMethod = String.class.getMethod( "length" );
		binder.defineInputCell( cell, new CallFrame( method )/**/.chain/**/( chainedMethod ) );
		// ---- bindChainedInputs

		// ---- bindPlainOutputs
		cell = spreadsheet.getCell( "RESULT" );
		method = Output.class.getMethod( "getResult" );
		binder.defineOutputCell( cell, new CallFrame( method ) );

		cell = spreadsheet.getCell( "COEFF" );
		method = Output.class.getMethod( "getCoefficient" );
		binder.defineOutputCell( cell, new CallFrame( method ) );
		// ---- bindPlainOutputs
		
		ComputationFactory factory = builder.compile().getComputationFactory();
		Input input = new InputImpl();
		Output output = (Output) factory.newComputation( input );
		
		assertEquals( input.getValueForYear( 1994 ) + input.getName().length(), output.getResult(), 0.001 );
		assertEquals( input.getSomeValue() + input.getAnotherValue(), output.getCoefficient(), 0.00001 );
	}


	// ---- Input
	public static interface Input
	{
		double getSomeValue();
		double getAnotherValue();
		double getValueForYear( int year );
		String getName();
	}
	// ---- Input


	// ---- Output
	public static interface Output
	{
		double getResult();
		double getCoefficient();
	}
	// ---- Output


	// ---- OutputWithDefaults
	public static abstract class OutputWithDefault /**/implements Output/**/ 
	{
		private final Input input;

		public OutputWithDefault(/**/Input _input/**/)
		{
			super();
			this.input = _input;
		}

		public abstract double getResult();

		public double getCoefficient()
		{
			return /**/this.input.getSomeValue()/**/ * 0.02;
		}
	}
	// ---- OutputWithDefaults

	// ---- Factory
	public static interface Factory
	{
		/**/Output/**/ newInstance( Input _input );
	}
	// ---- Factory
	
	public void testDefaults() throws Exception
	{
		final String path = "src/test-system/testdata/sej/tutorials/BindingCells.xls";

		// ---- setupBuilderWithDefaults
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		/**/builder.setFactoryClass( Factory.class );/**/
		/**/builder.setOutputClass( OutputWithDefault.class );/**/
		// ---- setupBuilderWithDefaults
		builder.createCellNamesFromRowTitles();
		Spreadsheet spreadsheet = builder./**/getSpreadsheet()/**/;
		SpreadsheetBinder.Section binder = builder./**/getRootBinder/**/();

		Method method;
		Spreadsheet.Cell cell;

		cell = spreadsheet.getCell( "RESULT" );
		method = Output.class.getMethod( "getResult" );
		binder.defineOutputCell( cell, new CallFrame( method ) );
		
		Factory factory = (Factory) builder.compile().getComputationFactory();
		Input input = new InputImpl();
		Output output = factory.newInstance( input );
		
		assertEquals( input.getSomeValue() * 0.02, output.getCoefficient(), 0.00001 );
	}

	
	private static class InputImpl implements Input
	{

		public double getAnotherValue()
		{
			return 12.34;
		}

		public String getName()
		{
			return "SEJ";
		}

		public double getSomeValue()
		{
			return 43.21;
		}

		public double getValueForYear( int _year )
		{
			return _year - 1900;
		}
		
	}

}
