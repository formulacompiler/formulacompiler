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

package org.formulacompiler.runtime.internal;

import java.util.Formatter;
import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.runtime.Computation;

import junit.framework.TestCase;

public class RuntimeTest extends TestCase
{
	private static final TimeZone STD = TimeZone.getDefault().getRawOffset() == 9 ? TimeZone.getTimeZone( "GMT-9:00" )
			: TimeZone.getTimeZone( "GMT+9:00" );

	public void testParseDateAndOrTime_de_CH() throws Exception
	{
		Environment env = Environment.getInstance( new Computation.Config( new Locale( "de", "CH" ), STD ) );

		assertDate( env, 1930, 1, 1, "1.1.30" );
		assertDate( env, 2029, 12, 31, "31.12.29" );

		assertDate( env, 1970, 6, 13, "13. 6. 1970" );
		assertDate( env, 1970, 6, 13, "13.6.1970" );
		assertDate( env, 1970, 6, 13, "13.6.70" );
		assertDate( env, 1970, 6, 3, "3.6.70" );
		assertDate( env, 2007, 6, 3, "3.6.7" );
		assertDate( env, 7, 6, 3, "3.6.0007" );

		assertDateTime( env, 1970, 6, 13, 12, 15, 16, "13.6.70 12:15:16" );
		assertDateTime( env, 1970, 6, 13, 12, 15, 0, "13.6.70 12:15" );

		assertTime( env, 12, 15, 16, "12:15:16" );
		assertTime( env, 12, 15, 0, "12:15" );

		assertDateTime( env, 2000, 1, 1, 12, 15, 16, "1.1.00 12:15:16" );
		assertDateTime( env, 2000, 1, 1, 12, 15, 0, "1.1.00 12:15" );

		assertISO( env );
	}

	public void testParseDateAndOrTime_en_US() throws Exception
	{
		Environment env = Environment.getInstance( new Computation.Config( new Locale( "en", "US" ), STD ) );

		assertDate( env, 1930, 1, 1, "1/1/30" );
		assertDate( env, 2029, 12, 31, "12/31/29" );

		assertDate( env, 1970, 6, 13, "6/13/1970" );
		assertDate( env, 1970, 6, 13, "6/13/70" );
		assertDate( env, 1970, 6, 3, "6/3/70" );
		assertDate( env, 2007, 6, 3, "6/3/7" );
		assertDate( env, 7, 6, 3, "6/3/0007" );

		assertDateTime( env, 1970, 6, 13, 0, 15, 16, "6/13/70 12:15:16 am" );
		assertDateTime( env, 1970, 6, 13, 0, 15, 0, "6/13/70 12:15 am" );
		assertDateTime( env, 1970, 6, 13, 12, 15, 16, "6/13/70 12:15:16 pm" );
		assertDateTime( env, 1970, 6, 13, 12, 15, 0, "6/13/70 12:15 pm" );

		assertDateTime( env, 1970, 6, 13, 0, 15, 16, "6/13/70 0:15:16" );
		assertDateTime( env, 1970, 6, 13, 0, 15, 0, "6/13/70 0:15" );
		assertDateTime( env, 1970, 6, 13, 12, 15, 16, "6/13/70 12:15:16" );
		assertDateTime( env, 1970, 6, 13, 12, 15, 0, "6/13/70 12:15" );

		assertTime( env, 0, 15, 16, "0:15:16 am" );
		assertTime( env, 0, 15, 0, "0:15 am" );
		assertTime( env, 0, 15, 16, "12:15:16 am" );
		assertTime( env, 0, 15, 0, "12:15 am" );

		assertTime( env, 12, 15, 16, "0:15:16 pm" );
		assertTime( env, 12, 15, 0, "0:15 pm" );
		assertTime( env, 12, 15, 16, "12:15:16 pm" );
		assertTime( env, 12, 15, 0, "12:15 pm" );

		assertTime( env, 0, 15, 16, "0:15:16" );
		assertTime( env, 0, 15, 0, "0:15" );
		assertTime( env, 12, 15, 16, "12:15:16" );
		assertTime( env, 12, 15, 0, "12:15" );

		assertDateTime( env, 2000, 1, 1, 0, 15, 16, "1/1/00 12:15:16 am" );
		assertDateTime( env, 2000, 1, 1, 0, 15, 0, "1/1/00 12:15 am" );

		assertISO( env );
	}


	public void testParseDateAndOrTime_ru_RU() throws Exception
	{
		Environment env = Environment.getInstance( new Computation.Config( new Locale( "ru", "RU" ), STD ) );
		assertDate( env, 1930, 2, 1, "1.2.30" );
		assertDate( env, 1930, 2, 1, "1.02.30" );
		assertDate( env, 2029, 12, 31, "31.12.29" );
		assertDate( env, 1997, 12, 31, "31.12.1997" );
		// LATER assertDate( env, 1997, 12, 31, "31/12/1997" );
		assertDate( env, 1997, 12, 31, "1997-12-31" );
		assertDateTime( env, 1970, 11, 6, 12, 15, 16, "6.11.70 12:15:16" );
		assertDateTime( env, 1970, 11, 6, 12, 15, 0, "6.11.70 12:15" );
		assertTime( env, 12, 15, 16, "12:15:16" );
		assertTime( env, 14, 15, 0, "14:15" );
		assertISO( env );
	}


	private void assertISO( Environment _env ) throws Exception
	{
		assertISO( _env, '-' );
		assertISO( _env, '/' );
	}

	private void assertISO( Environment _env, char _dateSep ) throws Exception
	{
		assertDateOrTime( _env, 1930, 1, 1, "1930-1-1", _dateSep );
		assertDateOrTime( _env, 2029, 12, 31, "2029-12-31", _dateSep );

		assertDateOrTime( _env, 1970, 6, 13, "1970-6-13", _dateSep );
		assertDateOrTime( _env, 2007, 6, 3, "2007-6-3", _dateSep );
		assertDateOrTime( _env, 7, 6, 3, "0007-6-3", _dateSep );

		assertDateOrTime( _env, 1970, 6, 13, 12, 15, 16, "1970-6-13 12:15:16", _dateSep );
		assertDateOrTime( _env, 1970, 6, 13, 12, 15, 0, "1970-6-13 12:15", _dateSep );

		assertDateOrTime( _env, 2000, 1, 1, 0, 15, 16, "2000-1-1 00:15:16", _dateSep );
		assertDateOrTime( _env, 2000, 1, 1, 0, 15, 0, "2000-1-1 00:15", _dateSep );
	}

	private void assertDateOrTime( Environment _env, int _year, int _month, int _day, String _toParse, char _dateSep )
			throws Exception
	{
		assertDate( _env, _year, _month, _day, _toParse.replace( '-', _dateSep ) );
	}

	private void assertDateOrTime( Environment _env, int _year, int _month, int _day, int _hours, int _minutes,
			int _seconds, String _toParse, char _dateSep ) throws Exception
	{
		assertDateTime( _env, _year, _month, _day, _hours, _minutes, _seconds, _toParse.replace( '-', _dateSep ) );
	}


	private void assertDate( Environment _env, int _year, int _month, int _day, String _toParse ) throws Exception
	{
		assertDate( _env, _year, _month, _day, _toParse, true );
		assertDate( _env, _year, _month, _day, _toParse, false );
	}

	private void assertDate( Environment _env, int _year, int _month, int _day, String _toParse,
			boolean _excelCompatible ) throws Exception
	{
		assertEquals( "Parsing " + _toParse + " failed. Expected " + formatDate( _year, _month, _day ),
				RuntimeDouble_v2.dateToNum( _year, _month, _day, _excelCompatible ),
				Runtime_v2.parseDateAndOrTime( _toParse, _env, _excelCompatible ), 1e-9 );
	}

	private void assertDateTime( Environment _env, int _year, int _month, int _day, int _hours, int _minutes,
			int _seconds, String _toParse ) throws Exception
	{
		assertDateTime( _env, _year, _month, _day, _hours, _minutes, _seconds, _toParse, true );
		assertDateTime( _env, _year, _month, _day, _hours, _minutes, _seconds, _toParse, false );
	}

	private void assertDateTime( Environment _env, int _year, int _month, int _day,
			int _hours, int _minutes, int _seconds, String _toParse, boolean _excelCompatible ) throws Exception
	{
		assertEquals( "Parsing " + _toParse + " failed. Expected " + formatDate( _year, _month, _day ) +
				" " + formatTime( _hours, _minutes, _seconds ),
				RuntimeDouble_v2.dateToNum( _year, _month, _day, _excelCompatible ) +
						RuntimeDouble_v2.fun_TIME( _hours, _minutes, _seconds ),
				Runtime_v2.parseDateAndOrTime( _toParse, _env, _excelCompatible ), 1e-9 );
	}

	private void assertTime( Environment _env, int _hours, int _minutes, int _seconds, String _toParse ) throws Exception
	{
		assertTime( _env, _hours, _minutes, _seconds, _toParse, true );
		assertTime( _env, _hours, _minutes, _seconds, _toParse, false );
	}

	private void assertTime( Environment _env, int _hours, int _minutes, int _seconds, String _toParse,
			boolean _excelCompatible ) throws Exception
	{
		assertEquals( "Parsing " + _toParse + " failed. Expected " + formatTime( _hours, _minutes, _seconds ),
				RuntimeDouble_v2.fun_TIME( _hours, _minutes, _seconds ),
				Runtime_v2.parseDateAndOrTime( _toParse, _env, _excelCompatible ), 1e-9 );
	}

	private String formatDate( int _year, int _month, int _day )
	{
		Formatter formatter = new Formatter();
		formatter.format( "%1$04d-%2$02d-%3$02d", _year, _month, _day );
		return formatter.toString();
	}

	private String formatTime( int _hours, int _minutes, int _seconds )
	{
		Formatter formatter = new Formatter();
		formatter.format( "%1$02d:%2$02d:%3$02d", _hours, _minutes, _seconds );
		return formatter.toString();
	}
}
