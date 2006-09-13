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

import sej.CallFrame;
import sej.CompilerException;
import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet.Cell;
import junit.framework.TestCase;

public class ErrorUnsupportedFunctionVariant extends TestCase
{

	public void testBindBad() throws Exception
	{
		// ---- BindBad
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Bad"/**/);
		/**/bindInputNamed( builder, "Type" );/**/
		try {
			/**/builder.compile();/**/
			fail();
		}
		catch (/**/CompilerException.UnsupportedExpression e/**/) {
			String err = /**/"The last argument to MATCH, the match type, must be constant, but is MyInputs.value()."
					+ "\nIn expression (1.0 + MATCH( B1, C1:E1,  >> F1 <<  )); error location indicated by >>..<<."
					+ "\nCell containing expression is A1." 
					+ "\nReferenced by cell A1."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- BindBad
	}

	public void testBindReferencesBad() throws Exception
	{
		// ---- BindReferencesBad
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"ReferencesBad"/**/);
		bindInputNamed( builder, "Type" );
		try {
			builder.compile();
			fail();
		}
		catch (CompilerException.UnsupportedExpression e) {
			String err = "The last argument to MATCH, the match type, must be constant, but is MyInputs.value()."
					+ "\nIn expression (1.0 + MATCH( B1, C1:E1,  >> F1 <<  )); error location indicated by >>..<<."
					+ "\nCell containing expression is A1." 
					+ /**/"\nReferenced by cell A2."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- BindReferencesBad
	}

	public void testBindOK() throws Exception
	{
		EngineBuilder builder = builderForComputationOfCellNamed( "Bad" );
		SaveableEngine engine = builder.compile();
		MyFactory factory = (MyFactory) engine.getComputationFactory();
		MyComputation computation = factory.newComputation( new MyInputs() );
		assertEquals( 3, computation.result() );
	}

	public void testBindOKWithInput() throws Exception
	{
		EngineBuilder builder = builderForComputationOfCellNamed( "Bad" );
		bindInputNamed( builder, "LookedFor" );
		SaveableEngine engine = builder.compile();
		MyFactory factory = (MyFactory) engine.getComputationFactory();
		MyComputation computation = factory.newComputation( new MyInputs() );
		assertEquals( 4, computation.result() );
	}


	// FIXME Systematic pass through error cases


	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( "src/test-system/testdata/sej/tutorials/ErrorUnsupportedFunctionVariant.xls" );
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

	public static class MyInputs
	{
		public int value()
		{
			return 30;
		}
	}

	public static interface MyComputation
	{
		public int result();
	}

}
