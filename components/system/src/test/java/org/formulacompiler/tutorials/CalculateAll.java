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
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class CalculateAll
{
	private final boolean isCached;

	@Rule
	public final TestName testName = new TestName();

	public CalculateAll( final boolean _cached )
	{
		this.isCached = _cached;
	}

	@Parameterized.Parameters
	public static List<Object[]> params()
	{
		return Arrays.asList( new Object[]{ false }, new Object[]{ true } );
	}

	@Test
	public void testOutputsByCellName() throws Exception
	{
		final Engine engine = compileEngine( this.isCached, OutputsByCellName.class, OutputsByCellName.class.getMethod( "getOutput", String.class ) );

		// ---- using
		final ComputationFactory computationFactory = engine.getComputationFactory();
		final OutputsByCellName outputs = (OutputsByCellName) computationFactory.newComputation( new Inputs( 5, 6, 7 ) );

		assertEquals( 5.0, outputs.getOutput( "Sheet1!B1" ), 1e-8 );
		assertEquals( 6.0, outputs.getOutput( "Sheet1!B2" ), 1e-8 );
		assertEquals( 7.0, outputs.getOutput( "Sheet1!B3" ), 1e-8 );
		assertEquals( 30.0, outputs.getOutput( "Sheet1!B4" ), 1e-8 );
		assertEquals( 210.0, outputs.getOutput( "Sheet1!B5" ), 1e-8 );
		// ---- using
	}

	@Test
	public void testOutputsBySheetNameCellIndex() throws Exception
	{
		final Engine engine = compileEngine( this.isCached, OutputsBySheetNameCellIndex.class, OutputsBySheetNameCellIndex.class.getMethod( "getOutput", String.class, int.class, int.class ) );

		final ComputationFactory computationFactory = engine.getComputationFactory();
		final OutputsBySheetNameCellIndex outputs = (OutputsBySheetNameCellIndex) computationFactory.newComputation( new Inputs( 5, 6, 7 ) );

		assertEquals( 5.0, outputs.getOutput( "Sheet1", 1, 0 ), 1e-8 );
		assertEquals( 6.0, outputs.getOutput( "Sheet1", 1, 1 ), 1e-8 );
		assertEquals( 7.0, outputs.getOutput( "Sheet1", 1, 2 ), 1e-8 );
		assertEquals( 30.0, outputs.getOutput( "Sheet1", 1, 3 ), 1e-8 );
		assertEquals( 210.0, outputs.getOutput( "Sheet1", 1, 4 ), 1e-8 );
	}

	@Test
	public void testOutputsBySheetIndexCellIndex() throws Exception
	{
		final Engine engine = compileEngine( this.isCached, OutputsBySheetIndexCellIndex.class, OutputsBySheetIndexCellIndex.class.getMethod( "getOutput", int.class, int.class, int.class ) );

		final ComputationFactory computationFactory = engine.getComputationFactory();
		final OutputsBySheetIndexCellIndex outputs = (OutputsBySheetIndexCellIndex) computationFactory.newComputation( new Inputs( 5, 6, 7 ) );

		assertEquals( 5.0, outputs.getOutput( 0, 1, 0 ), 1e-8 );
		assertEquals( 6.0, outputs.getOutput( 0, 1, 1 ), 1e-8 );
		assertEquals( 7.0, outputs.getOutput( 0, 1, 2 ), 1e-8 );
		assertEquals( 30.0, outputs.getOutput( 0, 1, 3 ), 1e-8 );
		assertEquals( 210.0, outputs.getOutput( 0, 1, 4 ), 1e-8 );
	}


	@Test
	public void testStringsOutput() throws Exception
	{
		final Engine engine = compileEngine( this.isCached, StringOutputsByCellName.class, StringOutputsByCellName.class.getMethod( "getOutput", String.class ) );

		final ComputationFactory computationFactory = engine.getComputationFactory();
		final StringOutputsByCellName outputs = (StringOutputsByCellName) computationFactory.newComputation( new Inputs( 5, 6, 7 ) );

		assertEquals( "Length", outputs.getOutput( "Sheet1!A1" ) );
		assertEquals( "Width", outputs.getOutput( "Sheet1!A2" ) );
		assertEquals( "Height", outputs.getOutput( "Sheet1!A3" ) );
		assertEquals( "Area", outputs.getOutput( "Sheet1!A4" ) );
		assertEquals( "Volume", outputs.getOutput( "Sheet1!A5" ) );
		assertEquals( "5", outputs.getOutput( "Sheet1!B1" ) );
		assertEquals( "6", outputs.getOutput( "Sheet1!B2" ) );
		assertEquals( "7", outputs.getOutput( "Sheet1!B3" ) );
		assertEquals( "30", outputs.getOutput( "Sheet1!B4" ) );
		assertEquals( "210", outputs.getOutput( "Sheet1!B5" ) );
	}

	private Engine compileEngine( final boolean _fullCaching, Class<?> _outputClass, Method _outputMethod ) throws IOException, CompilerException, NoSuchMethodException, EngineException
	{
		// ---- binding
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		// -omit-
		builder.loadSpreadsheet( new File( "src/test/data/org/formulacompiler/tutorials/CubeVolume.xls" ) );
		builder.setInputClass( Inputs.class );
		builder.setOutputClass( _outputClass );
		builder.setFullCaching( _fullCaching );
		// -omit-
		final SpreadsheetBinder.Section rootBinder = builder.getRootBinder();
		rootBinder./**/defineOutputByCellAddress/**/( _outputMethod );
		// ---- binding
		builder.bindAllByName();

		final Engine engine = builder.compile();

		FormulaDecompiler.decompile( engine ).saveTo( new File( "temp/test/decompiled/CalculateAll/" + this.testName.getMethodName() + (_fullCaching ? "/caching" : "/non-caching") ) );
		return engine;
	}

	public static class Inputs
	{
		private final double length;
		private final double width;
		private final double height;

		public Inputs( final double _length, final double _width, final double _height )
		{
			this.length = _length;
			this.width = _width;
			this.height = _height;
		}

		public double getLength()
		{
			return this.length;
		}

		public double getWidth()
		{
			return this.width;
		}

		public double getHeight()
		{
			return this.height;
		}
	}

	// ---- cellAddress
	public static interface OutputsByCellName
	{
		double getOutput( /**/String cellName/**/ );
	}
	// ---- cellAddress

	// ---- sheetNameColIndexRowIndex
	public static interface OutputsBySheetNameCellIndex
	{
		double getOutput( /**/String sheetName, int colIndex, int rowIndex/**/ );
	}
	// ---- sheetNameColIndexRowIndex

	// ---- sheetIndexColIndexRowIndex
	public static interface OutputsBySheetIndexCellIndex
	{
		double getOutput( /**/int sheetIndex, int colIndex, int rowIndex/**/ );
	}
	// ---- sheetIndexColIndexRowIndex

	public static interface StringOutputsByCellName
	{
		String getOutput( String cellName );
	}
}
