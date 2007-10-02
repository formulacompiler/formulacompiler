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
package org.formulacompiler.tutorials;

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;



public class BindingToMultipleInterfaces
{


	public void bindingToMultipleInputs() throws Exception
	{
		final String path = "src/test-system/data/tutorials/BindingToMultipleInputs.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		Spreadsheet.Cell cell;
		Method intfGetter, valueGetter;

		// ---- bindInputs
		cell = spreadsheet.getCell( "A_VALUE" );
		intfGetter = Input.class.getMethod( /**/"getA"/**/ );
		valueGetter = String.class.getMethod( "getValue" );
		binder.defineInputCell( cell, new CallFrame( intfGetter )./**/chain/**/( valueGetter ) );

		cell = spreadsheet.getCell( "B_VALUE" );
		intfGetter = Input.class.getMethod( /**/"getB"/**/ );
		valueGetter = String.class.getMethod( "getValue" );
		binder.defineInputCell( cell, new CallFrame( intfGetter )./**/chain/**/( valueGetter ) );
		// ---- bindInputs

	}


	// ---- Inputs
	public static interface InputA
	{
		double getValue();
	}

	public static interface InputB
	{
		double getValue();
		double getOther();
	}
	// ---- Inputs


	// ---- Input
	public static final class Input
	{
		private final InputA a;
		private final InputB b;

		public Input(InputA _a, InputB _b)
		{
			super();
			this.a = _a;
			this.b = _b;
		}

		public InputA getA()
		{
			return this.a;
		}

		public InputB getB()
		{
			return this.b;
		}
	}
	// ---- Input


	// ---- Outputs
	public static interface OutputA
	{
		double getResult();
	}

	public static interface OutputB
	{
		double getResult();
		double getOther();
	}
	// ---- Outputs

	// ---- Output
	public static interface Output extends OutputA, OutputB
	{
		// no own content
	}
	// ---- Output


	// ---- Output2
	public static interface Output2 
	{
		double getResultA();
		double getResultB();
		double getOtherB();
	}
	// ---- Output2

	// ---- Output2A
	public static class OutputAImpl implements /**/OutputA/**/
	{
		private final Output2 output;

		public OutputAImpl(Output2 _output)
		{
			super();
			this.output = _output;
		}

		public double getResult()
		{
			return this.output./**/getResultA/**/();
		}
	}
	// ---- Output2A

	// ---- Output2B
	public static class OutputBImpl implements /**/OutputB/**/
	{
		private final Output2 output;

		public OutputBImpl(Output2 _output)
		{
			super();
			this.output = _output;
		}

		public double getResult()
		{
			return this.output./**/getResultB/**/();
		}

		public double getOther()
		{
			return this.output.getOtherB();
		}
	}
	// ---- Output2B

	
	// ---- Output3
	public static interface Output3
	{
		OutputA getA();
		OutputB getB();
	}
	// ---- Output3

}
