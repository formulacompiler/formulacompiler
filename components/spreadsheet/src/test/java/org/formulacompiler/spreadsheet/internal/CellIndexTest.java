/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

import junit.framework.TestCase;

public class CellIndexTest extends TestCase
{

	public void testGetCellNameForCellIndex()
	{
		final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
		SheetImpl sheet1 = new SheetImpl( spreadsheet );
		SheetImpl sheet2 = new SheetImpl( spreadsheet );
		assertEquals( "A1", getNameA1( sheet1, 0, false, 0, false, sheet1 ) );
		assertEquals( "Sheet1!A1", getNameA1( sheet1, 0, false, 0, false, sheet2 ) );
		assertEquals( "Sheet2!A1", getNameA1( sheet2, 0, false, 0, false, sheet1 ) );
		assertEquals( "A$1", getNameA1( sheet1, 0, false, 0, true, sheet1 ) );
		assertEquals( "$A1", getNameA1( sheet1, 0, true, 0, false, sheet1 ) );
		assertEquals( "$A$1", getNameA1( sheet1, 0, true, 0, true, sheet1 ) );
		assertEquals( "B1", getNameA1( sheet1, 1, false, 0, false, sheet1 ) );
		assertEquals( "Z1", getNameA1( sheet1, 25, false, 0, false, sheet1 ) );
		assertEquals( "AA1", getNameA1( sheet1, 26, false, 0, false, sheet1 ) );
		assertEquals( "AB1", getNameA1( sheet1, 27, false, 0, false, sheet1 ) );
		assertEquals( "AZ1", getNameA1( sheet1, 51, false, 0, false, sheet1 ) );
		assertEquals( "BA1", getNameA1( sheet1, 52, false, 0, false, sheet1 ) );
		assertEquals( "ZZ1", getNameA1( sheet1, 701, false, 0, false, sheet1 ) );
		assertEquals( "AAA1", getNameA1( sheet1, 702, false, 0, false, sheet1 ) );
		assertEquals( "XFD1", getNameA1( sheet1, 16383, false, 0, false, sheet1 ) );
	}

	private String getNameA1( SheetImpl _sheet, int _col, boolean _colAbs, int _row, boolean _rowAbs,
			SheetImpl _contextSheet )
	{
		final CellIndex cellIndex = new CellIndex( _sheet.getSpreadsheet(), _sheet.getSheetIndex(), _col, _colAbs, _row, _rowAbs );
		final DescriptionBuilder builder = new DescriptionBuilder();
		builder.pushContext( _contextSheet );
		cellIndex.describeTo( builder );
		return builder.toString();
	}

}
