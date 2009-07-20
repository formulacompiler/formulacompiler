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

package org.formulacompiler.spreadsheet.internal.odf.saver;

import java.io.File;

import org.formulacompiler.compiler.Operator;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.internal.odf.AbstractOdsVerifyingTestCase;

public class BuildSaveTest extends AbstractOdsVerifyingTestCase
{
	private static final File TEST_FILES_DIR = new File( "src/test/data/BuildSaveTest" );

	@Override
	protected File getDataDirectory()
	{
		return TEST_FILES_DIR;
	}

	public void testStylesWithTemplate() throws Exception
	{
		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();

		b.newCell( b.cst( 1.0 ) );
		b.styleCell( "Input" );
		SpreadsheetBuilder.CellRef a1 = b.currentCell();

		b.newCell( b.cst( 2.0 ) );
		b.styleCell( "Input" );
		SpreadsheetBuilder.CellRef b1 = b.currentCell();

		b.newRow();
		b.newCell( b.op( Operator.PLUS, b.ref( a1 ), b.ref( b1 ) ) );
		b.styleCell( "Output" );

		this.spreadsheet = b.getSpreadsheet();
	}

	public void testStylesWithoutTemplate() throws Exception
	{
		SpreadsheetBuilder b = SpreadsheetCompiler.newSpreadsheetBuilder();

		b.newCell( b.cst( 1.0 ) );
		b.styleCell( "Input" );
		SpreadsheetBuilder.CellRef a1 = b.currentCell();

		b.newCell( b.cst( 2.0 ) );
		b.styleCell( "Input" );
		SpreadsheetBuilder.CellRef b1 = b.currentCell();

		b.newRow();
		b.newCell( b.op( Operator.PLUS, b.ref( a1 ), b.ref( b1 ) ) );
		b.styleCell( "Output" );

		this.spreadsheet = b.getSpreadsheet();
	}
}
