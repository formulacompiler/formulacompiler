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
package org.formulacompiler.spreadsheet.internal.loader.odf;

import org.formulacompiler.compiler.internal.LocalExcelDate;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;
import org.formulacompiler.spreadsheet.Spreadsheet.Sheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

public class LoadTest extends TestCase
{

	public void testNavigation() throws Exception
	{
		String path = "src/test/data/LoadTest.ods";
		Spreadsheet spreadsheet = SpreadsheetCompiler.loadSpreadsheet( path );

		Sheet[] sheets = spreadsheet.getSheets();
		assertEquals( 2, sheets.length );

		for (Sheet sheet : sheets) {
			assertSame( spreadsheet, sheet.getSpreadsheet() );
		}

		assertEquals( "TestSheet", sheets[ 0 ].getName() );
		assertEquals( "Has space", sheets[ 1 ].getName() );

		testConstantValues( sheets[ 0 ] );
		testCoveredCells( sheets[ 1 ] );
	}

	private void testConstantValues( final Sheet _sheet )
	{
		final Row[] rows = _sheet.getRows();
		assertEquals( 21, rows.length );

		for (Row row : rows) {
			assertSame( _sheet, row.getSheet() );
		}

		int[] rowLengths = { 1, 3, 0, 0, 0, 3, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2 };
		for (int i = 0; i < rows.length; i++) {
			final Row row = rows[ i ];
			assertRowLength( i, rowLengths[ i ], row );
		}

		assertEquals( "a", (String) rows[ 0 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( "b", (String) rows[ 1 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( "b", (String) rows[ 1 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( "b", (String) rows[ 1 ].getCells()[ 2 ].getConstantValue() );
		assertEquals( "c", (String) rows[ 5 ].getCells()[ 2 ].getConstantValue() );

		assertEquals( "Text", (String) rows[ 7 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( "\u0422\u0435\u043a\u0441\u0442", (String) rows[ 8 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 10, ((Double) rows[ 9 ].getCells()[ 0 ].getConstantValue()).doubleValue(), 0.000000000000001 );
		assertEquals( 3.14159, ((Double) rows[ 10 ].getCells()[ 0 ].getConstantValue()).doubleValue(), 0.000000000000001 );
		assertEquals( 100, ((Double) rows[ 11 ].getCells()[ 0 ].getConstantValue()).doubleValue(), 0.000000000000001 );
		assertEquals( 50, ((Double) rows[ 12 ].getCells()[ 0 ].getConstantValue()).doubleValue(), 0.000000000000001 );
		assertEquals( 0.2312, ((Double) rows[ 13 ].getCells()[ 0 ].getConstantValue()).doubleValue(), 0.000000000000001 );
		assertEquals( true, ((Boolean) rows[ 14 ].getCells()[ 0 ].getConstantValue()).booleanValue() );
		assertEquals( false, ((Boolean) rows[ 15 ].getCells()[ 0 ].getConstantValue()).booleanValue() );
		assertEquals( ((Double) rows[ 16 ].getCells()[ 1 ].getConstantValue()).doubleValue(), ((LocalExcelDate) rows[ 16 ]
				.getCells()[ 0 ].getConstantValue()).value(), 0.000000000000001 );
		assertEquals( ((Double) rows[ 17 ].getCells()[ 1 ].getConstantValue()).doubleValue(), ((LocalExcelDate) rows[ 17 ]
				.getCells()[ 0 ].getConstantValue()).value(), 0.0000000001 );
		assertEquals( ((Double) rows[ 18 ].getCells()[ 1 ].getConstantValue()).doubleValue(), ((LocalExcelDate) rows[ 18 ]
				.getCells()[ 0 ].getConstantValue()).value(), 0.0000000000001 );
		assertEquals( ((Double) rows[ 19 ].getCells()[ 1 ].getConstantValue()).doubleValue(), ((LocalExcelDate) rows[ 19 ]
				.getCells()[ 0 ].getConstantValue()).value(), 0.000000000000001 );
		assertEquals( ((Double) rows[ 20 ].getCells()[ 1 ].getConstantValue()).doubleValue(), ((LocalExcelDate) rows[ 20 ]
				.getCells()[ 0 ].getConstantValue()).value(), 0.000000000000001 );
	}

	private void testCoveredCells( final Sheet _sheet )
	{
		final Row[] rows = _sheet.getRows();
		assertEquals( 7, rows.length );

		for (Row row : rows) {
			assertSame( _sheet, row.getSheet() );
		}

		for (int rowNum = 0; rowNum < rows.length; rowNum++) {
			final Row row = rows[ rowNum ];
			assertRowLength( rowNum, 3, row );
			final Cell[] cells = row.getCells();
			for (int colNum = 0; colNum < cells.length; colNum++) {
				final Cell cell = cells[ colNum ];
				if (rowNum == 1 && colNum == 1) {
					assertNull( cell.getConstantValue() );
				}
				else {
					final String expectedCellValue = "" + (char) ('A' + colNum) + (rowNum + 1);
					assertEquals( expectedCellValue, cell.getConstantValue() );
				}
			}
		}
	}

	private static void assertRowLength( final int _rowIndex, final int _expectedLength, final Row _row )
	{
		assertEquals( "Row[" + _rowIndex + "]:", _expectedLength, _row.getCells().length );
	}

}
