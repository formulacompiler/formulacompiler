/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;
import org.formulacompiler.tests.utils.AbstractIOTestCase;


public class StringsTest extends AbstractIOTestCase
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
				rb.defineOutputCell( s.getCell( RESULT_NAME ), FormulaCompiler.newCallFrame( MyOutputs.class
						.getMethod( "result" ) ) );
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
				rb.defineInputCell( s.getCell( RESULT_NAME ), FormulaCompiler.newCallFrame( MyInputs.class
						.getMethod( "value" ) ) );
				rb.defineOutputCell( s.getCell( RESULT_NAME ), FormulaCompiler.newCallFrame( MyOutputs.class
						.getMethod( "result" ) ) );
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
				rb.defineInputCell( s.getCell( VALUE_NAME ), FormulaCompiler.newCallFrame( MyInputs.class
						.getMethod( "value" ) ) );
				rb.defineOutputCell( s.getCell( RESULT_NAME ), FormulaCompiler.newCallFrame( MyOutputs.class
						.getMethod( "result" ) ) );
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

		public ConcatTester( int a, int b, int c )
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
			if (this.bindA > 0)
				rb.defineInputCell( s.getCell( "A" ), FormulaCompiler.newCallFrame( MyInputs.class.getMethod( "a" ) ) );
			if (this.bindB > 0)
				rb.defineInputCell( s.getCell( "B" ), FormulaCompiler.newCallFrame( MyInputs.class.getMethod( "b" ) ) );
			if (this.bindC > 0)
				rb.defineInputCell( s.getCell( "C" ), FormulaCompiler.newCallFrame( MyInputs.class.getMethod( "c" ) ) );
			rb.defineOutputCell( s.getCell( RESULT_NAME ), FormulaCompiler.newCallFrame( MyOutputs.class
					.getMethod( "result" ) ) );
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
