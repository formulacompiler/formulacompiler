package sej.internal.spreadsheet;

import sej.SEJ;
import sej.Spreadsheet;
import sej.Spreadsheet.Row;
import sej.Spreadsheet.Sheet;
import junit.framework.TestCase;

public class SpreadsheetTest extends TestCase
{

	public void testNavigation() throws Exception
	{
		String path = "src/test-unit/testdata/sej/internal/spreadsheet/SpreadsheetTest.xls";

		Spreadsheet spreadsheet = SEJ.loadSpreadsheet( path );
		Sheet[] sheets = spreadsheet.getSheets();

		assertEquals( 1, sheets.length );

		Sheet sheet = sheets[ 0 ];

		assertSame( spreadsheet, sheet.getSpreadsheet() );

		Row[] rows = sheet.getRows();

		assertEquals( 8, rows.length );

		int[] rowLengths = new int[] { 1, 0, 1, 2, 2, 0, 1, 2 };
		for (int i = 0; i < rows.length; i++) {
			Row row = rows[ i ];

			assertEquals( rowLengths[ i ], row.getCells().length );

		}

		assertEquals( "A", (String) rows[ 3 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 10.0, ((Double) rows[ 3 ].getCells()[ 1 ].getConstantValue()).doubleValue(), 0.001 );
		assertEquals( "B", (String) rows[ 4 ].getCells()[ 0 ].getConstantValue() );
		assertEquals( 100.0, ((Double) rows[ 4 ].getCells()[ 1 ].getConstantValue()).doubleValue(), 0.001 );
	}

}
