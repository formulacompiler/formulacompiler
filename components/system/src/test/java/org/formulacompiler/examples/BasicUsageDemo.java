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

package org.formulacompiler.examples;

import java.io.File;

import org.formulacompiler.runtime.Engine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith( MultiFormat.class )
public class BasicUsageDemo
{
	private final String spreadsheetExtension;

	public BasicUsageDemo( final String _spreadsheetExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	private double compute() throws Exception
	{

		// ---- BasicUsage
		// ---- Construction
		// Get an engine builder (represents AFC's simplified API).
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();

		// Load and parse the spreadsheet file into memory.
		builder.loadSpreadsheet( getSpreadsheetFile() );

		// Set the factory interface to implement. This interface defines the method
		// Outputs newInstance( Inputs _inputs ), from which AFC derives the input
		// and output interfaces.
		builder.setFactoryClass( OutputFactory.class );

		// Define which cells will be variable inputs to the engine, and which will be
		// computable outputs, by cell name. All cells whose name correspond to a method
		// on the output interface will be outputs, and similarly for inputs.
		// Inputs are bound to your input methods that will be called to obtain their value.
		// Outputs are bound to your output methods that are implemented by the engine.
		builder.bindAllByName();

		// Build an engine for the given spreadsheet, inputs, and outputs.
		Engine engine = builder.compile();

		// Get the factory instance from the compiled engine.
		OutputFactory factory = (OutputFactory) engine.getComputationFactory();
		// ---- Construction

		// ---- Computation
		// Compute an actual output value for a given set of actual input values.
		// This code is not dependent on AFC. It is a simple instance of the strategy
		// pattern.
		Inputs inputs = new Inputs( 4, 40 );
		Outputs outputs = factory.newInstance( inputs );
		double result = outputs.getResult();
		// ---- Computation

		return result;
		// ---- BasicUsage

	}


	private static final File DATA_PATH = new File( "src/test/data/org/formulacompiler/examples/" );

	private File getSpreadsheetFile()
	{
		return new File( DATA_PATH, "test" + getSpreadsheetExtension() );
	}

	public static void main( String[] args ) throws Exception
	{
		final BasicUsageDemo demo = new BasicUsageDemo( ".ods" );
		System.out.printf( "Result is: %f", demo.compute() );
	}

	@Test
	public void testComputation() throws Exception
	{
		assertEquals( 160.0, compute(), 0.0001 );
	}
}
