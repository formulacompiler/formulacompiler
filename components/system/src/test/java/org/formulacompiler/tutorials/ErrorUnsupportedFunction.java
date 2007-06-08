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

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;

import junit.framework.TestCase;

public class ErrorUnsupportedFunction extends TestCase
{

	public void testBindInfo() throws Exception
	{
		// ---- BindInfo
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Info"/**/ );
		try {
			/**/builder.compile();/**/
			fail();
		}
		catch (/**/CompilerException.UnsupportedExpression e/**/) {
			String err = /**/"Unsupported function INFO encountered in expression 1.0+INFO( <<? B1); error location indicated by <<?." 
				+ "\nCell containing expression is A1."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- BindInfo
	}

	public void testBindReferencesInfo() throws Exception
	{
		// ---- BindReferencesInfo
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"ReferencesInfo"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (CompilerException.UnsupportedExpression e) {
			String err = "Unsupported function INFO encountered in expression 1.0+INFO( <<? B1); error location indicated by <<?." 
				+ "\nCell containing expression is A1."
				+ /**/"\nReferenced by cell A2."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- BindReferencesInfo
	}

	public void testBindIndependent() throws Exception
	{
		// ---- BindIndependent
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Independent"/**/);
		SaveableEngine engine = builder.compile();
		MyFactory factory = (MyFactory) engine.getComputationFactory();
		MyComputation computation = factory.newComputation( new MyInputs() );
		/**/assertEquals( 3, computation.result() );/**/
		// ---- BindIndependent
	}

	public void testParsedButUnsupportedFunction() throws Exception
	{
		// ---- BindParsedButUnsupported
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Unsupported"/**/);
		try {
			builder.compile();
			fail();
		}
		catch (CompilerException.UnsupportedExpression e) {
			String err = /**/"Function LENB is not supported for double engines."/**/
				+ /**/"\nIn expression LEN(  >> LENB( B4 ) <<  ); error location indicated by >>..<<."/**/
				+ "\nCell containing expression is A4."
				+ "\nReferenced by cell A4.";
			assertEquals( err, e.getMessage() );
		}
		// ---- BindParsedButUnsupported
	}
	
	

	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tutorials/ErrorUnsupportedFunction.xls" );
		builder.setFactoryClass( MyFactory.class );
		Cell cell = builder.getSpreadsheet().getCell( _cellName );
		CallFrame call = new CallFrame( MyComputation.class.getMethod( "result" ) );
		builder.getRootBinder().defineOutputCell( cell, call );
		return builder;
	}

	public static interface MyFactory
	{
		public MyComputation newComputation( MyInputs _inputs );
	}

	public static class MyInputs
	{
		public int value()
		{
			return 1;
		}
	}

	public static interface MyComputation
	{
		public int result();
	}

}
