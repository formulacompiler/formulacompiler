/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.tutorials;

import java.lang.reflect.Method;
import java.util.Map;

import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;


@SuppressWarnings( "unchecked" )
public class BindingParams extends TestCase
{


	public void testBindingByName() throws Exception
	{
		final String path = "src/test/data/org/formulacompiler/tutorials/BindingParams.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		// ---- bindNamedInputs
		final Method inputMethod = Input.class./**/getMethod( "getInput", String.class )/**/;
		for (Map.Entry<String, Spreadsheet.Range> def : /**/spreadsheet.getRangeNames().entrySet()/**/) {
			final Spreadsheet.Range range = def.getValue();
			if (range instanceof Spreadsheet.Cell) {
				final String /**/cellName/**/ = def.getKey();
				if ("I_".equals( cellName.substring( 0, 2 ) )) {
					final Spreadsheet.Cell cell = (Spreadsheet.Cell) range;
					final String /**/valueName/**/ = cellName.substring( 2 ).toUpperCase();
					binder.defineInputCell( cell, /**/inputMethod, valueName/**/ );
				}
			}
		}
		// ---- bindNamedInputs

		// ---- bindNamedOutputs
		final Method outputMethod = Output.class./**/getMethod( "getOutput", String.class )/**/;
		for (Map.Entry<String, Spreadsheet.Range> def : /**/spreadsheet.getRangeNames().entrySet()/**/) {
			final Spreadsheet.Range range = def.getValue();
			if (range instanceof Spreadsheet.Cell) {
				final String /**/cellName/**/ = def.getKey();
				if ("O_".equals( cellName.substring( 0, 2 ) )) {
					final Spreadsheet.Cell cell = (Spreadsheet.Cell) range;
					final String /**/valueName/**/ = cellName.substring( 2 ).toUpperCase();
					binder.defineOutputCell( cell, /**/outputMethod, valueName/**/ );
				}
			}
		}
		// ---- bindNamedOutputs

		ComputationFactory factory = builder.compile().getComputationFactory();
		Input input = new InputImpl();
		Output output = (Output) factory.newComputation( input );

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
		double getInput( /**/String _valueName/**/ );
	}

	// ---- Input


	// ---- Output
	public static abstract class Output
	{
		public double getOutput( /**/String _valueName/**/ )
		{
			return -1;
		}
	}

	// ---- Output


	public static final class SimulatedEngine extends Output
	{
		private final Input input;

		public SimulatedEngine( Input _input )
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


	public void testInputVariants() throws Exception
	{
		final String path = "src/test/data/org/formulacompiler/tutorials/BindingParams_InputVariants.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( InputVariants.class );
		builder.setOutputClass( SimpleOutput.class );

		Class ic = InputVariants.class;
		Spreadsheet ss = builder.getSpreadsheet();
		SpreadsheetBinder.Section bnd = builder.getRootBinder();

		// ---- bindInputVariants
		// Native types
		bnd.defineInputCell( ss.getCell( "byte" ), ic.getMethod( "getInput", Byte.TYPE ), (byte) 123 );
		bnd.defineInputCell( ss.getCell( "short" ), ic.getMethod( "getInput", Short.TYPE ), (short) 1234 );
		bnd.defineInputCell( ss.getCell( "int" ), ic.getMethod( "getInput", Integer.TYPE ), 12345 );
		bnd.defineInputCell( ss.getCell( "long" ), ic.getMethod( "getInput", Long.TYPE ), 123456L );
		bnd.defineInputCell( ss.getCell( "double" ), ic.getMethod( "getInput", Double.TYPE ), 123.45 );
		bnd.defineInputCell( ss.getCell( "float" ), ic.getMethod( "getInput", Float.TYPE ), 123.456F );
		bnd.defineInputCell( ss.getCell( "char" ), ic.getMethod( "getInput", Character.TYPE ), 'a' );
		bnd.defineInputCell( ss.getCell( "bool" ), ic.getMethod( "getInput", Boolean.TYPE ), true );

		// Boxed types
		bnd.defineInputCell( ss.getCell( "bbyte" ), ic.getMethod( "getInput", Byte.class ), (byte) 123 );
		bnd.defineInputCell( ss.getCell( "bshort" ), ic.getMethod( "getInput", Short.class ), (short) 1234 );
		bnd.defineInputCell( ss.getCell( "bint" ), ic.getMethod( "getInput", Integer.class ), 12345 );
		bnd.defineInputCell( ss.getCell( "blong" ), ic.getMethod( "getInput", Long.class ), 123456L );
		bnd.defineInputCell( ss.getCell( "bdouble" ), ic.getMethod( "getInput", Double.class ), 123.45 );
		bnd.defineInputCell( ss.getCell( "bfloat" ), ic.getMethod( "getInput", Float.class ), 123.456F );
		bnd.defineInputCell( ss.getCell( "bchar" ), ic.getMethod( "getInput", Character.class ), 'a' );
		bnd.defineInputCell( ss.getCell( "bbool" ), ic.getMethod( "getInput", Boolean.class ), true );

		// Other types
		bnd.defineInputCell( ss.getCell( "string" ), ic.getMethod( "getInput", String.class ), "123.4567" );

		// Application-defined enumerations
		bnd.defineInputCell( ss.getCell( "enum" ), ic.getMethod( "getInput", MyEnum.class ), MyEnum.TWO );
		// ---- bindInputVariants

		// ---- bindInputCombination
		Method mtd = ic.getMethod( "getInput", /**/Integer.TYPE, Boolean.TYPE, String.class/**/ );
		bnd.defineInputCell( ss.getCell( "comb" ), mtd, /**/12, true, "24"/**/ );
		// ---- bindInputCombination

		bnd.defineOutputCell( ss.getCell( "result" ), SimpleOutput.class.getMethod( "getResult" ) );

		Engine engine = builder.compile();
		ComputationFactory factory = engine.getComputationFactory();
		InputVariants input = new InputVariants();
		SimpleOutput output = (SimpleOutput) factory.newComputation( input );

		assertEquals( 275167.2687, output.getResult(), 0.001 );
	}

	public static final class InputVariants
	{

		// ---- InputVariants
		// Native types
		public double getInput( byte _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( short _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( int _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( long _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( double _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( float _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( char _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( boolean _param ) /**/
		{
			return (_param ? 1 : 0);
		}/**/

		// Boxed types
		public double getInput( Byte _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Short _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Integer _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Long _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Double _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Float _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Character _param ) /**/
		{
			return _param;
		}/**/
		public double getInput( Boolean _param ) /**/
		{
			return (_param ? 1 : 0);
		}/**/

		// Other types
		public double getInput( String _param ) /**/
		{
			return Double.valueOf( _param );
		}/**/

		// Application-defined enumerations
		public double getInput( MyEnum _param ) /**/
		{
			return _param.ordinal();
		};/**/
		// ---- InputVariants

		// ---- InputCombination
		public double getInput( int _a, boolean _b, String _c ) /**/
		{
			return _a + Integer.valueOf( _c );
		}/**/
		// ---- InputCombination

	}


	public static interface SimpleOutput
	{
		double getResult();
	}


	// ---- MyEnum
	public static enum MyEnum {
		ZERO, ONE, TWO;
	}
	// ---- MyEnum


	public void testComplexOutputBinding() throws Exception
	{
		final String path = "src/test/data/org/formulacompiler/tutorials/BindingParams.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( ComplexOutput.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		Method inputMethod = Input.class./**/getMethod( "getInput", String.class )/**/;
		binder.defineInputCell( spreadsheet.getCell( "I_One" ), inputMethod, "ONE" );
		binder.defineInputCell( spreadsheet.getCell( "I_Two" ), inputMethod, "TWO" );
		binder.defineInputCell( spreadsheet.getCell( "I_Three" ), inputMethod, "THREE" );

		// ---- bindComplexOutput
		Method outputMethod = ComplexOutput.class./**/getMethod( "getComplex", Integer.TYPE, Long.TYPE, String.class )/**/;
		binder.defineOutputCell( spreadsheet.getCell( "Complex" ), /**/outputMethod, 1, 2, "THREE"/**/ );
		// ---- bindComplexOutput

		ComputationFactory factory = builder.compile().getComputationFactory();
		Input input = new InputImpl();
		ComplexOutput output = (ComplexOutput) factory.newComputation( input );

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

		public SimulatedComplexEngine( Input _input )
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
