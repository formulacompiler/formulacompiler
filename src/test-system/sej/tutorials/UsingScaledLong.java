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
import sej.Compiler;
import sej.CompilerFactory;
import sej.Engine;
import sej.NumericType;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.engine.bytecode.compiler.ByteCodeCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;
import junit.framework.TestCase;

public class UsingScaledLong extends TestCase
{
	private static final String PATH = "src/test-system/testdata/sej/tutorials/UsingNumericTypes.xls";

	static {
		ByteCodeCompiler.registerAsDefault();
		ExcelXLSLoader.register();
	}


	public void testUsingScaledLong() throws Exception
	{
		String path = PATH;

		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		// ---- buildCompiler
		NumericType type = /* hl */NumericType.getInstance( Long.TYPE, 3 )/* hl */;
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
		assertEquals( /* hl */166L/* hl */, o.getResult() );
		// ---- checkResult
	}


	public void testUsingScaledLong4() throws Exception
	{
		String path = PATH;

		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		// ---- buildCompiler4
		NumericType type = /* hl */NumericType.LONG4/* hl */;
		Compiler compiler = CompilerFactory.newDefaultCompiler( sheet, inp, outp, type );
		// ---- buildCompiler4

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

		// ---- checkResult4
		Input i = new Input( 6 );
		Output o = (Output) engine.newComputation( i );
		assertEquals( /* hl */1666L/* hl */, o.getResult() );
		// ---- checkResult4
		
	}


	public void testUsingScaledLong0() throws Exception
	{
		String path = PATH;

		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		// ---- buildCompiler0
		NumericType type = /* hl */NumericType.LONG;/* hl */
		Compiler compiler = CompilerFactory.newDefaultCompiler( sheet, inp, outp, type );
		// ---- buildCompiler0

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

		// ---- checkResult0
		Input i = new Input( 4 );
		Output o = (Output) engine.newComputation( i );
		assertEquals( /* hl */0/* hl */, o.getResult() );
		// ---- checkResult0

	}


	// ---- IO
	public static class Input
	{
		private static final long SCALING_FACTOR = 1000;  // corresponds to scale 3
		public Input(int b)  { this.b = b; }
		public /* hl */long/* hl */ getA()  { return 1 * SCALING_FACTOR; }
		public /* hl */long/* hl */ getB()  { return this.b * SCALING_FACTOR; }
		private final int b;
	}

	public static interface Output
	{
		/* hl */long/* hl */ getResult();
	}
	// ---- IO


}
