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

package org.formulacompiler.tests.reference;


import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.tests.reference.base.SheetSuiteSetup;

import junit.framework.Test;
import junit.framework.TestSuite;

public class Dates extends SheetSuiteSetup
{
	private static final TimeZone TIME_ZONE_1 = TimeZone.getTimeZone( "GMT+12" );
	private static final TimeZone TIME_ZONE_2 = TimeZone.getTimeZone( "GMT-12" );

	public static Test suite() throws Exception
	{
		final TestSuite sheets = new TestSuite( "Files" );

		sheets.addTest( sheetSuite( "DateFunctions" ) );

		{
			TimeZone defaultTimeZone = TimeZone.getDefault();
			TimeZone timeZone = getDayOfYear( defaultTimeZone ) != getDayOfYear( TIME_ZONE_1 ) ? TIME_ZONE_1 : TIME_ZONE_2;
			if (getDayOfYear( defaultTimeZone ) == getDayOfYear( timeZone )) {
				throw new IllegalStateException( "The day must be different from local." );
			}
			sheets.addTest( sheetSuite( "DateFunctions", new Computation.Config( timeZone ), false ) );
		}

		return sheets;
	}

	private static int getDayOfYear( TimeZone _timeZone )
	{
		Calendar calendar = new GregorianCalendar( _timeZone );
		return calendar.get( Calendar.DAY_OF_YEAR );
	}

}
