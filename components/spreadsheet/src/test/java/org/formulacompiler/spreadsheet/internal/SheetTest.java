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

import junit.framework.TestCase;

public class SheetTest extends TestCase
{

	public void testGetCellNameForCellIndex()
	{
		assertEquals( "A1", SheetImpl.getNameA1ForCellIndex( 0, false, 0, false ) );
		assertEquals( "A$1", SheetImpl.getNameA1ForCellIndex( 0, false, 0, true ) );
		assertEquals( "$A1", SheetImpl.getNameA1ForCellIndex( 0, true, 0, false ) );
		assertEquals( "$A$1", SheetImpl.getNameA1ForCellIndex( 0, true, 0, true ) );
		assertEquals( "B1", SheetImpl.getNameA1ForCellIndex( 1, false, 0, false ) );
		assertEquals( "Z1", SheetImpl.getNameA1ForCellIndex( 25, false, 0, false ) );
		assertEquals( "AA1", SheetImpl.getNameA1ForCellIndex( 26, false, 0, false ) );
		assertEquals( "AB1", SheetImpl.getNameA1ForCellIndex( 27, false, 0, false ) );
		assertEquals( "AZ1", SheetImpl.getNameA1ForCellIndex( 51, false, 0, false ) );
		assertEquals( "BA1", SheetImpl.getNameA1ForCellIndex( 52, false, 0, false ) );
	}

}
