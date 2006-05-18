/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.runtime;

import java.math.BigDecimal;
import java.util.Date;


public class RuntimeBigDecimal_v1 extends Runtime_v1
{
	public static BigDecimal ZERO = new BigDecimal( 0 );
	public static BigDecimal ONE = new BigDecimal( 1 );


	public static BigDecimal newBigDecimal( final String _value )
	{
		return new BigDecimal( _value );
	}

	public static BigDecimal round( final BigDecimal _val, final int _maxFrac )
	{
		return _val.setScale( _maxFrac, BigDecimal.ROUND_HALF_UP );
	}

	public static BigDecimal stdROUND( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return round( _val, _maxFrac.intValue() );
	}
	
	public static BigDecimal pow( final BigDecimal x, final BigDecimal n )
	{
		return x.pow( n.intValue() );
	}


	public static boolean booleanFromExcel( final BigDecimal _val )
	{
		return (_val.compareTo( ZERO ) != 0);
	}

	public static BigDecimal booleanToExcel( final boolean _val )
	{
		return _val ? ONE : ZERO;
	}


	private static BigDecimal MSINADAY = new BigDecimal( MS_PER_DAY );
	private static BigDecimal NONLEAPDAY = new BigDecimal( NON_LEAP_DAY );
	private static BigDecimal UTCOFFSETDAYS = new BigDecimal( UTC_OFFSET_DAYS );


	public static Date dateFromExcel( final BigDecimal _excel )
	{
		return RuntimeDouble_v1.dateFromExcel( _excel.doubleValue() );
	}

	public static BigDecimal dateToExcel( final Date _date )
	{
		final long utcValue = _date.getTime();
		final boolean time = (utcValue < MS_PER_DAY);

		// Convert this to the number of days, plus fractions of a day since
		// 01 Jan 1970
		final BigDecimal utcDays = new BigDecimal( utcValue ).divide( MSINADAY, 8, BigDecimal.ROUND_HALF_UP );

		// Add in the offset to get the number of days since 01 Jan 1900
		BigDecimal value = utcDays.add( UTCOFFSETDAYS );

		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but this was not a leap year.
		// Therefore for values less than 61, we must subtract 1. Only do
		// this for full dates, not times
		if (!time && value.compareTo( NONLEAPDAY ) < 0) {
			value = value.subtract( ONE );
		}

		// If this refers to a time, then get rid of the integer part
		if (time) {
			value = value.subtract( new BigDecimal( value.toBigInteger() ) );
		}

		return value;
	}

}
