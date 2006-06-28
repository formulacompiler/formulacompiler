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


public class BindingByName extends TestCase
{


	public void testBindingByName() throws Exception
	{
		final String path = "src/test-system/testdata/sej/tutorials/BindingByName.xls";

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		// ---- bindNamedInputs
		final Method inputMethod = Input.class./**/getMethod( "getInput", String.class )/**/;
		for (Spreadsheet.NameDefinition def : /**/spreadsheet.getDefinedNames()/**/) {
			if (def instanceof Spreadsheet.CellNameDefinition) {
				final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) def;
				final String /**/cellName/**/= cellDef.getName();
				if ("I_".equals( cellName.substring( 0, 2 ) )) {
					final Spreadsheet.Cell cell = cellDef.getCell();
					final String /**/valueName/**/= cellName.substring( 2 ).toUpperCase();
					binder.defineInputCell( cell, /**/new CallFrame( inputMethod, valueName )/**/);
				}
			}
		}
		// ---- bindNamedInputs

		// ---- bindNamedOutputs
		final Method outputMethod = Output.class./**/getMethod( "getOutput", String.class )/**/;
		for (Spreadsheet.NameDefinition def : /**/spreadsheet.getDefinedNames()/**/) {
			if (def instanceof Spreadsheet.CellNameDefinition) {
				final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) def;
				final String /**/cellName/**/= cellDef.getName();
				if ("O_".equals( cellName.substring( 0, 2 ) )) {
					final Spreadsheet.Cell cell = cellDef.getCell();
					final String /**/valueName/**/= cellName.substring( 2 ).toUpperCase();
					binder.defineOutputCell( cell, /**/new CallFrame( outputMethod, valueName )/**/);
				}
			}
		}
		// ---- bindNamedOutputs

		ComputationFactory factory = builder.compile().getComputationFactory();
		Input input = new InputImpl();
		Output output = (Output) factory.newInstance( input );

		// ---- checkResults
		assertEquals( 6.0, output.getOutput( "ONETWOTHREE" ), 0.001 );
		assertEquals( 8.0, output.getOutput( "SUMINTER" ), 0.001 );
		assertEquals( -1.0, output.getOutput( "UNDEF" ), 0.001 );
		// ---- checkResults

		SimulatedEngine simulation = new SimulatedEngine( input );
		assertEquals( 6.0, simulation.getOutput( "ONETWOTHREE" ), 0.001 );
		assertEquals( 8.0, simulation.getOutput( "SUMINTER" ), 0.001 );
	}


	// ---- Input
	public static interface Input
	{
		double getInput( /**/String _valueName/**/);
	}

	// ---- Input


	// ---- Output
	public static abstract class Output
	{
		public double getOutput( /**/String _valueName/**/)
		{
			return -1;
		}
	}

	// ---- Output


	public static final class SimulatedEngine extends Output
	{
		private final Input input;

		public SimulatedEngine(Input _input)
		{
			super();
			this.input = _input;
		}

		// ---- GeneratedGetter
		@Override
		public double getOutput( String _valueName )
		{
			if (_valueName.equals( "ONETWOTHREE" )) return getOutput__1();
			if (_valueName.equals( "SUMINTER" )) return getOutput__2();
			// ... other bound outputs
			return super.getOutput( _valueName );
		}

		private double getOutput__1()
		{
			// Generated computation for cell ONETWOTHREE (corresponds to test spreadsheet):
			return getInput__1() + getInput__2() + getInput__3();
		}

		private double getOutput__2()
		{
			// Generated computation for cell SUMINTER (corresponds to test spreadsheet):
			return getInter__1() + getInter__2();
		}
		// ---- GeneratedGetter

		private double getInput__1()
		{
			return this.input.getInput( "ONE" );
		}

		private double getInput__2()
		{
			return this.input.getInput( "TWO" );
		}

		private double getInput__3()
		{
			return this.input.getInput( "THREE" );
		}

		private double getInter__1()
		{
			return getInput__1() + getInput__2();
		}

		private double getInter__2()
		{
			return getInput__2() + getInput__3();
		}

	}


	public static class InputImpl implements Input
	{

		// ---- InputSample
		public double getInput( String _valueName )
		{
			if (_valueName.equals( "ONE" )) return 1.0;
			if (_valueName.equals( "TWO" )) return 2.0;
			if (_valueName.equals( "THREE" )) return 3.0;
			return 0.0;
		}
		// ---- InputSample

	}


	public void testComplexOutputBinding() throws Exception
	{
		final String path = "src/test-system/testdata/sej/tutorials/BindingByName.xls";

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( ComplexOutput.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		Method inputMethod = Input.class./**/getMethod( "getInput", String.class )/**/;
		binder.defineInputCell( spreadsheet.getCell( "I_One" ), new CallFrame( inputMethod, "ONE" ) );
		binder.defineInputCell( spreadsheet.getCell( "I_Two" ), new CallFrame( inputMethod, "TWO" ) );
		binder.defineInputCell( spreadsheet.getCell( "I_Three" ), new CallFrame( inputMethod, "THREE" ) );

		// ---- bindComplexOutput
		Method outputMethod = ComplexOutput.class./**/getMethod( "getComplex", Integer.TYPE, Long.TYPE, String.class )/**/;
		binder
				.defineOutputCell( spreadsheet.getCell( "Complex" ), /**/new CallFrame( outputMethod, 1, 2, "THREE" )/**/);
		// ---- bindComplexOutput

		ComputationFactory factory = builder.compile().getComputationFactory();
		Input input = new InputImpl();
		ComplexOutput output = (ComplexOutput) factory.newInstance( input );

		// ---- checkComplexResults
		assertEquals( 5.0, output.getComplex( 1, 2, "THREE" ), 0.001 );

		// Check undefined results by incrementing each argument in turn:
		assertEquals( -1.0, output.getComplex( 2, 2, "THREE" ), 0.001 );
		assertEquals( -1.0, output.getComplex( 1, 3, "THREE" ), 0.001 );
		assertEquals( -1.0, output.getComplex( 1, 2, "FOUR" ), 0.001 );
		// ---- checkComplexResults

		SimulatedComplexEngine simulation = new SimulatedComplexEngine( input );
		assertEquals( 5.0, simulation.getComplex( 1, 2, "THREE" ), 0.001 );
	}


	// ---- ComplexOutput
	public static abstract class ComplexOutput
	{
		public double getComplex( int _int, long _long, String _string )
		{
			return -1;
		}
	}

	// ---- ComplexOutput


	public static final class SimulatedComplexEngine extends ComplexOutput
	{
		private final Input input;

		public SimulatedComplexEngine(Input _input)
		{
			super();
			this.input = _input;
		}

		// ---- GeneratedComplexGetter
		@Override
		public double getComplex( int _int, long _long, String _string )
		{
			if (_int == 1 && _long == 2 && _string.equals( "THREE" )) return getComplex__1();
			// ... other bound outputs
			return super.getComplex( _int, _long, _string );
		}
		// ---- GeneratedComplexGetter

		private double getComplex__1()
		{
			// Generated computation for cell COMPLEX (corresponds to test spreadsheet):
			return getInput__1() + getInput__2() + getInput__3() - 1;
		}

		private double getInput__1()
		{
			return this.input.getInput( "ONE" );
		}

		private double getInput__2()
		{
			return this.input.getInput( "TWO" );
		}

		private double getInput__3()
		{
			return this.input.getInput( "THREE" );
		}
	}


}
