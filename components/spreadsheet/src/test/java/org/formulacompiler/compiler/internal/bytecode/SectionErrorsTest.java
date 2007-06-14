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
package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;

import junit.framework.TestCase;

public class SectionErrorsTest extends TestCase
{


	public void testOuterCellReferencingInnerCellDirectly() throws Exception
	{
		SpreadsheetBuilder sb = SpreadsheetCompiler.newSpreadsheetBuilder();
		sb.newCell( sb.cst( 1 ) );
		CellRef sectionCell = sb.currentCell();
		sb.nameCell( "Inner" );
		sb.nameRange( sb.range( sectionCell, sectionCell ), "Section" );
		sb.newRow();
		sb.newCell( sb.ref( sectionCell ) );
		sb.nameCell( "Outer" );
		Spreadsheet s = sb.getSpreadsheet();

		EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
		eb.setSpreadsheet( s );
		eb.setInputClass( MyInputs.class );
		eb.setOutputClass( MyOutputs.class );
		Section rb = eb.getRootBinder();
		rb.defineOutputCell( s.getCell( "Outer" ), new CallFrame( MyOutputs.class.getMethod( "result" ) ) );
		Section db = rb.defineRepeatingSection( s.getRange( "Section" ), Orientation.VERTICAL, new CallFrame(
				MyInputs.class.getMethod( "details" ) ), MyInputs.class, null, null );
		db.defineInputCell( s.getCell( "Inner" ), new CallFrame( MyInputs.class.getMethod( "value" ) ) );

		try {
			eb.compile();
			fail();
		}
		catch (CompilerException.ReferenceToInnerCellNotAggregated e) {
			// OK
		}
	}


	public static interface MyInputs
	{
		double value();
		MyInputs[] details();
	}

	public static interface MyOutputs extends Resettable
	{
		double result();
	}

}