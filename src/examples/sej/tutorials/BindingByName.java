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


public class BindingByName
{


	public void bindingByName() throws IOException, ModelError, NoSuchMethodException
	{
		Spreadsheet spreadsheet = SpreadsheetLoader.loadFromFile( "src/test-system/data/tutorials/BindingByName.xls" );

		// ---- createCompiler
		Class input = Input.class;
		Class output = Output.class;
		Compiler compiler = CompilerFactory.newDefaultCompiler( spreadsheet, input, output );
		Compiler.Section root = compiler.getRoot();
		// ---- createCompiler

		// ---- bindNamedInputs
		final Method inputMethod = input.getMethod( "getInput", String.class );
		for (Spreadsheet.NameDefinition def : spreadsheet.getDefinedNames()) {
			if (def instanceof Spreadsheet.CellNameDefinition) {
				final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) def;
				final String name = cellDef.getName();
				if ('I' == name.charAt( 0 )) {
					final Spreadsheet.Cell cell = cellDef.getCell();
					root.defineInputCell( cell, new CallFrame( inputMethod, name ) );
				}
			}
		}
		// ---- bindNamedInputs

		// ---- bindNamedOutputs
		final Method outputMethod = output.getMethod( "getOutput", String.class );
		for (Spreadsheet.NameDefinition def : spreadsheet.getDefinedNames()) {
			if (def instanceof Spreadsheet.CellNameDefinition) {
				final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) def;
				final String name = cellDef.getName();
				if ('O' == name.charAt( 0 )) {
					final Spreadsheet.Cell cell = cellDef.getCell();
					root.defineOutputCell( cell, new CallFrame( outputMethod, name ) );
				}
			}
		}
		// ---- bindNamedOutputs

	}


	// ---- Input
	public static interface Input
	{
		double getInput( String _name );
	}
	// ---- Input


	// ---- Output
	public static abstract class Output
	{
		public double getOutput( String _name )
		{
			return 0;
		}
	}
	// ---- Output


	public static final class GeneratedOutput extends Output
	{

		// ---- GeneratedGetter
		@Override
		public final double getOutput( String _name )
		{
			if (_name.equals( "O_Result" )) return getOutput__1();
			if (_name.equals( "O_Coeff" )) return getOutput__2();
			// ... other bound outputs
			return super.getOutput( _name );
		}

		private final double getOutput__1()
		{
			return 0; // generated computation for cell O_Result
		}

		private final double getOutput__2()
		{
			return 0; // generated computation for cell O_Coeff
		}
		// ---- GeneratedGetter

	}


}
