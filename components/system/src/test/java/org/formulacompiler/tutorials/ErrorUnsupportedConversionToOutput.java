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

package org.formulacompiler.tutorials;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormatTestFactory;

import junit.framework.Test;

public class ErrorUnsupportedConversionToOutput extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testStringAsInt() throws Exception
	{
		// ---- StringAsInt
		EngineBuilder builder = builderForComputationOfCellNamed( /**/"stringOutput"/**/ );
		bindInputNamed( builder, "stringInput" );
		try {
			builder.compile();
			fail();
		}
		catch (/**/CompilerException.UnsupportedDataType e/**/) {
			String err = /**/"Cannot convert from a string to a int."
					+ "\nCaused by return type of input 'public abstract int org.formulacompiler.tutorials.ErrorUnsupportedConversionToOutput$MyComputation.result()'."
					+ "\nCell containing expression is Sheet1!A1."
					+ "\nReferenced by cell Sheet1!A1."/**/;
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
		return MultiFormatTestFactory.testSuite( ErrorUnsupportedConversionToOutput.class );
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

	// ---- MyComputation
	public static interface MyComputation
	{
		public int result();
	}
	// ---- MyComputation

}
