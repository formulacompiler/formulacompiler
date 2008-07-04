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

package org.formulacompiler.spreadsheet.internal.odf.xml;

import junit.framework.TestCase;

public class DataTypeUtilTest extends TestCase
{
	public void testSimpleDuration()
	{
		testDuration( "PT1H1M1.000S", 3661000 );
	}

	public void testNegativeDuration()
	{
		testDuration( "-PT1H1M1.000S", -3661000 );
	}

	public void testZeroDuration()
	{
		testDuration( "PT0H0M0.000S", 0 );
	}

	public void testFractionalSecondDuration()
	{
		testDuration( "PT1H1M1.111S", 3661111 );
	}

	public void testLongDuration()
	{
		testDuration( "PT1543209876H32M35.000S", 5555555555555000L );
	}

	private static void testDuration( final String _xmlDuration, final long _millis )
	{
		assertEquals( _xmlDuration, DataTypeUtil.durationToXmlFormat( _millis ) );
		assertEquals( _millis, DataTypeUtil.durationFromXmlFormat( _xmlDuration ) );
	}

}
