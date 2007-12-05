/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.formulacompiler.runtime.internal;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.runtime.Computation;

import junit.framework.TestCase;

public class EnvironmentTest extends TestCase
{
	private static final TimeZone STD = TimeZone.getDefault().getRawOffset() == 9? TimeZone.getTimeZone( "GMT-9:00" )
			: TimeZone.getTimeZone( "GMT+9:00" );

	public void testParseDateAndOrTime_de_CH() throws Exception
	{
		Environment env = Environment.getInstance( new Computation.Config( new Locale( "de", "CH" ), STD ) );

		assertDateOrTime( env, 1930, 1, 1, "1.1.30" );
		assertDateOrTime( env, 2029, 12, 31, "31.12.29" );

		assertDateOrTime( env, 1970, 6, 13, "13. 6. 1970" );
		assertDateOrTime( env, 1970, 6, 13, "13.6.1970" );
		assertDateOrTime( env, 1970, 6, 13, "13.6.70" );
		assertDateOrTime( env, 1970, 6, 3, "3.6.70" );
		assertDateOrTime( env, 2007, 6, 3, "3.6.7" );
		assertDateOrTime( env, 7, 6, 3, "3.6.0007" );

		assertDateOrTime( env, 1970, 6, 13, 12, 15, 16, "13.6.70 12:15:16" );
		assertDateOrTime( env, 1970, 6, 13, 12, 15, 0, "13.6.70 12:15" );

		assertDateOrTime( env, 1899, 12, 31, 12, 15, 16, "12:15:16" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 0, "12:15" );

		assertDateOrTime( env, 2000, 1, 1, 12, 15, 16, "1.1.00 12:15:16" );
		assertDateOrTime( env, 2000, 1, 1, 12, 15, 0, "1.1.00 12:15" );

		assertISO( env );
	}

	public void testParseDateAndOrTime_en_US() throws Exception
	{
		Environment env = Environment.getInstance( new Computation.Config( new Locale( "en", "US" ), STD ) );

		assertDateOrTime( env, 1930, 1, 1, "1/1/30" );
		assertDateOrTime( env, 2029, 12, 31, "12/31/29" );

		assertDateOrTime( env, 1970, 6, 13, "6/13/1970" );
		assertDateOrTime( env, 1970, 6, 13, "6/13/70" );
		assertDateOrTime( env, 1970, 6, 3, "6/3/70" );
		assertDateOrTime( env, 2007, 6, 3, "6/3/7" );
		assertDateOrTime( env, 7, 6, 3, "6/3/0007" );

		assertDateOrTime( env, 1970, 6, 13, 0, 15, 16, "6/13/70 12:15:16 am" );
		assertDateOrTime( env, 1970, 6, 13, 0, 15, 0, "6/13/70 12:15 am" );
		assertDateOrTime( env, 1970, 6, 13, 12, 15, 16, "6/13/70 12:15:16 pm" );
		assertDateOrTime( env, 1970, 6, 13, 12, 15, 0, "6/13/70 12:15 pm" );

		assertDateOrTime( env, 1970, 6, 13, 0, 15, 16, "6/13/70 0:15:16" );
		assertDateOrTime( env, 1970, 6, 13, 0, 15, 0, "6/13/70 0:15" );
		assertDateOrTime( env, 1970, 6, 13, 12, 15, 16, "6/13/70 12:15:16" );
		assertDateOrTime( env, 1970, 6, 13, 12, 15, 0, "6/13/70 12:15" );

		assertDateOrTime( env, 1899, 12, 31, 0, 15, 16, "0:15:16 am" );
		assertDateOrTime( env, 1899, 12, 31, 0, 15, 0, "0:15 am" );
		assertDateOrTime( env, 1899, 12, 31, 0, 15, 16, "12:15:16 am" );
		assertDateOrTime( env, 1899, 12, 31, 0, 15, 0, "12:15 am" );

		assertDateOrTime( env, 1899, 12, 31, 12, 15, 16, "0:15:16 pm" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 0, "0:15 pm" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 16, "12:15:16 pm" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 0, "12:15 pm" );

		assertDateOrTime( env, 1899, 12, 31, 0, 15, 16, "0:15:16" );
		assertDateOrTime( env, 1899, 12, 31, 0, 15, 0, "0:15" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 16, "12:15:16" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 0, "12:15" );

		assertDateOrTime( env, 2000, 1, 1, 0, 15, 16, "1/1/00 12:15:16 am" );
		assertDateOrTime( env, 2000, 1, 1, 0, 15, 0, "1/1/00 12:15 am" );

		assertISO( env );
	}


	public void testParseDateAndOrTime_ru_RU() throws Exception
	{
		Environment env = Environment.getInstance( new Computation.Config( new Locale( "ru", "RU" ), STD ) );
		assertDateOrTime( env, 1930, 2, 1, "1.2.30" );
		assertDateOrTime( env, 1930, 2, 1, "1.02.30" );
		assertDateOrTime( env, 2029, 12, 31, "31.12.29" );
		assertDateOrTime( env, 1997, 12, 31, "31.12.1997" );
		// LATER assertDateOrTime( env, 1997, 12, 31, "31/12/1997" );
		assertDateOrTime( env, 1997, 12, 31, "1997-12-31" );
		assertDateOrTime( env, 1970, 11, 6, 12, 15, 16, "6.11.70 12:15:16" );
		assertDateOrTime( env, 1970, 11, 6, 12, 15, 0, "6.11.70 12:15" );
		assertDateOrTime( env, 1899, 12, 31, 12, 15, 16, "12:15:16" );
		assertDateOrTime( env, 1899, 12, 31, 14, 15, 0, "14:15" );
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
		assertDateOrTime( _env, _year, _month, _day, _toParse.replace( '-', _dateSep ) );
	}

	private void assertDateOrTime( Environment _env, int _year, int _month, int _day, int _hours, int _minutes,
			int _seconds, String _toParse, char _dateSep ) throws Exception
	{
		assertDateOrTime( _env, _year, _month, _day, _hours, _minutes, _seconds, _toParse.replace( '-', _dateSep ) );
	}


	private void assertDateOrTime( Environment _env, int _year, int _month, int _day, String _toParse ) throws Exception
	{
		assertDateOrTime( _env, _year, _month, _day, 0, 0, 0, _toParse );
	}

	private void assertDateOrTime( Environment _env, int _year, int _month, int _day, int _hours, int _minutes,
			int _seconds, String _toParse ) throws Exception
	{
		Date parsed = _env.parseDateAndOrTime( _toParse );
		Calendar cal = Calendar.getInstance( STD, _env.locale() );
		cal.clear();
		cal.set( _year, _month - 1, _day, _hours, _minutes, _seconds );
		Date want = cal.getTime();
		if (want.getTime() != parsed.getTime()) {
			assertEquals( _toParse, want, parsed );
		}
	}

}
