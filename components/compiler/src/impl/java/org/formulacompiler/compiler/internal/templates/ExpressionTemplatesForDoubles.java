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
package org.formulacompiler.compiler.internal.templates;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.formulacompiler.runtime.internal.ComputationTime;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.runtime.internal.Runtime_v2;


public final class ExpressionTemplatesForDoubles
{
	private ComputationTime computationTime = null; // not supposed to be called at compile-time

	private final Environment environment;

	public ExpressionTemplatesForDoubles( Environment _env )
	{
		this.environment = _env;
	}


	// ------------------------------------------------ Utils


	double util_round( double a, int _maxFrac )
	{
		return RuntimeDouble_v2.round( a, _maxFrac );
	}


	double util_fromInt( int a )
	{
		return a;
	}

	double util_fromLong( long a )
	{
		return a;
	}

	double util_fromDouble( double a )
	{
		return a;
	}

	double util_fromFloat( float a )
	{
		return a;
	}

	double util_fromNumber( Number a )
	{
		return RuntimeDouble_v2.numberToNum( a );
	}

	double util_fromBoolean( boolean a )
	{
		return RuntimeDouble_v2.booleanToNum( a );
	}

	double util_fromDate( Date a )
	{
		return RuntimeDouble_v2.dateToNum( a, this.environment.timeZone() );
	}

	double util_fromMsSinceUTC1970( long a )
	{
		return RuntimeDouble_v2.msSinceUTC1970ToNum( a, this.environment.timeZone() );
	}

	double util_fromMs( long a )
	{
		return RuntimeDouble_v2.msToNum( a );
	}


	byte util_toByte( double a )
	{
		return (byte) Runtime_v2.checkDouble( a );
	}

	short util_toShort( double a )
	{
		return (short) Runtime_v2.checkDouble( a );
	}

	int util_toInt( double a )
	{
		return (int) Runtime_v2.checkDouble( a );
	}

	long util_toLong( double a )
	{
		return (long) Runtime_v2.checkDouble( a );
	}

	double util_toDouble( double a )
	{
		return a;
	}

	float util_toFloat( double a )
	{
		return (float) Runtime_v2.checkDouble( a );
	}

	BigInteger util_toBigInteger( double a )
	{
		return BigInteger.valueOf( (long) Runtime_v2.checkDouble( a ) );
	}

	BigDecimal util_toBigDecimal( double a )
	{
		return BigDecimal.valueOf( Runtime_v2.checkDouble( a ) );
	}

	boolean util_toBoolean( double a )
	{
		return RuntimeDouble_v2.booleanFromNum( a );
	}

	char util_toCharacter( double a )
	{
		return (char) Runtime_v2.checkDouble( a );
	}

	Date util_toDate( double a )
	{
		return RuntimeDouble_v2.dateFromNum( a, this.environment.timeZone() );
	}

	long util_toMsSinceUTC1970( double a )
	{
		return RuntimeDouble_v2.msSinceUTC1970FromNum( a, this.environment.timeZone() );
	}

	long util_toMs( double a )
	{
		return RuntimeDouble_v2.msFromNum( a );
	}

	String util_toString( double a )
	{
		return RuntimeDouble_v2.toExcelString( a, this.environment );
	}

	Number util_toNumber( double a )
	{
		return a;
	}


	double util_fromScaledLong( long a, long _scalingFactor )
	{
		return RuntimeDouble_v2.fromScaledLong( a, _scalingFactor );
	}

	long util_toScaledLong( double a, long _scalingFactor )
	{
		return RuntimeDouble_v2.toScaledLong( a, _scalingFactor );
	}


	double util_testForErrors( double a )
	{
		return Double.isInfinite( a ) || Double.isNaN( a ) ? 1 : 0;
	}


	// ------------------------------------------------ Operators


	public double op_PLUS( double a, double b )
	{
		return a + b;
	}

	public double op_MINUS( double a, double b )
	{
		return a - b;
	}

	public double op_MINUS( double a )
	{
		return -a;
	}

	public double op_TIMES( double a, double b )
	{
		return a * b;
	}

	public double op_DIV( double a, double b )
	{
		return a / b;
	}

	public double op_EXP( double a, double b )
	{
		return Math.pow( a, b );
	}

	public double op_PERCENT( double a )
	{
		return a / 100;
	}

	public double op_INTERNAL_MIN( double a, double b )
	{
		/*
		 * Using a direct comparison like
		 * 
		 * return (a < b) ? a : b;
		 * 
		 * generates too much code for inlining.
		 */
		return RuntimeDouble_v2.min( a, b );
	}

	public double op_INTERNAL_MAX( double a, double b )
	{
		return RuntimeDouble_v2.max( a, b );
	}


	// ------------------------------------------------ Numeric Functions


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ABS
	public double fun_ABS( double a )
	{
		return Math.abs( a );
	}
	// ---- fun_ABS

	public double fun_ACOS( double a )
	{
		return RuntimeDouble_v2.fun_ACOS( a );
	}

	public double fun_ACOSH( double a )
	{
		return RuntimeDouble_v2.fun_ACOSH( a );
	}

	public double fun_ASIN( double a )
	{
		return Math.asin( a );
	}

	public double fun_ASINH( double a )
	{
		return RuntimeDouble_v2.fun_ASINH( a );
	}

	public double fun_ATAN( double a )
	{
		return Math.atan( a );
	}

	public double fun_ATANH( double a )
	{
		return RuntimeDouble_v2.fun_ATANH( a );
	}

	public double fun_ATAN2( double x, double y )
	{
		return Math.atan2( y, x );
	}

	public double fun_COS( double a )
	{
		return Math.cos( a );
	}

	public double fun_COSH( double a )
	{
		return Math.cosh( a );
	}

	public double fun_SIN( double a )
	{
		return Math.sin( a );
	}

	public double fun_SINH( double a )
	{
		return RuntimeDouble_v2.fun_SINH( a );
	}

	public double fun_SIGN( double _a )
	{
		return Math.signum( _a );
	}

	public double fun_TAN( double a )
	{
		return Math.tan( a );
	}

	public double fun_TANH( double a )
	{
		return Math.tanh( a );
	}

	public double fun_DEGREES( double a )
	{
		return Math.toDegrees( a );
	}

	public double fun_RADIANS( double a )
	{
		return Math.toRadians( a );
	}

	public double fun_RAND()
	{
		return RuntimeDouble_v2.fun_RAND();
	}

	public double fun_PI()
	{
		return Math.PI;
	}

	public double fun_CEILING( double _number, double _significance )
	{
		return RuntimeDouble_v2.fun_CEILING( _number, _significance );
	}

	public double fun_FLOOR( double _number, double _significance )
	{
		return RuntimeDouble_v2.fun_FLOOR( _number, _significance );
	}

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ROUND
	public double fun_ROUND( double a, double b )
	{
		return RuntimeDouble_v2.round( a, (int) b );
	}
	// ---- fun_ROUND

	public double fun_ROUNDDOWN( double a, double b )
	{
		return RuntimeDouble_v2.fun_ROUNDDOWN( a, (int) b );
	}

	public double fun_ROUNDUP( double a, double b )
	{
		return RuntimeDouble_v2.fun_ROUNDUP( a, (int) b );
	}

	public double fun_TRUNC( double a, double b )
	{
		return RuntimeDouble_v2.trunc( a, (int) b );
	}

	public double fun_TRUNC( double a )
	{
		return RuntimeDouble_v2.fun_TRUNC( a );
	}

	public double fun_EVEN( double a )
	{
		return RuntimeDouble_v2.fun_EVEN( a );
	}

	public double fun_ODD( double a )
	{
		return RuntimeDouble_v2.fun_ODD( a );
	}

	public double fun_INT( double a )
	{
		return Math.floor( a );
	}

	public double fun_EXP( double p )
	{
		return Math.exp( p );
	}

	public double fun_POWER( double n, double p )
	{
		return RuntimeDouble_v2.fun_POWER( n, p );
	}

	public double fun_LN( double p )
	{
		return RuntimeDouble_v2.fun_LN( p );
	}

	public double fun_LOG( double p )
	{
		return RuntimeDouble_v2.fun_LOG10( p );
	}

	public double fun_LOG( double n, double x )
	{
		return RuntimeDouble_v2.fun_LOG( n, x );
	}

	public double fun_LOG10( double p )
	{
		return RuntimeDouble_v2.fun_LOG10( p );
	}

	public double fun_ERF( double _x )
	{
		return RuntimeDouble_v2.fun_ERF( _x );
	}

	public double fun_ERFC( double _x )
	{
		return RuntimeDouble_v2.fun_ERFC( _x );
	}

	public double fun_BETADIST( double _x, double _alpha, double _beta )
	{
		return RuntimeDouble_v2.fun_BETADIST( _x, _alpha, _beta );
	}

	public double fun_BETAINV( double _x, double _alpha, double _beta )
	{
		return RuntimeDouble_v2.fun_BETAINV( _x, _alpha, _beta );
	}

	public double fun_BINOMDIST( double _number, double _trials, double _probability, double _cumulative )
	{
		return RuntimeDouble_v2.fun_BINOMDIST( (int) _number, (int) _trials, _probability, _cumulative != 0 );
	}

	public double fun_CHIDIST( double _x, double _degFreedom )
	{
		return RuntimeDouble_v2.fun_CHIDIST( _x, _degFreedom );
	}

	public double fun_CHIINV( double _x, double _degFreedom )
	{
		return RuntimeDouble_v2.fun_CHIINV( _x, _degFreedom );
	}

	public double fun_CRITBINOM( double _n, double _p, double _alpha )
	{
		return RuntimeDouble_v2.fun_CRITBINOM( _n, _p, _alpha );
	}

	public double fun_FINV( double _x, double _f1, double _f2 )
	{
		return RuntimeDouble_v2.fun_FINV( _x, _f1, _f2 );
	}

	public double fun_GAMMADIST( double _x, double _alpha, double _beta, double _cumulative )
	{
		return RuntimeDouble_v2.fun_GAMMADIST( _x, _alpha, _beta, _cumulative != 0 );
	}

	public double fun_GAMMAINV( double _x, double _alpha, double _beta )
	{
		return RuntimeDouble_v2.fun_GAMMAINV( _x, _alpha, _beta );
	}

	public double fun_GAMMALN( double _x )
	{
		return RuntimeDouble_v2.fun_GAMMALN( _x );
	}

	public double fun_POISSON( double _x, double _mean, double _cumulative )
	{
		return RuntimeDouble_v2.fun_POISSON( (int) _x, _mean, _cumulative != 0 );
	}

	public double fun_TDIST( double _x, double _degFreedom, double _tails )
	{
		return RuntimeDouble_v2.fun_TDIST( _x, _degFreedom, (int) _tails, false );
	}

	public double fun_TDIST( double _x, double _degFreedom, double _tails, double _no_floor )
	{
		return RuntimeDouble_v2.fun_TDIST( _x, _degFreedom, (int) _tails, _no_floor == 0 ? false : true );
	}

	public double fun_TINV( double _x, double _degFreedom )
	{
		return RuntimeDouble_v2.fun_TINV( _x, _degFreedom );
	}

	public double fun_MOD( double n, double d )
	{
		return RuntimeDouble_v2.fun_MOD( n, d );
	}

	public double fun_SQRT( double n )
	{
		return RuntimeDouble_v2.fun_SQRT( n );
	}

	public double fun_WEIBULL( double _x, double _alpha, double _beta, double _cumulative )
	{
		return RuntimeDouble_v2.fun_WEIBULL( _x, _alpha, _beta, _cumulative != 0 );
	}

	// ------------------------------------------------ Combinatorics


	public double fun_FACT( double a )
	{
		return RuntimeDouble_v2.fun_FACT( a );
	}


	// ------------------------------------------------ Financials


	public double fun_IRR( double[] _values, double _guess )
	{
		return RuntimeDouble_v2.fun_IRR( _values, _guess );
	}

	public double fun_DB( double _cost, double _salvage, double _life, double _period, double _month )
	{
		return RuntimeDouble_v2.fun_DB( _cost, _salvage, _life, _period, _month );
	}

	public double fun_DB( double _cost, double _salvage, double _life, double _period )
	{
		return RuntimeDouble_v2.fun_DB( _cost, _salvage, _life, _period, 12 );
	}

	public double fun_DDB( double _cost, double _salvage, double _life, double _period, double _factor )
	{
		return RuntimeDouble_v2.fun_DDB( _cost, _salvage, _life, _period, _factor );
	}

	public double fun_DDB( double _cost, double _salvage, double _life, double _period )
	{
		return RuntimeDouble_v2.fun_DDB( _cost, _salvage, _life, _period, 2 );
	}


	public double fun_VDB( double _cost, double _salvage, double _life, double _start_period, double _end_period,
			double _factor, double _no_switch )
	{
		boolean no_switch = _no_switch != 0;
		return RuntimeDouble_v2.fun_VDB( _cost, _salvage, _life, _start_period, _end_period, _factor, no_switch );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv, double _fv, double _type, double _guess )
	{
		return RuntimeDouble_v2.fun_RATE( _nper, _pmt, _pv, _fv, _type, _guess );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv, double _fv, double _type )
	{
		return RuntimeDouble_v2.fun_RATE( _nper, _pmt, _pv, _fv, _type, 0.1 );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv, double _fv )
	{
		return RuntimeDouble_v2.fun_RATE( _nper, _pmt, _pv, _fv, 0, 0.1 );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv )
	{
		return RuntimeDouble_v2.fun_RATE( _nper, _pmt, _pv, 0, 0, 0.1 );
	}


	// ------------------------------------------------ Date Functions


	public double fun_DATE( double _year, double _month, double _day )
	{
		return RuntimeDouble_v2.fun_DATE( (int) _year, (int) _month, (int) _day );
	}

	public double fun_TIME( double _hour, double _minute, double _second )
	{
		return RuntimeDouble_v2.fun_TIME( _hour, _minute, _second );
	}

	public double fun_SECOND( double _date )
	{
		return RuntimeDouble_v2.fun_SECOND( _date );
	}

	public double fun_MINUTE( double _date )
	{
		return RuntimeDouble_v2.fun_MINUTE( _date );
	}

	public double fun_HOUR( double _date )
	{
		return RuntimeDouble_v2.fun_HOUR( _date );
	}

	public double fun_HYPGEOMDIST( double sample_s, double number_sample, double population_s, double number_population )
	{
		return RuntimeDouble_v2.fun_HYPGEOMDIST( (int) sample_s, (int) number_sample, (int) population_s,
				(int) number_population );
	}

	public double fun_WEEKDAY( double _date, double _type )
	{
		return RuntimeDouble_v2.fun_WEEKDAY( _date, (int) Math.round( _type ) );
	}

	public double fun_WEEKDAY( double _date )
	{
		return RuntimeDouble_v2.fun_WEEKDAY( _date, 1 );
	}

	public double fun_DAY( double _date )
	{
		return RuntimeDouble_v2.fun_DAY( _date );
	}

	public double fun_DAYS360( double _start_date, double _end_date, double _method )
	{
		return RuntimeDouble_v2.fun_DAYS360( _start_date, _end_date, _method != 0 );
	}

	public double fun_MONTH( double _date )
	{
		return RuntimeDouble_v2.fun_MONTH( _date );
	}

	public double fun_YEAR( double _date )
	{
		return RuntimeDouble_v2.fun_YEAR( _date );
	}

	public double fun_NOW()
	{
		return RuntimeDouble_v2.fun_NOW( this.environment, this.computationTime );
	}

	public double fun_TODAY()
	{
		return RuntimeDouble_v2.fun_TODAY( this.environment, this.computationTime );
	}


	// ------------------------------------------------ Conversions Functions


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_VALUE
	public double fun_VALUE( String _text )
	{
		return RuntimeDouble_v2.fun_VALUE( _text, this.environment );
	}
	// ---- fun_VALUE

	public double fun_DATEVALUE( String _text )
	{
		return RuntimeDouble_v2.fun_DATEVALUE( _text, this.environment );
	}

	public double fun_TIMEVALUE( String _text )
	{
		return RuntimeDouble_v2.fun_TIMEVALUE( _text, this.environment );
	}

}
