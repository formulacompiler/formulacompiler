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

import java.io.File;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.tests.utils.AbstractTestBase;


public class Decompilation extends AbstractTestBase
{

	public void testAsString() throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setSpreadsheet( makeSpreadsheet() );
		builder.setInputClass( Inputs.class );
		builder.setOutputClass( Outputs.class );
		builder.bindAllByName();
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
		final String pathToTargetFolder = "temp/test/decompiled/test";
		
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setSpreadsheet( makeSpreadsheet() );
		builder.setInputClass( Inputs.class );
		builder.setOutputClass( Outputs.class );
		builder.bindAllByName();
		// ---- saveTo
		// ... set up engine definition
		SaveableEngine engine = builder.compile();
		ByteCodeEngineSource source = FormulaDecompiler.decompile( engine );
		source.saveTo( new File( pathToTargetFolder ) );
		// ---- saveTo
		File tgt = new File( pathToTargetFolder );
		File src = new File( "src/test/data/org/formulacompiler/tutorials/decompiler" );
		assertGeneratedFile( src, tgt, "org/formulacompiler/gen/$Factory.java" );
		assertGeneratedFile( src, tgt, "org/formulacompiler/gen/$Root.java" );
	}


	private void assertGeneratedFile( File _src, File _tgt, String _class ) throws Exception
	{
		assertEqualTextFiles( new File( _src, _class ), new File( _tgt, _class ) );
	}


	private Spreadsheet makeSpreadsheet() throws Exception
	{
		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();
		SpreadsheetBuilder.CellRef cr, ar;

		b.newCell( b.cst( "CustomerRebate" ) );
		b.newCell( b.cst( 0.1 ) );
		b.nameCell( "CustomerRebate" );
		cr = b.currentCell();

		b.newRow();
		b.newCell( b.cst( "ArticleRebate" ) );
		b.newCell( b.cst( 0.05 ) );
		b.nameCell( "ArticleRebate" );
		ar = b.currentCell();

		b.newRow();
		b.newRow();
		b.newCell( b.cst( "Rebates" ) );
		b.newCell( b.fun( Function.MAX, b.ref( cr ), b.ref( ar ) ) );
		b.nameCell( "RebateOp" );

		return b.getSpreadsheet();
	}


	public static final class Inputs
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


	public static interface Outputs
	{
		public double rebateOp();
	}


}