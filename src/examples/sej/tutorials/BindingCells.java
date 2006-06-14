/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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

import java.lang.reflect.Method;

import sej.CallFrame;
import sej.EngineBuilder;
import sej.SEJ;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;


public class BindingCells
{

	public void bindingCells() throws Exception
	{
		final String path = "src/test-system/data/tutorials/BindingCells.xls";

		// ---- setupBuilder
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		/* -in- */builder.setInputClass( Input.class );/* -in- */
		/* -out- */builder.setOutputClass( Output.class );/* -out- */
		// ---- setupBuilder
		// ---- getBinder
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder./**/getRootBinder/**/();
		// ---- getBinder

		Method method, chainedMethod;
		Spreadsheet.Cell cell;

		// ---- bindPlainInputs
		cell = spreadsheet./**/getCell/**/( "SOME_VALUE" );
		method = Input.class./**/getMethod/**/( "getSomeValue" );
		binder./**/defineInputCell/**/( cell, new CallFrame( method ) );

		cell = spreadsheet.getCell( "OTHER_VALUE" );
		method = Input.class.getMethod( "getAnotherValue" );
		binder.defineInputCell( cell, new CallFrame( method ) );
		// ---- bindPlainInputs

		// ---- bindParamInputs
		cell = spreadsheet.getCell( "YEAR_1994" );
		method = Input.class.getMethod( "getValueForYear", /**/Integer.TYPE/**/ );
		binder.defineInputCell( cell, new CallFrame( method, /**/1994/**/ ) );
		// ---- bindParamInputs

		// ---- bindChainedInputs
		cell = spreadsheet.getCell( "NAME_LENGTH" );
		method = Input.class.getMethod( "getName" );
		chainedMethod = String.class.getMethod( "length" );
		binder.defineInputCell( cell, new CallFrame( method )/**/.chain/**/( chainedMethod ) );
		// ---- bindChainedInputs

		// ---- bindPlainOutputs
		cell = spreadsheet.getCell( "RESULT" );
		method = Output.class.getMethod( "getResult" );
		binder.defineOutputCell( cell, new CallFrame( method ) );

		cell = spreadsheet.getCell( "COEFF" );
		method = Output.class.getMethod( "getCoefficient" );
		binder.defineOutputCell( cell, new CallFrame( method ) );
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
