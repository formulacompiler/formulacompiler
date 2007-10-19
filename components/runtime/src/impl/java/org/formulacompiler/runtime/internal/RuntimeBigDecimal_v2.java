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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;
import java.util.TimeZone;


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
		return (_value == null)? ZERO : new BigDecimal( _value );
	}

	public static BigDecimal newBigDecimal( final BigInteger _value )
	{
		return (_value == null)? ZERO : new BigDecimal( _value );
	}


	public static BigDecimal round( final BigDecimal _val, final int _maxFrac )
	{
		return _val.setScale( _maxFrac, RoundingMode.HALF_UP );
	}

	public static BigDecimal min( BigDecimal a, BigDecimal b )
	{
		if (a == EXTREMUM) return b;
		if (b == EXTREMUM) return a;
		return (a.compareTo( b ) <= 0)? a : b;
	}

	public static BigDecimal max( BigDecimal a, BigDecimal b )
	{
		if (a == EXTREMUM) return b;
		if (b == EXTREMUM) return a;
		return (a.compareTo( b ) >= 0)? a : b;
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
		return _val? ONE : ZERO;
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


	public static Date dateFromNum( final BigDecimal _excel, final TimeZone _timeZone )
	{
		return RuntimeDouble_v2.dateFromNum( _excel.doubleValue(), _timeZone );
	}

	public static BigDecimal dateToNum( final Date _date, final TimeZone _timeZone )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.dateToNum( _date, _timeZone ) );
	}

	private static BigDecimal valueOrZero( final double _value )
	{
		if (Double.isNaN( _value ) || Double.isInfinite( _value )) {
			return ZERO; // Excel #NUM!
		}
		else {
			return BigDecimal.valueOf( _value );
		}
	}


	public static String toExcelString( BigDecimal _num, Environment _environment )
	{
		return stringFromBigDecimal( _num, _environment );
	}


	public static BigDecimal fun_ACOS( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		if (a > 1 || a < -1) {
			return ZERO; // Excel #NUM!
		}
		return BigDecimal.valueOf( Math.acos( a ) );
	}

	public static BigDecimal fun_ACOSH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_ACOSH( a ) );
	}

	public static BigDecimal fun_ASIN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		if (a > 1 || a < -1) {
			return ZERO; // Excel #NUM!
		}
		return BigDecimal.valueOf( Math.asin( a ) );
	}

	public static BigDecimal fun_ASINH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_ASINH( a ) );
	}

	public static BigDecimal fun_ATAN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.atan( a ) );
	}

	public static BigDecimal fun_ATANH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_ATANH( a ) );
	}

	public static BigDecimal fun_ATAN2( BigDecimal _x, BigDecimal _y )
	{
		final double x = _x.doubleValue();
		final double y = _y.doubleValue();
		return BigDecimal.valueOf( Math.atan2( y, x ) );
	}

	public static BigDecimal fun_COS( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.cos( a ) );
	}

	public static BigDecimal fun_COSH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.cosh( a ) );
	}

	public static BigDecimal fun_SIN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.sin( a ) );
	}

	public static BigDecimal fun_SINH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_SINH( a ) );
	}

	public static BigDecimal fun_TAN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.tan( a ) );
	}

	public static BigDecimal fun_TANH( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.tanh( a ) );
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
		return valueOrZero( Math.pow( _n.doubleValue(), _p.doubleValue() ) );
	}

	public static BigDecimal fun_CEILING( BigDecimal _number, BigDecimal _significance, MathContext _cx )
	{
		final BigDecimal a = _number.divide( _significance, _cx );
		if (a.signum() < 0) {
			return ZERO; // Excel #NUM!
		}
		return a.setScale( 0, RoundingMode.UP ).multiply( _significance, _cx );
	}

	public static BigDecimal fun_FLOOR( BigDecimal _number, BigDecimal _significance, MathContext _cx )
	{
		final BigDecimal a = _number.divide( _significance, _cx );
		if (a.signum() < 0) {
			return ZERO; // Excel #NUM!
		}
		return a.setScale( 0, RoundingMode.DOWN ).multiply( _significance, _cx );
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
		return valueOrZero( result );
	}

	public static BigDecimal fun_LOG10( final BigDecimal _p )
	{
		final double result = Math.log10( _p.doubleValue() );
		return valueOrZero( result );
	}

	public static BigDecimal fun_ERF( BigDecimal _z )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_ERF( _z.doubleValue() ) );
	}

	public static BigDecimal fun_ERFC( BigDecimal _z )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_ERFC( _z.doubleValue() ) );
	}

	public static BigDecimal fun_BETADIST( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_BETADIST( _x.doubleValue(), _alpha.doubleValue(), _beta
				.doubleValue() ) );
	}

	public static BigDecimal fun_BINOMDIST( BigDecimal _successes, BigDecimal _trials, BigDecimal _probability,
			boolean _cumulative )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_BINOMDIST( _successes.intValue(), _trials.intValue(),
				_probability.doubleValue(), _cumulative ) );
	}

	public static BigDecimal fun_CHIDIST( BigDecimal _x, BigDecimal _degFreedom )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_CHIDIST( _x.doubleValue(), _degFreedom.doubleValue() ) );
	}

	public static BigDecimal fun_GAMMADIST( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta, boolean _cumulative )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_GAMMADIST( _x.doubleValue(), _alpha.doubleValue(), _beta
				.doubleValue(), _cumulative ) );
	}

	public static BigDecimal fun_POISSON( BigDecimal _x, BigDecimal _mean, boolean _cumulative )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_POISSON( _x.intValue(), _mean.doubleValue(), _cumulative ) );
	}

	public static BigDecimal fun_TDIST( BigDecimal _x, BigDecimal _degFreedom, BigDecimal _tails )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.fun_TDIST( _x.doubleValue(), _degFreedom.doubleValue(), _tails
				.intValue() ) );
	}

	public static BigDecimal fun_LOG( final BigDecimal _n, final BigDecimal _x )
	{
		final double lnN = Math.log( _n.doubleValue() );
		if (Double.isNaN( lnN ) || Double.isInfinite( lnN )) {
			return ZERO; // Excel #NUM!
		}
		final double lnX = Math.log( _x.doubleValue() );
		if (Double.isNaN( lnX ) || Double.isInfinite( lnX )) {
			return ZERO; // Excel #NUM!
		}
		if (lnX == 0) {
			return ZERO; // Excel #DIV/0!
		}
		return BigDecimal.valueOf( lnN / lnX );
	}

	public static BigDecimal fun_MOD( final BigDecimal _n, final BigDecimal _d )
	{
		if (_d.signum() == 0) {
			return ZERO; // Excel #DIV/0!
		}
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
		if (_n.signum() < 0) {
			return ZERO; // Excel #NUM!
		}

		// the Babylonian square root method (Newton's method)
		BigDecimal x0 = ZERO;
		BigDecimal x1 = new BigDecimal( Math.sqrt( _n.doubleValue() ) );

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
			return ZERO; // Excel #NUM!
		}
		else if (a < FACTORIALS.length) {
			return BigDecimal.valueOf( FACTORIALS[ a ] );
		}
		else {
			BigDecimal r = ONE;
			while (a > 1)
				r = r.multiply( BigDecimal.valueOf( a-- ), _cx );
			return r;
		}
	}


	public static BigDecimal fun_DATE( BigDecimal _year, BigDecimal _month, BigDecimal _day )
	{
		final int year = _year.intValue();
		final int month = _month.intValue();
		final int day = _day.intValue();
		final double result = RuntimeDouble_v2.fun_DATE( year, month, day );
		return BigDecimal.valueOf( result );
	}

	public static BigDecimal fun_WEEKDAY( BigDecimal _date, BigDecimal _type )
	{
		final double date = _date.doubleValue();
		final int type = _type.intValue();
		final int result = RuntimeDouble_v2.fun_WEEKDAY( date, type );
		return BigDecimal.valueOf( result );
	}

	public static BigDecimal fun_DAY( BigDecimal _date )
	{
		final double date = _date.doubleValue();
		final int result = RuntimeDouble_v2.fun_DAY( date );
		return BigDecimal.valueOf( result );
	}

	public static BigDecimal fun_MONTH( BigDecimal _date )
	{
		final double date = _date.doubleValue();
		final int result = RuntimeDouble_v2.fun_MONTH( date );
		return BigDecimal.valueOf( result );
	}

	public static BigDecimal fun_YEAR( BigDecimal _date )
	{
		final double date = _date.doubleValue();
		final int result = RuntimeDouble_v2.fun_YEAR( date );
		return BigDecimal.valueOf( result );
	}

	public static BigDecimal fun_NOW( final Environment _environment, final ComputationTime _computationTime )
	{
		return dateToNum( now( _computationTime ), _environment.timeZone() );
	}

	public static BigDecimal fun_TODAY( final Environment _environment, final ComputationTime _computationTime )
	{
		final TimeZone timeZone = _environment.timeZone();
		return dateToNum( today( timeZone, _computationTime ), timeZone );
	}

	public static BigDecimal fun_TIME( BigDecimal _hour, BigDecimal _minute, BigDecimal _second, MathContext _context )
	{
		final BigDecimal seconds = _hour.multiply( BIG_SECS_PER_HOUR ).add( _minute.multiply( BIG_SECS_PER_MINUTE ) )
				.add( _second ).remainder( BIG_SECS_PER_DAY );
		return seconds.divide( BIG_SECS_PER_DAY, _context );
	}

	public static BigDecimal fun_SECOND( BigDecimal _date )
	{
		final long seconds = getDaySecondsFromNum( _date ) % 60;
		return BigDecimal.valueOf( seconds );
	}

	public static BigDecimal fun_MINUTE( BigDecimal _date )
	{
		final long minutes = getDaySecondsFromNum( _date ) / 60 % 60;
		return BigDecimal.valueOf( minutes );
	}

	public static BigDecimal fun_HOUR( BigDecimal _date )
	{
		final long hours = getDaySecondsFromNum( _date ) / Runtime_v2.SECS_PER_HOUR % 24;
		return BigDecimal.valueOf( hours );
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
		throw new IllegalArgumentException( "IRR does not converge" );
	}

	public static BigDecimal fun_DB( final BigDecimal _cost, final BigDecimal _salvage, final BigDecimal _life,
			final BigDecimal _period, final BigDecimal _month, MathContext _cx )
	{
		final BigDecimal month = _month.setScale( 0, RoundingMode.HALF_UP );
		final BigDecimal rate = BigDecimal.valueOf(
				1 - Math.pow( (_salvage.doubleValue() / _cost.doubleValue()), (1 / _life.doubleValue()) ) ).setScale( 3,
				RoundingMode.HALF_UP );
		final BigDecimal depreciation1 = _cost.multiply( rate, _cx ).multiply( month, _cx ).divide( TWELVE, _cx );
		BigDecimal depreciation = depreciation1;
		if (_period.intValue() > 1) {
			BigDecimal totalDepreciation = depreciation1;
			final int maxPeriod = (_life.compareTo( _period ) > 0? _period : _life).intValue();
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
			remainingCost = (period == 1)? _cost : ZERO;
			newCost = (period == 0)? _cost : ZERO;
		}
		else {
			final double k_p1 = Math.pow( k, period - 1 );
			final double k_p = k_p1 * k;
			remainingCost = _cost.multiply( BigDecimal.valueOf( k_p1 ), _cx );
			newCost = _cost.multiply( BigDecimal.valueOf( k_p ), _cx );
		}

		BigDecimal depreciation = remainingCost.subtract( (newCost.compareTo( _salvage ) < 0? _salvage : newCost), _cx );
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
				|| _end_period.compareTo( _start_period ) < 0 || _end_period.compareTo( _life ) > 0
				|| _cost.compareTo( ZERO ) < 0 || _salvage.compareTo( _cost ) > 0
				|| _factor.compareTo( BigDecimal.valueOf( 0 ) ) <= 0) {
			return ZERO;
		}
		else {
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
				if (start.compareTo( BigDecimal.valueOf( Math.floor( start.doubleValue() ) ) ) != 0) {
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
				final BigDecimal b = a.add( type? _pmt : _pmt.negate( _cx ), _cx );
				final BigDecimal c = _pv.add( _fv, _cx ).add( a, _cx );
				final BigDecimal d = _nper.multiply( _pv.add( b.divide( TWO, _cx ), _cx ), _cx );
				rate1 = rate0.subtract( c.divide( d, _cx ), _cx );
			}
			else {
				final BigDecimal a = rate0.add( ONE, _cx );
				final BigDecimal b = a.pow( nper - 1, _cx );
				final BigDecimal c = b.multiply( a, _cx );
				final BigDecimal d = _pmt.multiply( type? ONE.add( rate0, _cx ) : ONE, _cx );
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
			return ZERO; // Excel #NUM!
		}
		return rate0;
	}


	public static BigDecimal fun_VALUE( String _text, final Environment _environment )
	{
		final String text = _text.trim();
		final Number number = parseNumber( text, true, _environment );
		if (number != null) {
			if (number instanceof BigDecimal) {
				return (BigDecimal) number;
			}
			else if (number instanceof Long) {
				return BigDecimal.valueOf( number.longValue() );
			}
			else {
				return BigDecimal.valueOf( number.doubleValue() );
			}
		}
		else {
			return ZERO; // Excel #NUM!
		}
	}


	public static int fun_MATCH_Exact( BigDecimal _x, BigDecimal[] _xs )
	{
		for (int i = 0; i < _xs.length; i++) {
			if (_x.equals( _xs[ i ] )) return i + 1; // Excel is 1-based
		}
		return 0;
	}

	public static int fun_MATCH_Ascending( BigDecimal _x, BigDecimal[] _xs )
	{
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_x.compareTo( _xs[ iMid ] ) > 0) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _x.compareTo( _xs[ iLeft ] ) < 0) iLeft--;
		return iLeft + 1; // Excel is 1-based
	}

	public static int fun_MATCH_Descending( BigDecimal _x, BigDecimal[] _xs )
	{
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_x.compareTo( _xs[ iMid ] ) < 0) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _x.compareTo( _xs[ iLeft ] ) > 0) iLeft--;
		return iLeft + 1; // Excel is 1-based
	}


}
