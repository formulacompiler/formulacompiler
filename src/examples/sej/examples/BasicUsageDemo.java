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

import sej.EngineBuilder;
import sej.SEJ;
import sej.runtime.Engine;

public class BasicUsageDemo
{


	public static void main( String[] args ) throws Exception
	{

		// ---- BasicUsage
		// ---- Construction
		// Get an engine builder (represents SEJ's simplified API).
		EngineBuilder builder = SEJ.newEngineBuilder();
		
		// Load and parse the spreadsheet file into memory.
		builder.loadSpreadsheet( "src/examples/testdata/sej/examples/Test.xls" );
		
		// Set the factory interface to implement. This interface defines the method
		// Outputs newInstance( Inputs _inputs ), from which SEJ derives the input
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
		// This code is not dependent on SEJ. It is a simple instance of the strategy 
		// pattern.
		Inputs inputs = new Inputs( 4, 40 );
		Outputs outputs = factory.newInstance( inputs );
		double result = outputs.getResult();
		// ---- Computation

		System.out.printf( "Result is: %f", result );
		// ---- BasicUsage

	}


}
