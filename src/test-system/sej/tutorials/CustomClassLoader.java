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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.runtime.Engine;
import sej.runtime.EngineLoader;
import sej.runtime.SEJRuntime;
import junit.framework.TestCase;

public class CustomClassLoader extends TestCase
{

	public void testCustomClassLoader() throws Exception
	{
		MyClassLoader myClassLoader = new MyClassLoader();

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( "src/test-system/testdata/sej/tutorials/CustomClassLoader.xls" );
		builder.setFactoryClass( MyFactory.class );
		builder.bindAllByName();
		// ---- compile
		builder./**/setParentClassLoaderForEngine/**/( myClassLoader );
		SaveableEngine compiledEngine = builder.compile();
		assertSame( myClassLoader, ((ClassLoader) compiledEngine).getParent() );
		// ---- compile

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		compiledEngine.saveTo( out );
		byte[] bytes = out.toByteArray();

		// ---- load
		EngineLoader.Config cfg = new EngineLoader.Config();
		cfg./**/parentClassLoader/**/ = myClassLoader;
		Engine loadedEngine = SEJRuntime.loadEngine( cfg, new ByteArrayInputStream( bytes ) );
		assertSame( myClassLoader, ((ClassLoader) loadedEngine).getParent() );
		// ---- load
	}

	public static class MyClassLoader extends ClassLoader
	{
		// pure dummy
	}

	public static interface MyFactory
	{
		public MyComputation newComputation( MyInputs _inputs );
	}

	public static class MyInputs
	{
		public int value()
		{
			return 1;
		}
	}

	public static interface MyComputation
	{
		public int result();
	}


}
