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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineLoader;
import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

public final class EnvironmentConfig extends TestCase
{
	
	
	public void testCustomLocaleTZ() throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tutorials/LocaleAndTimeZone.xls" );
		builder.setFactoryClass( MyFactory.class );
		builder.bindAllByName();
		SaveableEngine compiledEngine = builder.compile();
		assertComputations( compiledEngine );

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		compiledEngine.saveTo( out );
		byte[] bytes = out.toByteArray();
		EngineLoader.Config cfg = new EngineLoader.Config();
		Engine loadedEngine = FormulaRuntime.loadEngine( cfg, new ByteArrayInputStream( bytes ) );
		assertComputations( loadedEngine );
	}

	private void assertComputations( Engine _engine )
	{
		// ---- customLocaleTest
		assertComputation( "37287 in de", _engine, Locale.GERMAN );
		assertComputation( "37287 in fr", _engine, Locale.FRENCH );
		// ---- customLocaleTest
		assertDefaultLocaleIsPickedUp( _engine );
	}

	private void assertDefaultLocaleIsPickedUp( Engine _engine )
	{
		Locale def = Locale.getDefault();
		try {
			if (Locale.GERMAN == def) {
				// ---- defaultLocaleTest
				Locale.setDefault( Locale.FRENCH );
				assertComputation( "37287 in fr", (MyFactory) _engine.getComputationFactory() );
				// ---- defaultLocaleTest
			}
			else {
				Locale.setDefault( Locale.GERMAN );
				assertComputation( "37287 in de", (MyFactory) _engine.getComputationFactory() );
			}
		}
		finally {
			Locale.setDefault( def );
		}
	}

	private void assertComputation( String _expected, Engine _engine, final Locale _locale )
	{

		// ---- customLocaleFactory
		/**/Computation.Config config = new Computation.Config( _locale );/**/
		MyFactory factory = (MyFactory) _engine.getComputationFactory( /**/config/**/);
		// ---- customLocaleFactory

		assertComputation( _expected, factory );
	}

	private void assertComputation( String _expected, MyFactory _factory )
	{
		// ---- customLocaleUse
		MyComputation computation = _factory.newComputation( new MyInputs() );
		String actual = computation.formatted();
		// ---- customLocaleUse

		assertEquals( _expected, actual );
	}


	public static interface MyFactory
	{
		public MyComputation newComputation( MyInputs _inputs );
	}

	public static class MyInputs
	{
		public Date date()
		{
			final Calendar cal = Calendar.getInstance();
			cal.set( 2001, 12, 31 );
			return cal.getTime();
		}
	}

	public static interface MyComputation
	{
		public String formatted();
	}


}
