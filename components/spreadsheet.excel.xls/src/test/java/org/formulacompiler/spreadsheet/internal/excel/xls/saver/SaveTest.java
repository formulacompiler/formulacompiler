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

package org.formulacompiler.spreadsheet.internal.excel.xls.saver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.RangeRef;
import org.formulacompiler.tests.utils.AbstractSpreadsheetTestCase;


public class SaveTest extends AbstractSpreadsheetTestCase
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
