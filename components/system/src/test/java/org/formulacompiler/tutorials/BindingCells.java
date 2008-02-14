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

import java.io.File;
import java.lang.reflect.Method;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;


public class BindingCells extends TestCase
{

	public void testBindingCells() throws Exception
	{
		final String path = "src/test/data/org/formulacompiler/tutorials/BindingCells.xls";
		final String pathToTargetFolder = "temp/test/decompiled/binding";

		// ---- setupBuilder
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		/* -in- */builder.setInputClass( Input.class );/* -in- */
		/* -out- */builder.setOutputClass( Output.class );/* -out- */
		// ---- setupBuilder
		builder.createCellNamesFromRowTitles();
		// ---- getBinder
		Spreadsheet spreadsheet = builder./**/getSpreadsheet()/**/;
		SpreadsheetBinder.Section binder = builder./**/getRootBinder/**/();
		// ---- getBinder

		Method method, chainedMethod;
		Spreadsheet.Cell cell;

		// ---- bindPlainInputs1
		cell = spreadsheet./**/getCell/**/( "SOME_VALUE" );
		method = Input.class./**/getMethod/**/( "getSomeValue" );
		binder./**/defineInputCell/**/( cell, method );
		// ---- bindPlainInputs1

		// ---- bindPlainInputs2
		cell = spreadsheet.getCell( "OTHER_VALUE" );
		binder.defineInputCell( cell, "getAnotherValue" );
		// ---- bindPlainInputs2

		cell = spreadsheet.getCell( "DECADE_X" );
		binder.defineInputCell( cell, "getDecade" );

		// ---- bindParamInputs
		cell = spreadsheet.getCell( "YEAR_1994" );
		method = Input.class.getMethod( "getValueForYear", /**/Integer.TYPE/**/ );
		binder.defineInputCell( cell, method, /**/1994/**/ );
		// ---- bindParamInputs

		// ---- bindDynamicParamInputs
		cell = spreadsheet.getCell( "YEAR_x" );
		method = Input.class.getMethod( "getValueForYear", /**/Integer.TYPE/**/ );
		binder.defineInputCell( cell, method, /**/spreadsheet.getCell( "x" )/**/ );
		// ---- bindDynamicParamInputs

		// ---- bindChainedInputs
		cell = spreadsheet.getCell( "NAME_LENGTH" );
		method = Input.class.getMethod( "getName" );
		chainedMethod = String.class.getMethod( "length" );
		binder.defineInputCell( cell, builder.newCallFrame( method )/**/.chain/**/( chainedMethod ) );
		// ---- bindChainedInputs

		// ---- bindPlainOutputs
		cell = spreadsheet.getCell( "RESULT" );
		method = Output.class.getMethod( "getResult" );
		binder.defineOutputCell( cell, method );

		cell = spreadsheet.getCell( "COEFF" );
		binder.defineOutputCell( cell, "getCoefficient" );
		// ---- bindPlainOutputs

		SaveableEngine engine = builder.compile();

		ByteCodeEngineSource source = FormulaDecompiler.decompile( engine );
		source.saveTo( new File( pathToTargetFolder ) );

		ComputationFactory factory = engine.getComputationFactory();
		Input input = new InputImpl();
		Output output = (Output) factory.newComputation( input );

		assertEquals( input.getValueForYear( 1994 )
				+ input.getValueForYear( 1900 + input.getDecade() * 10 ) + input.getName().length(), output.getResult(),
				0.001 );
		assertEquals( input.getSomeValue() + input.getAnotherValue(), output.getCoefficient(), 0.00001 );
	}


	// ---- Input
	public static interface Input
	{
		double getSomeValue();
		double getAnotherValue();
		int getDecade();
		double getValueForYear( int year );
		String getName();
	}

	// ---- Input


	// ---- Output
	public static interface Output
	{
		double getResult();
		double getCoefficient();
	}

	// ---- Output


	// ---- OutputWithDefaults
	public static abstract class OutputWithDefault /**/implements Output/**/
	{
		private final Input input;

		public OutputWithDefault( /**/Input _input/**/ )
		{
			super();
			this.input = _input;
		}

		public abstract double getResult();

		public double getCoefficient()
		{
			return /**/this.input.getSomeValue()/**/ * 0.02;
		}
	}

	// ---- OutputWithDefaults

	// ---- Factory
	public static interface Factory
	{
		/**/Output/**/ newInstance( Input _input );
	}
	// ---- Factory

	public void testDefaults() throws Exception
	{
		final String path = "src/test/data/org/formulacompiler/tutorials/BindingCells.xls";

		// ---- setupBuilderWithDefaults
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		/**/builder.setFactoryClass( Factory.class );/**/
		/**/builder.setOutputClass( OutputWithDefault.class );/**/
		// ---- setupBuilderWithDefaults
		builder.createCellNamesFromRowTitles();
		Spreadsheet spreadsheet = builder./**/getSpreadsheet()/**/;
		SpreadsheetBinder.Section binder = builder./**/getRootBinder/**/();

		Method method;
		Spreadsheet.Cell cell;

		cell = spreadsheet.getCell( "RESULT" );
		method = Output.class.getMethod( "getResult" );
		binder.defineOutputCell( cell, builder.newCallFrame( method ) );

		Factory factory = (Factory) builder.compile().getComputationFactory();
		Input input = new InputImpl();
		Output output = factory.newInstance( input );

		assertEquals( input.getSomeValue() * 0.02, output.getCoefficient(), 0.00001 );
	}


	private static class InputImpl implements Input
	{

		public double getAnotherValue()
		{
			return 12.34;
		}

		public String getName()
		{
			return "AFC";
		}

		public double getSomeValue()
		{
			return 43.21;
		}

		public int getDecade()
		{
			return 4;
		}

		public double getValueForYear( int _year )
		{
			return _year - 1900;
		}

	}

}
