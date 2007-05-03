/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.spreadsheet;

import sej.spreadsheet.SpreadsheetException;
import junit.framework.TestCase;

public class CellRangeTest extends TestCase
{
	SpreadsheetImpl workbook = new SpreadsheetImpl();
	SheetImpl sheet = new SheetImpl( this.workbook );


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		for (int iRow = 0; iRow < 10; iRow++) {
			RowImpl row = new RowImpl( this.sheet );
			for (int iCol = 0; iCol < 10; iCol++) {
				new CellWithConstant( row, 123 );
			}
		}
	}
	
	
	protected CellIndex newCellIndex( int _c, int _r )
	{
		return new CellIndex( this.workbook, 0, _c, _r );
	}


	public void testRangeIterator()
	{
		assertIteration( 2, 2, 5, 5, "C3D3E3F3C4D4E4F4C5D5E5F5C6D6E6F6" );
		assertIteration( 0, 0, 1, 1, "A1B1A2B2" );
		assertIteration( 0, 0, 0, 1, "A1A2" );
		assertIteration( 0, 0, 1, 0, "A1B1" );
		assertIteration( 8, 8, 10, 10, "I9J9__I10J10________" );
		assertIteration( 2, 2, 2, 2, "C3" );
	}


	private void assertIteration( int _fromCol, int _fromRow, int _toCol, int _toRow, String _expected )
	{
		CellIndex start = newCellIndex( _fromCol, _fromRow );
		CellIndex end = newCellIndex( _toCol, _toRow );
		CellRange range = new CellRange( start, end );
		StringBuilder cells = new StringBuilder();
		for (CellIndex ix : range) {
			CellInstance cell = ix.getCell();
			if (null != cell) cells.append( cell.getCanonicalName() );
			else cells.append( "__" );
		}
		assertEquals( _expected, cells.toString() );
	}


	public void testCellIndexRelativeTo() throws Exception
	{
		{
			CellRange rng = new CellRange( newCellIndex( 3, 3 ), newCellIndex( 3, 5 ) );

			assertEquals( "D4", rng.getCellIndexRelativeTo( newCellIndex( 2, 3 ) ).toString() );
			assertEquals( "D5", rng.getCellIndexRelativeTo( newCellIndex( 2, 4 ) ).toString() );
			assertEquals( "D6", rng.getCellIndexRelativeTo( newCellIndex( 2, 5 ) ).toString() );
			assertEquals( "D6", rng.getCellIndexRelativeTo( newCellIndex( 0, 5 ) ).toString() );
		}

		{
			CellRange rng = new CellRange( newCellIndex( 3, 3 ), newCellIndex( 5, 3 ) );

			assertEquals( "D4", rng.getCellIndexRelativeTo( newCellIndex( 3, 1 ) ).toString() );
			assertEquals( "E4", rng.getCellIndexRelativeTo( newCellIndex( 4, 1 ) ).toString() );
			assertEquals( "F4", rng.getCellIndexRelativeTo( newCellIndex( 5, 1 ) ).toString() );
			assertEquals( "F4", rng.getCellIndexRelativeTo( newCellIndex( 5, 2 ) ).toString() );
		}

		{
			CellRange rng = new CellRange( newCellIndex( 3, 3 ), newCellIndex( 5, 5 ) );
			try {
				rng.getCellIndexRelativeTo( newCellIndex( 3, 1 ) );
				fail();
			}
			catch (SpreadsheetException.CellRangeNotUniDimensional e) {
				// expected
			}
		}
	}


}
