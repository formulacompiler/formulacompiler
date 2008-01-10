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
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;


abstract class AbstractExpressionTemplatesForBigDecimals
{
	private ComputationTime computationTime = null; // not supposed to be called at compile-time

	protected final Environment environment;

	public AbstractExpressionTemplatesForBigDecimals( Environment _env )
	{
		this.environment = _env;
	}


	// ------------------------------------------------ Utils


	BigDecimal util_round( BigDecimal a, int _maxFrac )
	{
		return RuntimeBigDecimal_v2.round( a, _maxFrac );
	}


	BigDecimal util_fromInt( int a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromLong( long a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromDouble( double a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromFloat( float a )
	{
		return BigDecimal.valueOf( a );
	}

	BigDecimal util_fromBigDecimal( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.toNum( a );
	}

	BigDecimal util_fromBigInteger( BigInteger a )
	{
		return a == null? BigDecimal.ZERO : new BigDecimal( a );
	}

	BigDecimal util_fromNumber( Number a )
	{
		return a == null? BigDecimal.ZERO : new BigDecimal( a.toString() );
	}

	BigDecimal util_fromBoolean( boolean a )
	{
		return RuntimeBigDecimal_v2.booleanToNum( a );
	}

	BigDecimal util_fromDate( Date a )
	{
		return RuntimeBigDecimal_v2.dateToNum( a, this.environment.timeZone() );
	}

	BigDecimal util_fromMsSinceUTC1970( long a )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.msSinceUTC1970ToNum( a, this.environment.timeZone() ) );
	}

	BigDecimal util_fromMs( long a )
	{
		return BigDecimal.valueOf( RuntimeDouble_v2.msToNum( a ) );
	}


	@ReturnsAdjustedValue
	byte util_toByte( BigDecimal a )
	{
		return a.byteValue();
	}

	@ReturnsAdjustedValue
	short util_toShort( BigDecimal a )
	{
		return a.shortValue();
	}

	@ReturnsAdjustedValue
	int util_toInt( BigDecimal a )
	{
		return a.intValue();
	}

	@ReturnsAdjustedValue
	long util_toLong( BigDecimal a )
	{
		return a.longValue();
	}

	@ReturnsAdjustedValue
	double util_toDouble( BigDecimal a )
	{
		return a.doubleValue();
	}

	@ReturnsAdjustedValue
	float util_toFloat( BigDecimal a )
	{
		return a.floatValue();
	}

	@ReturnsAdjustedValue
	BigInteger util_toBigInteger( BigDecimal a )
	{
		return a.toBigInteger();
	}

	@ReturnsAdjustedValue
	BigDecimal util_toBigDecimal( BigDecimal a )
	{
		return a;
	}

	@ReturnsAdjustedValue
	boolean util_toBoolean( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.booleanFromNum( a );
	}

	@ReturnsAdjustedValue
	char util_toCharacter( BigDecimal a )
	{
		return (char) a.intValue();
	}

	@ReturnsAdjustedValue
	Date util_toDate( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.dateFromNum( a, this.environment.timeZone() );
	}

	@ReturnsAdjustedValue
	long util_toMsSinceUTC1970( BigDecimal a )
	{
		return RuntimeDouble_v2.msSinceUTC1970FromNum( a.doubleValue(), this.environment.timeZone() );
	}

	@ReturnsAdjustedValue
	long util_toMs( BigDecimal a )
	{
		return RuntimeDouble_v2.msFromNum( a.doubleValue() );
	}

	@ReturnsAdjustedValue
	String util_toString( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.toExcelString( a, this.environment );
	}

	Number util_toNumber( BigDecimal a )
	{
		return a;
	}


	BigDecimal util_fromScaledLong( long a, int _scale )
	{
		return RuntimeBigDecimal_v2.fromScaledLong( a, _scale );
	}

	@ReturnsAdjustedValue
	long util_toScaledLong( BigDecimal a, int _scale )
	{
		return RuntimeBigDecimal_v2.toScaledLong( a, _scale );
	}


	// ------------------------------------------------ Operators


	@ReturnsAdjustedValue
	public BigDecimal op_MINUS( BigDecimal a )
	{
		return a.negate();
	}

	@ReturnsAdjustedValue
	public BigDecimal op_INTERNAL_MIN( BigDecimal a, BigDecimal b )
	{
		/*
		 * Using a direct comparison like
		 * 
		 * return (a.compareTo( b ) <= 0) ? a : b;
		 * 
		 * generates too much code for inlining.
		 */
		return RuntimeBigDecimal_v2.min( a, b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_INTERNAL_MAX( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.max( a, b );
	}


	// ------------------------------------------------ Numeric Functions


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ABS
	@ReturnsAdjustedValue
	public BigDecimal fun_ABS( BigDecimal a )
	{
		return a.abs();
	}
	// ---- fun_ABS

	public BigDecimal fun_ACOS( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ACOS( a );
	}

	public BigDecimal fun_ACOSH( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ACOSH( a );
	}

	public BigDecimal fun_ASIN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ASIN( a );
	}

	public BigDecimal fun_ASINH( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ASINH( a );
	}

	public BigDecimal fun_ATAN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ATAN( a );
	}

	public BigDecimal fun_ATANH( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ATANH( a );
	}

	public BigDecimal fun_ATAN2( BigDecimal x, BigDecimal y )
	{
		return RuntimeBigDecimal_v2.fun_ATAN2( x, y );
	}

	public BigDecimal fun_COS( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_COS( a );
	}

	public BigDecimal fun_COSH( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_COSH( a );
	}

	public BigDecimal fun_SIN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_SIN( a );
	}

	public BigDecimal fun_SINH( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_SINH( a );
	}

	public BigDecimal fun_SIGN( BigDecimal _a )
	{
		return BigDecimal.valueOf( _a.signum() );
	}

	public BigDecimal fun_TAN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_TAN( a );
	}

	public BigDecimal fun_TANH( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_TANH( a );
	}

	public BigDecimal fun_PI()
	{
		return RuntimeBigDecimal_v2.fun_PI();
	}

	public BigDecimal fun_RAND()
	{
		return RuntimeBigDecimal_v2.fun_RAND();
	}

	public BigDecimal fun_ROUND( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_ROUND( a, b );
	}

	public BigDecimal fun_ROUNDDOWN( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_ROUNDDOWN( a, b );
	}

	public BigDecimal fun_ROUNDUP( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_ROUNDUP( a, b );
	}

	public BigDecimal fun_TRUNC( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v2.fun_TRUNC( a, b );
	}

	public BigDecimal fun_TRUNC( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_TRUNC( a );
	}

	public BigDecimal fun_EVEN( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_EVEN( a );
	}

	public BigDecimal fun_ODD( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_ODD( a );
	}

	public BigDecimal fun_INT( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_INT( a );
	}

	public BigDecimal fun_EXP( BigDecimal p )
	{
		return BigDecimal.valueOf( Math.exp( p.doubleValue() ) );
	}

	public BigDecimal fun_LN( BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_LN( p );
	}

	public BigDecimal fun_LOG( BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_LOG10( p );
	}

	public BigDecimal fun_LOG( BigDecimal n, BigDecimal x )
	{
		return RuntimeBigDecimal_v2.fun_LOG( n, x );
	}

	public BigDecimal fun_LOG10( BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_LOG10( p );
	}

	public BigDecimal fun_ERF( BigDecimal x )
	{
		return RuntimeBigDecimal_v2.fun_ERF( x );
	}

	public BigDecimal fun_ERFC( BigDecimal x )
	{
		return RuntimeBigDecimal_v2.fun_ERFC( x );
	}

	public BigDecimal fun_BETADIST( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return RuntimeBigDecimal_v2.fun_BETADIST( _x, _alpha, _beta );
	}

	public BigDecimal fun_BETAINV( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return RuntimeBigDecimal_v2.fun_BETAINV( _x, _alpha, _beta );
	}

	public BigDecimal fun_BINOMDIST( BigDecimal _number, BigDecimal _trials, BigDecimal _probability, BigDecimal _cumulative )
	{
		return RuntimeBigDecimal_v2.fun_BINOMDIST( _number, _trials, _probability, _cumulative.signum() != 0 );
	}

	public BigDecimal fun_CHIDIST( BigDecimal _x, BigDecimal _degFreedom )
	{
		return RuntimeBigDecimal_v2.fun_CHIDIST( _x, _degFreedom );
	}

	public BigDecimal fun_CHIINV( BigDecimal _x, BigDecimal _degFreedom )
	{
		return RuntimeBigDecimal_v2.fun_CHIINV( _x, _degFreedom );
	}

	public BigDecimal fun_CRITBINOM( BigDecimal _n, BigDecimal _p, BigDecimal _alpha )
	{
		return RuntimeBigDecimal_v2.fun_CRITBINOM( _n, _p, _alpha );
	}

	public BigDecimal fun_FINV( BigDecimal _x, BigDecimal _f1, BigDecimal _f2 )
	{
		return RuntimeBigDecimal_v2.fun_FINV( _x, _f1, _f2 );
	}

	public BigDecimal fun_GAMMADIST( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta, BigDecimal _cumulative )
	{
		return RuntimeBigDecimal_v2.fun_GAMMADIST( _x, _alpha, _beta, _cumulative.signum() != 0 );
	}

	public BigDecimal fun_GAMMAINV( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta )
	{
		return RuntimeBigDecimal_v2.fun_GAMMAINV( _x, _alpha, _beta );
	}

	public BigDecimal fun_GAMMALN( BigDecimal _x )
	{
		return RuntimeBigDecimal_v2.fun_GAMMALN( _x );
	}

	public BigDecimal fun_POISSON( BigDecimal _x, BigDecimal _mean, BigDecimal _cumulative )
	{
		return RuntimeBigDecimal_v2.fun_POISSON( _x, _mean, _cumulative.signum() != 0 );
	}

	public BigDecimal fun_TDIST( BigDecimal _x, BigDecimal _degFreedom, BigDecimal _tails )
	{
		return RuntimeBigDecimal_v2.fun_TDIST( _x, _degFreedom, _tails, false );
	}

	public BigDecimal fun_TDIST( BigDecimal _x, BigDecimal _degFreedom, BigDecimal _tails, BigDecimal _no_floor )
	{

		return RuntimeBigDecimal_v2.fun_TDIST( _x, _degFreedom, _tails, RuntimeBigDecimal_v2.booleanFromNum( _no_floor ) );
	}

	public BigDecimal fun_TINV( BigDecimal _x, BigDecimal _degFreedom )
	{
		return RuntimeBigDecimal_v2.fun_TINV( _x, _degFreedom );
	}

	public BigDecimal fun_MOD( BigDecimal n, BigDecimal d )
	{
		return RuntimeBigDecimal_v2.fun_MOD( n, d );
	}

	public BigDecimal fun_WEIBULL( BigDecimal _x, BigDecimal _alpha, BigDecimal _beta, BigDecimal _cumulative )
	{
		return RuntimeBigDecimal_v2.fun_WEIBULL( _x, _alpha, _beta, _cumulative.signum() != 0 );
	}

	// ------------------------------------------------ Date Functions


	public BigDecimal fun_DATE( BigDecimal _year, BigDecimal _month, BigDecimal _day )
	{
		return RuntimeBigDecimal_v2.fun_DATE( _year, _month, _day );
	}

	public BigDecimal fun_SECOND( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_SECOND( _date );
	}

	public BigDecimal fun_MINUTE( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_MINUTE( _date );
	}

	public BigDecimal fun_HOUR( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_HOUR( _date );
	}

	public BigDecimal fun_HYPGEOMDIST( BigDecimal sample_s, BigDecimal number_sample, BigDecimal population_s, BigDecimal number_population )
	{
		return RuntimeBigDecimal_v2.fun_HYPGEOMDIST( sample_s, number_sample, population_s, number_population );
	}

	public BigDecimal fun_WEEKDAY( BigDecimal _date, BigDecimal _type )
	{
		return RuntimeBigDecimal_v2.fun_WEEKDAY( _date, _type );
	}

	public BigDecimal fun_WEEKDAY( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_WEEKDAY( _date, RuntimeBigDecimal_v2.ONE );
	}

	public BigDecimal fun_DAY( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_DAY( _date );
	}

	public BigDecimal fun_DAYS360( BigDecimal _start_date, BigDecimal _end_date, BigDecimal _method )
	{
		return RuntimeBigDecimal_v2.fun_DAYS360( _start_date, _end_date, _method.signum() != 0 );
	}

	public BigDecimal fun_MONTH( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_MONTH( _date );
	}

	public BigDecimal fun_YEAR( BigDecimal _date )
	{
		return RuntimeBigDecimal_v2.fun_YEAR( _date );
	}

	public BigDecimal fun_NOW()
	{
		return RuntimeBigDecimal_v2.fun_NOW( this.environment, this.computationTime );
	}

	public BigDecimal fun_TODAY()
	{
		return RuntimeBigDecimal_v2.fun_TODAY( this.environment, this.computationTime );
	}


	// ------------------------------------------------ Conversions Functions


	public BigDecimal fun_VALUE( String _text )
	{
		return RuntimeBigDecimal_v2.fun_VALUE( _text, this.environment );
	}


}
