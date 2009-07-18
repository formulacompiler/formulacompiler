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

package org.formulacompiler.spreadsheet.internal;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetException;

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
		assertIteration( 8, 8, 10, 10, "I9J9K9I10J10K10I11J11K11" );
		assertIteration( 2, 2, 2, 2, "C3" );
	}


	public void testShortName()
	{
		CellIndex start = new CellIndex( this.workbook, 0, 0, true, 0, true );
		CellIndex end = new CellIndex( this.workbook, 0, 1, true, 1, true );
		CellRange range = CellRange.getCellRange( start, end );
		assertEquals( "Sheet1!$A$1:$B$2", range.toString() );
		assertEquals( "Sheet1!A1:B2", range.getShortName() );
	}


	private void assertIteration( int _fromCol, int _fromRow, int _toCol, int _toRow, String _expected )
	{
		CellIndex start = newCellIndex( _fromCol, _fromRow );
		CellIndex end = newCellIndex( _toCol, _toRow );
		CellRange range = CellRange.getCellRange( start, end );
		DescriptionBuilder cells = new DescriptionBuilder();
		cells.pushContext( this.workbook.getSheetList().get( 0 ) );
		for (CellIndex ix : range) {
			ix.describeTo( cells );
		}
		assertEquals( _expected, cells.toString() );
	}


	public void testCellIndexRelativeTo() throws Exception
	{
		{
			final CellRange rng = CellRange.getCellRange( newCellIndex( 3, 3 ), newCellIndex( 3, 5 ) );

			assertEquals( "Sheet1!D4", rng.getCellIndexRelativeTo( newCellIndex( 2, 3 ) ).toString() );
			assertEquals( "Sheet1!D5", rng.getCellIndexRelativeTo( newCellIndex( 2, 4 ) ).toString() );
			assertEquals( "Sheet1!D6", rng.getCellIndexRelativeTo( newCellIndex( 2, 5 ) ).toString() );
			assertEquals( "Sheet1!D6", rng.getCellIndexRelativeTo( newCellIndex( 0, 5 ) ).toString() );
		}

		{
			final CellRange rng = CellRange.getCellRange( newCellIndex( 3, 3 ), newCellIndex( 5, 3 ) );

			assertEquals( "Sheet1!D4", rng.getCellIndexRelativeTo( newCellIndex( 3, 1 ) ).toString() );
			assertEquals( "Sheet1!E4", rng.getCellIndexRelativeTo( newCellIndex( 4, 1 ) ).toString() );
			assertEquals( "Sheet1!F4", rng.getCellIndexRelativeTo( newCellIndex( 5, 1 ) ).toString() );
			assertEquals( "Sheet1!F4", rng.getCellIndexRelativeTo( newCellIndex( 5, 2 ) ).toString() );
		}

		{
			final CellRange rng = CellRange.getCellRange( newCellIndex( 3, 3 ), newCellIndex( 5, 5 ) );
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
