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
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.Date;


public class RuntimeBigDecimal_v1 extends Runtime_v1
{
	public static final BigDecimal ZERO = BigDecimal.ZERO;
	public static final BigDecimal ONE = BigDecimal.ONE;
	public static final BigDecimal TWO = BigDecimal.valueOf( 2 );
	private static final BigDecimal PI = BigDecimal.valueOf( Math.PI );
	private static final MathContext INTERNAL_HIGH_PREC_CONTEXT = MathContext.DECIMAL128;


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
		return toScaledLong( _value, _scale, BigDecimal.ROUND_HALF_UP );
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

	private static BigDecimal valueOrZero( final double _value )
	{
		if (Double.isNaN( _value ) || Double.isInfinite( _value )) {
			return ZERO; // Excel #NUM!
		}
		else {
			return BigDecimal.valueOf( _value );
		}
	}


	public static String toExcelString( BigDecimal _num )
	{
		return stringFromBigDecimal( _num );
	}


	public static BigDecimal fun_ACOS( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		if (a > 1 || a < -1) {
			return ZERO; // Excel #NUM!
		}
		return BigDecimal.valueOf( Math.acos( a ) );
	}

	public static BigDecimal fun_ASIN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		if (a > 1 || a < -1) {
			return ZERO; // Excel #NUM!
		}
		return BigDecimal.valueOf( Math.asin( a ) );
	}

	public static BigDecimal fun_ATAN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.atan( a ) );
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

	public static BigDecimal fun_SIN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.sin( a ) );
	}

	public static BigDecimal fun_TAN( BigDecimal _a )
	{
		final double a = _a.doubleValue();
		return BigDecimal.valueOf( Math.tan( a ) );
	}

	public static BigDecimal fun_DEGREES( BigDecimal _a )
	{
		final BigDecimal product = _a.multiply( BigDecimal.valueOf( 180 ) );
		return product.divide( PI, RoundingMode.HALF_UP );
	}

	public static BigDecimal fun_RADIANS( BigDecimal _a )
	{
		final BigDecimal product = _a.multiply( PI );
		return product.divide( BigDecimal.valueOf( 180 ), RoundingMode.HALF_UP );
	}

	public static BigDecimal fun_PI()
	{
		return PI;
	}

	public static BigDecimal fun_ROUND( final BigDecimal _val, final BigDecimal _maxFrac )
	{
		return round( _val, _maxFrac.intValue() );
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

	public static BigDecimal fun_POWER( BigDecimal _n, BigDecimal _p )
	{
		final BigDecimal pNormalized = _p.stripTrailingZeros();
		if (pNormalized.scale() <= 0) {
			final int p = pNormalized.intValueExact();
			if (p >= 0 && p <= 999999999) {
				return _n.pow( p );
			}
		}
		return valueOrZero( Math.pow( _n.doubleValue(), _p.doubleValue() ) );
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
			return ZERO; //Excel #DIV/0!
		}
		return BigDecimal.valueOf( lnN ).divide( BigDecimal.valueOf( lnX ), RoundingMode.HALF_UP );
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

	public static BigDecimal fun_SQRT( BigDecimal _n )
	{
		if (_n.signum() < 0) {
			return ZERO; // Excel #NUM!
		}

		// the Babylonian square root method (Newton's method)
		BigDecimal x0 = ZERO;
		BigDecimal x1 = new BigDecimal( Math.sqrt( _n.doubleValue() ) );

		while (x0.compareTo( x1 ) != 0) {
			x0 = x1;
			final BigDecimal a = _n.divide( x0, INTERNAL_HIGH_PREC_CONTEXT );
			final BigDecimal b = a.add( x0, INTERNAL_HIGH_PREC_CONTEXT );
			x1 = b.divide( TWO, INTERNAL_HIGH_PREC_CONTEXT );
		}

		return x1;
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

	public static BigDecimal fun_DB( final BigDecimal _cost, final BigDecimal _salvage, final BigDecimal _life,
			final BigDecimal _period, final BigDecimal _month )
	{
		final BigDecimal month = _month.setScale( 0, RoundingMode.FLOOR );
		final BigDecimal rate = BigDecimal.valueOf(
				1 - Math.pow( (_salvage.doubleValue() / _cost.doubleValue()), (1 / _life.doubleValue()) ) ).setScale( 3,
				RoundingMode.HALF_UP );
		final BigDecimal twelve = BigDecimal.valueOf( 12 );
		final BigDecimal depreciation1 = _cost.multiply( rate ).multiply( _month ).divide( twelve, RoundingMode.HALF_UP );
		BigDecimal depreciation = depreciation1;
		if (_period.intValue() > 1) {
			BigDecimal totalDepreciation = depreciation1;
			final int maxPeriod = (_life.compareTo( _period ) > 0 ? _period : _life).intValue();
			for (int i = 2; i <= maxPeriod; i++) {
				depreciation = _cost.subtract( totalDepreciation ).multiply( rate );
				totalDepreciation = totalDepreciation.add( depreciation );
			}
			if (_period.compareTo( _life ) > 0) {
				depreciation = _cost.subtract( totalDepreciation ).multiply( rate ).multiply( twelve.subtract( month ) )
						.divide( twelve, RoundingMode.HALF_UP );
			}
		}
		return depreciation;
	}

	public static BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period,
			BigDecimal _factor )
	{
		final BigDecimal remainingCost;
		double k = ONE.subtract( _factor.divide( _life, INTERNAL_HIGH_PREC_CONTEXT ) ).doubleValue();
		final double period = _period.doubleValue();
		if (k <= 0) {
			k = 0;
			remainingCost = period == 1 ? _cost : ZERO;
		}
		else {
			remainingCost = _cost.multiply( BigDecimal.valueOf( Math.pow( k, period - 1 ) ) );
		}
		final BigDecimal newCost = _cost.multiply( BigDecimal.valueOf( Math.pow( k, period ) ) );

		BigDecimal depreciation = remainingCost.subtract( (newCost.compareTo( _salvage ) < 0 ? _salvage : newCost) );
		if (depreciation.signum() < 0) {
			depreciation = ZERO;
		}
		return depreciation;
	}

	public static BigDecimal fun_PMT( BigDecimal _rate, BigDecimal _nper, BigDecimal _pv, BigDecimal _fv,
			BigDecimal _type )
	{
		final BigDecimal pmt;
		if (_rate.signum() == 0) {
			pmt = _pv.add( _fv ).divide( _nper, INTERNAL_HIGH_PREC_CONTEXT ).negate();
		}
		else {
			final BigDecimal a = BigDecimal.valueOf( Math.pow( _rate.add( ONE ).doubleValue(), _nper.doubleValue() ) );
			final BigDecimal b = _pv.divide( ONE.subtract( ONE.divide( a, INTERNAL_HIGH_PREC_CONTEXT ) ),
					INTERNAL_HIGH_PREC_CONTEXT );
			final BigDecimal c = _fv.divide( a.subtract( ONE ), INTERNAL_HIGH_PREC_CONTEXT );
			final BigDecimal d = b.add( c ).multiply( _rate.negate() );
			pmt = _type.signum() > 0 ? d.divide( _rate.add( ONE ), INTERNAL_HIGH_PREC_CONTEXT ) : d;
		}
		return pmt;
	}

	public static BigDecimal fun_PV( BigDecimal _rate, BigDecimal _nper, BigDecimal _pmt, BigDecimal _fv,
			BigDecimal _type )
	{
		final BigDecimal pv;
		if (_rate.signum() == 0) {
			pv = _fv.negate().subtract( _pmt.multiply( _nper ) );
		}
		else {
			final BigDecimal a = BigDecimal.valueOf( Math.pow( _rate.add( ONE ).doubleValue(), -_nper.doubleValue() ) );
			final BigDecimal k = _fv.multiply( a ).negate();
			if (_type.signum() > 0) {
				final BigDecimal b = BigDecimal.valueOf( Math.pow( _rate.add( ONE ).doubleValue(), ONE.subtract( _nper )
						.doubleValue() ) );
				pv = k.add( _pmt.multiply( b.subtract( ONE ) ).divide( _rate, INTERNAL_HIGH_PREC_CONTEXT ) )
						.subtract( _pmt );
			}
			else {
				final BigDecimal b = BigDecimal.valueOf( Math.pow( _rate.add( ONE ).doubleValue(), _nper.negate()
						.doubleValue() ) );
				pv = k.add( _pmt.multiply( b.subtract( ONE ) ).divide( _rate, INTERNAL_HIGH_PREC_CONTEXT ) );
			}
		}
		return pv;
	}

	public static BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv,
			BigDecimal _type, BigDecimal _guess )
	{
		final int MAX_ITER = 50;
		final boolean type = _type.signum() != 0;
		BigDecimal eps = ONE;
		BigDecimal rate0 = _guess;
		for (int count = 0; eps.compareTo( EXCEL_EPSILON ) > 0 && count < MAX_ITER; count++) {
			final BigDecimal rate1;
			if (rate0.signum() == 0) {
				final BigDecimal a = _pmt.multiply( _nper );
				final BigDecimal b = a.add( type ? _pmt : _pmt.negate() );
				final BigDecimal c = _pv.add( _fv ).add( a );
				final BigDecimal d = _nper.multiply( _pv.add( b.divide( TWO, INTERNAL_HIGH_PREC_CONTEXT ) ) );
				rate1 = rate0.subtract( c.divide( d, INTERNAL_HIGH_PREC_CONTEXT ) );
			}
			else {
				final BigDecimal a = rate0.add( ONE );
				final BigDecimal b = BigDecimal.valueOf( Math.pow( a.doubleValue(), _nper.subtract( ONE ).doubleValue() ) );
				final BigDecimal c = b.multiply( a );
				final BigDecimal d = _pmt.multiply( ONE.add( type ? rate0 : ZERO ) );
				final BigDecimal e = rate0.multiply( _nper ).multiply( b );
				final BigDecimal f = c.subtract( ONE );
				final BigDecimal g = rate0.multiply( _pv );
				final BigDecimal h = g.multiply( c ).add( d.multiply( f ) ).add( rate0.multiply( _fv ) );
				final BigDecimal k = g.multiply( e ).subtract( _pmt.multiply( f ) ).add( d.multiply( e ) );
				rate1 = rate0.multiply( ONE.subtract( h.divide( k, INTERNAL_HIGH_PREC_CONTEXT ) ) );
			}
			eps = rate1.subtract( rate0 ).abs();
			rate0 = rate1;
		}
		if (eps.compareTo( EXCEL_EPSILON ) >= 0) {
			return ZERO; // Excel #NUM!
		}
		return rate0;
	}


}
