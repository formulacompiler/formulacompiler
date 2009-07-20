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
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class ErrorUnsupportedConversionFromInput extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testStringAsInt() throws Exception
	{
		// ---- StringAsInt
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"numOutput"/**/ );
		bindInputNamed( builder, "numInput" );
		try {
			builder.compile();
			fail();
		}
		catch (/**/CompilerException.UnsupportedDataType e/**/) {
			String err = /**/"Cannot convert from a java.lang.String to a double."
					+ "\nCaused by return type of input 'public java.lang.String org.formulacompiler.tutorials.ErrorUnsupportedConversionFromInput$MyInputs.value()'."
					+ "\nCell containing expression is Sheet1!B2." + "\nReferenced by cell Sheet1!B2."/**/;
			assertEquals( err, e.getMessage() );
		}
		// ---- StringAsInt
	}


	private EngineBuilder builderForComputationOfCellNamed( String _cellName ) throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		String path = "src/test/data/org/formulacompiler/tutorials/ErrorUnsupportedConversion" + getSpreadsheetExtension();
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
		return MultiFormatTestFactory.testSuite( ErrorUnsupportedConversionFromInput.class );
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
