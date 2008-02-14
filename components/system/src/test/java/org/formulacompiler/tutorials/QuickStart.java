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

// ---- Imports
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.math.BigDecimal;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

// ---- Imports

public class QuickStart extends org.formulacompiler.tests.utils.AbstractSpreadsheetTestCase
{

	// ---- Path
	public static final File PATH = new File( "src/test/data/org/formulacompiler/tutorials" );
	// ---- Path


	public void testBlurb() throws Exception
	{
		// ---- Blurb
		// Compile price finding factory and strategy implementation from spreadsheet:
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder./**/loadSpreadsheet/**/( new File( PATH, /**/"CustomPriceFormula.xls"/**/ ) );
		builder.setFactoryClass( PriceFinderFactory.class );
		builder.bindAllByName();
		Engine engine = builder./**/compile/**/();
		PriceFinderFactory factory = (PriceFinderFactory) engine.getComputationFactory();

		// Use it to compute a line item price:
		LineItem item = getCurrentLineItem();
		PriceFinder priceFinder = factory./**/newInstance/**/( item );
		BigDecimal price = priceFinder./**/getPrice/**/();
		// ---- Blurb

		assertEquals( item.getArticlePrice().doubleValue() * item.getItemCount() * (1 - 0.04), price.doubleValue(),
				0.00001 );
	}


	// ---- Compile
	private SaveableEngine compile() throws FileNotFoundException, IOException, CompilerException, EngineException
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( new File( PATH, "CustomPriceFormula.xls" ) );
		builder.setFactoryClass( PriceFinderFactory.class );
		builder.bindAllByName();
		return builder.compile();
	}

	private PriceFinderFactory factoryFor( Engine engine )
	{
		return (PriceFinderFactory) engine.getComputationFactory();
	}
	// ---- Compile

	// ---- Compute
	private BigDecimal compute( PriceFinderFactory factory )
	{
		PriceFinder priceFinder = factory.newInstance( getCurrentLineItem() );
		return priceFinder.getPrice();
	}
	// ---- Compute

	// ---- Decompile
	private void decompile( SaveableEngine engine ) throws Exception
	{
		ByteCodeEngineSource source = FormulaDecompiler.decompile( engine );
		source.saveTo( "temp/test/decompiled/quickstart" );
	}
	// ---- Decompile

	// ---- Save
	private void save( SaveableEngine engine ) throws Exception
	{
		engine.saveTo( new FileOutputStream( "temp/test/CustomPriceFormula.jar" ) );
	}
	// ---- Save

	// ---- Load
	private Engine load() throws Exception
	{
		return FormulaRuntime.loadEngine( new FileInputStream( "temp/test/CustomPriceFormula.jar" ) );
	}
	// ---- Load

	// ---- Main
	public static void main( String[] args ) throws Exception
	{
		QuickStart app = new QuickStart();
		SaveableEngine engine = app.compile();
		PriceFinderFactory factory = app.factoryFor( engine );
		BigDecimal price = app.compute( factory );
		System.out.println( "The result is " + price );
		// ---- Main

		// ---- Main-Decompile
		app.decompile( engine );
		// ---- Main-Decompile

		// ---- Main-Save-Load
		app.save( engine );

		QuickStart app2 = new QuickStart();
		Engine engine2 = app2./**/load/**/();
		PriceFinderFactory factory2 = app2.factoryFor( engine2 );
		BigDecimal price2 = app2.compute( factory2 );
		System.out.println( "The result is " + price2 );
		// ---- Main-Save-Load

		// ---- Main
	}
	// ---- Main


	public void testMain() throws Exception
	{
		final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		final PrintStream sysOut = System.out;
		System.setOut( new PrintStream( bytes, true, USASCII ) );
		try {
			QuickStart.main( null );
		}
		finally {
			System.setOut( sysOut );
		}
		final String have = new String( bytes.toByteArray(), USASCII ).replace( "\n", "" ).replace( "\r", "" );
		assertEquals( "The result is 1075.2The result is 1075.2", have );
	}
	private static final String USASCII = "US-ASCII";


	// DO NOT REFORMAT BELOW THIS LINE
	
	// ---- PriceFinderIntf
	public static interface PriceFinder {
		BigDecimal getPrice();
	}
	
	public static interface PriceFinderFactory {
		PriceFinder newInstance( LineItem item );
	}
	// ---- PriceFinderIntf
	
	// ---- LineItemIntf
	public static class LineItem {
		public BigDecimal getArticlePrice() { return BigDecimal.valueOf( 112.00 ); } 
		public int getItemCount() { return 10; }
		public String getCustomerCategory() { return "B"; }
	}
	// ---- LineItemIntf
	
	// ---- LineItemGetter
	private LineItem getCurrentLineItem() {
		return new LineItem();
	}
	// ---- LineItemGetter
	
	// DO NOT REFORMAT ABOVE THIS LINE


}
