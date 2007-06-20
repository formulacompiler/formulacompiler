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

import org.formulacompiler.runtime.ScaledLongSupport;


public final class RuntimeLong_v1 extends Runtime_v1
{
	public static long[] ONE_AT_SCALE = ScaledLongSupport.ONE;
	public static long[] HALF_AT_SCALE = new long[ ONE_AT_SCALE.length ];

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

		public Context(final int _scale)
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
			else if (Double.isNaN( _value ) || Double.isInfinite( _value )) {
				return 0L; // Excel #NUM
			}
			else if (this.scale == 0) {
				return Math.round( _value );
			}
			else {
				return Math.round( _value * this.oneAsDouble );
			}
		}

		long fromBoxedDouble( Number _value )
		{
			if (_value == null) {
				return 0L;
			}
			else {
				return fromDouble( _value.doubleValue() );
			}
		}

		@Deprecated
		long fromNumber( Number _value )
		{
			if (_value == null) {
				return 0L;
			}
			else if (this.scale == 0) {
				return _value.longValue();
			}
			else {
				return _value.longValue() * this.one;
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
				return RuntimeBigDecimal_v1.toScaledLong( _value, this.scale );
			}
		}

		public BigDecimal toBigDecimal( long _value )
		{
			if (_value == 0) {
				return RuntimeBigDecimal_v1.ZERO;
			}
			else if (this.scale == 0) {
				return BigDecimal.valueOf( _value );
			}
			else {
				return RuntimeBigDecimal_v1.fromScaledLong( _value, this.scale );
			}
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

	@Deprecated
	public static long pow( final long x, final long n, Context _cx )
	{
		return _cx.fromDouble( Math.pow( _cx.toDouble( x ), _cx.toDouble( n ) ) );
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

	public static long trunc( final long _val, final int _maxFrac, Context _cx )
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

	@Deprecated
	public static long stdROUND( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return round( _val, (int) _maxFrac, _cx );
		return round( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	@Deprecated
	public static long stdTODAY( Context _cx )
	{
		return dateToNum( today(), _cx, TimeZone.getDefault() );
	}


	public static boolean booleanFromNum( final long _val )
	{
		return (_val != 0);
	}

	public static long booleanToNum( final boolean _val, Context _cx )
	{
		return _val ? _cx.one : 0;
	}

	public static Date dateFromNum( final long _val, Context _cx, final TimeZone _timeZone )
	{
		return RuntimeDouble_v1.dateFromNum( toDouble( _val, _cx ), _timeZone );
	}

	public static long dateToNum( final Date _val, Context _cx, final TimeZone _timeZone )
	{
		return fromDouble( RuntimeDouble_v1.dateToNum( _val, _timeZone ), _cx );
	}

	@Deprecated
	public static long fromNumber( Number _val, Context _cx )
	{
		return _cx.fromNumber( _val );
	}

	public static long fromDouble( double _val, Context _cx )
	{
		return _cx.fromDouble( _val );
	}

	public static long fromBoxedDouble( Number _val, Context _cx )
	{
		return _cx.fromBoxedDouble( _val );
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


	public static String toExcelString( long _val, Context _cx )
	{
		return toExcelString( _val, _cx.scale );
	}

	public static String toExcelString( long _value, int _scale )
	{
		if (_value == 0) {
			return "0";
		}
		else if (_scale == 0) {
			return Long.toString( _value );
		}
		else {
			return stringFromBigDecimal( RuntimeBigDecimal_v1.fromScaledLong( _value, _scale ) );
		}
	}


	public static long fun_ACOS( final long _val, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v1.fun_ACOS( _cx.toDouble( _val ) ) );
	}

	public static long fun_ASIN( final long _val, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v1.fun_ASIN( _cx.toDouble( _val ) ) );
	}

	public static long fun_ATAN( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.atan( a ) );
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

	public static long fun_SIN( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.sin( a ) );
	}

	public static long fun_TAN( final long _val, Context _cx )
	{
		final double a = _cx.toDouble( _val );
		return _cx.fromDouble( Math.tan( a ) );
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

	public static long fun_ROUND( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return round( _val, (int) _maxFrac, _cx );
		return round( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	public static long fun_TRUNC( final long _val, final long _maxFrac, Context _cx )
	{
		if (_cx.scale == 0) return trunc( _val, (int) _maxFrac, _cx );
		return trunc( _val, (int) (_maxFrac / _cx.one), _cx );
	}

	public static long fun_TRUNC( final long _val, Context _cx )
	{
		return trunc( _val, 0, _cx );
	}

	public static long fun_EVEN( final long _val, Context _cx )
	{
		final long shiftFactor = ONE_AT_SCALE[ _cx.scale ] * 2;
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
		final long oneAtScale = ONE_AT_SCALE[ _cx.scale ];
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
			final long shiftFactor = ONE_AT_SCALE[ _cx.scale ];
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
		return _cx.fromDouble( RuntimeDouble_v1.fun_LN( _cx.toDouble( _p ) ) );
	}

	public static long fun_LOG( long _n, long _x, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v1.fun_LOG( _cx.toDouble( _n ), _cx.toDouble( _x ) ) );
	}

	public static long fun_LOG10( long _p, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v1.fun_LOG10( _cx.toDouble( _p ) ) );
	}

	public static long fun_MOD( final long _n, final long _d, final Context _cx )
	{
		if (_d == 0) {
			return 0; // Excel #DIV/0!
		}
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
		return _cx.fromDouble( RuntimeDouble_v1.fun_SQRT( _cx.toDouble( _n ) ) );
	}

	public static long fun_DATE( final long _year, final long _month, final long _day, final Context _cx )
	{
		final long one = _cx.one();
		final int year = (int) (_year / one);
		final int month = (int) (_month / one);
		final int day = (int) (_day / one);
		return _cx.fromDouble( RuntimeDouble_v1.excelDateToNum( year, month, day ) );
	}

	public static long fun_TIME( long _hour, long _minute, long _second, Context _cx )
	{
		final long seconds = (_hour * SECS_PER_HOUR + _minute * 60 + _second) % (SECS_PER_DAY * _cx.one());
		return seconds / SECS_PER_DAY;
	}

	public static long fun_SECOND( long _date, Context _cx )
	{
		final long seconds = RuntimeDouble_v1.getDaySecondsFromNum( _cx.toDouble( _date ) ) % 60;
		return seconds * _cx.one();
	}

	public static long fun_MINUTE( long _date, Context _cx )
	{
		final long minutes = RuntimeDouble_v1.getDaySecondsFromNum( _cx.toDouble( _date ) ) / 60 % 60;
		return minutes * _cx.one();
	}

	public static long fun_HOUR( long _date, Context _cx )
	{
		final long hours = RuntimeDouble_v1.getDaySecondsFromNum( _cx.toDouble( _date ) ) / SECS_PER_HOUR % 24;
		return hours * _cx.one();
	}

	public static long fun_WEEKDAY( final long _date, final long _type, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int type = (int) (_type / _cx.one());
		final int result = RuntimeDouble_v1.getWeekDayFromNum( date, type );
		return result * _cx.one();
	}

	public static long fun_DAY( long _date, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int result = RuntimeDouble_v1.getDayFromNum( date );
		return result * _cx.one();
	}

	public static long fun_MONTH( long _date, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int result = RuntimeDouble_v1.getMonthFromNum( date );
		return result * _cx.one();
	}

	public static long fun_YEAR( long _date, final Context _cx )
	{
		final double date = _cx.toDouble( _date );
		final int result = RuntimeDouble_v1.getYearFromNum( date );
		return result * _cx.one();
	}

	public static long fun_NOW( Context _cx, final Environment _environment )
	{
		return dateToNum( now(), _cx, _environment.timeZone );
	}

	public static long fun_TODAY( Context _cx, final Environment _environment )
	{
		return dateToNum( today(), _cx, _environment.timeZone );
	}

	public static long fun_FACT( long _a )
	{
		if (_a < 0) {
			return 0; // Excel #NUM!
		}
		else {
			int a = (int) _a;
			if (a < FACTORIALS.length) {
				return FACTORIALS[ a ];
			}
			else {
				throw new ArithmeticException( "Overflow in FACT() using (scaled) long." );
			}
		}
	}

	public static long fun_IRR( long[] _values, long _guess, Context _cx )
	{
		return _cx.fromDouble( RuntimeDouble_v1.fun_IRR( _cx.toDoubles( _values ), _cx.toDouble( _guess ) ) );
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
		final Number number = parseNumber( text, false, _environment.locale );
		if (number != null) {
			if (number instanceof Long) {
				return number.longValue() * _cx.one();
			}
			else {
				return _cx.fromDouble( number.doubleValue() );
			}
		}
		else {
			return 0; // Excel #NUM!
		}
	}


}
