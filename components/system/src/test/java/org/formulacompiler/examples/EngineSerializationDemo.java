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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class EngineSerializationDemo extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	private void compileAndSave() throws Exception
	{
		// ---- Serialization
		// Build an engine for the given spreadsheet, inputs, and outputs.
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( getSpreadsheetFile() );
		builder.setFactoryClass( OutputFactory.class );
		builder.bindAllByName();
		SaveableEngine compiledEngine = builder.compile();

		// Write the engine out to its serialized form, then drop the reference to it.
		File engineSerializationFile = new File( TEMP_ENGINE_JAR );
		OutputStream outStream = new BufferedOutputStream( new FileOutputStream( engineSerializationFile ) );
		try {
			compiledEngine.saveTo( outStream );
		}
		finally {
			outStream.close();
		}
		// ---- Serialization
	}

	private double loadAndCompute() throws Exception
	{
		// ---- Deserialization
		// Instantiate an engine from the serialized form.
		File engineSerializationFile = new File( TEMP_ENGINE_JAR );
		InputStream inStream = new BufferedInputStream( new FileInputStream( engineSerializationFile ) );
		Engine loadedEngine = FormulaRuntime.loadEngine( inStream );
		OutputFactory factory = (OutputFactory) loadedEngine.getComputationFactory();

		// Compute an actual output value for a given set of actual input values.
		Inputs inputs = new Inputs( 4, 40 );
		Outputs outputs = factory.newInstance( inputs );
		double result = outputs.getResult();

		return result;
		// ---- Deserialization
	}


	private static final String DATA_PATH = "src/test/data/org/formulacompiler/examples/";
	private static final String TEMP_ENGINE_JAR = "temp/Engine.jar";

	private String getSpreadsheetFile()
	{
		return DATA_PATH + "test" + getSpreadsheetExtension();
	}

	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( EngineSerializationDemo.class );
	}

	public static void main( String[] args ) throws Exception
	{
		EngineSerializationDemo demo = new EngineSerializationDemo();
		demo.setSpreadsheetExtension( args.length > 0 ? args[ 0 ] : ".ods" );
		demo.compileAndSave();
		System.out.printf( "Result is: %f", demo.loadAndCompute() );
	}

	public void testComputation() throws Exception
	{
		compileAndSave();
		assertEquals( 160.0, loadAndCompute(), 0.0001 );
		decompile();
	}


	private void decompile() throws Exception
	{
		File engineSerializationFile = new File( TEMP_ENGINE_JAR );
		InputStream inStream = new BufferedInputStream( new FileInputStream( engineSerializationFile ) );
		Engine loadedEngine = FormulaRuntime.loadEngine( inStream );
		ByteCodeEngineSource source = FormulaDecompiler.decompile( loadedEngine );
		final File saveDir = new File( "temp/test/decompiled/basicusage" + getSpreadsheetExtension() );
		source.saveTo( saveDir );
		final File destDir = new File( "temp/test/decompiled/basicusage" );
		copyOrCompare( saveDir, destDir );
	}

	private void copyOrCompare( File _saveDir, final File _destDir ) throws IOException
	{
		final File[] files = _saveDir.listFiles();
		for (File file : files) {
			final File destFile = new File( _destDir, file.getName() );
			if (file.isDirectory()) {
				copyOrCompare( file, destFile );
			}
			else {
				final String content = IOUtil.readStringFrom( file );
				if (destFile.exists()) {
					final String actual = IOUtil.readStringFrom( destFile );
					assertEquals( content, actual );
				}
				else {
					destFile.getParentFile().mkdirs();
					IOUtil.writeStringTo( content, destFile );
				}
			}
		}
	}

}
