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

package org.formulacompiler.compiler.internal.expressions;

import junit.framework.TestCase;

public class ExpressionNodeForArrayReferenceTest extends TestCase
{
	private ExpressionNodeForArrayReference array;


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		final int nRow = 4;
		final int nCol = 5;
		this.array = new ExpressionNodeForArrayReference( new ArrayDescriptor( 1, nRow, nCol ) );
		for (int iRow = 1; iRow <= nRow; iRow++) {
			for (int iCol = 1; iCol <= nCol; iCol++) {
				this.array.addArgument( new ExpressionNodeForConstantValue( Integer.valueOf( iRow * 10 + iCol ) ) );
			}
		}
	}


	public void testSubArray()
	{
		assertEquals( "#(1,4,5){11, 12, 13, 14, 15, 21, 22, 23, 24, 25, 31, 32, 33, 34, 35, 41, 42, 43, 44, 45}",
				this.array.subArray( 0, 4, 0, 5 ).toString() );

		assertEquals( "#(1,1,5){11, 12, 13, 14, 15}", this.array.subArray( 0, 1, 0, 5 ).toString() );
		assertEquals( "#(1,1,5){31, 32, 33, 34, 35}", this.array.subArray( 2, 1, 0, 5 ).toString() );

		assertEquals( "#(1,4,1){11, 21, 31, 41}", this.array.subArray( 0, 4, 0, 1 ).toString() );
		assertEquals( "#(1,4,1){13, 23, 33, 43}", this.array.subArray( 0, 4, 2, 1 ).toString() );

		assertEquals( "#(1,2,2){23, 24, 33, 34}", this.array.subArray( 1, 2, 2, 2 ).toString() );
	}

}
