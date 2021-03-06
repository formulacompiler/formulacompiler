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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.TimeZone;

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.NotAvailableException;


public abstract class RuntimeBigDecimal_v2 extends Runtime_v2
{
	public static final BigDecimal ZERO = BigDecimal.ZERO;
	public static final BigDecimal ONE = BigDecimal.ONE;
	public static final BigDecimal TWO = BigDecimal.valueOf( 2 );
	public static final BigDecimal TWELVE = BigDecimal.valueOf( 12 );
	public static final BigDecimal TENTH = BigDecimal.valueOf( 0.1 );
	public static final BigDecimal EXTREMUM = new BigDecimal( 9999999 );

	static final BigDecimal PI = BigDecimal.valueOf( Math.PI );

	private static final BigDecimal BIG_SECS_PER_MINUTE = BigDecimal.valueOf( 60 );
	private static final BigDecimal BIG_SECS_PER_HOUR = BigDecimal.valueOf( Runtime_v2.SECS_PER_HOUR );
	private static final BigDecimal BIG_SECS_PER_DAY = BigDecimal.valueOf( Runtime_v2.SECS_PER_DAY );
	private static final BigDecimal EXCEL_EPSILON = new BigDecimal( 0.0000001 );


	public static BigDecimal newBigDecimal( final String _value )
	{
		return (_value == null) ? ZERO : new BigDecimal( _value );
	}

	public static BigDecimal newBigDecimal( final BigInteger _value )
	{
		return (_value == null) ? ZERO : new BigDecimal( _value );
	}


	public static BigDecimal round( final BigDecimal _val, final int _maxFrac )
	{
		return _val.setScale( _maxFrac, RoundingMode.HALF_UP );
	}

	public static BigDecimal min( BigDecimal a, BigDecimal b )
	{
		if (a == EXTREMUM) return b;
		if (b == EXTREMUM) return a;
		return (a.compareTo( b ) <= 0) ? a : b;
	}

	public static BigDecimal max( BigDecimal a, BigDecimal b )
	{
		if (a == EXTREMUM) return b;
		if (b == EXTREMUM) return a;
		return (a.compareTo( b ) >= 0) ? a : b;
	}


	public static BigDecimal toNum( final BigDecimal _val )
	{
		return _val == null ? ZERO : _val;
	}


	public static boolean booleanFromNum( final BigDecimal _val )
	{
		return (_val.compareTo( ZERO ) != 0);
	}

	public static BigDecimal booleanToNum( final boolean _val )
	{
		return _val ? ONE : ZERO;
	}


	public static BigDecimal fromScaledLong( long _scaled, int _scale )
	{
		return BigDecimal.valueOf( _scaled, _scale );
	}

	public static long toScaledLong( BigDecimal _value, int _scale )
	{
		return toScaledLong( _value, _scale, RoundingMode.HALF_UP );
	}

	private static long toScaledLong( BigDecimal _value, int _scale, RoundingMode _roundingMode )
	{
		return _value.setScale( _scale, _roundingMode ).movePointRight( _scale ).longValue();
	}


	/**
	 * @deprecated replaced by {@link #dateFromNum(BigDecimal,TimeZone,ComputationMode)}
	 */
	@Deprecated
	public static Date dateFromNum( final BigDecimal _excel, final TimeZone _timeZone )
	{
		return dateFromNum( _excel, _timeZone, ComputationMode.EXCEL );
	}

	public static Date dateFromNum( final BigDecimal _date, final TimeZone _timeZone, ComputationMode _mode )
	{
		return RuntimeDouble_v2.dateFromNum( _date.doubleValue(), _timeZone, _mode );
	}

	/**
	 * @deprecated replaced by {@link #dateToNum(Date,TimeZone,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal dateToNum( final Date _date, final TimeZone _timeZone )
	{
		return dateToNum( _date, _timeZone, ComputationMode.EXCEL );
	}

	public static BigDecimal dateToNum( final Date _date, final TimeZone _timeZone, ComputationMode _mode )
	{
		return valueOf( RuntimeDouble_v2.dateToNum( _date, _timeZone, _mode ) );
	}

	public static BigDecimal valueOf( final double _value )
	{
		return BigDecimal.valueOf( checkDouble( _value ) );
	}


	public static String toExcelString( BigDecimal _num, Environment _environment )
	{
		return stringFromBigDecimal( _num, _environment );
	}


	public static BigDecimal fun_ACOS( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( RuntimeDouble_v2.fun_ACOS( a ) );
	}

	public static BigDecimal fun_ACOSH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( RuntimeDouble_v2.fun_ACOSH( a ) );
	}

	public static BigDecimal fun_ASIN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.asin( a ) );
	}

	public static BigDecimal fun_ASINH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( RuntimeDouble_v2.fun_ASINH( a ) );
	}

	public static BigDecimal fun_ATAN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.atan( a ) );
	}

	public static BigDecimal fun_ATANH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( RuntimeDouble_v2.fun_ATANH( a ) );
	}

	public static BigDecimal fun_ATAN2( BigDecimal _x, BigDecimal _y )
	{
		final double x = _x.doubleValue();
		final double y = _y.doubleValue();
		return valueOf( Math.atan2( y, x ) );
	}

	public static BigDecimal fun_COS( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.cos( a ) );
	}

	public static BigDecimal fun_COSH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.cosh( a ) );
	}

	public static BigDecimal fun_SIN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.sin( a ) );
	}

	public static BigDecimal fun_SINH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( RuntimeDouble_v2.fun_SINH( a ) );
	}

	public static BigDecimal fun_TAN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.tan( a ) );
	}

	public static BigDecimal fun_TANH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return valueOf( Math.tanh( a ) );
	}

	public static BigDecimal fun_DEGREES( BigDecimal _a, MathContext _cx )
	{
		final BigDecimal product = _a.multiply( BigDecimal.valueOf( 180 ), _cx );
		return product.divide( PI, _cx );
	}

	public static BigDecimal fun_RADIANS( BigDecimal _a, MathContext _cx )
	{
		final BigDecimal product = _a.multiply( PI, _cx );
		return product.divide( BigDecimal.valueOf( 180 ), _cx );
	}

	public static BigDecimal fun_PI()
	{
		return PI;
	}

	public static BigDecimal fun_POWER( BigDecimal _n, BigDecimal _p, MathContext _cx )
	{
		final BigDecimal pNormalized = _p.stripTrailingZeros();
		if (pNormalized.scale() <= 0) {
			final int p = pNormalized.intValueExact();
			if (p >= 0 && p <= 999999999) {
				return _n.pow( p, _cx );
			}
		}
		return valueOf( Math.pow( _n.doubleValue(), _p.doubleValue() ) );
	}

	public static BigDecimal fun_CEILING( BigDecimal _number, BigDecimal _significance, MathContext _cx )
	{
		final BigDecimal a = _number.divide( _significance, _cx );
		if (a.signum() < 0) {
			err_CEILING();
		}
		return a.setScale( 0, RoundingMode.UP ).multiply( _significance, _cx );
	}

	public static BigDecimal fun_CEILING_OOo( BigDecimal _number, BigDecimal _significance, MathContext _cx )
	{
		if (_number.signum() * _significance.signum() < 0) {
			err_CEILING();
		}
		final BigDecimal s = _significance.abs();
		return _number.divide( s, _cx ).setScale( 0, RoundingMode.CEILING ).multiply( s, _cx );
	}

	public static BigDecimal fun_FLOOR( BigDecimal _number, BigDecimal _significance, MathContext _cx )
	{
		final BigDecimal a = _number.divide( _significance, _cx );
		if (a.signum() < 0) {
			err_FLOOR();
		}
		return a.setScale( 0, RoundingMode.DOWN ).multiply( _significance, _cx );
	}

	public static BigDecimal fun_FLOOR_OOo( BigDecimal _number, BigDecimal _significance, MathContext _cx )
	{
		if (_number.signum() * _significance.signum() < 0) {
			err_CEILING();
		}
		final BigDecimal s = _significance.abs();
		return _number.divide( s, _cx ).setScale( 0, RoundingMode.FLOOR ).multiply( s, _cx );
	}

	public static BigDecimal fun_RAND()
	{
		return BigDecimal.valueOf( generator.nextDouble() );
	}

	public static BigDecimal fun_ROUND( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return round( _val, _maxFrac.intValue() );
	}

	public static BigDecimal fun_ROUNDDOWN( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return _val.setScale( _maxFrac.intValue(), RoundingMode.DOWN );
	}

	public static BigDecimal fun_ROUNDUP( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return _val.setScale( _maxFrac.intValue(), RoundingMode.UP );
	}

	public static BigDecimal fun_TRUNC( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return _val.setScale( _maxFrac.intValue(), RoundingMode.DOWN );
	}

	public static BigDecimal fun_TRUNC( final BigDecimal _val )
	{
		return _val.setScale( 0, RoundingMode.DOWN );
	}

	public static BigDecimal fun_EVEN( final BigDecimal _val )
	{
		final BigDecimal rounded = _val.divide( TWO, 0, RoundingMode.UP );
		return rounded.multiply( TWO );
	}

	public static BigDecimal fun_ODD( final BigDecimal _val )
	{
		switch (_val.signum()) {
			case -1:
				return _val.subtract( ONE ).divide( TWO, 0, RoundingMode.UP ).multiply( TWO ).add( ONE );
			case 1:
				return _val.add( ONE ).divide( TWO, 0, RoundingMode.UP ).multiply( TWO ).subtract( ONE );
			default: // zero
				return ONE;
		}
	}

	public static BigDecimal fun_INT( final BigDecimal _val )
	{
		return _val.setScale( 0, RoundingMode.FLOOR );
	}

	public static BigDecimal fun_LN( final BigDecimal _p )
	{
		final double result = Math.log( _p.doubleValue() );
		return valueOf( result );
	}

	public static BigDecimal fun_LOG10( final BigDecimal _p )
	{
		final double result = Math.log10( _p.doubleValue() );
		return valueOf( result );
	}

	public static BigDecimal fun_ERF( BigDecimal _z )
	{
		return valueOf( RuntimeDouble_v2.fun_ERF( _z.doubleValue() ) );
	}

	public static BigDecimal fun_ERFC( BigDecimal _z )
	{
		return valueOf( RuntimeDouble_v2.fun_ERFC( _z.doubleValue() ) );
	}

	public static BigDecimal fun_BETADIST( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return valueOf( RuntimeDouble_v2.fun_BETADIST( _x.doubleValue(), _alpha.doubleValue(), _beta.doubleValue() ) );
	}

	public static BigDecimal fun_BETAINV( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return valueOf( RuntimeDouble_v2.fun_BETAINV( _x.doubleValue(), _alpha.doubleValue(), _beta.doubleValue() ) );
	}


	public static BigDecimal fun_BINOMDIST( BigDecimal _successes, BigDecimal _trials, BigDecimal _probability,
			boolean _cumulative )
	{
		return valueOf( RuntimeDouble_v2.fun_BINOMDIST( _successes.intValue(), _trials.intValue(), _probability
				.doubleValue(), _cumulative ) );
	}

	public static BigDecimal fun_CHIDIST( BigDecimal _x, BigDecimal _degFreedom )
	{
		return valueOf( RuntimeDouble_v2.fun_CHIDIST( _x.doubleValue(), _degFreedom.doubleValue() ) );
	}

	public static BigDecimal fun_CHIINV( BigDecimal _x, BigDecimal _degFreedom )
	{
		return valueOf( RuntimeDouble_v2.fun_CHIINV( _x.doubleValue(), _degFreedom.doubleValue() ) );
	}

	public static BigDecimal fun_CRITBINOM( BigDecimal _n, BigDecimal _p, BigDecimal _alpha )
	{
		// p <= 0 is contrary to Excel's docs where it says p < 0; but the test case says otherwise.
		if (_n.signum() < 0 || _p.signum() < 0 || _p.compareTo( ONE ) > 0
				|| _alpha.signum() <= 0 || _alpha.compareTo( ONE ) >= 0) {
			fun_ERROR( "#NUM! because not n >= 0, 0 <= p <= 1, 0 < alpha < 1 in CRITBINOM" );
		}
		BigDecimal q = ONE.subtract( _p );
		final BigDecimal EPSILON = BigDecimal.valueOf( 0.1E-320 );
		int n = _n.intValue();
		if (n > 999999999) {
			throw new FormulaException( "#NUM! because n value is too large in CRITBINOM" );
		}
		BigDecimal factor = q.pow( n );
		if (factor.compareTo( EPSILON ) <= 0) {
			factor = _p.pow( n );
			if (factor.compareTo( EPSILON ) <= 0) {
				throw new FormulaException( "#NUM! because factor = 0 in CRITBINOM" );
			}
			else {
				BigDecimal sum = ONE.subtract( factor );
				int i;
				for (i = 0; i < n && sum.compareTo( _alpha ) >= 0; i++) {
					factor = factor.multiply( BigDecimal.valueOf( n - i ) ).divide( BigDecimal.valueOf( i + 1 ) ).multiply(
							q ).divide( _p );
					sum = sum.subtract( factor );
				}
				return BigDecimal.valueOf( n - i );
			}
		}
		else {
			BigDecimal sum = factor;
			int i;
			for (i = 0; i < n && sum.compareTo( _alpha ) < 0; i++) {
				factor = factor.multiply( BigDecimal.valueOf( n - i ) ).divide( BigDecimal.valueOf( i + 1 ) ).multiply( _p )
						.divide( q );
				sum = sum.add( factor );
			}
			return BigDecimal.valueOf( i );
		}
	}

	public static BigDecimal fun_FINV( BigDecimal _x, BigDecimal _f1, BigDecimal _f2 )
	{
		return valueOf( RuntimeDouble_v2.fun_FINV( _x.doubleValue(), _f1.doubleValue(), _f2.doubleValue() ) );
	}

	public static BigDecimal fun_GAMMADIST( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta, boolean _cumulative )
	{
		return valueOf( RuntimeDouble_v2.fun_GAMMADIST( _x.doubleValue(), _alpha.doubleValue(), _beta.doubleValue(),
				_cumulative ) );
	}

	public static BigDecimal fun_GAMMAINV( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return valueOf( RuntimeDouble_v2.fun_GAMMAINV( _x.doubleValue(), _alpha.doubleValue(), _beta.doubleValue() ) );
	}

	public static BigDecimal fun_GAMMALN( BigDecimal _x )
	{
		return valueOf( RuntimeDouble_v2.fun_GAMMALN( _x.doubleValue() ) );
	}

	public static BigDecimal fun_POISSON( BigDecimal _x, BigDecimal _mean, boolean _cumulative )
	{
		return valueOf( RuntimeDouble_v2.fun_POISSON( _x.intValue(), _mean.doubleValue(), _cumulative ) );
	}

	public static BigDecimal fun_TDIST( BigDecimal _x, BigDecimal _degFreedom, BigDecimal _tails, boolean _no_floor )
	{
		return valueOf( RuntimeDouble_v2.fun_TDIST( _x.doubleValue(), _degFreedom.doubleValue(), _tails.intValue(),
				_no_floor ) );
	}

	public static BigDecimal fun_TINV( BigDecimal _x, BigDecimal _degFreedom )
	{
		return valueOf( RuntimeDouble_v2.fun_TINV( _x.doubleValue(), _degFreedom.doubleValue() ) );
	}

	public static BigDecimal fun_WEIBULL( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta, boolean _cumulative )
	{
		return valueOf( RuntimeDouble_v2.fun_WEIBULL( _x.doubleValue(), _alpha.doubleValue(), _beta.doubleValue(),
				_cumulative ) );
	}

	public static BigDecimal fun_LOG( final BigDecimal _n, final BigDecimal _x )
	{
		final double n = _n.doubleValue();
		final double x = _x.doubleValue();
		return valueOf( RuntimeDouble_v2.fun_LOG( n, x ) );
	}

	public static BigDecimal fun_MOD( final BigDecimal _n, final BigDecimal _d )
	{
		final BigDecimal remainder = _n.remainder( _d );
		if (remainder.signum() != 0 && remainder.signum() != _d.signum()) {
			return remainder.add( _d );
		}
		else {
			return remainder;
		}
	}

	public static BigDecimal fun_SQRT( BigDecimal _n, MathContext _context )
	{
		// the Babylonian square root method (Newton's method)
		BigDecimal x0 = ZERO;
		BigDecimal x1 = valueOf( Math.sqrt( _n.doubleValue() ) );

		while (x0.compareTo( x1 ) != 0) {
			x0 = x1;
			final BigDecimal a = _n.divide( x0, _context );
			final BigDecimal b = a.add( x0, _context );
			x1 = b.divide( TWO, _context );
		}

		return x1;
	}

	public static BigDecimal fun_FACT( BigDecimal _a, MathContext _cx )
	{
		int a = _a.intValue();
		if (a < 0) {
			err_FACT();
		}
		if (a < FACTORIALS.length) {
			return BigDecimal.valueOf( FACTORIALS[ a ] );
		}
		else {
			BigDecimal r = ONE;
			while (a > 1)
				r = r.multiply( BigDecimal.valueOf( a-- ), _cx );
			return r;
		}
	}


	public static BigDecimal fun_MDETERM( BigDecimal[] _squareMatrix, int _sideLength )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_MDETERM( toDoubles( _squareMatrix ), _sideLength ) );
	}


	private static double[] toDoubles( BigDecimal[] _bigDecimals )
	{
		double[] result = new double[_bigDecimals.length];
		for (int i = 0; i < _bigDecimals.length; i++) {
			result[ i ] = _bigDecimals[ i ].doubleValue();
		}
		return result;
	}


	/**
	 * @deprecated replaced by {@link #fun_DATE(BigDecimal,BigDecimal,BigDecimal,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_DATE( BigDecimal _year, BigDecimal _month, BigDecimal _day )
	{
		return fun_DATE( _year, _month, _day, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_DATE( BigDecimal _year, BigDecimal _month, BigDecimal _day, ComputationMode _mode )
	{
		final int year = _year.intValue();
		final int month = _month.intValue();
		final int day = _day.intValue();
		final double result = RuntimeDouble_v2.fun_DATE( year, month, day, _mode );
		return valueOf( result );
	}

	/**
	 * @deprecated replaced by {@link #fun_WEEKDAY(BigDecimal,BigDecimal,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_WEEKDAY( BigDecimal _date, BigDecimal _type )
	{
		return fun_WEEKDAY( _date, _type, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_WEEKDAY( BigDecimal _date, BigDecimal _type, ComputationMode _mode )
	{
		final double date = _date.doubleValue();
		final int type = _type.intValue();
		final int result = RuntimeDouble_v2.fun_WEEKDAY( date, type, _mode );
		return valueOf( result );
	}

	public static BigDecimal fun_WORKDAY( BigDecimal _startDate, BigDecimal _days, BigDecimal[] _holidays, ComputationMode _mode )
	{
		final double startDate = _startDate.doubleValue();
		final double days = _days.doubleValue();
		final double[] holidays = toDoubles( _holidays );
		return valueOf( RuntimeDouble_v2.fun_WORKDAY( startDate, days, holidays, _mode ) );
	}

	/**
	 * @deprecated replaced by {@link #fun_DAY(BigDecimal,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_DAY( BigDecimal _date )
	{
		return fun_DAY( _date, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_DAY( BigDecimal _date, ComputationMode _mode )
	{
		final double date = _date.doubleValue();
		final int result = RuntimeDouble_v2.fun_DAY( date, _mode );
		return valueOf( result );
	}

	/**
	 * @deprecated replaced by {@link #fun_DAYS360(BigDecimal,BigDecimal,boolean,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_DAYS360( BigDecimal _start_date, BigDecimal _end_date, boolean _method )
	{
		return fun_DAYS360( _start_date, _end_date, _method, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_DAYS360( BigDecimal _start_date, BigDecimal _end_date, boolean _method, ComputationMode _mode )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_DAYS360( _start_date.doubleValue(), _end_date.doubleValue(),
				_method, _mode ) );
	}

	public static BigDecimal fun_YEARFRAC( BigDecimal _start_date, BigDecimal _end_date, int _basis, ComputationMode _mode )
	{
		return valueOf( RuntimeDouble_v2.fun_YEARFRAC( _start_date.doubleValue(), _end_date.doubleValue(), _basis, _mode ) );
	}

	/**
	 * @deprecated replaced by {@link #fun_MONTH(BigDecimal,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_MONTH( BigDecimal _date )
	{
		return fun_MONTH( _date, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_MONTH( BigDecimal _date, ComputationMode _mode )
	{
		final double date = _date.doubleValue();
		final int result = RuntimeDouble_v2.fun_MONTH( date, _mode );
		return valueOf( result );
	}

	/**
	 * @deprecated replaced by {@link #fun_YEAR(BigDecimal,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_YEAR( BigDecimal _date )
	{
		return fun_YEAR( _date, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_YEAR( BigDecimal _date, ComputationMode _mode )
	{
		final double date = _date.doubleValue();
		final int result = RuntimeDouble_v2.fun_YEAR( date, _mode );
		return valueOf( result );
	}

	/**
	 * @deprecated replaced by {@link #fun_NOW(Environment,ComputationTime,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_NOW( final Environment _environment, final ComputationTime _computationTime )
	{
		return fun_NOW( _environment, _computationTime, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_NOW( final Environment _environment, final ComputationTime _computationTime, ComputationMode _mode )
	{
		return dateToNum( now( _computationTime ), _environment.timeZone(), _mode );
	}

	/**
	 * @deprecated replaced by {@link #fun_TODAY(Environment,ComputationTime,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_TODAY( final Environment _environment, final ComputationTime _computationTime )
	{
		return fun_TODAY( _environment, _computationTime, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_TODAY( final Environment _environment, final ComputationTime _computationTime, ComputationMode _mode )
	{
		final TimeZone timeZone = _environment.timeZone();
		return dateToNum( today( timeZone, _computationTime ), timeZone, _mode );
	}

	public static BigDecimal fun_TIME( BigDecimal _hour, BigDecimal _minute, BigDecimal _second, MathContext _context )
	{
		final BigDecimal seconds = _hour.multiply( BIG_SECS_PER_HOUR ).add( _minute.multiply( BIG_SECS_PER_MINUTE ) )
				.add( _second ).remainder( BIG_SECS_PER_DAY );
		return seconds.divide( BIG_SECS_PER_DAY, _context );
	}

	public static BigDecimal fun_TIME_OOo( BigDecimal _hour, BigDecimal _minute, BigDecimal _second, MathContext _context )
	{
		final BigDecimal seconds = _hour.multiply( BIG_SECS_PER_HOUR ).add( _minute.multiply( BIG_SECS_PER_MINUTE ) )
				.add( _second );
		return seconds.divide( BIG_SECS_PER_DAY, _context );
	}

	public static BigDecimal fun_SECOND( BigDecimal _date )
	{
		final long seconds = getDaySecondsFromNum( _date ) % 60;
		return valueOf( seconds );
	}

	public static BigDecimal fun_MINUTE( BigDecimal _date )
	{
		final long minutes = getDaySecondsFromNum( _date ) / 60 % 60;
		return valueOf( minutes );
	}

	public static BigDecimal fun_HOUR( BigDecimal _date )
	{
		final long hours = getDaySecondsFromNum( _date ) / Runtime_v2.SECS_PER_HOUR % 24;
		return valueOf( hours );
	}

	public static BigDecimal fun_HYPGEOMDIST( BigDecimal _sample_s, BigDecimal _number_sample, BigDecimal _population_s,
			BigDecimal _number_population )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_HYPGEOMDIST( _sample_s.intValue(), _number_sample.intValue(),
				_population_s.intValue(), _number_population.intValue() ) );
	}

	private static long getDaySecondsFromNum( final BigDecimal _time )
	{
		return _time.multiply( BIG_SECS_PER_DAY ).remainder( BIG_SECS_PER_DAY ).setScale( 0, RoundingMode.HALF_UP )
				.longValue();
	}


	/**
	 * Computes IRR using Newton's method, where x[i+1] = x[i] - f( x[i] ) / f'( x[i] )
	 */
	public static BigDecimal fun_IRR( BigDecimal[] _values, BigDecimal _guess, MathContext _cx )
	{
		final int EXCEL_MAX_ITER = 20;

		BigDecimal x = _guess;
		int iter = 0;
		while (iter++ < EXCEL_MAX_ITER) {

			final BigDecimal x1 = x.add( BigDecimal.ONE, _cx );
			BigDecimal fx = BigDecimal.ZERO;
			BigDecimal dfx = BigDecimal.ZERO;
			for (int i = 0; i < _values.length; i++) {
				final BigDecimal v = _values[ i ];
				final BigDecimal x1_i = x1.pow( i, _cx );
				fx = fx.add( v.divide( x1_i, _cx ), _cx );
				final BigDecimal x1_i1 = x1_i.multiply( x1, _cx );
				dfx = dfx.add( v.divide( x1_i1, _cx ).multiply( BigDecimal.valueOf( -i ), _cx ), _cx );
			}
			final BigDecimal new_x = x.subtract( fx.divide( dfx, _cx ), _cx );
			final BigDecimal epsilon = new_x.subtract( x, _cx ).abs( _cx );

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
		throw new FormulaException( "#NUM! because result not found in " + EXCEL_MAX_ITER + " tries in IRR" );
	}

	private static BigDecimal xirrResultingAmount( BigDecimal[] _values, BigDecimal[] dates, BigDecimal rate, MathContext _cx )
	{
		final BigDecimal startDate = dates[ 0 ];
		final BigDecimal r = rate.add( BigDecimal.ONE, _cx );
		BigDecimal result = _values[ 0 ];
		for (int i = 1; i < _values.length; ++i) {
			final BigDecimal datePeriod = dates[ i ].subtract( startDate, _cx ).divide( BigDecimal.valueOf( 365 ), _cx );
			result = result.add( _values[ i ].divide( fun_POWER( r, datePeriod, _cx ), _cx ), _cx );
		}
		return result;
	}

	private static BigDecimal xirrResultingAmountDerivation( BigDecimal[] _values, BigDecimal[] dates, BigDecimal rate, MathContext _cx )
	{
		final BigDecimal startDate = dates[ 0 ];
		final BigDecimal r = rate.add( BigDecimal.ONE, _cx );
		BigDecimal result = _values[ 0 ];
		for (int i = 1; i < _values.length; ++i) {
			final BigDecimal datePeriod = dates[ i ].subtract( startDate, _cx ).divide( BigDecimal.valueOf( 365 ), _cx );
			result = result.subtract( datePeriod.multiply( _values[ i ].divide( fun_POWER( r, datePeriod.add( BigDecimal.ONE ), _cx ), _cx ), _cx ), _cx );
		}
		return result;
	}

	public static BigDecimal fun_XIRR( BigDecimal[] _values, BigDecimal[] _dates, BigDecimal _guess, MathContext _cx )
	{
		final int MAX_ITER = 50;
		final BigDecimal MAX_EPS = new BigDecimal( 0.0000000001 );

		final BigDecimal[] dates = new BigDecimal[_dates.length];
		for (int i = 0; i < _dates.length; i++) {
			dates[ i ] = _dates[ i ].setScale( 0, RoundingMode.DOWN );
		}
		if (_values.length != dates.length) {
			fun_ERROR( "#NUM! because values and dates array sizes are different in XIRR" );
		}
		if (_values.length < 2) {
			fun_ERROR( "#N/A! because values and dates array are too short in XIRR" );
		}
		if ((_guess.abs( _cx ).compareTo( BigDecimal.ONE ) >= 0)) {
			fun_ERROR( "#NUM! incorrect guess value in XIRR" );
		}
		boolean negativeValue = false;
		boolean positiveValue = false;
		for (BigDecimal value : _values) {
			final int valueSign = value.signum();
			if (valueSign < 0) negativeValue = true;
			if (valueSign > 0) positiveValue = true;
		}
		if (!(negativeValue && positiveValue)) {
			fun_ERROR( "#NUM! there are no positive or negative cash flow values in XIRR" );
		}

		BigDecimal resultRate = _guess;
		int iter = 0;
		boolean continuousFlag = true;
		do {
			final BigDecimal resultValue = xirrResultingAmount( _values, dates, resultRate, _cx );
			final BigDecimal newRate = resultRate.subtract( resultValue.divide( xirrResultingAmountDerivation( _values, dates, resultRate, _cx ), _cx ), _cx );
			final BigDecimal rateEps = newRate.subtract( resultRate, _cx ).abs( _cx );
			resultRate = newRate;
			continuousFlag = rateEps.compareTo( MAX_EPS ) > 0 && resultValue.abs( _cx ).compareTo( MAX_EPS ) > 0;
		}
		while (continuousFlag && (++iter < MAX_ITER));
		if (continuousFlag) {
			throw new FormulaException( "#NUM! because result not found in " + MAX_ITER + " tries in XIRR" );
		}
		return resultRate;
	}


	public static BigDecimal fun_DB( final BigDecimal _cost, final BigDecimal _salvage, final BigDecimal _life,
			final BigDecimal _period, final BigDecimal _month, MathContext _cx )
	{
		final BigDecimal month = _month.setScale( 0, RoundingMode.HALF_UP );
		final BigDecimal rate = valueOf(
				1 - Math.pow( (_salvage.doubleValue() / _cost.doubleValue()), (1 / _life.doubleValue()) ) ).setScale( 3,
				RoundingMode.HALF_UP );
		final BigDecimal depreciation1 = _cost.multiply( rate, _cx ).multiply( month, _cx ).divide( TWELVE, _cx );
		BigDecimal depreciation = depreciation1;
		if (_period.intValue() > 1) {
			BigDecimal totalDepreciation = depreciation1;
			final int maxPeriod = (_life.compareTo( _period ) > 0 ? _period : _life).intValue();
			for (int i = 2; i <= maxPeriod; i++) {
				depreciation = _cost.subtract( totalDepreciation, _cx ).multiply( rate, _cx );
				totalDepreciation = totalDepreciation.add( depreciation, _cx );
			}
			if (_period.compareTo( _life ) > 0) {
				depreciation = _cost.subtract( totalDepreciation, _cx ).multiply( rate, _cx ).multiply(
						TWELVE.subtract( month, _cx ), _cx ).divide( TWELVE, _cx );
			}
		}
		return depreciation;
	}

	public static BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period,
			BigDecimal _factor, MathContext _cx )
	{
		final double period = _period.doubleValue();
		final double k = ONE.subtract( _factor.divide( _life, _cx ), _cx ).doubleValue();
		final BigDecimal remainingCost;
		final BigDecimal newCost;
		if (k <= 0) {
			remainingCost = (period == 1) ? _cost : ZERO;
			newCost = (period == 0) ? _cost : ZERO;
		}
		else {
			final double k_p1 = Math.pow( k, period - 1 );
			final double k_p = k_p1 * k;
			remainingCost = _cost.multiply( valueOf( k_p1 ), _cx );
			newCost = _cost.multiply( valueOf( k_p ), _cx );
		}

		BigDecimal depreciation = remainingCost.subtract( (newCost.compareTo( _salvage ) < 0 ? _salvage : newCost), _cx );
		if (depreciation.signum() < 0) {
			depreciation = ZERO;
		}
		return depreciation;
	}

	public static BigDecimal fun_VDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _start_period,
			BigDecimal _end_period, BigDecimal _factor, boolean _no_switch, MathContext _cx )
	{
		BigDecimal valVDB = ZERO;
		if (_start_period.compareTo( ZERO ) < 0
				|| _end_period.compareTo( _life ) > 0 || _cost.compareTo( ZERO ) < 0
				|| _end_period.compareTo( _start_period ) < 0 || _factor.compareTo( BigDecimal.valueOf( 0 ) ) < 0) {
			fun_ERROR( "#NUM! because of illegal argument values in VDB" );
		}
		else {
			if (_salvage.compareTo( _cost ) > 0) {
				return ZERO; // correct result
			}
			int loopStart = (int) Math.floor( _start_period.doubleValue() );
			int loopEnd = (int) Math.ceil( _end_period.doubleValue() );
			if (_no_switch) {
				for (int i = loopStart + 1; i <= loopEnd; i++) {
					BigDecimal valDDB = fun_DDB( _cost, _salvage, _life, BigDecimal.valueOf( i ), _factor, _cx );
					if (i == loopStart + 1) {
						valDDB = valDDB.multiply( _end_period.min( BigDecimal.valueOf( loopStart + 1 ) ).subtract(
								_start_period, _cx ), _cx );
					}
					else if (i == loopEnd) {
						valDDB = valDDB.multiply( _end_period.add( BigDecimal.valueOf( 1 - loopEnd ), _cx ) );
					}
					valVDB = valVDB.add( valDDB, _cx );
				}
			}
			else {
				BigDecimal _life2 = _life;
				BigDecimal start = _start_period;
				BigDecimal end = _end_period;
				BigDecimal part;
				if (start.compareTo( valueOf( Math.floor( start.doubleValue() ) ) ) != 0) {
					if (_factor.compareTo( ONE ) > 0) {
						if (start.compareTo( _life.divide( TWO ) ) >= 0) {
							// this part works like in Open Office
							part = start.subtract( _life.divide( TWO ), _cx );
							start = _life.divide( TWO, _cx );
							end = end.subtract( part, _cx );
							_life2 = _life2.add( ONE, _cx );
						}
					}
				}
				final BigDecimal cost = _cost.subtract( interVDB( _cost, _salvage, _life, _life2, start, _factor, _cx ) );
				valVDB = interVDB( cost, _salvage, _life, _life.subtract( start, _cx ), end.subtract( start, _cx ),
						_factor, _cx );
			}
		}
		return valVDB;
	}

	private static BigDecimal interVDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _life2,
			BigDecimal _period, BigDecimal _factor, MathContext _cx )
	{
		BigDecimal valVDB = ZERO;
		int loopEnd = (int) Math.ceil( _period.doubleValue() );
		BigDecimal salvageCost = _cost.subtract( _salvage, _cx );
		boolean flagSLN = false;
		BigDecimal valDDB, valTmpRes;
		BigDecimal valSLN = ZERO;
		for (int i = 1; i <= loopEnd; i++) {
			if (!flagSLN) {
				valDDB = fun_DDB( _cost, _salvage, _life, BigDecimal.valueOf( i ), _factor, _cx );
				valSLN = salvageCost.divide( _life2.add( BigDecimal.valueOf( 1 - i ) ), _cx );
				if (valSLN.compareTo( valDDB ) > 0) {
					valTmpRes = valSLN;
					flagSLN = true;
				}
				else {
					valTmpRes = valDDB;
					salvageCost = salvageCost.subtract( valDDB, _cx );
				}
			}
			else {
				valTmpRes = valSLN;
			}
			if (i == loopEnd) valTmpRes = valTmpRes.multiply( _period.add( BigDecimal.valueOf( 1 - loopEnd ) ), _cx );
			valVDB = valVDB.add( valTmpRes, _cx );
		}
		return valVDB;
	}

	public static BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv,
			BigDecimal _type, BigDecimal _guess, MathContext _cx )
	{
		final int MAX_ITER = 50;
		final boolean type = _type.signum() != 0;
		final int nper = _nper.intValue();
		BigDecimal eps = ONE;
		BigDecimal rate0 = _guess;
		for (int count = 0; eps.compareTo( EXCEL_EPSILON ) > 0 && count < MAX_ITER; count++) {
			final BigDecimal rate1;
			if (rate0.signum() == 0) {
				final BigDecimal a = _pmt.multiply( _nper, _cx );
				final BigDecimal b = a.add( type ? _pmt : _pmt.negate( _cx ), _cx );
				final BigDecimal c = _pv.add( _fv, _cx ).add( a, _cx );
				final BigDecimal d = _nper.multiply( _pv.add( b.divide( TWO, _cx ), _cx ), _cx );
				rate1 = rate0.subtract( c.divide( d, _cx ), _cx );
			}
			else {
				final BigDecimal a = rate0.add( ONE, _cx );
				final BigDecimal b = a.pow( nper - 1, _cx );
				final BigDecimal c = b.multiply( a, _cx );
				final BigDecimal d = _pmt.multiply( type ? ONE.add( rate0, _cx ) : ONE, _cx );
				final BigDecimal e = rate0.multiply( _nper, _cx ).multiply( b, _cx );
				final BigDecimal f = c.subtract( ONE, _cx );
				final BigDecimal g = rate0.multiply( _pv, _cx );
				final BigDecimal h = g.multiply( c, _cx ).add( d.multiply( f, _cx ), _cx ).add( rate0.multiply( _fv, _cx ),
						_cx );
				final BigDecimal k = g.multiply( e, _cx ).subtract( _pmt.multiply( f, _cx ), _cx ).add(
						d.multiply( e, _cx ), _cx );
				rate1 = rate0.multiply( ONE.subtract( h.divide( k, _cx ), _cx ), _cx );
			}
			eps = rate1.subtract( rate0, _cx ).abs( _cx );
			rate0 = rate1;
		}
		if (eps.compareTo( EXCEL_EPSILON ) >= 0) {
			fun_ERROR( "#NUM! because of result do not converge to within "
					+ EXCEL_EPSILON + " after " + MAX_ITER + " iterations in RATE" );
		}
		return rate0;
	}


	/**
	 * @deprecated replaced by {@link #fun_VALUE(String,Environment,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_VALUE( String _text, final Environment _environment )
	{
		return fun_VALUE( _text, _environment, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_VALUE( String _text, final Environment _environment, ComputationMode _mode )
	{
		return fromString( _text, _environment, _mode );
	}

	public static BigDecimal fromString( final String _text, final Environment _environment, final ComputationMode _mode )
	{
		final String text = _text.trim();
		final Number number = parseNumber( text, true, _environment, _mode == ComputationMode.EXCEL );
		if (number != null) {
			if (number instanceof BigDecimal) {
				return (BigDecimal) number;
			}
			else if (number instanceof Long) {
				return BigDecimal.valueOf( number.longValue() );
			}
			else {
				return valueOf( number.doubleValue() );
			}
		}
		else {
			throw new FormulaException( "#VALUE! because " + _text + " is not a number" );
		}
	}

	/**
	 * @deprecated replaced by {@link #fun_DATEVALUE(String,Environment,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_DATEVALUE( String _text, final Environment _environment )
	{
		return fun_DATEVALUE( _text, _environment, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_DATEVALUE( String _text, final Environment _environment, ComputationMode _mode )
	{
		return valueOf( RuntimeDouble_v2.fun_DATEVALUE( _text, _environment, _mode ) );
	}

	/**
	 * @deprecated replaced by {@link #fun_TIMEVALUE(String,Environment,ComputationMode)}
	 */
	@Deprecated
	public static BigDecimal fun_TIMEVALUE( String _text, final Environment _environment )
	{
		return fun_TIMEVALUE( _text, _environment, ComputationMode.EXCEL );
	}

	public static BigDecimal fun_TIMEVALUE( String _text, final Environment _environment, ComputationMode _mode )
	{
		return valueOf( RuntimeDouble_v2.fun_TIMEVALUE( _text, _environment, _mode ) );
	}

	public static int fun_MATCH_Exact( BigDecimal _x, BigDecimal[] _xs )
	{
		for (int i = 0; i < _xs.length; i++) {
			if (_x.equals( _xs[ i ] )) return i + 1; // Excel is 1-based
		}
		throw new NotAvailableException();
	}

	public static int fun_MATCH_Ascending( BigDecimal _x, BigDecimal[] _xs )
	{
		return fun_MATCH_Sorted( _xs, _x );
	}

	public static int fun_MATCH_Descending( final BigDecimal _x, BigDecimal[] _xs )
	{
		return fun_MATCH_Sorted( _xs, new Comparable<BigDecimal>()
		{
			public int compareTo( BigDecimal _o )
			{
				return -_x.compareTo( _o );
			}
		} );
	}


}
