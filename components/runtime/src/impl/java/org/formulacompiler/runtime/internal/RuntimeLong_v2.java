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
import java.util.Date;
import java.util.TimeZone;

import org.formulacompiler.runtime.NotAvailableException;
import org.formulacompiler.runtime.ScaledLongSupport;
import org.formulacompiler.runtime.FormulaException;


public final class RuntimeLong_v2 extends Runtime_v2
{
	private static final long[] ONE_AT_SCALE = ScaledLongSupport.ONE;
	private static final long[] HALF_AT_SCALE = new long[ ONE_AT_SCALE.length ];

	static {
		for (int i = 0; i < ONE_AT_SCALE.length; i++) {
			HALF_AT_SCALE[ i ] = ONE_AT_SCALE[ i ] / 2;
		}
	}


	public static final class Context
	{
		final int scale;
		final long one;
		final double oneAsDouble;

		public Context( final int _scale )
		{
			super();
			this.scale = _scale;
			if (_scale < 0 || _scale >= ONE_AT_SCALE.length) {
				throw new IllegalArgumentException( "Scale is out of range" );
			}
			this.one = ONE_AT_SCALE[ _scale ];
			this.oneAsDouble = this.one;
		}

		public final int scale()
		{
			return this.scale;
		}

		public final long one()
		{
			return this.one;
		}

		double toDouble( long _value )
		{
			if (_value == 0) {
				return 0.0;
			}
			else if (this.scale == 0) {
				return _value;
			}
			else {
				return _value / this.oneAsDouble;
			}
		}

		double[] toDoubles( long[] _longs )
		{
			final int len = _longs.length;
			final double[] doubles = new double[ len ];
			for (int i = 0; i < len; i++) {
				doubles[ i ] = toDouble( _longs[ i ] );
			}
			return doubles;
		}

		long fromDouble( final double _value )
		{
			if (_value == 0.0) {
				return 0L;
			}
			checkDouble( _value );
			if (this.scale == 0) {
				return Math.round( _value );
			}
			else {
				return Math.round( _value * this.oneAsDouble );
			}
		}

		public long fromBigDecimal( BigDecimal _value )
		{
			if (_value == null) {
				return 0L;
			}
			else if (this.scale == 0) {
				return _value.longValue();
			}
			else {
				return RuntimeScaledBigDecimal_v2.toScaledLong( _value, this.scale );
			}
		}

		public BigDecimal toBigDecimal( long _value )
		{
			if (_value == 0) {
				return RuntimeScaledBigDecimal_v2.ZERO;
			}
			else if (this.scale == 0) {
				return BigDecimal.valueOf( _value );
			}
			else {
				return RuntimeScaledBigDecimal_v2.fromScaledLong( _value, this.scale );
			}
		}

		private int toInt( long _value )
		{
			return (int) (_value / this.one);
		}

	}


	public static long max( final long a, final long b )
	{
		return a >= b ? a : b;
	}

	public static long min( final long a, final long b )
	{
		return a <= b ? a : b;
	}

	public static long round( final long _val, final int _maxFrac, Context _cx )
	{
		if (_val == 0 || _maxFrac >= _cx.scale) {
			return _val;
		}
		else {
			final int truncateAt = _cx.scale - _maxFrac;
			final long shiftFactor = ONE_AT_SCALE[ truncateAt ];
			final long roundingCorrection = HALF_AT_SCALE[ truncateAt ];
			if (_val >= 0) {
				// I have: 123456 (scale = 3)
				// I want: 123500 (_maxFrac = 1)
				// So: (v + 50) / 100 * 100
				return (_val + roundingCorrection) / shiftFactor * shiftFactor;
			}
			else {
				// I have: -123456 (scale = 3)
				// I want: -123500 (_maxFrac = 1)
				// So: (v - 50) / 100 * 100
				return (_val - roundingCorrection) / shiftFactor * shiftFactor;
			}
		}
	}

	private static long trunc( final long _val, final int _maxFrac, Context _cx )
	{
		if (_val == 0 || _maxFrac >= _cx.scale) {
			return _val;
		}
		else {
			final int truncateAt = _cx.scale - _maxFrac;
			final long shiftFactor = ONE_AT_SCALE[ truncateAt ];
			return _val / shiftFactor * shiftFactor;
		}
	}

	public static Date dateFromNum( final long _val, Context _cx, final TimeZone _timeZone )
	{
		return RuntimeDouble_v2.dateFromNum( toDouble( _val, _cx ), _timeZone );
	}

	public static long msSinceUTC1970FromNum( long _msSinceUTC1970, Context _cx, TimeZone _timeZone )
	{
		return msSinceUTC1970FromDouble( toDouble( _msSinceUTC1970, _cx ), _timeZone );
	}

	public static long msFromNum( long _msSinceUTC1970, Context _cx )
	{
		return msFromDouble( toDouble( _msSinceUTC1970, _cx ) );
	}

	public static long dateToNum( final Date _val, Context _cx, final TimeZone _timeZone )
	{
		return fromDouble( RuntimeDouble_v2.dateToNum( _val, _timeZone ), _cx );
	}

	public static long msSinceUTC1970ToNum( long _msSinceUTC1970, Context _cx, TimeZone _timeZone )
	{
		return fromDouble( msSinceUTC1970ToDouble( _msSinceUTC1970, _timeZone ), _cx );
	}

	public static long msToNum( long _msSinceUTC1970, Context _cx )
	{
		return fromDouble( msToDouble( _msSinceUTC1970 ), _cx );
	}

	public static long fromDouble( double _val, Context _cx )
	{
		return _cx.fromDouble( _val );
	}

	public static double toDouble( long _val, Context _cx )
	{
		return _cx.toDouble( _val );
	}

	public static long fromBigDecimal( BigDecimal _val, Context _cx )
	{
		return _cx.fromBigDecimal( _val );
	}

	public static BigDecimal toBigDecimal( long _val, Context _cx )
	{
		return _cx.toBigDecimal( _val );
	}


	public static String toExcelString( long _val, Context _cx, Environment _environment )
	{
		return toExcelString( _val, _cx.scale, _environment );
	}

	public static String toExcelString( long _value, int _scale, Environment _environment )
	{
		if (_value == 0) {
			return "0";
		}
		else if (_scale == 0) {
			return Long.toString( _value );
		}
		else {
			return stringFromBigDecimal( RuntimeScaledBigDecimal_v2.fromScaledLong( _value, _scale ), _environment );
		}
	}


	public static long fun_ACOS( final long _val, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_ACOS( _cx.toDouble( _val ) ) );
	}

	public static long fun_ACOSH( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( RuntimeDouble_v2.fun_ACOSH( a ) );
	}

	public static long fun_ASIN( final long _val, Context _cx )
	{
		return _cx.fromDouble( Math.asin( _cx.toDouble( _val ) ) );
	}

	public static long fun_ASINH( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( RuntimeDouble_v2.fun_ASINH( a ) );
	}

	public static long fun_ATAN( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.atan( a ) );
	}

	public static long fun_ATANH( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( RuntimeDouble_v2.fun_ATANH( a ) );
	}

	public static long fun_ATAN2( final long _x, final long _y, Context _cx )
	{
		final double x = _cx.toDouble( _x );
		final double y = _cx.toDouble( _y );
		return _cx.fromDouble( Math.atan2( y, x ) );
	}

	public static long fun_COS( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.cos( a ) );
	}

	public static long fun_COSH( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.cosh( a ) );
	}


	public static long fun_SIN( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.sin( a ) );
	}

	public static long fun_SINH( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( RuntimeDouble_v2.fun_SINH( a ) );
	}

	public static long fun_TAN( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.tan( a ) );
	}

	public static long fun_TANH( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.tanh( a ) );
	}

	public static long fun_DEGREES( long _a, Context _cx )
	{
		return _cx.fromDouble( Math.toDegrees( _cx.toDouble( _a ) ) );
	}

	public static long fun_RADIANS( long _a, Context _cx )
	{
		return _cx.fromDouble( Math.toRadians( _cx.toDouble( _a ) ) );
	}

	public static long fun_PI( Context _cx )
	{
		return _cx.fromDouble( Math.PI );
	}

	public static long fun_CEILING( final long _number, final long _significance, final Context _cx )
	{
		long a = _number / _significance;
		if (a < 0) {
			err_CEILING();
		}
		if (a * _significance != _number) {
			a++;
		}
		return a * _significance;
	}

	public static long fun_FLOOR( final long _number, final long _significance, final Context _cx )
	{
		final long a = _number / _significance;
		if (a < 0) {
			err_FLOOR();
		}
		return a * _significance;
	}

	public static long fun_ROUND( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return round( _val, (int) _maxFrac, _cx );
		return round( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	public static long fun_RAND( Context _cx )
	{
		return _cx.fromDouble( generator.nextDouble() );
	}

	public static long fun_ROUNDDOWN( final long _val, final long _maxFrac, Context _cx )
	{
		return trunc( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	public static long fun_ROUNDUP( final long _val, final long _maxFrac, Context _cx )
	{
		final int maxFrac = (int) (_maxFrac / _cx.one);
		final int truncateAt = _cx.scale - maxFrac;
		final long shiftFactor = ONE_AT_SCALE[ truncateAt ];
		final long truncated = _val / shiftFactor * shiftFactor;
		if (_val == truncated) {
			return truncated;
		}
		else if (_val < 0) {
			return truncated - shiftFactor;
		}
		else {
			return truncated + shiftFactor;
		}
	}

	public static long fun_TRUNC( final long _val, final long _maxFrac, Context _cx )
	{
		return trunc( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	public static long fun_TRUNC( final long _val, Context _cx )
	{
		return trunc( _val, 0, _cx );
	}

	public static long fun_EVEN( final long _val, Context _cx )
	{
		final long shiftFactor = _cx.one * 2;
		final long truncated = _val / shiftFactor * shiftFactor;
		if (_val == truncated) {
			return truncated;
		}
		else if (_val < 0) {
			return truncated - shiftFactor;
		}
		else {
			return truncated + shiftFactor;
		}
	}

	public static long fun_ODD( final long _val, Context _cx )
	{
		final long oneAtScale = _cx.one;
		final long shiftFactor = oneAtScale * 2;
		if (_val < 0) {
			final long truncated = (_val - oneAtScale) / shiftFactor * shiftFactor + oneAtScale;
			if (truncated == _val) {
				return truncated;
			}
			else {
				return truncated - shiftFactor;
			}
		}
		else {
			final long truncated = (_val + oneAtScale) / shiftFactor * shiftFactor - oneAtScale;
			if (truncated == _val) {
				return truncated;
			}
			else {
				return truncated + shiftFactor;
			}
		}
	}

	public static long fun_INT( final long _val, Context _cx )
	{
		if (_cx.scale == 0) {
			return _val;
		}
		else {
			final long shiftFactor = _cx.one;
			final long truncated = _val / shiftFactor * shiftFactor;
			if (_val < 0 && _val != truncated) {
				return truncated - shiftFactor;
			}
			else {
				return truncated;
			}
		}
	}

	public static long fun_EXP( long _p, Context _cx )
	{
		return _cx.fromDouble( Math.exp( _cx.toDouble( _p ) ) );
	}

	public static long fun_POWER( long _n, long _p, Context _cx )
	{
		return _cx.fromDouble( Math.pow( _cx.toDouble( _n ), _cx.toDouble( _p ) ) );
	}

	public static long fun_LN( long _p, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_LN( _cx.toDouble( _p ) ) );
	}

	public static long fun_LOG( long _n, long _x, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_LOG( _cx.toDouble( _n ), _cx.toDouble( _x ) ) );
	}

	public static long fun_LOG10( long _p, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_LOG10( _cx.toDouble( _p ) ) );
	}

	public static long fun_ERF( long _x, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_ERF( _cx.toDouble( _x ) ) );
	}

	public static long fun_ERFC( long _x, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_ERFC( _cx.toDouble( _x ) ) );
	}

	public static long fun_BETADIST( long _x, long _alpha, long _beta, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_BETADIST( _cx.toDouble( _x ), _cx.toDouble( _alpha ), _cx
				.toDouble( _beta ) ) );
	}

	public static long fun_BETAINV( long _x, long _alpha, long _beta, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_BETAINV( _cx.toDouble( _x ), _cx.toDouble( _alpha ), _cx
				.toDouble( _beta ) ) );
	}

	public static long fun_BINOMDIST( long _successes, long _trials, long _probability, boolean _cumulative, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_BINOMDIST( _cx.toInt( _successes ), _cx.toInt( _trials ), _cx
				.toDouble( _probability ), _cumulative ) );
	}

	public static long fun_CHIDIST( long _x, long _degFreedom, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_CHIDIST( _cx.toDouble( _x ), _cx.toDouble( _degFreedom ) ) );
	}

	public static long fun_CHIINV( long _x, long _degFreedom, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_CHIINV( _cx.toDouble( _x ), _cx.toDouble( _degFreedom ) ) );
	}

	public static long fun_CRITBINOM( long _n, long _p, long _alpha, Context _cx )
	{
		// p <= 0 is contrary to Excel's docs where it says p < 0; but the test case says otherwise.
		if (_n < 0 || _p <= 0 || _p >= _cx.one || _alpha <= 0 || _alpha >= _cx.one) {
			fun_ERROR( "#NUM! because not n >= 0, 0 < p < 1, 0 < alpha < 1 in CRITBINOM" );
		}
		long n = fun_INT( _n, _cx );
		long q = _cx.one - _p;
		final long EPSILON = _cx.fromDouble( 0.1E-320 );
		long factor = fun_POWER( q, fun_INT( _n, _cx ), _cx );
		if (factor <= EPSILON) {
			factor = fun_POWER( _p, n, _cx );
			if (factor <= EPSILON) {
				throw new FormulaException( "#NUM! because factor = 0 in CRITBINOM" );
			}
			else {
				long sum = _cx.one - factor;
				long i;
				for (i = 0; i < n && sum >= _alpha; i = i + _cx.one) {
					factor = factor * (n - i) / (i + _cx.one) * q / _p;
					sum = sum - factor;
				}
				return n - i;
			}
		}
		else {
			long sum = factor;
			long i;
			for (i = 0; i < n && sum < _alpha; i = i + _cx.one) {
				factor = factor * (n - i) / (i + _cx.one) * _p / q;
				sum = sum + factor;
			}
			return i;
		}
	}

	public static long fun_FINV( long _x, long _f1, long _f2, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_FINV( _cx.toDouble( _x ), _cx.toDouble( _f1 ), _cx.toDouble( _f2 ) ) );
	}

	public static long fun_GAMMADIST( long _x, long _alpha, long _beta, boolean _cumulative, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_GAMMADIST( _cx.toDouble( _x ), _cx.toDouble( _alpha ), _cx
				.toDouble( _beta ), _cumulative ) );
	}

	public static long fun_GAMMAINV( long _x, long _alpha, long _beta, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_GAMMAINV( _cx.toDouble( _x ), _cx.toDouble( _alpha ), _cx
				.toDouble( _beta ) ) );
	}

	public static long fun_GAMMALN( long _x, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_GAMMALN( _cx.toDouble( _x ) ) );
	}

	public static long fun_POISSON( long _x, long _mean, boolean _cumulative, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_POISSON( _cx.toInt( _x ), _cx.toDouble( _mean ), _cumulative ) );
	}

	public static long fun_TDIST( long _x, long _degFreedom, long _tails, boolean _no_floor, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_TDIST( _cx.toDouble( _x ), _cx.toDouble( _degFreedom ), _cx
				.toInt( _tails ), _no_floor ) );
	}

	public static long fun_TINV( long _x, long _degFreedom, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_TINV( _cx.toDouble( _x ), _cx.toDouble( _degFreedom ) ) );
	}

	public static long fun_WEIBULL( long _x, long _alpha, long _beta, boolean _cumulative, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_WEIBULL( _cx.toDouble( _x ), _cx.toDouble( _alpha ), _cx
				.toDouble( _beta ), _cumulative ) );
	}

	public static long fun_MOD( final long _n, final long _d, final Context _cx )
	{
		final long remainder = _n % _d;
		if (remainder != 0 && Long.signum( remainder ) != Long.signum( _d )) {
			return remainder + _d;
		}
		else {
			return remainder;
		}
	}

	public static long fun_SQRT( final long _n, final Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_SQRT( _cx.toDouble( _n ) ) );
	}

	public static long fun_DATE( final long _year, final long _month, final long _day, final Context _cx )
	{
		final long one = _cx.one();
		final int year = (int) (_year / one);
		final int month = (int) (_month / one);
		final int day = (int) (_day / one);
		return _cx.fromDouble( RuntimeDouble_v2.fun_DATE( year, month, day ) );
	}

	public static long fun_TIME( long _hour, long _minute, long _second, Context _cx )
	{
		final long seconds = (_hour * SECS_PER_HOUR + _minute * 60 + _second) % (SECS_PER_DAY * _cx.one());
		return seconds / SECS_PER_DAY;
	}

	public static long fun_SECOND( long _date, Context _cx )
	{
		final long seconds = RuntimeDouble_v2.getDaySecondsFromNum( _cx.toDouble( _date ) ) % 60;
		return seconds * _cx.one();
	}

	public static long fun_MINUTE( long _date, Context _cx )
	{
		final long minutes = RuntimeDouble_v2.getDaySecondsFromNum( _cx.toDouble( _date ) ) / 60 % 60;
		return minutes * _cx.one();
	}

	public static long fun_HOUR( long _date, Context _cx )
	{
		final long hours = RuntimeDouble_v2.getDaySecondsFromNum( _cx.toDouble( _date ) ) / SECS_PER_HOUR % 24;
		return hours * _cx.one();
	}

	public static long fun_HYPGEOMDIST( long _sample_s, long _number_sample, long _population_s,
			long _number_population, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_HYPGEOMDIST( _cx.toInt( _sample_s ), _cx.toInt( _number_sample ), _cx
				.toInt( _population_s ), _cx.toInt( _number_population ) ) );
	}

	public static long fun_WEEKDAY( final long _date, final long _type, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int type = (int) (_type / _cx.one());
		final int result = RuntimeDouble_v2.fun_WEEKDAY( date, type );
		return result * _cx.one();
	}

	public static long fun_DAY( long _date, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int result = RuntimeDouble_v2.fun_DAY( date );
		return result * _cx.one();
	}

	public static long fun_DAYS360( long _date_start, long _end_start, boolean _method, final Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_DAYS360( _cx.toDouble( _date_start ), _cx.toDouble( _end_start ),
				_method ) );
	}

	public static long fun_MONTH( long _date, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int result = RuntimeDouble_v2.fun_MONTH( date );
		return result * _cx.one();
	}

	public static long fun_YEAR( long _date, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int result = RuntimeDouble_v2.fun_YEAR( date );
		return result * _cx.one();
	}

	public static long fun_NOW( Context _cx, final Environment _environment, final ComputationTime _computationTime )
	{
		return dateToNum( now( _computationTime ), _cx, _environment.timeZone() );
	}

	public static long fun_SIGN( long _a, Context _cx )
	{
		final double a = _cx.toDouble( _a );
		return _cx.fromDouble( Math.signum( a ) );
	}

	public static long fun_TODAY( Context _cx, final Environment _environment, final ComputationTime _computationTime )
	{
		final TimeZone timeZone = _environment.timeZone();
		return dateToNum( today( timeZone, _computationTime ), _cx, timeZone );
	}

	public static long fun_FACT( long _a )
	{
		if (_a < 0) {
			err_FACT();
		}
		int a = (int) _a;
		if (a < FACTORIALS.length) {
			return FACTORIALS[ a ];
		}
		else {
			throw new ArithmeticException( "Overflow in FACT() using (scaled) long." );
		}
	}

	public static long fun_IRR( long[] _values, long _guess, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_IRR( _cx.toDoubles( _values ), _cx.toDouble( _guess ) ) );
	}

	public static long fun_DB( long _cost, long _salvage, long _life, long _period, long _month, Context _cx )
	{
		final long month = _month / _cx.one(); // unscaled
		final long rate = round( _cx.fromDouble( 1 - Math.pow( (double) _salvage / (double) _cost, (double) _cx.one()
				/ (double) _life ) ), 3, _cx );
		final long depreciation1 = _cost * rate * month / (12 * _cx.one());
		long depreciation = depreciation1;
		if (_period / _cx.one() > 1) {
			long totalDepreciation = depreciation1;
			final int maxPeriod = (int) ((_life > _period ? _period : _life) / _cx.one());
			for (int i = 2; i <= maxPeriod; i++) {
				depreciation = (_cost - totalDepreciation) * rate / _cx.one();
				totalDepreciation += depreciation;
			}
			if (_period > _life) {
				depreciation = (_cost - totalDepreciation) * rate * (12 - month) / (12 * _cx.one());
			}
		}
		return depreciation;
	}


	public static long fun_VALUE( String _text, Context _cx, final Environment _environment )
	{
		final String text = _text.trim();
		final Number number = parseNumber( text, false, _environment );
		if (number != null) {
			if (number instanceof Long) {
				return number.longValue() * _cx.one();
			}
			else {
				return _cx.fromDouble( number.doubleValue() );
			}
		}
		else {
			throw new FormulaException( "#VALUE! because of argument of unsupported type in VALUE" );
		}
	}

	public static long fun_DATEVALUE( String _text, Context _cx, final Environment _environment )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_DATEVALUE( _text, _environment ) );
	}

	public static long fun_TIMEVALUE( String _text, Context _cx, final Environment _environment )
	{
		return _cx.fromDouble( RuntimeDouble_v2.fun_TIMEVALUE( _text, _environment ) );
	}

	public static int fun_MATCH_Exact( long _x, long[] _xs )
	{
		for (int i = 0; i < _xs.length; i++) {
			if (_x == _xs[ i ]) return i + 1; // Excel is 1-based
		}
		throw new NotAvailableException();
	}

	public static int fun_MATCH_Ascending( long _x, long[] _xs )
	{
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_x > _xs[ iMid ]) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _x < _xs[ iLeft ]) iLeft--;
		if (iLeft < 0) fun_NA();
		return iLeft + 1; // Excel is 1-based
	}

	public static int fun_MATCH_Descending( long _x, long[] _xs )
	{
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_x < _xs[ iMid ]) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _x > _xs[ iLeft ]) iLeft--;
		if (iLeft < 0) fun_NA();
		return iLeft + 1; // Excel is 1-based
	}


}
