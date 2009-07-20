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

package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
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
		rb.defineOutputCell( s.getCell( "Outer" ), FormulaCompiler.newCallFrame( MyOutputs.class.getMethod( "result" ) ) );
		Section db = rb.defineRepeatingSection( s.getRange( "Section" ), Orientation.VERTICAL, FormulaCompiler
				.newCallFrame( MyInputs.class.getMethod( "details" ) ), MyInputs.class, null, null );
		db.defineInputCell( s.getCell( "Inner" ), FormulaCompiler.newCallFrame( MyInputs.class.getMethod( "value" ) ) );

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
