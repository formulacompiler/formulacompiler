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
package org.formulacompiler.spreadsheet.internal.loader.excel.xls;

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
