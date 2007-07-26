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

import static org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2.HIGHPREC;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.formulacompiler.runtime.internal.ComputationTime;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeLong_v2;
import org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2;


public final class ExpressionTemplatesForScaledLongs
{
	final RuntimeLong_v2.Context context;
	final int scale;
	final long one;
	private Environment environment = null; // not supposed to be called at compile-time
	private ComputationTime computationTime = null; // not supposed to be called at compile-time


	public ExpressionTemplatesForScaledLongs(RuntimeLong_v2.Context _context)
	{
		super();
		this.context = _context;
		this.scale = _context.scale();
		this.one = _context.one();
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
		return RuntimeLong_v2.dateToNum( a, this.context, this.environment.timeZone() );
	}

	long util_fromMsSinceUTC1970( long a )
	{
		return RuntimeLong_v2.msSinceUTC1970ToNum( a, this.context, this.environment.timeZone() );
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
		return RuntimeLong_v2.dateFromNum( a, this.context, this.environment.timeZone() );
	}

	long util_toMsSinceUTC1970( long a )
	{
		return RuntimeLong_v2.msSinceUTC1970FromNum( a, this.context, this.environment.timeZone() );
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
		// We don't want to pass around scaled longs as Number (where the scale is non-obvious), so convert to BigDecimal.
		return RuntimeLong_v2.toBigDecimal( a, this.context );
	}


	// ------------------------------------------------ Array Fold


	long foldArray( long[] _a )
	{
		final long[] a = _a;
		long acc = foldInitial();
		int i = 1;
		for (long ai : a) {
			acc = foldElement( acc, ai, i );
			i++;
		}
		return acc;
	}

	private long foldInitial() // abstract, really
	{
		return 0;
	}

	private long foldElement( long _acc, long _d, int _i ) // abstract, really
	{
		return 0;
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

	public long fun_ASIN( long a )
	{
		return RuntimeLong_v2.fun_ASIN( a, this.context );
	}

	public long fun_ATAN( long a )
	{
		return RuntimeLong_v2.fun_ATAN( a, this.context );
	}

	public long fun_ATAN2( long x, long y )
	{
		return RuntimeLong_v2.fun_ATAN2( x, y, this.context );
	}

	public long fun_COS( long a )
	{
		return RuntimeLong_v2.fun_COS( a, this.context );
	}

	public long fun_SIN( long a )
	{
		return RuntimeLong_v2.fun_SIN( a, this.context );
	}

	public long fun_TAN( long a )
	{
		return RuntimeLong_v2.fun_TAN( a, this.context );
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

	public long fun_MOD( long n, long d )
	{
		return RuntimeLong_v2.fun_MOD( n, d, this.context );
	}

	public long fun_SQRT( long n )
	{
		return RuntimeLong_v2.fun_SQRT( n, this.context );
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
		final BigDecimal guess =RuntimeScaledBigDecimal_v2.TENTH;
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
		return RuntimeLong_v2.fun_DATE( _year, _month, _day, this.context );
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

	public long fun_WEEKDAY( long _date, long _type )
	{
		return RuntimeLong_v2.fun_WEEKDAY( _date, _type, this.context );
	}

	public long fun_WEEKDAY( long _date )
	{
		return RuntimeLong_v2.fun_WEEKDAY( _date, this.context.one(), this.context );
	}

	public long fun_DAY( long _date )
	{
		return RuntimeLong_v2.fun_DAY( _date, this.context );
	}

	public long fun_MONTH( long _date )
	{
		return RuntimeLong_v2.fun_MONTH( _date, this.context );
	}

	public long fun_YEAR( long _date )
	{
		return RuntimeLong_v2.fun_YEAR( _date, this.context );
	}

	public long fun_NOW()
	{
		return RuntimeLong_v2.fun_NOW( this.context, this.environment, this.computationTime );
	}

	public long fun_TODAY()
	{
		return RuntimeLong_v2.fun_TODAY( this.context, this.environment, this.computationTime );
	}


	// ------------------------------------------------ Conversions Functions


	public long fun_VALUE( String _text )
	{
		return RuntimeLong_v2.fun_VALUE( _text, this.context, this.environment );
	}


}
