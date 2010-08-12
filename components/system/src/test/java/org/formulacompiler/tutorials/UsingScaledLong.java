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

import java.io.File;

import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.ScaledLongSupport;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith( MultiFormat.class )
public class UsingScaledLong
{
	private final String spreadsheetExtension;

	public UsingScaledLong( final String _spreadsheetExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	@Test
	public void testUsingScaledLong() throws Exception
	{
		File file = getFile();

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( file );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler
		builder.setNumericType( /**/SpreadsheetCompiler.getNumericType( Long.TYPE, 3 )/**/ );
		// ---- buildCompiler
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		// ---- checkResult
		Output output = factory.newInstance( new Input( 6 ) );
		assertEquals( /**/1166L/**/, output.getResult() );
		// ---- checkResult

		FormulaDecompiler.decompile( engine ).saveTo( new File( "temp/test/decompiled/numeric_type/scaledlong3" ) );
	}


	@Test
	public void testUsingLong() throws Exception
	{
		File file = getFile();

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( file );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output0.class );
		// ---- buildCompiler0
		builder.setNumericType( /**/SpreadsheetCompiler.LONG/**/ );
		// ---- buildCompiler0
		builder.bindAllByName();
		Engine engine = builder.compile();
		ComputationFactory factory = engine.getComputationFactory();

		// ---- checkResult0
		Output0 output = (Output0) factory.newComputation( new Input0( 6 ) );
		assertEquals( /**/1L/**/, output.getResult() );
		// ---- checkResult0

		FormulaDecompiler.decompile( engine ).saveTo( new File( "temp/test/decompiled/numeric_type/long" ) );
	}

	private File getFile()
	{
		return new File( "src/test/data/org/formulacompiler/tutorials/UsingNumericTypes" + getSpreadsheetExtension() );
	}


	@Test
	public void testWorkingMultiplication() throws Exception
	{
		// ---- workingMultiplication
		long scale = 1000000L;
		long a = (long) (1.2 * scale);
		long b = 100000L * scale;
		long intermediate = a * b;
		long result = intermediate / scale;

		assertEquals( 1200000L, a );
		assertEquals( 100000000000L, b );
		assertEquals( 120000000000000000L, intermediate );
		assertEquals( 120000000000L, result );
		// ---- workingMultiplication
	}


	@Test
	public void testProblemWithMultiplication() throws Exception
	{
		// ---- problemWithMultiplication
		long scale = 1000000L;
		long a = (long) (1.2 * scale);
		long b = 10000000L * scale;
		long intermediate = a * b;
		long result = intermediate / scale;

		assertEquals( 1200000L, a );
		assertEquals( 10000000000000L, b );
		assertEquals( -6446744073709551616L, intermediate ); // silent integer overflow!
		assertEquals( -6446744073709L, result );
		// ---- problemWithMultiplication
	}


	// DO NOT REFORMAT BELOW THIS LINE
	// ---- IO
	public static class Input
	{
		public Input( int b ) { this.b = b; }
		public /**/long/**/ getA() { return 1; } // will be scaled by AFC
		public /**/@ScaledLong( 3 ) long/**/ getB() { return ScaledLongSupport.scale( this.b, 3 ); }
		private final int b;
	}

	/**/@ScaledLong( 3 )/**/
	public static interface Output
	{
		/**/long/**/ getResult();
		/**/long/**/ getNegated();
	}
	// ---- IO
	// DO NOT REFORMAT ABOVE THIS LINE

	public static interface Factory
	{
		Output newInstance( Input _input );
	}


	public static class Input0 extends Input
	{
		public Input0( int _b )
		{
			super( _b );
		}
	}

	public static interface Output0
	{
		/**/long/**/ getResult();
		/**/long/**/ getNegated();
	}

}
