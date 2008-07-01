/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.compiler.internal.templates;

import static org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.ComputationTime;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeLong_v2;
import org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2;


public final class ExpressionTemplatesForScaledLongs
{
	private ComputationTime computationTime = null; // not supposed to be called at compile-time

	final RuntimeLong_v2.Context context;
	final int scale;
	final long one;
	private final ComputationMode computationMode;
	private final Environment environment;


	public ExpressionTemplatesForScaledLongs( RuntimeLong_v2.Context _context, ComputationMode _mode, Environment _env )
	{
		super();
		this.context = _context;
		this.scale = _context.scale();
		this.one = _context.one();
		this.computationMode = _mode;
		this.environment = _env;
	}

	// ------------------------------------------------ Utils


	long util_round( long a, int _maxFrac )
	{
		return RuntimeLong_v2.round( a, _maxFrac, this.context );
	}


	long util_scaleUp( long a, long by )
	{
		return a * by;
	}

	long util_scaleDown( long a, long by )
	{
		return a / by;
	}


	long util_fromInt( int a )
	{
		return a;
	}

	long util_fromLong( long a )
	{
		return a;
	}

	long util_fromDouble( double a )
	{
		return (long) a;
	}

	long util_fromDouble_Scaled( double a )
	{
		return RuntimeLong_v2.fromDouble( a, this.context );
	}

	long util_fromFloat( float a )
	{
		return (long) a;
	}

	long util_fromFloat_Scaled( float a )
	{
		return RuntimeLong_v2.fromDouble( a, this.context );
	}

	long util_fromBigDecimal( BigDecimal a )
	{
		return RuntimeLong_v2.fromBigDecimal( a, this.context );
	}

	long util_fromBigDecimal_Scaled( BigDecimal a )
	{
		return RuntimeLong_v2.fromBigDecimal( a, this.context );
	}

	long util_fromNumber( Number a )
	{
		return (a == null) ? 0 : a.longValue();
	}

	long util_fromBoolean( boolean a )
	{
		return a ? this.one : 0;
	}

	long util_fromDate( Date a )
	{
		return RuntimeLong_v2.dateToNum( a, this.context, this.environment.timeZone(), this.computationMode );
	}

	long util_fromMsSinceUTC1970( long a )
	{
		return RuntimeLong_v2.msSinceUTC1970ToNum( a, this.context, this.environment.timeZone(), this.computationMode );
	}

	long util_fromMs( long a )
	{
		return RuntimeLong_v2.msToNum( a, this.context );
	}


	byte util_toByte( long a )
	{
		return (byte) a;
	}

	short util_toShort( long a )
	{
		return (short) a;
	}

	int util_toInt( long a )
	{
		return (int) a;
	}

	long util_toLong( long a )
	{
		return a;
	}

	double util_toDouble( long a )
	{
		return a;
	}

	double util_toDouble_Scaled( long a )
	{
		return RuntimeLong_v2.toDouble( a, this.context );
	}

	float util_toFloat( long a )
	{
		return a;
	}

	float util_toFloat_Scaled( long a )
	{
		return (float) RuntimeLong_v2.toDouble( a, this.context );
	}

	BigInteger util_toBigInteger( long a )
	{
		return BigInteger.valueOf( a );
	}

	BigDecimal util_toBigDecimal( long a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_toBigDecimal_Scaled( long a )
	{
		return RuntimeLong_v2.toBigDecimal( a, this.context );
	}

	boolean util_toBoolean( long a )
	{
		// Use a local so the conditional does not span the return instruction.
		final boolean b = a != 0;
		return b;
	}

	char util_toCharacter( long a )
	{
		return (char) a;
	}

	Date util_toDate( long a )
	{
		return RuntimeLong_v2.dateFromNum( a, this.context, this.environment.timeZone(), this.computationMode );
	}

	long util_toMsSinceUTC1970( long a )
	{
		return RuntimeLong_v2.msSinceUTC1970FromNum( a, this.context, this.environment.timeZone(), this.computationMode );
	}

	long util_toMs( long a )
	{
		return RuntimeLong_v2.msFromNum( a, this.context );
	}

	String util_toString( long a )
	{
		return RuntimeLong_v2.toExcelString( a, this.context, this.environment );
	}

	Number util_toNumber( long a )
	{
		return a;
	}

	Number util_toNumber_Scaled( long a )
	{
		// We don't want to pass around scaled longs as Number (where the scale is non-obvious), so
		// convert to BigDecimal.
		return RuntimeLong_v2.toBigDecimal( a, this.context );
	}


	// ------------------------------------------------ Operators


	public long op_PLUS( long a, long b )
	{
		return a + b;
	}

	public long op_MINUS( long a, long b )
	{
		return a - b;
	}

	public long op_MINUS( long a )
	{
		return -a;
	}

	public long op_TIMES__if_isScaled( long a, long b )
	{
		return a * b / this.one;
	}

	public long op_TIMES( long a, long b )
	{
		return a * b;
	}

	public long op_DIV__if_isScaled( long a, long b )
	{
		return a * this.one / b;
	}

	public long op_DIV( long a, long b )
	{
		return a / b;
	}

	public long op_EXP( long a, long b )
	{
		return RuntimeLong_v2.fun_POWER( a, b, this.context );
	}

	public long op_PERCENT( long a )
	{
		return a / 100;
	}

	public long op_INTERNAL_MIN( long a, long b )
	{
		/*
		 * Using a direct comparison like
		 * 
		 * return (a < b) ? a : b;
		 * 
		 * generates too much code for inlining.
		 */
		return RuntimeLong_v2.min( a, b );
	}

	public long op_INTERNAL_MAX( long a, long b )
	{
		return RuntimeLong_v2.max( a, b );
	}


	// ------------------------------------------------ Numeric Functions


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ABS
	public long fun_ABS( long a )
	{
		return (a < 0) ? -a : a;
	}
	// ---- fun_ABS

	public long fun_ACOS( long a )
	{
		return RuntimeLong_v2.fun_ACOS( a, this.context );
	}

	public long fun_ACOSH( long a )
	{
		return RuntimeLong_v2.fun_ACOSH( a, this.context );
	}

	public long fun_ASIN( long a )
	{
		return RuntimeLong_v2.fun_ASIN( a, this.context );
	}

	public long fun_ASINH( long a )
	{
		return RuntimeLong_v2.fun_ASINH( a, this.context );
	}

	public long fun_ATAN( long a )
	{
		return RuntimeLong_v2.fun_ATAN( a, this.context );
	}

	public long fun_ATANH( long a )
	{
		return RuntimeLong_v2.fun_ATANH( a, this.context );
	}

	public long fun_ATAN2( long x, long y )
	{
		return RuntimeLong_v2.fun_ATAN2( x, y, this.context );
	}

	public long fun_COS( long a )
	{
		return RuntimeLong_v2.fun_COS( a, this.context );
	}

	public long fun_COSH( long a )
	{
		return RuntimeLong_v2.fun_COSH( a, this.context );
	}

	public long fun_SIN( long a )
	{
		return RuntimeLong_v2.fun_SIN( a, this.context );
	}

	public long fun_SINH( long a )
	{
		return RuntimeLong_v2.fun_SINH( a, this.context );
	}

	public long fun_SIGN( long _date )
	{
		return RuntimeLong_v2.fun_SIGN( _date, this.context );
	}

	public long fun_TAN( long a )
	{
		return RuntimeLong_v2.fun_TAN( a, this.context );
	}

	public long fun_TANH( long a )
	{
		return RuntimeLong_v2.fun_TANH( a, this.context );
	}

	public long fun_DEGREES( long a )
	{
		return RuntimeLong_v2.fun_DEGREES( a, this.context );
	}

	public long fun_RADIANS( long a )
	{
		return RuntimeLong_v2.fun_RADIANS( a, this.context );
	}

	public long fun_PI()
	{
		return RuntimeLong_v2.fun_PI( this.context );
	}

	public long fun_CEILING( long _number, long _significance )
	{
		return RuntimeLong_v2.fun_CEILING( _number, _significance, this.context );
	}

	public long fun_FLOOR( long _number, long _significance )
	{
		return RuntimeLong_v2.fun_FLOOR( _number, _significance, this.context );
	}

	public long fun_RAND()
	{
		return RuntimeLong_v2.fun_RAND( this.context );
	}

	public long fun_ROUND( long a, long b )
	{
		return RuntimeLong_v2.fun_ROUND( a, b, this.context );
	}

	public long fun_ROUNDDOWN( long a, long b )
	{
		return RuntimeLong_v2.fun_ROUNDDOWN( a, b, this.context );
	}

	public long fun_ROUNDUP( long a, long b )
	{
		return RuntimeLong_v2.fun_ROUNDUP( a, b, this.context );
	}

	public long fun_TRUNC( long a, long b )
	{
		return RuntimeLong_v2.fun_TRUNC( a, b, this.context );
	}

	public long fun_TRUNC( long a )
	{
		return RuntimeLong_v2.fun_TRUNC( a, this.context );
	}

	public long fun_EVEN( long a )
	{
		return RuntimeLong_v2.fun_EVEN( a, this.context );
	}

	public long fun_ODD( long a )
	{
		return RuntimeLong_v2.fun_ODD( a, this.context );
	}

	public long fun_INT( long a )
	{
		return RuntimeLong_v2.fun_INT( a, this.context );
	}

	public long fun_EXP( long p )
	{
		return RuntimeLong_v2.fun_EXP( p, this.context );
	}

	public long fun_POWER( long n, long p )
	{
		return RuntimeLong_v2.fun_POWER( n, p, this.context );
	}

	public long fun_LN( long p )
	{
		return RuntimeLong_v2.fun_LN( p, this.context );
	}

	public long fun_LOG( long p )
	{
		return RuntimeLong_v2.fun_LOG10( p, this.context );
	}

	public long fun_LOG( long n, long x )
	{
		return RuntimeLong_v2.fun_LOG( n, x, this.context );
	}

	public long fun_LOG10( long p )
	{
		return RuntimeLong_v2.fun_LOG10( p, this.context );
	}

	public long fun_ERF( long x )
	{
		return RuntimeLong_v2.fun_ERF( x, this.context );
	}

	public long fun_ERFC( long x )
	{
		return RuntimeLong_v2.fun_ERFC( x, this.context );
	}

	public long fun_BETADIST( long _x, long _alpha, long _beta )
	{
		return RuntimeLong_v2.fun_BETADIST( _x, _alpha, _beta, this.context );
	}

	public long fun_BETAINV( long _x, long _alpha, long _beta )
	{
		return RuntimeLong_v2.fun_BETAINV( _x, _alpha, _beta, this.context );
	}

	public long fun_BINOMDIST( long _number, long _trials, long _probability, long _cumulative )
	{
		return RuntimeLong_v2.fun_BINOMDIST( _number, _trials, _probability, _cumulative != 0, this.context );
	}

	public long fun_CHIDIST( long _x, long _degFreedom )
	{
		return RuntimeLong_v2.fun_CHIDIST( _x, _degFreedom, this.context );
	}

	public long fun_CHIINV( long _x, long _degFreedom )
	{
		return RuntimeLong_v2.fun_CHIINV( _x, _degFreedom, this.context );
	}

	public long fun_CRITBINOM( long _n, long _p, long _alpha )
	{
		return RuntimeLong_v2.fun_CRITBINOM( _n, _p, _alpha, this.context );
// final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_RATE( nper, pmt, pv, fv, type, guess,
// HIGHPREC );
	}

	public long fun_FINV( long _x, long _f1, long _f2 )
	{
		return RuntimeLong_v2.fun_FINV( _x, _f1, _f2, this.context );
	}

	public long fun_GAMMADIST( long _x, long _alpha, long _beta, long _cumulative )
	{
		return RuntimeLong_v2.fun_GAMMADIST( _x, _alpha, _beta, _cumulative != 0, this.context );
	}

	public long fun_GAMMAINV( long _x, long _alpha, long _beta )
	{
		return RuntimeLong_v2.fun_GAMMAINV( _x, _alpha, _beta, this.context );
	}

	public long fun_GAMMALN( long _x )
	{
		return RuntimeLong_v2.fun_GAMMALN( _x, this.context );
	}

	public long fun_POISSON( long _x, long _mean, long _cumulative )
	{
		return RuntimeLong_v2.fun_POISSON( _x, _mean, _cumulative != 0, this.context );
	}

	public long fun_TDIST( long _x, long _degFreedom, long _tails )
	{
		return RuntimeLong_v2.fun_TDIST( _x, _degFreedom, _tails, false, this.context );
	}

	public long fun_TDIST( long _x, long _degFreedom, long _tails, long _no_floor )
	{
		return RuntimeLong_v2.fun_TDIST( _x, _degFreedom, _tails, _no_floor == 0 ? false : true, this.context );
	}

	public long fun_TINV( long _x, long _degFreedom )
	{
		return RuntimeLong_v2.fun_TINV( _x, _degFreedom, this.context );
	}

	public long fun_MOD( long n, long d )
	{
		return RuntimeLong_v2.fun_MOD( n, d, this.context );
	}

	public long fun_SQRT( long n )
	{
		return RuntimeLong_v2.fun_SQRT( n, this.context );
	}

	public long fun_WEIBULL( long _x, long _alpha, long _beta, long _cumulative )
	{
		return RuntimeLong_v2.fun_WEIBULL( _x, _alpha, _beta, _cumulative != 0, this.context );
	}

	// ------------------------------------------------ Combinatorics


	public long fun_FACT__if_isScaled( long a )
	{
		return RuntimeLong_v2.fun_FACT( a / this.one ) * this.one;
	}

	public long fun_FACT( long a )
	{
		return RuntimeLong_v2.fun_FACT( a );
	}


	// ------------------------------------------------ Financials


	public long fun_IRR( long[] _values, long _guess )
	{
		return RuntimeLong_v2.fun_IRR( _values, _guess, this.context );
	}

	public long fun_DB( long _cost, long _salvage, long _life, long _period, long _month )
	{
		return RuntimeLong_v2.fun_DB( _cost, _salvage, _life, _period, _month, this.context );
	}

	public long fun_DB( long _cost, long _salvage, long _life, long _period )
	{
		return RuntimeLong_v2.fun_DB( _cost, _salvage, _life, _period, 12 * this.context.one(), this.context );
	}

	public long fun_DDB( long _cost, long _salvage, long _life, long _period, long _factor )
	{
		final BigDecimal cost = this.context.toBigDecimal( _cost );
		final BigDecimal salvage = this.context.toBigDecimal( _salvage );
		final BigDecimal life = this.context.toBigDecimal( _life );
		final BigDecimal period = this.context.toBigDecimal( _period );
		final BigDecimal factor = this.context.toBigDecimal( _factor );
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_DDB( cost, salvage, life, period, factor, HIGHPREC );
		return this.context.fromBigDecimal( result );
	}

	public long fun_DDB( long _cost, long _salvage, long _life, long _period )
	{
		final BigDecimal cost = this.context.toBigDecimal( _cost );
		final BigDecimal salvage = this.context.toBigDecimal( _salvage );
		final BigDecimal life = this.context.toBigDecimal( _life );
		final BigDecimal period = this.context.toBigDecimal( _period );
		final BigDecimal factor = RuntimeScaledBigDecimal_v2.TWO;
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_DDB( cost, salvage, life, period, factor, HIGHPREC );
		return this.context.fromBigDecimal( result );
	}

	public long fun_VDB( long _cost, long _salvage, long _life, long _start_period, long _end_period, long _factor,
			long _no_switch )
	{
		final BigDecimal cost = this.context.toBigDecimal( _cost );
		final BigDecimal salvage = this.context.toBigDecimal( _salvage );
		final BigDecimal life = this.context.toBigDecimal( _life );
		final BigDecimal start = this.context.toBigDecimal( _start_period );
		final BigDecimal end = this.context.toBigDecimal( _end_period );
		final BigDecimal factor = this.context.toBigDecimal( _factor );
		final boolean no_switch = _no_switch != 0;
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_VDB( cost, salvage, life, start, end, factor, no_switch,
				HIGHPREC );
		return this.context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv, long _fv, long _type, long _guess )
	{
		final BigDecimal nper = this.context.toBigDecimal( _nper );
		final BigDecimal pmt = this.context.toBigDecimal( _pmt );
		final BigDecimal pv = this.context.toBigDecimal( _pv );
		final BigDecimal fv = this.context.toBigDecimal( _fv );
		final BigDecimal type = this.context.toBigDecimal( _type );
		final BigDecimal guess = this.context.toBigDecimal( _guess );
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_RATE( nper, pmt, pv, fv, type, guess, HIGHPREC );
		return this.context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv, long _fv, long _type )
	{
		final BigDecimal nper = this.context.toBigDecimal( _nper );
		final BigDecimal pmt = this.context.toBigDecimal( _pmt );
		final BigDecimal pv = this.context.toBigDecimal( _pv );
		final BigDecimal fv = this.context.toBigDecimal( _fv );
		final BigDecimal type = this.context.toBigDecimal( _type );
		final BigDecimal guess = RuntimeScaledBigDecimal_v2.TENTH;
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_RATE( nper, pmt, pv, fv, type, guess, HIGHPREC );
		return this.context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv, long _fv )
	{
		final BigDecimal nper = this.context.toBigDecimal( _nper );
		final BigDecimal pmt = this.context.toBigDecimal( _pmt );
		final BigDecimal pv = this.context.toBigDecimal( _pv );
		final BigDecimal fv = this.context.toBigDecimal( _fv );
		final BigDecimal type = RuntimeScaledBigDecimal_v2.ZERO;
		final BigDecimal guess = RuntimeScaledBigDecimal_v2.TENTH;
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_RATE( nper, pmt, pv, fv, type, guess, HIGHPREC );
		return this.context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv )
	{
		final BigDecimal nper = this.context.toBigDecimal( _nper );
		final BigDecimal pmt = this.context.toBigDecimal( _pmt );
		final BigDecimal pv = this.context.toBigDecimal( _pv );
		final BigDecimal fv = RuntimeScaledBigDecimal_v2.ZERO;
		final BigDecimal type = RuntimeScaledBigDecimal_v2.ZERO;
		final BigDecimal guess = RuntimeScaledBigDecimal_v2.TENTH;
		final BigDecimal result = RuntimeScaledBigDecimal_v2.fun_RATE( nper, pmt, pv, fv, type, guess, HIGHPREC );
		return this.context.fromBigDecimal( result );
	}


	// ------------------------------------------------ Date Functions


	public long fun_DATE( long _year, long _month, long _day )
	{
		return RuntimeLong_v2.fun_DATE( _year, _month, _day, this.context, this.computationMode );
	}

	public long fun_TIME( long _hour, long _minute, long _second )
	{
		return RuntimeLong_v2.fun_TIME( _hour, _minute, _second, this.context );
	}

	public long fun_SECOND( long _date )
	{
		return RuntimeLong_v2.fun_SECOND( _date, this.context );
	}

	public long fun_MINUTE( long _date )
	{
		return RuntimeLong_v2.fun_MINUTE( _date, this.context );
	}

	public long fun_HOUR( long _date )
	{
		return RuntimeLong_v2.fun_HOUR( _date, this.context );
	}

	public long fun_HYPGEOMDIST( long sample_s, long number_sample, long population_s, long number_population )
	{
		return RuntimeLong_v2.fun_HYPGEOMDIST( sample_s, number_sample, population_s, number_population, this.context );
	}

	public long fun_WEEKDAY( long _date, long _type )
	{
		return RuntimeLong_v2.fun_WEEKDAY( _date, _type, this.context, this.computationMode );
	}

	public long fun_WEEKDAY( long _date )
	{
		return RuntimeLong_v2.fun_WEEKDAY( _date, this.context.one(), this.context, this.computationMode );
	}

	public long fun_DAY( long _date )
	{
		return RuntimeLong_v2.fun_DAY( _date, this.context, this.computationMode );
	}

	public long fun_DAYS360( long _date_start, long _end_start, long _method )
	{
		return RuntimeLong_v2.fun_DAYS360( _date_start, _end_start, _method != 0, this.context, this.computationMode );
	}

	public long fun_MONTH( long _date )
	{
		return RuntimeLong_v2.fun_MONTH( _date, this.context, this.computationMode );
	}

	public long fun_YEAR( long _date )
	{
		return RuntimeLong_v2.fun_YEAR( _date, this.context, this.computationMode );
	}

	public long fun_NOW()
	{
		return RuntimeLong_v2.fun_NOW( this.context, this.environment, this.computationTime, this.computationMode );
	}

	public long fun_TODAY()
	{
		return RuntimeLong_v2.fun_TODAY( this.context, this.environment, this.computationTime, this.computationMode );
	}


	// ------------------------------------------------ Conversions Functions


	public long fun_VALUE( String _text )
	{
		return RuntimeLong_v2.fun_VALUE( _text, this.context, this.environment, this.computationMode );
	}

	public long fun_DATEVALUE( String _text )
	{
		return RuntimeLong_v2.fun_DATEVALUE( _text, this.context, this.environment, this.computationMode );
	}

	public long fun_TIMEVALUE( String _text )
	{
		return RuntimeLong_v2.fun_TIMEVALUE( _text, this.context, this.environment, this.computationMode );
	}

}
