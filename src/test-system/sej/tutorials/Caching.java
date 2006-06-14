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
import java.math.BigDecimal;

import sej.CallFrame;
import sej.CompilerError;
import sej.Engine;
import sej.NumericType;
import sej.Resettable;
import sej.Spreadsheet;
import sej.internal.spreadsheet.loader.SpreadsheetLoader;
import sej.internal.spreadsheet.loader.excel.xls.ExcelXLSLoader;
import junit.framework.TestCase;

public class Caching extends TestCase
{
	private static final String path = "src/test-system/testdata/sej/tutorials/Caching.xls";

	static {
		ByteCodeCompiler.registerAsDefault();
		ExcelXLSLoader.register();
	}


	public void testNoCachingWithModifiedInputs() throws Exception
	{
		Engine engine = compile( PlainOutput.class );

		// ---- noCache
		Input input = new Input();
		PlainOutput output = (PlainOutput) engine.newComputation( input );

		input.setSide( /**/"10"/**/);
		assertEquals( /**/"100"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"1000"/**/, output.getVolume().toPlainString() );

		input.setSide( /**/"5"/**/);
		assertEquals( /**/"25"/**/, output.getArea().toPlainString() );
		assertEquals( /**/"125"/**/, output.getVolume().toPlainString() );
		// ---- noCache
	}


	public static class NonCachingEngine
	{
		private Input input;
		// ---- noCacheEngine
		private BigDecimal getSide()   { return this.input.getSide(); }
		public BigDecimal getArea()    { return getSide().multiply( getSide() ); }
		public BigDecimal getVolume()  { return getArea().multiply( getSide() ); }
		// ---- noCacheEngine
	}


	public void testCachingWithModifiedInputs() throws Exception
	{
		Engine engine = compile( CachingOutput.class );

		// ---- cache
		Input input = new Input();
		CachingOutput output = (CachingOutput) engine.newComputation( input );

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


	public static class CachingEngine
	{
		private Input input;
		// ---- cacheEngine
		private boolean haveSide;
		private BigDecimal cacheSide;
		private BigDecimal getSide()
		{
			if (!this.haveSide) {
				this.cacheSide = this.input.getSide();
				this.haveSide = true;
			}
			return this.cacheSide;
		}

		private boolean haveArea;
		private BigDecimal cacheArea;
		public BigDecimal getArea()
		{
			if (!this.haveArea) {
				this.cacheArea = getSide().multiply( getSide() );
				this.haveArea = true;
			}
			return this.cacheArea;
		}

		private boolean haveVolume;
		private BigDecimal cacheVolume;
		public BigDecimal getVolume()
		{
			if (!this.haveVolume) {
				this.cacheVolume = getArea().multiply( getSide() );
				this.haveVolume = true;
			}
			return this.cacheVolume;
		}
		
		public void reset() 
		{
			this.haveSide = false;
			this.haveArea = false;
			this.haveVolume = false;
		}
		// ---- cacheEngine
	}
	
	
	public void testSpeed() throws Exception
	{
		Engine plainEngine = compile( PlainOutput.class );
		Engine cachingEngine = compile( CachingPlainOutput.class );
		Input input = new Input(); 

		// ---- timing
		input.setSide( "123456789123456789" );
		long plainTime = time( plainEngine, input );
		long cachingTime = time( cachingEngine, input );
		assertTrue( "Caching is at least twice as fast", cachingTime * 2 < plainTime );
		// ---- timing
	}


	private long time( Engine _engine, Input _input )
	{
		PlainOutput output = (PlainOutput) _engine.newComputation( _input );
		// ---- timed
		long startTime = System.nanoTime(); 
		output.getArea();
		output.getVolume();
		long endTime = System.nanoTime();
		return endTime - startTime;
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


	private Engine compile( Class _outputClass ) throws IOException, CompilerError, NoSuchMethodException
	{
		Spreadsheet sheet = SpreadsheetLoader.loadFromFile( path );
		Class inp = Input.class;
		Class outp = _outputClass;
		Compiler compiler = CompilerFactory.newDefaultCompiler( sheet, inp, outp, NumericType.getInstance(
				BigDecimal.class, 0, BigDecimal.ROUND_HALF_UP ) );
		Compiler.Section root = compiler.getRoot();
		root.defineInputCell( sheet.getCell( "Side" ), new CallFrame( inp.getMethod( "getSide" ) ) );
		root.defineOutputCell( sheet.getCell( "Area" ), new CallFrame( outp.getMethod( "getArea" ) ) );
		root.defineOutputCell( sheet.getCell( "Volume" ), new CallFrame( outp.getMethod( "getVolume" ) ) );
		Engine engine = compiler.compileNewEngine();
		return engine;
	}

}
