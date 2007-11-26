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

import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.ScaledLongSupport;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

public class UsingScaledLong extends TestCase
{
	private static final String PATH = "src/test/data/org/formulacompiler/tutorials/UsingNumericTypes.xls";


	public void testUsingScaledLong() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler
		builder.setNumericType( /**/SpreadsheetCompiler.getNumericType( Long.TYPE, 3 )/**/ );
		// ---- buildCompiler
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		// ---- checkResult
		Output output = factory.newInstance( new Input( 6 ) );
		assertEquals( /**/1166L/**/, output.getResult() );
		// ---- checkResult

		FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/numeric_type/scaledlong3" );
	}


	public void testUsingLong() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output0.class );
		// ---- buildCompiler0
		builder.setNumericType( /**/SpreadsheetCompiler.LONG/**/ );
		// ---- buildCompiler0
		builder.bindAllByName();
		Engine engine = builder.compile();
		ComputationFactory factory = engine.getComputationFactory();

		// ---- checkResult0
		Output0 output = (Output0) factory.newComputation( new Input0( 6 ) );
		assertEquals( /**/1L/**/, output.getResult() );
		// ---- checkResult0

		FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/numeric_type/long" );
	}
	
	
	public void testWorkingMultiplication() throws Exception
	{
		// ---- workingMultiplication
		long scale = 1000000L;
		long a = (long) (1.2 * scale);
		long b = 100000L * scale;
		long intermediate = a * b;
		long result = intermediate / scale;
		
		assertEquals( 1200000L, a );
		assertEquals( 100000000000L, b );
		assertEquals( 120000000000000000L, intermediate );
		assertEquals( 120000000000L, result );
		// ---- workingMultiplication
	}


	public void testProblemWithMultiplication() throws Exception
	{
		// ---- problemWithMultiplication
		long scale = 1000000L;
		long a = (long) (1.2 * scale);
		long b = 10000000L * scale;
		long intermediate = a * b;
		long result = intermediate / scale;
		
		assertEquals( 1200000L, a );
		assertEquals( 10000000000000L, b );
		assertEquals( -6446744073709551616L, intermediate ); // silent integer overflow!
		assertEquals( -6446744073709L, result );
		// ---- problemWithMultiplication
	}


	// ---- IO
	public static class Input
	{
		public Input(int b)  { this.b = b; }
		public /**/long/**/ getA()  { return 1; } // will be scaled by AFC 
		public /**/@ScaledLong(3) long/**/ getB()  { return ScaledLongSupport.scale( this.b, 3 ); }
		private final int b;
	}

	/**/@ScaledLong(3)/**/
	public static interface Output
	{
		/**/long/**/ getResult();
		/**/long/**/ getNegated();
	}
	// ---- IO

	public static interface Factory
	{
		Output newInstance( Input _input );
	}
	
	
	public static class Input0 extends Input
	{
		public Input0(int _b)
		{
			super( _b );
		}
	}

	public static interface Output0
	{
		/**/long/**/ getResult();
		/**/long/**/ getNegated();
	}

}
