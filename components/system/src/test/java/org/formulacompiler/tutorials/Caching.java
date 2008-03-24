/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.runtime.FormulaCompilerException;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

public class Caching extends TestCase
{
	private static final String path = "src/test/data/org/formulacompiler/tutorials/Caching.xls";


	public void testNoCaching() throws Exception
	{
		ComputationFactory factory = compile( false, "plain" );

		// ---- noCacheAccesses
		Input input = new Input();
		Output output = (Output) factory.newComputation( input );
		assertEquals( /**/0/**/, input.getNumberOfAccessesToSide() );
		output./**/getArea()/**/;
		assertEquals( /**/2/**/, input.getNumberOfAccessesToSide() );
		output./**/getVolume()/**/;
		assertEquals( /**/5/**/, input.getNumberOfAccessesToSide() );
		// ---- noCacheAccesses
	}

	public void testCaching() throws Exception
	{
		ComputationFactory factory = compile( true, "caching" );

		// ---- cacheAccesses
		Input input = new Input();
		Output output = (Output) factory.newComputation( input );
		assertEquals( /**/0/**/, input.getNumberOfAccessesToSide() );
		output./**/getArea()/**/;
		assertEquals( /**/1/**/, input.getNumberOfAccessesToSide() );
		output./**/getVolume()/**/;
		assertEquals( /**/1/**/, input.getNumberOfAccessesToSide() );
		// ---- cacheAccesses
	}


	public void testNoCachingWithModifiedInputs() throws Exception
	{
		ComputationFactory factory = compile( false, null );

		// ---- noCache
		Input input = new Input();
		Output output = (Output) factory.newComputation( input );

		input.setSide( /**/"10"/**/ );
		assertEquals( /**/"100"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"1000"/**/, output.getVolume().toPlainString() );

		input.setSide( /**/"5"/**/ );
		assertEquals( /**/"25"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"125"/**/, output.getVolume().toPlainString() );
		// ---- noCache
	}


	public void testCachingWithModifiedInputs() throws Exception
	{
		ComputationFactory factory = compile( true, null );

		// ---- cache
		Input input = new Input();
		Output output = (Output) factory.newComputation( input );

		input.setSide( "10" );
		assertEquals( "100", output.getArea().toPlainString() );
		assertEquals( "1000", output.getVolume().toPlainString() );

		input.setSide( "5" );
		assertEquals( /**/"100"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"1000"/**/, output.getVolume().toPlainString() );
		// ---- cache

		output.reset();
		// ---- reset
		input.setSide( "10" );
		assertEquals( "100", output.getArea().toPlainString() );
		assertEquals( "1000", output.getVolume().toPlainString() );

		input.setSide( "5" );
		/**/output.reset();/**/
		assertEquals( /**/"25"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"125"/**/, output.getVolume().toPlainString() );
		// ---- reset
	}


	public void testSpeed() throws Exception
	{
		if (Boolean.getBoolean( "org.formulacompiler.tutorials.Caching.testSpeed.disabled" )) return;

		ComputationFactory plainFactory = compile( false, null );
		ComputationFactory cachingFactory = compile( true, null );
		Input input = new Input();

		// ---- timing
		input.setSide( "123456789123456789123456789123456789123456789123456789123456789123456789" );
		long plainTime = 0;
		long cachingTime = 0;
		for (int i = 0; i < 100; i++) {
			plainTime += time( plainFactory, input );
			cachingTime += time( cachingFactory, input );
		}
		assertTrue( "Caching is at least half as fast again; caching is " + cachingTime + " vs. " + plainTime,
				cachingTime * 3 / 2 < plainTime );
		// ---- timing
	}


	private long time( ComputationFactory _factory, Input _input )
	{
		Output output = (Output) _factory.newComputation( _input );
		// ---- timed
		long startTime = System.nanoTime();
		output.getArea();
		output.getVolume();
		output.getArea();
		output.getVolume();
		output.getArea();
		output.getVolume();
		long timeTaken = System.nanoTime() - startTime;
		// ---- timed
		return timeTaken;
	}


	public static class Input
	{
		private String side = "10";
		private int sideAccesses = 0;
		public BigDecimal getSide()
		{
			this.sideAccesses++;
			return new BigDecimal( this.side );
		}
		public void setSide( String _side )
		{
			this.side = _side;
		}
		public int getNumberOfAccessesToSide()
		{
			return this.sideAccesses;
		}
	}

	// ---- Output
	public static interface Output /**/extends Resettable/**/
	{
		BigDecimal getArea();
		BigDecimal getVolume();
	}
	// ---- Output


	private ComputationFactory compile( boolean _caching, String _path ) throws FileNotFoundException, IOException,
			FormulaCompilerException
	{
		final NumericType unboundedBigDecimal = SpreadsheetCompiler.getNumericType( BigDecimal.class, 0,
				BigDecimal.ROUND_HALF_UP );
		// DO NOT REFORMAT BELOW THIS LINE
		// ---- compile
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output.class );
		builder.setNumericType( unboundedBigDecimal );
		// ---- compile
		if (_caching)
		// ---- compile
		/**/builder.setFullCaching( true );/**/
		builder.bindAllByName();
		SaveableEngine engine = builder.compile();
		// ---- compile
		// DO NOT REFORMAT ABOVE THIS LINE
		if (null != _path) {
			FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/caching/" + _path );
		}
		return engine.getComputationFactory();
	}

}
