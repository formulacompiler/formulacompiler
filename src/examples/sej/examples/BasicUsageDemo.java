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

import java.io.IOException;

import sej.CallFrame;
import sej.Compiler;
import sej.CompilerFactory;
import sej.Engine;
import sej.ModelError;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.engine.standard.compiler.StandardCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;
import sej.loader.excel.xml.ExcelXMLLoader;

public class BasicUsageDemo
{

	static {
		ExcelXLSLoader.register();
		StandardCompiler.registerAsDefault();
	}


	public static void main( String[] args ) throws IOException, ModelError, NoSuchMethodException
	{

		// ---- BasicUsage
		// ---- Construction
		// Load and parse the spreadsheet file into memory.
		Spreadsheet model = SpreadsheetLoader.loadFromFile( "examples/data/Test.xls" );

		// Create an engine builder for the loaded spreadsheet file.
		// Pass to it the input and output types.
		Compiler compiler = CompilerFactory.newDefaultCompiler( model, Inputs.class, Outputs.class );

		// Define which of the cells will be variable inputs to the engine.
		// All inputs are bound to a method that will be called to obtain their value.
		Compiler.Section root = compiler.getRoot();
		root.defineInputCell( model.getCell( 0, 1, 0 ), new CallFrame( Inputs.class.getMethod( "getA" ) ) );
		root.defineInputCell( model.getCell( 0, 1, 1 ), new CallFrame( Inputs.class.getMethod( "getB" ) ) );

		// Define which of the cells will be computable outputs of the engine.
		// Outputs are bound to prototype methods that are implemented by the engine.
		root.defineOutputCell( model.getCell( 0, 1, 2 ), new CallFrame( Outputs.class.getMethod( "getResult" ) ) );

		// Build an engine for the given spreadsheet, inputs, and outputs.
		Engine engine = compiler.compileNewEngine();
		// ---- Construction

		// ---- Computation
		// Compute an actual output value for a given set of actual input values.
		Inputs inputs = new Inputs( 4, 40 );
		Outputs outputs = (Outputs) engine.newComputation( inputs );
		double result = outputs.getResult();
		// ---- Computation

		System.out.printf( "Result is: %f", result );
		// ---- BasicUsage

	}


	public static void samples() throws IOException
	{
		// ---- LoaderRegistry
		// Register the Microsoft Excel loaders.
		ExcelXLSLoader.register();
		ExcelXMLLoader.register();

		// Load and parse the spreadsheet files into memory.
		Spreadsheet model1 = SpreadsheetLoader.loadFromFile( "Test.xls" );
		Spreadsheet model2 = SpreadsheetLoader.loadFromFile( "Test.xml" );
		// ---- LoaderRegistry

		if (model1 == model2)
		; // skip warnings
	}


}
