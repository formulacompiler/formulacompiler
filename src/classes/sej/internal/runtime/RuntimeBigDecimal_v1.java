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
package sej.internal.runtime;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;


public class RuntimeBigDecimal_v1 extends Runtime_v1
{
	public static BigDecimal ZERO = BigDecimal.ZERO;
	public static BigDecimal ONE = BigDecimal.ONE;


	public static BigDecimal newBigDecimal( final String _value )
	{
		return (_value == null) ? ZERO : new BigDecimal( _value );
	}

	public static BigDecimal newBigDecimal( final BigInteger _value )
	{
		return (_value == null) ? ZERO : new BigDecimal( _value );
	}


	/**
	 * JRE 1.4 does not have BigDecimal.valueOf, so I cannot compile it. Retrotranslator handles the
	 * call here.
	 */
	@Deprecated
	public static BigDecimal newBigDecimal( final double _value )
	{
		return BigDecimal.valueOf( _value );
	}

	/**
	 * JRE 1.4 does not have BigDecimal.valueOf, so I cannot compile it. Retrotranslator handles the
	 * call here.
	 */
	@Deprecated
	public static BigDecimal newBigDecimal( final long _value )
	{
		return BigDecimal.valueOf( _value );
	}


	public static BigDecimal round( final BigDecimal _val, final int _maxFrac )
	{
		return _val.setScale( _maxFrac, BigDecimal.ROUND_HALF_UP );
	}

	@Deprecated
	public static BigDecimal stdROUND( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return round( _val, _maxFrac.intValue() );
	}

	@Deprecated
	public static BigDecimal stdTODAY()
	{
		return dateToNum( today() );
	}

	public static BigDecimal pow( final BigDecimal x, final BigDecimal n )
	{
		return x.pow( n.intValueExact() );
	}

	public static BigDecimal min( BigDecimal a, BigDecimal b )
	{
		return (a.compareTo( b ) <= 0) ? a : b;
	}

	public static BigDecimal max( BigDecimal a, BigDecimal b )
	{
		return (a.compareTo( b ) >= 0) ? a : b;
	}

	@Deprecated
	public static BigDecimal and( BigDecimal a, BigDecimal b )
	{
		return booleanToNum( booleanFromNum( a ) && booleanFromNum( b ) );
	}

	@Deprecated
	public static BigDecimal or( BigDecimal a, BigDecimal b )
	{
		return booleanToNum( booleanFromNum( a ) || booleanFromNum( b ) );
	}
	
	
	public static BigDecimal toNum( final BigDecimal _val )
	{
		return _val == null? ZERO : _val; 
	}
	

	public static boolean booleanFromNum( final BigDecimal _val )
	{
		return (_val.compareTo( ZERO ) != 0);
	}

	public static BigDecimal booleanToNum( final boolean _val )
	{
		return _val ? ONE : ZERO;
	}


	public static long numberToLong( final Number _val )
	{
		return (_val == null) ? 0L : _val.longValue();
	}

	public static double numberToDouble( final Number _val )
	{
		return (_val == null) ? 0.0 : _val.doubleValue();
	}


	public static BigDecimal fromScaledLong( long _scaled, int _scale )
	{
		return BigDecimal.valueOf( _scaled, _scale );
	}

	public static long toScaledLong( BigDecimal _value, int _scale )
	{
		return toScaledLong( _value, _scale, BigDecimal.ROUND_DOWN );
	}

	public static long toScaledLong( BigDecimal _value, int _scale, int _roundingMode )
	{
		return _value.setScale( _scale, _roundingMode ).movePointRight( _scale ).longValue();
	}


	private static BigDecimal MSINADAY = new BigDecimal( MS_PER_DAY );
	private static BigDecimal NONLEAPDAY = new BigDecimal( NON_LEAP_DAY );
	private static BigDecimal UTCOFFSETDAYS = new BigDecimal( UTC_OFFSET_DAYS );


	public static Date dateFromNum( final BigDecimal _excel )
	{
		return RuntimeDouble_v1.dateFromNum( _excel.doubleValue() );
	}

	public static BigDecimal dateToNum( final Date _date )
	{
		final long utcValue = (_date == null) ? 0 : _date.getTime();
		final boolean time = (utcValue < MS_PER_DAY);

		// Convert this to the number of days, plus fractions of a day since
		// 01 Jan 1970
		final BigDecimal utcDays = new BigDecimal( utcValue ).divide( MSINADAY, 8, BigDecimal.ROUND_HALF_UP );

		// Add in the offset to get the number of days since 01 Jan 1900
		BigDecimal value = utcDays.add( UTCOFFSETDAYS );

		// Work round a bug in Excel. Excel seems to think there is a date
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


	public static String toExcelString( BigDecimal _num )
	{
		return stringFromBigDecimal( _num );
	}


	public static BigDecimal fun_ROUND( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return round( _val, _maxFrac.intValue() );
	}

	public static BigDecimal fun_TODAY()
	{
		return dateToNum( today() );
	}

	public static BigDecimal fun_FACT( BigDecimal _a )
	{
		int a = _a.intValue();
		if (a < 0) {
			return ZERO; // Excel #NUM!
		}
		else if (a < FACTORIALS.length) {
			return BigDecimal.valueOf( FACTORIALS[ a ] );
		}
		else {
			BigDecimal r = ONE;
			while (a > 1)
				r = r.multiply( BigDecimal.valueOf( a-- ) );
			return r;
		}
	}


	private static final BigDecimal EXCEL_EPSILON = new BigDecimal( 0.0000001 );

	/**
	 * Computes IRR using Newton's method, where x[i+1] = x[i] - f( x[i] ) / f'( x[i] )
	 */
	public static BigDecimal fun_IRR( BigDecimal[] _values, BigDecimal _guess )
	{
		final int EXCEL_MAX_ITER = 20;

		BigDecimal x = _guess;
		int iter = 0;
		while (iter++ < EXCEL_MAX_ITER) {

			final BigDecimal x1 = x.add( BigDecimal.ONE );
			BigDecimal fx = BigDecimal.ZERO;
			BigDecimal dfx = BigDecimal.ZERO;
			for (int i = 0; i < _values.length; i++) {
				final BigDecimal v = _values[ i ];
				fx = fx.add( v.divide( x1.pow( i ) ) );
				dfx = dfx.add( v.divide( x1.pow( i + 1 ) ).multiply( BigDecimal.valueOf( -i ) ) );
			}
			final BigDecimal new_x = x.subtract( fx.divide( dfx ) );
			final BigDecimal epsilon = new_x.subtract( x ).abs();

			if (epsilon.compareTo( EXCEL_EPSILON ) <= 0) {
				if (_guess.compareTo( BigDecimal.ZERO ) == 0 && new_x.abs().compareTo( EXCEL_EPSILON ) <= 0) {
					return BigDecimal.ZERO; // OpenOffice calc does this
				}
				else {
					return new_x;
				}
			}
			x = new_x;

		}
		throw new IllegalArgumentException( "IRR does not converge" );
	}

	public static BigDecimal fun_IRR( BigDecimal[] _values, BigDecimal _guess, int _fixedScale, int _roudingMode )
	{
		final int EXCEL_MAX_ITER = 20;

		BigDecimal x = _guess;
		int iter = 0;
		while (iter++ < EXCEL_MAX_ITER) {

			final BigDecimal x1 = x.add( BigDecimal.ONE );
			BigDecimal fx = BigDecimal.ZERO;
			BigDecimal dfx = BigDecimal.ZERO;
			for (int i = 0; i < _values.length; i++) {
				final BigDecimal v = _values[ i ];
				fx = fx.add( v.divide( x1.pow( i ), _fixedScale, _roudingMode ) );
				dfx = dfx.add( v.divide( x1.pow( i + 1 ), _fixedScale, _roudingMode ).multiply( BigDecimal.valueOf( -i ) ) );
			}
			final BigDecimal new_x = x.subtract( fx.divide( dfx, _fixedScale, _roudingMode ) );
			final BigDecimal epsilon = new_x.subtract( x ).abs();

			if (epsilon.compareTo( EXCEL_EPSILON ) <= 0) {
				if (_guess.compareTo( BigDecimal.ZERO ) == 0 && new_x.abs().compareTo( EXCEL_EPSILON ) <= 0) {
					return BigDecimal.ZERO; // OpenOffice calc does this
				}
				else {
					return new_x;
				}
			}
			x = new_x;

		}
		return BigDecimal.ZERO; // LATER: NaN
	}


}
