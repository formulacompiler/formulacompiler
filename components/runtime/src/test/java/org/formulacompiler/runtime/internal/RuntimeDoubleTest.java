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

package org.formulacompiler.runtime.internal;

import junit.framework.TestCase;

public class RuntimeDoubleTest extends TestCase
{


	public void testRound()
	{
		assertEquals( 1.22, RuntimeDouble_v2.round( 1.224, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.225, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.229, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.230, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.234, 2 ) );
		assertEquals( 1.24, RuntimeDouble_v2.round( 1.235, 2 ) );
		assertEquals( 1.24, RuntimeDouble_v2.round( 1.239999, 2 ) );

		assertEquals( 1.2, RuntimeDouble_v2.round( 1.234, 1 ) );
		assertEquals( 1.3, RuntimeDouble_v2.round( 1.25, 1 ) );

		assertEquals( 1.0, RuntimeDouble_v2.round( 1.4, 0 ) );
		assertEquals( 2.0, RuntimeDouble_v2.round( 1.5, 0 ) );

		assertEquals( 10.0, RuntimeDouble_v2.round( 14, -1 ) );
		assertEquals( 20.0, RuntimeDouble_v2.round( 15, -1 ) );

		assertEquals( -12.01, RuntimeDouble_v2.round( -12.005, 2 ) );
	}


}
