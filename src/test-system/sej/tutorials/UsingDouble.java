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
import sej.runtime.Engine;
import junit.framework.TestCase;

public class UsingDouble extends TestCase
{
	
	public void testUsingDouble() throws Exception
	{
		String path = "src/test-system/testdata/sej/tutorials/UsingNumericTypes.xls";
		
		// ---- buildCompiler
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( Factory.class );
		/**/builder.setNumericType( SEJ.DOUBLE );/**/
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();
		// ---- buildCompiler

		// ---- checkResult
		Output output = factory.newInstance( new Input() );
		assertEquals( /**/"1.1666666666666667"/**/, String.valueOf( output.getResult()) );
		// ---- checkResult
	}


	// ---- IO
	public static class Input
	{
		public /**/double/**/ getA() { return 1.0; }
		public /**/double/**/ getB() { return 6.0; }
	}
	
	public static interface Output
	{
		/**/double/**/ getResult();
	}

	public static interface Factory
	{
		Output newInstance( Input _input );
	}
	// ---- IO

}
