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

import java.io.File;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.tests.utils.AbstractStandardInputsOutputsTestCase;


public class Decompilation extends AbstractStandardInputsOutputsTestCase
{


	public void testAsString() throws Exception
	{
		EngineBuilder builder = makeBuilder();
		// ---- asString
		// ... set up engine definition
		SaveableEngine engine = builder.compile();
		ByteCodeEngineSource source = FormulaDecompiler.decompile( engine );
		String text = source.toString();
		// ---- asString
		assertEqualToFile( "src/test/data/org/formulacompiler/tutorials/decompiler/asString.txt", text );
	}


	public void testSaveTo() throws Exception
	{
		final String pathToTargetFolder = "temp/test/decompiled/test/std";

		EngineBuilder builder = makeBuilder();
		// ---- saveTo
		// ... set up engine definition
		SaveableEngine engine = builder.compile();
		ByteCodeEngineSource source = FormulaDecompiler.decompile( engine );
		source.saveTo( new File( pathToTargetFolder ) );
		// ---- saveTo
		File tgt = new File( pathToTargetFolder );
		File src = new File( "src/test/data/org/formulacompiler/tutorials/decompiler/std" );
		assertGeneratedFile( src, tgt, "org/formulacompiler/gen/$Factory.java" );
		assertGeneratedFile( src, tgt, "org/formulacompiler/gen/$Root.java" );
	}


	public void testSaveToReadableCode() throws Exception
	{
		final String pathToTargetFolder = "temp/test/decompiled/test/readable";

		EngineBuilder builder = makeBuilder();
		// ---- saveReadable
		// ... set up engine definition
		builder./**/setCompileToReadableCode/**/( true );
		SaveableEngine engine = builder.compile();
		ByteCodeEngineSource source = FormulaDecompiler.decompile( engine );
		source.saveTo( new File( pathToTargetFolder ) );
		// ---- saveReadable
		File tgt = new File( pathToTargetFolder );
		File src = new File( "src/test/data/org/formulacompiler/tutorials/decompiler/readable" );
		assertGeneratedFile( src, tgt, "org/formulacompiler/gen/$Factory.java" );
		assertGeneratedFile( src, tgt, "org/formulacompiler/gen/$Root.java" );
	}


	private void assertGeneratedFile( File _src, File _tgt, String _class ) throws Exception
	{
		assertEqualTextFiles( new File( _src, _class ), new File( _tgt, _class ) );
	}


	private EngineBuilder makeBuilder() throws Exception, CompilerException
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setSpreadsheet( makeSpreadsheet() );
		builder.setFactoryClass( MyFactory.class );
		builder.bindAllByName();
		return builder;
	}


	private Spreadsheet makeSpreadsheet() throws Exception
	{
		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();
		SpreadsheetBuilder.CellRef cr, ar;

		// ---- makeSheet
		b.newCell( b.cst( 0.1 ) );
		b.nameCell( "CustomerRebate" );
		cr = b.currentCell();

		b.newRow();
		b.newCell( b.cst( 0.05 ) );
		b.nameCell( "ArticleRebate" );
		ar = b.currentCell();

		b.newRow();
		b.newRow();
		b.newCell( b.fun( Function.MAX, b.ref( cr ), b.ref( ar ) ) );
		b.nameCell( "Rebate" );
		// ---- makeSheet

		return b.getSpreadsheet();
	}


	public static final class MyInputs
	{
		public double customerRebate()
		{
			return 0.2;
		}
		public double articleRebate()
		{
			return 0.04;
		}
	}

	public static interface MyFactory
	{
		public MyOutputs newOutputs( MyInputs _inputs );
	}

	public static interface MyOutputs
	{
		public double rebate();
	}


}
