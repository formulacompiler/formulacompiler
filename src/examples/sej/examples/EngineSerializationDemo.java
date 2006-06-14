/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.examples;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.runtime.Engine;
import sej.runtime.SEJRuntime;

public class EngineSerializationDemo
{

	public static void main( String[] args ) throws Exception
	{
		// ---- Serialization
		// Build an engine for the given spreadsheet, inputs, and outputs.
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( "examples/testdata/sej/Test.xls" );
		builder.setFactoryClass( Factory.class );
		builder.bindAllByName();
		SaveableEngine compiledEngine = builder.compile();

		// Write the engine out to its serialized form, then drop the reference to it.
		File engineSerializationFile = new File( "/temp/Engine.jar" );
		OutputStream outStream = new BufferedOutputStream( new FileOutputStream( engineSerializationFile ) );
		try {
			compiledEngine.saveTo( outStream );
		}
		finally {
			outStream.close();
		}
		// ---- Serialization

		// ---- Deserialization
		// Instantiate an engine from the serialized form.
		InputStream inStream = new BufferedInputStream( new FileInputStream( engineSerializationFile ) );
		Engine loadedEngine = SEJRuntime.loadEngine( inStream );
		Factory factory = (Factory) loadedEngine.getComputationFactory();

		// Compute an actual output value for a given set of actual input values.
		Inputs inputs = new Inputs( 4, 40 );
		Outputs outputs = factory.newInstance( inputs );
		double result = outputs.getResult();

		System.out.printf( "Result is: %f", result );
		// ---- Deserialization

	}
}
