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

import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class UsingDouble extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testUsingDouble() throws Exception
	{
		String path = "src/test/data/org/formulacompiler/tutorials/UsingNumericTypes" + getSpreadsheetExtension();

		// ---- buildCompiler
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		/**/builder.setNumericType( SpreadsheetCompiler.DOUBLE );/**/
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();
		// ---- buildCompiler

		// ---- checkResult
		Output output = factory.newInstance( new Input() );
		assertEquals( /**/"1.1666666666666667"/**/, String.valueOf( output.getResult() ) );
		// ---- checkResult

		FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/numeric_type/double" );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( UsingDouble.class );
	}


	// DO NOT REFORMAT BELOW THIS LINE
	// ---- IO
	public static class Input
	{
		public /**/double/**/ getA() { return 1.0; }
		public /**/double/**/ getB() { return 6.0; }
	}

	public static interface Output
	{
		/**/double/**/ getResult();
		/**/double/**/ getNegated();
	}

	public static interface Factory
	{
		Output newInstance( Input _input );
	}
	// ---- IO
	// DO NOT REFORMAT ABOVE THIS LINE

}
