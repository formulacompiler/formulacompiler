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
package org.formulacompiler.examples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

public class EngineSerializationDemo extends TestCase
{

	private void compileAndSave() throws Exception
	{
		// ---- Serialization
		// Build an engine for the given spreadsheet, inputs, and outputs.
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( DATA_PATH + "test.xls" );
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

	public static void main( String[] args ) throws Exception
	{
		EngineSerializationDemo demo = new EngineSerializationDemo();
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
		source.saveTo( new File( "temp/test/decompiled/basicusage" ) );
	}

}
