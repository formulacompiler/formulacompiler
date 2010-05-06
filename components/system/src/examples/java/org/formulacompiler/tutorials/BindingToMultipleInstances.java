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

import java.io.File;
import java.lang.reflect.Method;
import java.util.Map;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;


public class BindingToMultipleInstances
{


	public void bindingToMultipleInputs() throws Exception
	{
		final File file = new File( "src/test-system/data/tutorials/BindingToMultipleInputs.xls" );

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( file );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		Method intfGetter, valueGetter;

		// ---- bindInputs
		intfGetter = Input.class.getMethod( /**/"getCC", Integer.TYPE/**/ );
		valueGetter = CustomerCategory.class.getMethod( /**/"getDiscount"/**/ );
		for (Map.Entry<String, Spreadsheet.Range> def : spreadsheet.getRangeNames().entrySet()) {
			final Spreadsheet.Range range = def.getValue();
			if (range instanceof Spreadsheet.Cell) {
				final String name = def.getKey();
				if (name.startsWith( "CC_DISCOUNT_" )) {
					final int iCC = Integer.parseInt( name.substring( "CC_DISCOUNT_".length() ) );
					final Spreadsheet.Cell cell = (Spreadsheet.Cell) range;
					final CallFrame chain = builder.newCallFrame( intfGetter, /**/iCC/**/ )./**/chain/**/( valueGetter );
					binder.defineInputCell( cell, chain );
				}
			}
		}
		// ---- bindInputs

		Method outputGetter;

		// ---- bindOutputs
		outputGetter = Output.class.getMethod( /**/"getNewDiscount", Integer.TYPE/**/ );
		for (Map.Entry<String, Spreadsheet.Range> def : spreadsheet.getRangeNames().entrySet()) {
			final Spreadsheet.Range range = def.getValue();
			if (range instanceof Spreadsheet.Cell) {
				final String name = def.getKey();
				if (name.startsWith( "CC_NEWDISCOUNT_" )) {
					final int iCC = Integer.parseInt( name.substring( "CC_NEWDISCOUNT_".length() ) );
					final Spreadsheet.Cell cell = (Spreadsheet.Cell) range;
					binder.defineOutputCell( cell, outputGetter, /**/iCC/**/ );
				}
				// ... dito for CreditLimit
			}
		}
		// ---- bindOutputs

	}


	// ---- CC
	public static interface CustomerCategory
	{
		// ...
		double getDiscount();
		// ...
	}

	// ---- CC


	// ---- Input
	public static interface Input
	{
		CustomerCategory getCC( int _iCC );
	}

	// ---- Input


	// ---- Output
	public static interface Output
	{
		double getNewDiscount( int _iCC );
		double getNewCreditLimit( int _iCC );
	}

	// ---- Output


	// ---- Output2
	public static interface Output2
	{
		CC getCC( int _iCC );

		public interface CC
		{
			double getNewDiscount();
			double getNewCreditLimit();
		}
	}

	// ---- Output2

	// ---- OutputFacade
	public static class OutputFacade
	{
		final Output output;

		public OutputFacade( Output _output )
		{
			super();
			this.output = _output;
		}

		public Output getOutput()
		{
			return this.output;
		}

		public CC getCC( /**/int n/**/ )
		{
			return new CC( /**/n/**/ );
		}

		private class CC
		{
			private int iCC;

			public CC( /**/int _iCC/**/ )
			{
				super();
				this.iCC = _iCC;
			}

			public double getNewDiscount()
			{
				return getOutput().getNewDiscount( /**/this.iCC/**/ );
			}

			public double getNewCreditLimit()
			{
				return getOutput().getNewCreditLimit( /**/this.iCC/**/ );
			}
		}
	}
	// ---- OutputFacade


}
