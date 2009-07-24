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

package org.formulacompiler.compiler.internal;

import org.formulacompiler.compiler.NumericType;

import junit.framework.TestCase;

public abstract class AbstractNumericTypeTest extends TestCase
{

	protected abstract NumericType getType();


	public void testConstantsToString() throws Exception
	{
		assertEquals( "", getType().valueToString( null ) );
		assertEquals( "", getType().valueToConciseString( null ) );
		assertEquals( "0", getType().valueToConciseString( getType().getZero() ) );
		assertEquals( "1", getType().valueToConciseString( getType().getOne() ) );
	}

	public void testConstantsFromString() throws Exception
	{
		assertEquals( getType().getZero(), getType().valueOf( (String) null ) );
		assertEquals( getType().getZero(), getType().valueOf( "" ) );
		assertEquals( getType().getZero(), getType().valueOf( "0" ) );
		assertEquals( getType().getOne(), getType().valueOf( "1" ) );
	}


	@SuppressWarnings( "unchecked" )
	protected static void assertEquals( Number _a, Number _b )
	{
		Comparable a = (Comparable) _a;
		Comparable b = (Comparable) _b;
		assertEquals( 0, a.compareTo( b ) );
	}

}
