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

package org.formulacompiler.tests;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormatTestFactory;

import junit.framework.Test;

public class LookupTest extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{
	private static final File DECOMP_PATH = new File( "temp/test/decompiled/impl/lookup" );

	static {
		DECOMP_PATH.mkdirs();
	}


	public void testMatchConsts() throws Exception
	{
		final String test = "MatchConsts";
		assertSheet( test, "MC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double[] $constarr$0() {", "}" );
	}

	public void testMatchInputs() throws Exception
	{
		final String test = "MatchInputs";
		assertSheet( test, "MI_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double[] $constarr$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double[] $arr$0() {", "}" );
		markInDecompiledSource( test, "    ", "public final void reset() {", "}" );
	}


	public void testIndexConsts() throws Exception
	{
		final String test = "IndexConsts";
		assertSheet( test, "IC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double $idx$0(int i) {", "}" );
	}

	public void testIndexInputs() throws Exception
	{
		final String test = "IndexInputs";
		assertSheet( test, "II_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double $idx$0(int i) {", "}" );
	}


	public void testLookupConsts() throws Exception
	{
		final String test = "LookupConsts";
		assertSheet( test, "LC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
	}

	public void testLookupInputs() throws Exception
	{
		final String test = "LookupInputs";
		assertSheet( test, "LI_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
	}

	public void testIndexMatchConsts() throws Exception
	{
		final String test = "IndexMatchConsts";
		assertSheet( test, "IMC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
	}

	public void testMultiMatchConsts() throws Exception
	{
		final String test = "MultiMatchConsts";
		assertSheet( test, "MMC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double get$2() {", "}" );
	}

	public void testMultiIndexConsts() throws Exception
	{
		final String test = "MultiIndexConsts";
		assertSheet( test, "MIC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double get$2() {", "}" );
	}

	public void testMultiLookupConsts() throws Exception
	{
		final String test = "MultiLookupConsts";
		assertSheet( test, "MLC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double get$2() {", "}" );
	}

	public void testSubArrayConsts() throws Exception
	{
		final String test = "SubArrayConsts";
		assertSheet( test, "SAC_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double get$2() {", "}" );
		markInDecompiledSource( test, "    ", "final double get$3() {", "}" );
	}

	public void testHLookupInputs() throws Exception
	{
		final String test = "HLookupInputs";
		assertSheet( test, "HLI_" );
		markInDecompiledSource( test, "    ", "final double get$0() {", "}" );
		markInDecompiledSource( test, "    ", "final double get$5(double d) {", "}" );
	}


	private void assertSheet( String _testName, String _paramPrefix ) throws Exception
	{
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setLoadAllCellValues( true );
		builder.loadSpreadsheet( new File( "src/test/data/org/formulacompiler/tests/LookupTest" + getSpreadsheetExtension() ) );
		builder.setInputClass( Inputs.class );
		builder.setOutputClass( Outputs.class );

		final Inputs inputs = new Inputs();
		final Inputs expected = new Inputs();
		setupParams( builder, _paramPrefix, inputs, expected );

		SaveableEngine engine = builder.compile();
		engine.saveTo( new FileOutputStream( new File( DECOMP_PATH, _testName.toLowerCase() + ".jar" ) ) );
		try {
			ByteCodeEngineSource decompiled = FormulaDecompiler.decompile( engine );
			decompiled.saveTo( new File( DECOMP_PATH, _testName.toLowerCase() ) );
		}
		catch (Exception e) {
			System.out.println( "Failed to decompile: " + e.getMessage() );
		}

		ComputationFactory factory = engine.getComputationFactory();
		Outputs computation = (Outputs) factory.newComputation( inputs );
		for (int i = 0; i < expected.size(); i++) {
			final double want = expected.get( i );
			final double have = computation.get( i );
			assertEquals( expected.name( i ), want, have, 0.00001 );
		}
	}

	private void setupParams( final EngineBuilder _builder, String _paramPrefix, final Inputs _inputs,
			final Inputs _expected ) throws Exception
	{
		final Method inputGetter = Inputs.class.getMethod( "get", Integer.TYPE );
		final Method outputGetter = Outputs.class.getMethod( "get", Integer.TYPE );
		final String inputPrefix = _paramPrefix + "IN";
		final String outputPrefix = _paramPrefix + "OUT";
		int inputIndex = 0;
		int outputIndex = 0;
		final Map<String, Spreadsheet.Range> nameDefs = _builder.getSpreadsheet().getRangeNames();
		for (Map.Entry<String, Spreadsheet.Range> nameDef : nameDefs.entrySet()) {
			final String name = nameDef.getKey().toUpperCase();
			if (name.startsWith( _paramPrefix )) {
				final Spreadsheet.Cell cell = (Spreadsheet.Cell) nameDef.getValue();
				if (name.startsWith( inputPrefix )) {
					_builder.getRootBinder().defineInputCell( cell, inputGetter, inputIndex++ );
					_inputs.add( ((Number) cell.getValue()).doubleValue() );
				}
				else if (name.startsWith( outputPrefix )) {
					_builder.getRootBinder().defineOutputCell( cell, outputGetter, outputIndex++ );
					_expected.add( ((Number) cell.getValue()).doubleValue(), name );
				}
			}
		}
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( LookupTest.class );
	}


	public static final class Inputs
	{
		private List<Double> inputs = New.arrayList();
		private List<String> names = New.arrayList();

		public double get( int _index )
		{
			return this.inputs.get( _index );
		}

		public String name( int _index )
		{
			return this.names.get( _index );
		}

		public void add( double _value )
		{
			this.inputs.add( _value );
		}

		public void add( double _doubleValue, String _name )
		{
			add( _doubleValue );
			this.names.add( _name );
		}

		public int size()
		{
			return this.inputs.size();
		}

	}

	public static interface Outputs extends Resettable
	{
		double get( int _index );
	}


	private void markInDecompiledSource( String _testName, String _indentation, String _start, String _end )
			throws Exception
	{
		final File decompFile = new File( DECOMP_PATH, _testName.toLowerCase() + "/org/formulacompiler/gen/$Root.java" );
		if (decompFile.exists()) {
			final String decomp = IOUtil.readStringFrom( decompFile );
			final String start = lineSep() + _indentation + _start + lineSep();
			final String end = lineSep() + _indentation + _end + lineSep();
			final int iFrom = decomp.indexOf( start ) + 1;
			final int iUpToExcl = decomp.indexOf( end, iFrom ) + end.length() - 1;

			assertTrue( _start + " not found in decompiled source", iFrom >= 1 );
			assertTrue( _end + " not found in decompiled source", iUpToExcl > iFrom );

			final String marked = decomp.substring( 0, iFrom )
					+ lineSep() + _indentation + "// ---- fragment" + lineSep() + decomp.substring( iFrom, iUpToExcl )
					+ lineSep() + _indentation + "// ---- fragment" + lineSep() + decomp.substring( iUpToExcl );

			IOUtil.writeStringTo( marked, decompFile );
		}
	}

	// Not static so line.separator is already properly set:
	private final String lineSep = System.getProperty( "line.separator" );
	public String lineSep()
	{
		return this.lineSep;
	}

}
