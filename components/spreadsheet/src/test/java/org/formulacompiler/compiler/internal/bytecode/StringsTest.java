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
package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;
import org.formulacompiler.tests.utils.AbstractIOTestBase;


public class StringsTest extends AbstractIOTestBase
{
	private static final String DYNAMIC_STRING = "This is a dynamic string.";
	private static final String STATIC_STRING = "This is a string.";
	private static final String VALUE_NAME = "Value";
	private static final String RESULT_NAME = "Result";


	private abstract class StringTester
	{

		public void run() throws Exception
		{
			runWithOutput( MyOutputs.class );
			runWithOutput( MyCachingOutputs.class );
		}

		private void runWithOutput( Class _outputClass ) throws Exception
		{
			SpreadsheetBuilder sb = SpreadsheetCompiler.newSpreadsheetBuilder();
			buildSheet( sb );
			Spreadsheet s = sb.getSpreadsheet();

			EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
			eb.setSpreadsheet( s );
			eb.setFactoryClass( MyFactory.class );
			eb.setOutputClass( _outputClass );
			Section rb = eb.getRootBinder();
			bindSheet( s, rb );
			SaveableEngine e = eb.compile();
			checkEngine( e );
			
			MyFactory f = (MyFactory) e.getComputationFactory();

			MyInputs i = new MyInputs();
			MyOutputs o = f.newComputation( i );
			test( o );
		}

		protected abstract void buildSheet( SpreadsheetBuilder sb ) throws Exception;
		protected abstract void bindSheet( Spreadsheet s, Section rb ) throws Exception;
		protected abstract void test( MyOutputs o ) throws Exception;

	}


	public void testStringOutput() throws Exception
	{
		new StringTester()
		{

			@Override
			protected void buildSheet( SpreadsheetBuilder sb ) throws Exception
			{
				sb.newCell( sb.cst( STATIC_STRING ) );
				sb.nameCell( RESULT_NAME );
			}

			@Override
			protected void bindSheet( Spreadsheet s, Section rb ) throws Exception
			{
				rb.defineOutputCell( s.getCell( RESULT_NAME ), new CallFrame( MyOutputs.class.getMethod( "result" ) ) );
			}

			@Override
			protected void test( MyOutputs o ) throws Exception
			{
				String r = o.result();
				assertEquals( STATIC_STRING, r );
			}

		}.run();
	}


	public void testStringInputIsOutput() throws Exception
	{
		new StringTester()
		{

			@Override
			protected void buildSheet( SpreadsheetBuilder sb ) throws Exception
			{
				sb.newCell( sb.cst( STATIC_STRING ) );
				sb.nameCell( RESULT_NAME );
			}

			@Override
			protected void bindSheet( Spreadsheet s, Section rb ) throws Exception
			{
				rb.defineInputCell( s.getCell( RESULT_NAME ), new CallFrame( MyInputs.class.getMethod( "value" ) ) );
				rb.defineOutputCell( s.getCell( RESULT_NAME ), new CallFrame( MyOutputs.class.getMethod( "result" ) ) );
			}

			@Override
			protected void test( MyOutputs o ) throws Exception
			{
				String r = o.result();
				assertEquals( DYNAMIC_STRING, r );
			}

		}.run();
	}


	public void testStringInputRefdByOutput() throws Exception
	{
		new StringTester()
		{

			@Override
			protected void buildSheet( SpreadsheetBuilder sb ) throws Exception
			{
				sb.newCell( sb.cst( STATIC_STRING ) );
				sb.nameCell( VALUE_NAME );
				CellRef val = sb.currentCell();
				sb.newCell( sb.ref( val ) );
				sb.nameCell( RESULT_NAME );
			}

			@Override
			protected void bindSheet( Spreadsheet s, Section rb ) throws Exception
			{
				rb.defineInputCell( s.getCell( VALUE_NAME ), new CallFrame( MyInputs.class.getMethod( "value" ) ) );
				rb.defineOutputCell( s.getCell( RESULT_NAME ), new CallFrame( MyOutputs.class.getMethod( "result" ) ) );
			}

			@Override
			protected void test( MyOutputs o ) throws Exception
			{
				String r = o.result();
				assertEquals( DYNAMIC_STRING, r );
			}

		}.run();
	}


	private class ConcatTester extends StringTester
	{
		private final String expected;
		private final int bindA;
		private final int bindB;
		private final int bindC;

		public ConcatTester(int a, int b, int c)
		{
			super();
			this.bindA = a;
			this.bindB = b;
			this.bindC = c;
			this.expected = ((a == 0) ? "a" : "A") + ((b == 0) ? "b" : "B") + ((c == 0) ? "c" : "C");
		}

		@Override
		protected void buildSheet( SpreadsheetBuilder sb ) throws Exception
		{
			sb.newCell( sb.cst( "a" ) );
			sb.nameCell( "A" );
			CellRef a = sb.currentCell();
			sb.newCell( sb.cst( "b" ) );
			sb.nameCell( "B" );
			CellRef b = sb.currentCell();
			sb.newCell( sb.cst( "c" ) );
			sb.nameCell( "C" );
			CellRef c = sb.currentCell();
			sb.newCell( sb.op( Operator.CONCAT, sb.ref( a ), sb.ref( b ), sb.ref( c ) ) );
			sb.nameCell( RESULT_NAME );
		}

		@Override
		protected void bindSheet( Spreadsheet s, Section rb ) throws Exception
		{
			if (this.bindA > 0) rb.defineInputCell( s.getCell( "A" ), new CallFrame( MyInputs.class.getMethod( "a" ) ) );
			if (this.bindB > 0) rb.defineInputCell( s.getCell( "B" ), new CallFrame( MyInputs.class.getMethod( "b" ) ) );
			if (this.bindC > 0) rb.defineInputCell( s.getCell( "C" ), new CallFrame( MyInputs.class.getMethod( "c" ) ) );
			rb.defineOutputCell( s.getCell( RESULT_NAME ), new CallFrame( MyOutputs.class.getMethod( "result" ) ) );
		}

		@Override
		protected void test( MyOutputs o ) throws Exception
		{
			String r = o.result();
			assertEquals( this.expected, r );
		}

	}


	public void testStringConcat() throws Exception
	{
		new ConcatTester( 0, 0, 0 ).run();
		new ConcatTester( 0, 0, 1 ).run();
		new ConcatTester( 0, 1, 0 ).run();
		new ConcatTester( 0, 1, 1 ).run();
		new ConcatTester( 1, 0, 0 ).run();
		new ConcatTester( 1, 0, 1 ).run();
		new ConcatTester( 1, 1, 0 ).run();
		new ConcatTester( 1, 1, 1 ).run();
	}


	public static class MyInputs
	{
		public String value()
		{
			return DYNAMIC_STRING;
		}

		public String a()
		{
			return "A";
		}

		public String b()
		{
			return "B";
		}

		public String c()
		{
			return "C";
		}
	}

	public static interface MyOutputs
	{
		String result();
	}

	public static interface MyCachingOutputs extends MyOutputs, Resettable
	{
		// Nothing to see here.
	}

	public static interface MyFactory
	{
		MyOutputs newComputation( MyInputs _inputs );
	}


}
