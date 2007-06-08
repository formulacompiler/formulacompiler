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
package org.formulacompiler.spreadsheet.internal.saver.excel.xls;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.RangeRef;
import org.formulacompiler.tests.utils.AbstractSpreadsheetTestBase;


public class SaveTest extends AbstractSpreadsheetTestBase
{


	public void testNames() throws Exception
	{
		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();

		b.newCell( b.cst( 1.0 ) );
		b.nameCell( "in1" );
		CellRef a1 = b.currentCell();

		b.newCell( b.cst( 2.0 ) );
		b.nameCell( "in2" );
		CellRef b1 = b.currentCell();
		
		RangeRef ins = b.range( a1, b1 );
		b.nameRange( ins, "Inputs" );

		b.newRow();
		b.newCell( b.fun( Function.SUM, b.ref( b.range( a1, b1 ) ) ) );
		b.nameCell( "out" );
		
		Spreadsheet s = b.getSpreadsheet();
		
		byte[] saved = saveTo( s, ".xls" );
		checkSpreadsheetStream( s, new ByteArrayInputStream( saved ), ".xls" );
	}

	
	private byte[] saveTo( Spreadsheet _s, String _typeExtension ) throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();

		SpreadsheetSaver.Config cfg = new SpreadsheetSaver.Config();
		cfg.spreadsheet = _s;
		cfg.typeExtension = ".xls";
		cfg.outputStream = os;
		SpreadsheetCompiler.newSpreadsheetSaver( cfg ).save();
		return os.toByteArray();
	}


}
