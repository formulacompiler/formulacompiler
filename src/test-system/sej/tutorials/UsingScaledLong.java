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

import sej.EngineBuilder;
import sej.SEJ;
import sej.runtime.ComputationFactory;
import sej.runtime.Engine;
import sej.runtime.ScaledLong;
import junit.framework.TestCase;

public class UsingScaledLong extends TestCase
{
	private static final String PATH = "src/test-system/testdata/sej/tutorials/UsingNumericTypes.xls";


	public void testUsingScaledLong() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		// ---- buildCompiler
		builder.setNumericType( /**/SEJ.getNumericType( Long.TYPE, 3 )/**/ );
		// ---- buildCompiler
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();

		// ---- checkResult
		Output output = factory.newInstance( new Input( 6 ) );
		assertEquals( /**/1166L/**/, output.getResult() );
		// ---- checkResult
	}


	public void testUsingScaledLong4() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output4.class );
		// ---- buildCompiler4
		builder.setNumericType( /**/SEJ.LONG4/**/ );
		// ---- buildCompiler4
		builder.bindAllByName();
		Engine engine = builder.compile();
		ComputationFactory factory = engine.getComputationFactory();

		// ---- checkResult4
		Output4 output = (Output4) factory.newInstance( new Input4( 6 ) );
		assertEquals( /**/11666L/**/, output.getResult() );
		// ---- checkResult4
	}


	public void testUsingScaledLong0() throws Exception
	{
		String path = PATH;

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output0.class );
		// ---- buildCompiler0
		builder.setNumericType( /**/SEJ.LONG/**/ );
		// ---- buildCompiler0
		builder.bindAllByName();
		Engine engine = builder.compile();
		ComputationFactory factory = engine.getComputationFactory();

		// ---- checkResult0
		Output0 output = (Output0) factory.newInstance( new Input0( 6 ) );
		assertEquals( /**/1L/**/, output.getResult() );
		// ---- checkResult0

	}


	// ---- IO
	public static class Input
	{
		public Input(int b)  { this.b = b; }
		public /**/long/**/ getA()  { return 1; } // will be scaled by SEJ 
		public /**/@ScaledLong(3) long/**/ getB()  { return this.b * ScaledLong.ONE[ 3 ]; }
		private final int b;
	}

	/**/@ScaledLong(3)/**/
	public static interface Output
	{
		/**/long/**/ getResult();
	}
	// ---- IO

	public static interface Factory
	{
		Output newInstance( Input _input );
	}
	
	
	/**/@ScaledLong(4)/**/
	public static class Input4 extends Input
	{
		public Input4(int _b)
		{
			super( _b );
		}
	}

	/**/@ScaledLong(4)/**/
	public static interface Output4
	{
		/**/long/**/ getResult();
	}

	
	/**/@ScaledLong(0)/**/
	public static class Input0 extends Input
	{
		public Input0(int _b)
		{
			super( _b );
		}
	}

	/**/@ScaledLong(0)/**/
	public static interface Output0
	{
		/**/long/**/ getResult();
	}

}
