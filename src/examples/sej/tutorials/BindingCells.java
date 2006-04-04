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
package sej.tutorials;

import java.io.IOException;
import java.lang.reflect.Method;

import sej.CallFrame;
import sej.Compiler;
import sej.CompilerFactory;
import sej.ModelError;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;


public class BindingCells
{

	public void bindingCells() throws IOException, ModelError, NoSuchMethodException
	{
		Spreadsheet spreadsheet = SpreadsheetLoader.loadFromFile( "src/test-system/data/tutorials/BindingCells.xls" );

		// ---- createCompiler
		Class input = Input.class;
		Class output = Output.class;
		Compiler compiler = CompilerFactory.newDefaultCompiler( spreadsheet, input, output );
		Compiler.Section root = compiler.getRoot();
		// ---- createCompiler

		Method method, chainedMethod;
		Spreadsheet.Cell cell;

		// ---- bindPlainInputs
		cell = spreadsheet.getCell( "SOME_VALUE" );
		method = input.getMethod( "getSomeValue" );
		root.defineInputCell( cell, new CallFrame( method ) );

		cell = spreadsheet.getCell( "OTHER_VALUE" );
		method = input.getMethod( "getAnotherValue" );
		root.defineInputCell( cell, new CallFrame( method ) );
		// ---- bindPlainInputs

		// ---- bindParamInputs
		cell = spreadsheet.getCell( "YEAR_1994" );
		method = input.getMethod( "getValueForYear", Integer.TYPE );
		root.defineInputCell( cell, new CallFrame( method, 1994 ) );
		// ---- bindParamInputs

		// ---- bindChainedInputs
		cell = spreadsheet.getCell( "NAME_LENGTH" );
		method = input.getMethod( "getName" );
		chainedMethod = String.class.getMethod( "length" );
		root.defineInputCell( cell, new CallFrame( method ).chain( chainedMethod ) );
		// ---- bindChainedInputs

		// ---- bindPlainOutputs
		cell = spreadsheet.getCell( "RESULT" );
		method = output.getMethod( "getResult" );
		root.defineOutputCell( cell, new CallFrame( method ) );

		cell = spreadsheet.getCell( "COEFF" );
		method = output.getMethod( "getCoefficient" );
		root.defineOutputCell( cell, new CallFrame( method ) );
		// ---- bindPlainOutputs
	}


	// ---- Input
	public static interface Input
	{
		double getSomeValue();
		double getAnotherValue();
		double getValueForYear( int year );
		String getName();
	}
	// ---- Input


	// ---- Output
	public static interface Output
	{
		double getResult();
		double getCoefficient();
	}
	// ---- Output


	// ---- OutputWithDefaults
	public static abstract class OutputWithDefault 
	{
		private final Input input;

		public OutputWithDefault(Input _input)
		{
			super();
			this.input = _input;
		}

		public abstract double getResult();

		public double getCoefficient()
		{
			return this.input.getSomeValue() * 0.02;
		}
	}
	// ---- OutputWithDefaults


}
