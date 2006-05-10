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

public class UsingDouble extends TestCase
{
	
	static {
		ByteCodeCompiler.registerAsDefault();
		ExcelXLSLoader.register();
	}
	

	public void testUsingDouble() throws Exception
	{
		String path = "src/test-system/testdata/sej/tutorials/UsingNumericTypes.xls";
		
		// ---- buildCompiler
		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = Output.class;
		NumericType type = /* hl */NumericType.DOUBLE/* hl */;
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
		Input i = new Input();
		Output o = (Output) engine.newComputation( i );
		assertEquals( /* hl */"0.16666666666666666"/* hl */, String.valueOf( o.getResult()) );
		// ---- checkResult
	}


	// ---- IO
	public static class Input
	{
		public /* hl */double/* hl */ getA() { return 1.0; }
		public /* hl */double/* hl */ getB() { return 6.0; }
	}

	public static interface Output
	{
		/* hl */double/* hl */ getResult();
	}
	// ---- IO


}
