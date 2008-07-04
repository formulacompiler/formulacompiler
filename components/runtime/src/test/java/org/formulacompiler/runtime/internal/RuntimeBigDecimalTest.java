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

import java.math.BigDecimal;
import java.util.Locale;

import org.formulacompiler.runtime.Computation;

import junit.framework.TestCase;

public class RuntimeBigDecimalTest extends TestCase
{


	public void testStringFromBigDecimal() throws Exception
	{
		final Locale locale = Locale.ENGLISH;
		final Environment environment = Environment.getInstance( new Computation.Config( locale ) );
		assertEquals( "1.2", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 1.2 ), environment ) );
		assertEquals( "12", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 12 ), environment ) );
		assertEquals( "120", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 120 ), environment ) );
		assertEquals( "12000000000000000000", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 1.2e19 ),
				environment ) );
		assertEquals( "1.2E+20", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 1.2e20 ), environment ) );
		assertEquals( "12340000000000000000", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 12.34e18 ),
				environment ) );
		assertEquals( "1.234E+20", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( 12.34e19 ), environment ) );
		assertEquals( "-12340000000000000000", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( -12.34e18 ),
				environment ) );
		assertEquals( "-1.234E+20", RuntimeBigDecimal_v2.toExcelString( BigDecimal.valueOf( -12.34e19 ), environment ) );
	}


}
