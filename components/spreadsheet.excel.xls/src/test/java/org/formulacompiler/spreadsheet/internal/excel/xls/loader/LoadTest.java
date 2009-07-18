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

package org.formulacompiler.spreadsheet.internal.excel.xls.loader;

import java.io.File;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;
import org.formulacompiler.spreadsheet.Spreadsheet.Sheet;
import org.formulacompiler.tests.utils.AbstractSpreadsheetTestCase;

public class LoadTest extends AbstractSpreadsheetTestCase
{

	public void testNavigation() throws Exception
	{
		final File path = new File( "src/test/data" );
		final String base = "LoadTest";
		final String ext = ".xls";
		final String name = base + ext;

		Spreadsheet spreadsheet = SpreadsheetCompiler.loadSpreadsheet( new File( path, name ) );
		Sheet[] sheets = spreadsheet.getSheets();
		assertEquals( 1, sheets.length );

		Sheet sheet = sheets[ 0 ];
		assertSame( spreadsheet, sheet.getSpreadsheet() );

		Row[] rows = sheet.getRows();
		assertEquals( 8, rows.length );

		int[] rowLengths = { 1, 0, 1, 2, 2, 0, 1, 2 };
		for (int i = 0; i < rows.length; i++) {
			Row row = rows[ i ];
			assertEquals( rowLengths[ i ], row.getCells().length );
		}

		assertEquals( "A", (String) rows[ 3 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 10.0, ((Double) rows[ 3 ].getCells()[ 1 ].getConstantValue()).doubleValue(), 0.001 );
		assertEquals( "B", (String) rows[ 4 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 100.0, ((Double) rows[ 4 ].getCells()[ 1 ].getConstantValue()).doubleValue(), 0.001 );

		assertYaml( path, base, spreadsheet, name );
	}

}
