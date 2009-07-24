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

package org.formulacompiler.tutorials;

import java.lang.reflect.Method;

import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;


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
		binder.defineInputCell( cell, builder.newCallFrame( intfGetter )./**/chain/**/( valueGetter ) );

		cell = spreadsheet.getCell( "B_VALUE" );
		intfGetter = Input.class.getMethod( /**/"getB"/**/ );
		valueGetter = String.class.getMethod( "getValue" );
		binder.defineInputCell( cell, builder.newCallFrame( intfGetter )./**/chain/**/( valueGetter ) );
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

		public Input( InputA _a, InputB _b )
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

		public OutputAImpl( Output2 _output )
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

		public OutputBImpl( Output2 _output )
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
