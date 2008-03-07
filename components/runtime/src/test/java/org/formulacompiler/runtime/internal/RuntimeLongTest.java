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

public class RuntimeLongTest extends TestCase
{

	public void testFromDouble()
	{
		final RuntimeLong_v2.Context context2 = new RuntimeLong_v2.Context( 2 );

		assertEquals( -2, RuntimeLong_v2.fromDouble( -0.0150001, context2 ) );
		assertEquals( -1, RuntimeLong_v2.fromDouble( -0.0149999, context2 ) );
		assertEquals( -1, RuntimeLong_v2.fromDouble( -0.0050001, context2 ) );
		assertEquals( 0, RuntimeLong_v2.fromDouble( -0.0049999, context2 ) );
		assertEquals( 0, RuntimeLong_v2.fromDouble( 0.0049999, context2 ) );
		assertEquals( 1, RuntimeLong_v2.fromDouble( 0.0050001, context2 ) );
		assertEquals( 1, RuntimeLong_v2.fromDouble( 0.0149999, context2 ) );
		assertEquals( 2, RuntimeLong_v2.fromDouble( 0.0150001, context2 ) );

		final RuntimeLong_v2.Context context0 = new RuntimeLong_v2.Context( 0 );

		assertEquals( -2, RuntimeLong_v2.fromDouble( -1.50001, context0 ) );
		assertEquals( -1, RuntimeLong_v2.fromDouble( -1.49999, context0 ) );
		assertEquals( -1, RuntimeLong_v2.fromDouble( -0.50001, context0 ) );
		assertEquals( 0, RuntimeLong_v2.fromDouble( -0.49999, context0 ) );
		assertEquals( 0, RuntimeLong_v2.fromDouble( 0.49999, context0 ) );
		assertEquals( 1, RuntimeLong_v2.fromDouble( 0.50001, context0 ) );
		assertEquals( 1, RuntimeLong_v2.fromDouble( 1.49999, context0 ) );
		assertEquals( 2, RuntimeLong_v2.fromDouble( 1.50001, context0 ) );
	}

}
