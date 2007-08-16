/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.formulacompiler.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.CellNameDefinition;
import org.formulacompiler.spreadsheet.Spreadsheet.NameDefinition;

import junit.framework.TestCase;

public class LookupTest extends TestCase
{
	private static final File DECOMP_PATH = new File( "temp/test/decompiled/impl/lookup" );
	
	static {
		DECOMP_PATH.mkdirs();
	}

	
	// FIXME Switch in interpreter
	

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
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tests/LookupTest.xls" );
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
		final NameDefinition[] nameDefs = _builder.getSpreadsheet().getDefinedNames();
		for (NameDefinition nameDef : nameDefs) {
			final String name = nameDef.getName();
			if (name.startsWith( _paramPrefix )) {
				final CellNameDefinition namedCell = (CellNameDefinition) nameDef;
				final Cell cell = namedCell.getCell();
				if (name.startsWith( inputPrefix )) {
					_builder.getRootBinder().defineInputCell( cell, new CallFrame( inputGetter, inputIndex++ ) );
					_inputs.add( ((Number) cell.getValue()).doubleValue() );
				}
				else if (name.startsWith( outputPrefix )) {
					_builder.getRootBinder().defineOutputCell( cell, new CallFrame( outputGetter, outputIndex++ ) );
					_expected.add( ((Number) cell.getValue()).doubleValue(), namedCell.getName() );
				}
			}
		}
	}


	public static final class Inputs
	{
		private List<Double> inputs = New.newArrayList();
		private List<String> names = New.newArrayList();

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
			final String decomp = readStringFrom( decompFile );
			final String start = "\n" + _indentation + _start + "\n";
			final String end = "\n" + _indentation + _end + "\n";
			final int iFrom = decomp.indexOf( start ) + 1;
			final int iUpToExcl = decomp.indexOf( end, iFrom ) + end.length() - 1;

			assertTrue( _start + " not found in decompiled source", iFrom >= 1 );
			assertTrue( _end + " not found in decompiled source", iUpToExcl > iFrom );

			final String marked = decomp.substring( 0, iFrom )
					+ "\n" + _indentation + "// ---- fragment\n" + decomp.substring( iFrom, iUpToExcl ) + "\n"
					+ _indentation + "// ---- fragment\n" + decomp.substring( iUpToExcl );

			writeStringTo( marked, decompFile );
		}
	}

	protected static String readStringFrom( File _source ) throws IOException
	{
		StringBuffer sb = new StringBuffer( 1024 );
		BufferedReader reader = new BufferedReader( new FileReader( _source ) );
		try {
			char[] chars = new char[ 1024 ];
			int red;
			while ((red = reader.read( chars )) > -1) {
				sb.append( chars, 0, red );
			}
		}
		finally {
			reader.close();
		}
		return sb.toString();
	}

	protected static void writeStringTo( String _value, File _target ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( _target ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}

}
