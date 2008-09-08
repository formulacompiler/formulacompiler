/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tutorials;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class ErrorUnsupportedFunctionVariant extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testBindBad() throws Exception
	{
		// ---- BindBad
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"Bad"/**/ );
		/**/bindInputNamed( builder, "Type" );/**/
		try {
			/**/builder.compile();/**/
			fail();
		}
		catch (/**/CompilerException.UnsupportedExpression e/**/) {
			String err = /**/"The last argument to MATCH, the match type, must be constant, but is MyInputs.value()."
					+ "\nIn expression (1.0 + MATCH( Sheet1!B1, Sheet1!C1:E1,  >> Sheet1!F1 <<  )); error location indicated by >>..<<."
					+ "\nCell containing expression is Sheet1!A1." + "\nReferenced by cell Sheet1!A1."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- BindBad
	}

	public void testBindReferencesBad() throws Exception
	{
		// ---- BindReferencesBad
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"ReferencesBad"/**/ );
		bindInputNamed( builder, "Type" );
		try {
			builder.compile();
			fail();
		}
		catch (CompilerException.UnsupportedExpression e) {
			String err = "The last argument to MATCH, the match type, must be constant, but is MyInputs.value()."
					+ "\nIn expression (1.0 + MATCH( Sheet1!B1, Sheet1!C1:E1,  >> Sheet1!F1 <<  )); error location indicated by >>..<<."
					+ "\nCell containing expression is Sheet1!A1." + /**/"\nReferenced by cell Sheet1!A2."/**/;
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


	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		String path = "src/test/data/org/formulacompiler/tutorials/ErrorUnsupportedFunctionVariant" + getSpreadsheetExtension();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( MyFactory.class );
		Cell cell = builder.getSpreadsheet().getCell( _cellName );
		builder.getRootBinder().defineOutputCell( cell, MyComputation.class.getMethod( "result" ) );
		return builder;
	}

	private void bindInputNamed( EngineBuilder _builder, String _cellName ) throws Exception
	{
		_builder.getRootBinder().defineInputCell( _builder.getSpreadsheet().getCell( _cellName ), "value" );
	}

	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( ErrorUnsupportedFunctionVariant.class );
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
