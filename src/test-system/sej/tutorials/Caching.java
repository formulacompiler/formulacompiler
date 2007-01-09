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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.math.BigDecimal;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.runtime.ComputationFactory;
import sej.runtime.Resettable;
import sej.runtime.SEJException;
import junit.framework.TestCase;

public class Caching extends TestCase
{
	private static final String path = "src/test-system/testdata/sej/tutorials/Caching.xls";


	public void testNoCachingWithModifiedInputs() throws Exception
	{
		ComputationFactory factory = compile( PlainOutput.class, "plain" );

		// ---- noCache
		Input input = new Input();
		PlainOutput output = (PlainOutput) factory.newComputation( input );

		input.setSide( /**/"10"/**/);
		assertEquals( /**/"100"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"1000"/**/, output.getVolume().toPlainString() );

		input.setSide( /**/"5"/**/);
		assertEquals( /**/"25"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"125"/**/, output.getVolume().toPlainString() );
		// ---- noCache
	}


	public void testCachingWithModifiedInputs() throws Exception
	{
		ComputationFactory factory = compile( CachingOutput.class, "caching" );

		// ---- cache
		Input input = new Input();
		CachingOutput output = (CachingOutput) factory.newComputation( input );

		input.setSide( "10" );
		assertEquals( "100", output.getArea().toPlainString() );
		assertEquals( "1000", output.getVolume().toPlainString() );

		input.setSide( "5" );
		assertEquals( /**/"100"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"1000"/**/, output.getVolume().toPlainString() );
		// ---- cache

		// ---- reset
		input.setSide( "5" );
		/**/output.reset();/**/
		assertEquals( /**/"25"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"125"/**/, output.getVolume().toPlainString() );
		// ---- reset
	}


	public void testSpeed() throws Exception
	{
		if (Boolean.getBoolean( "sej.tutorials.Caching.testSpeed.disabled" )) return;
		
		ComputationFactory plainFactory = compile( PlainOutput.class, null );
		ComputationFactory cachingFactory = compile( CachingPlainOutput.class, null );
		Input input = new Input();

		// ---- timing
		input.setSide( "123456789123456789123456789123456789123456789123456789123456789123456789" );
		long plainTime = time( plainFactory, input );
		long cachingTime = time( cachingFactory, input );
		assertTrue( "Caching is at least half as fast again; caching is " + cachingTime + " vs. " + plainTime,
				cachingTime * 3 / 2 < plainTime );
		// ---- timing
	}


	private long time( ComputationFactory _factory, Input _input )
	{
		long result = 0;
		for (int i = 0; i < 100; i++) {
			PlainOutput output = (PlainOutput) _factory.newComputation( _input );
			// ---- timed
			long startTime = System.nanoTime();
			output.getArea();
			output.getVolume();
			long endTime = System.nanoTime();
			result += (endTime - startTime);
		}
		return result;
		// ---- timed
	}


	public static class Input
	{
		private String side;
		public BigDecimal getSide()
		{
			return new BigDecimal( this.side );
		}
		public void setSide( String _side )
		{
			this.side = _side;
		}
	}

	public static interface PlainOutput
	{
		BigDecimal getArea();
		BigDecimal getVolume();
	}

	public static interface CachingPlainOutput extends PlainOutput, Resettable
	{
		// nothing here
	}


	// ---- CachingOutput
	public static interface CachingOutput /**/extends Resettable/**/
	{
		BigDecimal getArea();
		BigDecimal getVolume();
	}
	// ---- CachingOutput


	private ComputationFactory compile( Class _outputClass, String _path ) throws FileNotFoundException, IOException, SEJException
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( _outputClass );
		builder.setNumericType( SEJ.getNumericType( BigDecimal.class, 0, BigDecimal.ROUND_HALF_UP ) );
		builder.bindAllByName();
		SaveableEngine engine = builder.compile();
		if (null != _path) {
			SEJ.decompileEngine( engine ).saveTo( "temp/decompiled/caching/" + _path );
		}
		return engine.getComputationFactory();
	}

}
