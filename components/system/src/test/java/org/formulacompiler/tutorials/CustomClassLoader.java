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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineLoader;
import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class CustomClassLoader extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testCustomClassLoader() throws Exception
	{
		MyClassLoader myClassLoader = new MyClassLoader();

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		final String path = "src/test/data/org/formulacompiler/tutorials/CustomClassLoader" + getSpreadsheetExtension();
		builder.loadSpreadsheet( path );
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
		Engine loadedEngine = FormulaRuntime.loadEngine( cfg, new ByteArrayInputStream( bytes ) );
		assertSame( myClassLoader, ((ClassLoader) loadedEngine).getParent() );
		// ---- load
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( CustomClassLoader.class );
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
