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
import java.math.BigDecimal;

import sej.CallFrame;
import sej.Compiler;
import sej.CompilerFactory;
import sej.Engine;
import sej.NumericType;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.engine.bytecode.compiler.ByteCodeCompiler;
import sej.spreadsheet.loader.excel.xls.ExcelXLSLoader;
import junit.framework.TestCase;

public class UsingBigDecimal extends TestCase
{
	private static final String PATH = "src/test-system/testdata/sej/tutorials/UsingNumericTypes.xls";

	static {
		ByteCodeCompiler.registerAsDefault();
		ExcelXLSLoader.register();
	}


	public void testUsingBigDecimal() throws Exception
	{
		String path = PATH;

		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		// ---- buildCompiler
		NumericType type = /**/NumericType.getInstance( BigDecimal.class, 20, BigDecimal.ROUND_UP )/**/;
		Compiler compiler = CompilerFactory.newDefaultCompiler( sheet, inp, outp, type );
		// ---- buildCompiler

		Compiler.Section root = compiler.getRoot();
		Method method;
		Spreadsheet.Cell cell;

		cell = sheet.getCell( "InputA" );
		method = inp.getMethod( "getA" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = sheet.getCell( "InputB" );
		method = inp.getMethod( "getB" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = sheet.getCell( "Result" );
		method = outp.getMethod( "getResult" );
		root.defineOutputCell( cell, new CallFrame( method ) );

		Engine engine = compiler.compileNewEngine();

		// ---- checkResult
		Input i = new Input( 6 );
		Output o = (Output) engine.newComputation( i );
		assertEquals( /**/"0.16666666666666666667"/**/, o.getResult().toPlainString() );
		// ---- checkResult
	}


	public void testUsingBigDecimal8() throws Exception
	{
		String path = PATH;

		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		// ---- buildCompiler8
		NumericType type = /**/NumericType.BIGDECIMAL8/**/;
		Compiler compiler = CompilerFactory.newDefaultCompiler( sheet, inp, outp, type );
		// ---- buildCompiler8

		Compiler.Section root = compiler.getRoot();
		Method method;
		Spreadsheet.Cell cell;

		cell = sheet.getCell( "InputA" );
		method = inp.getMethod( "getA" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = sheet.getCell( "InputB" );
		method = inp.getMethod( "getB" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = sheet.getCell( "Result" );
		method = outp.getMethod( "getResult" );
		root.defineOutputCell( cell, new CallFrame( method ) );

		Engine engine = compiler.compileNewEngine();

		{
			// ---- checkResult8a
			Input i = new Input( 6 );
			Output o = (Output) engine.newComputation( i );
			assertEquals( /**/"0.16666667"/**/, o.getResult().toPlainString() );
			// ---- checkResult8a
		}

		{
			// ---- checkResult8b
			Input i = new Input( /**/3/**/ );
			Output o = (Output) engine.newComputation( i );
			assertEquals( /**/"0.33333333"/**/, o.getResult().toPlainString() );
			// ---- checkResult8b
		}
	}


	public void testUsingBigDecimalN() throws Exception
	{
		String path = PATH;

		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		// ---- buildCompilerN
		NumericType type = /**/NumericType.getInstance( BigDecimal.class );/**/
		Compiler compiler = CompilerFactory.newDefaultCompiler( sheet, inp, outp, type );
		// ---- buildCompilerN

		Compiler.Section root = compiler.getRoot();
		Method method;
		Spreadsheet.Cell cell;

		cell = sheet.getCell( "InputA" );
		method = inp.getMethod( "getA" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = sheet.getCell( "InputB" );
		method = inp.getMethod( "getB" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = sheet.getCell( "Result" );
		method = outp.getMethod( "getResult" );
		root.defineOutputCell( cell, new CallFrame( method ) );

		Engine engine = compiler.compileNewEngine();

		{
			// ---- checkResultNa
			Input i = new Input( 4 );
			Output o = (Output) engine.newComputation( i );
			assertEquals( /**/"0.25"/**/, o.getResult().toPlainString() );
			// ---- checkResultNa
		}

		// ---- checkResultNb
		try {
			Input i = new Input( /**/3/**/ );
			Output o = (Output) engine.newComputation( i );
			o.getResult();
			fail( "ArithmeticException expected" );
		}
		catch (/**/ArithmeticException e/**/) {
			assertEquals( "Non-terminating decimal expansion; no exact representable decimal result.", e.getMessage() );
		}
		// ---- checkResultNb
	}


	// ---- IO
	public static class Input
	{
		public Input(int b)  { this.b = b; }
		public /**/BigDecimal/**/ getA()  { return BigDecimal.valueOf( 1 ); }
		public /**/BigDecimal/**/ getB()  { return BigDecimal.valueOf( this.b ); }
		private final int b;
	}

	public static interface Output
	{
		/**/BigDecimal/**/ getResult();
	}
	// ---- IO


}
