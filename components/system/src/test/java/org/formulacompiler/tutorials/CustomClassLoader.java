/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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
import java.io.File;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineLoader;
import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormat;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith( MultiFormat.class )
public class CustomClassLoader
{

	private final String spreadsheetExtension;

	public CustomClassLoader( final String _spreadsheetExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	@Test
	public void testCustomClassLoader() throws Exception
	{
		MyClassLoader myClassLoader = new MyClassLoader();

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		final String path = "src/test/data/org/formulacompiler/tutorials/CustomClassLoader" + getSpreadsheetExtension();
		builder.loadSpreadsheet( new File( path ) );
		builder.setFactoryClass( MyFactory.class );
		builder.bindAllByName();
		// ---- compile
		builder./**/setParentClassLoaderForEngine/**/( myClassLoader );
		SaveableEngine compiledEngine = builder.compile();
		ComputationFactory compiledFactory = compiledEngine.getComputationFactory();
		assertSame( myClassLoader, compiledFactory.getClass().getClassLoader().getParent() );
		// ---- compile

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		compiledEngine.saveTo( out );
		byte[] bytes = out.toByteArray();

		// ---- load
		EngineLoader.Config cfg = new EngineLoader.Config();
		cfg./**/parentClassLoader/**/ = myClassLoader;
		Engine loadedEngine = FormulaRuntime.loadEngine( cfg, new ByteArrayInputStream( bytes ) );
		ComputationFactory loadedFactory = loadedEngine.getComputationFactory();
		assertSame( myClassLoader, loadedFactory.getClass().getClassLoader().getParent() );
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
