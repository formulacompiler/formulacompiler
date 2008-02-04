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
import java.util.Map;

import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;



public class BindingToMultipleInstances
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
					binder.defineInputCell( cell, builder.newCallFrame( intfGetter, /**/iCC/**/ )./**/chain/**/( valueGetter ) );
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
					binder.defineOutputCell( cell, builder.newCallFrame( outputGetter, /**/iCC/**/ ) );
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

		public OutputFacade(Output _output)
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
			
			public CC(/**/int _iCC/**/)
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
