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

import sej.compiler.CallFrame;
import sej.compiler.CompilerException;
import sej.spreadsheet.EngineBuilder;
import sej.spreadsheet.SEJ;
import sej.spreadsheet.Spreadsheet.Cell;
import junit.framework.TestCase;

public class ErrorUnsupportedConversionFromInput extends TestCase
{

	public void testStringAsInt() throws Exception
	{
		// ---- StringAsInt
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"numOutput"/**/);
		bindInputNamed( builder, "numInput" );
		try {
			builder.compile();
			fail();
		}
		catch (/**/CompilerException.UnsupportedDataType e/**/) {
			String err = /**/"Cannot convert from a java.lang.String to a double."
					+ "\nCaused by return type of input 'public java.lang.String sej.tutorials.ErrorUnsupportedConversionFromInput$MyInputs.value()'."
					+ "\nCell containing expression is B2."
					+ "\nReferenced by cell B2."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- StringAsInt
	}

	
	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( "src/test-system/testdata/sej/tutorials/ErrorUnsupportedConversion.xls" );
		builder.setFactoryClass( MyFactory.class );
		Cell cell = builder.getSpreadsheet().getCell( _cellName );
		CallFrame call = new CallFrame( MyComputation.class.getMethod( "result" ) );
		builder.getRootBinder().defineOutputCell( cell, call );
		return builder;
	}

	private void bindInputNamed( EngineBuilder _builder, String _cellName ) throws Exception
	{
		Cell cell = _builder.getSpreadsheet().getCell( _cellName );
		CallFrame call = new CallFrame( MyInputs.class.getMethod( "value" ) );
		_builder.getRootBinder().defineInputCell( cell, call );
	}

	public static interface MyFactory
	{
		public MyComputation newComputation( MyInputs _inputs );
	}

	// ---- MyInputs
	public static class MyInputs
	{
		public String value()
		{
			return "Hello, world!";
		}
	}
	// ---- MyInputs

	public static interface MyComputation
	{
		public String result();
	}

}
