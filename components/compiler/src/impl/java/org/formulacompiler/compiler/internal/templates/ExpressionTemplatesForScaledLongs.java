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

import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v1;
import org.formulacompiler.runtime.internal.RuntimeLong_v1;



@SuppressWarnings("unqualified-field-access")
public final class ExpressionTemplatesForScaledLongs
{
	final RuntimeLong_v1.Context context;
	final int scale;
	final long one;
	
	public ExpressionTemplatesForScaledLongs( RuntimeLong_v1.Context _context )
	{
		super();
		this.context = _context;
		this.scale = _context.scale();
		this.one = _context.one();
	}


	// ------------------------------------------------ Utils


	long util_round( long a, int _maxFrac )
	{
		return RuntimeLong_v1.round( a, _maxFrac, context );
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
		return RuntimeLong_v1.fromDouble( a, context );
	}

	long util_fromFloat( float a )
	{
		return (long) a;
	}

	long util_fromFloat_Scaled( float a )
	{
		return RuntimeLong_v1.fromDouble( a, context );
	}

	long util_fromBigDecimal( BigDecimal a )
	{
		return RuntimeLong_v1.fromBigDecimal( a, context );
	}

	long util_fromBigDecimal_Scaled( BigDecimal a )
	{
		return RuntimeLong_v1.fromBigDecimal( a, context );
	}

	long util_fromNumber( Number a )
	{
		return (a == null) ? 0 : a.longValue();
	}

	long util_fromBoolean( boolean a )
	{
		return a ? one : 0;
	}

	long util_fromDate( Date a )
	{
		return RuntimeLong_v1.dateToNum( a, context );
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
		return RuntimeLong_v1.toDouble( a, context );
	}

	float util_toFloat( long a )
	{
		return a;
	}

	float util_toFloat_Scaled( long a )
	{
		return (float) RuntimeLong_v1.toDouble( a, context );
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
		return RuntimeLong_v1.toBigDecimal( a, context );
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
		return RuntimeLong_v1.dateFromNum( a, context );
	}

	String util_toString( long a )
	{
		return RuntimeLong_v1.toExcelString( a, context );
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
		return a * b / one;
	}

	public long op_TIMES( long a, long b )
	{
		return a * b;
	}

	public long op_DIV__if_isScaled( long a, long b )
	{
		return a * one / b;
	}

	public long op_DIV( long a, long b )
	{
		return a / b;
	}

	public long op_EXP( long a, long b )
	{
		return RuntimeLong_v1.fun_POWER( a, b, context );
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
		return RuntimeLong_v1.min( a, b );
	}

	public long op_INTERNAL_MAX( long a, long b )
	{
		return RuntimeLong_v1.max( a, b );
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
		return RuntimeLong_v1.fun_ACOS( a, context );
	}

	public long fun_ASIN( long a )
	{
		return RuntimeLong_v1.fun_ASIN( a, context );
	}

	public long fun_ATAN( long a )
	{
		return RuntimeLong_v1.fun_ATAN( a, context );
	}

	public long fun_ATAN2( long x, long y )
	{
		return RuntimeLong_v1.fun_ATAN2( x, y, context );
	}

	public long fun_COS( long a )
	{
		return RuntimeLong_v1.fun_COS( a, context );
	}

	public long fun_SIN( long a )
	{
		return RuntimeLong_v1.fun_SIN( a, context );
	}

	public long fun_TAN( long a )
	{
		return RuntimeLong_v1.fun_TAN( a, context );
	}

	public long fun_DEGREES( long a )
	{
		return RuntimeLong_v1.fun_DEGREES( a, context );
	}

	public long fun_RADIANS( long a )
	{
		return RuntimeLong_v1.fun_RADIANS( a, context );
	}

	public long fun_PI()
	{
		return RuntimeLong_v1.fun_PI( context );
	}

	public long fun_ROUND( long a, long b )
	{
		return RuntimeLong_v1.fun_ROUND( a, b, context );
	}

	public long fun_TRUNC( long a, long b )
	{
		return RuntimeLong_v1.fun_TRUNC( a, b, context );
	}

	public long fun_TRUNC( long a )
	{
		return RuntimeLong_v1.fun_TRUNC( a, context );
	}

	public long fun_EVEN( long a )
	{
		return RuntimeLong_v1.fun_EVEN( a, context );
	}

	public long fun_ODD( long a )
	{
		return RuntimeLong_v1.fun_ODD( a, context );
	}

	public long fun_INT( long a )
	{
		return RuntimeLong_v1.fun_INT( a, context );
	}

	public long fun_EXP( long p )
	{
		return RuntimeLong_v1.fun_EXP( p, context );
	}

	public long fun_POWER( long n, long p )
	{
		return RuntimeLong_v1.fun_POWER( n, p, context );
	}

	public long fun_LN( long p )
	{
		return RuntimeLong_v1.fun_LN( p, context );
	}

	public long fun_LOG( long p )
	{
		return RuntimeLong_v1.fun_LOG10( p, context );
	}

	public long fun_LOG( long n, long x )
	{
		return RuntimeLong_v1.fun_LOG( n, x, context );
	}

	public long fun_LOG10( long p )
	{
		return RuntimeLong_v1.fun_LOG10( p, context );
	}

	public long fun_MOD( long n, long d )
	{
		return RuntimeLong_v1.fun_MOD( n, d, context );
	}

	public long fun_SQRT( long n )
	{
		return RuntimeLong_v1.fun_SQRT( n, context );
	}


	// ------------------------------------------------ Combinatorics


	public long fun_FACT__if_isScaled( long a )
	{
		return RuntimeLong_v1.fun_FACT( a / one ) * one;
	}

	public long fun_FACT( long a )
	{
		return RuntimeLong_v1.fun_FACT( a );
	}


	// ------------------------------------------------ Financials
	
	
	public long fun_IRR( long[] _values, long _guess )
	{
		return RuntimeLong_v1.fun_IRR( _values, _guess, context );
	}

	public long fun_DB( long _cost, long _salvage, long _life, long _period, long _month )
	{
		return RuntimeLong_v1.fun_DB( _cost, _salvage, _life, _period, _month, context );
	}

	public long fun_DB( long _cost, long _salvage, long _life, long _period )
	{
		return RuntimeLong_v1.fun_DB( _cost, _salvage, _life, _period, 12 * context.one(), context );
	}

	public long fun_DDB( long _cost, long _salvage, long _life, long _period, long _factor )
	{
		final BigDecimal cost = context.toBigDecimal( _cost );
		final BigDecimal salvage = context.toBigDecimal( _salvage );
		final BigDecimal life = context.toBigDecimal( _life );
		final BigDecimal period = context.toBigDecimal( _period );
		final BigDecimal factor = context.toBigDecimal( _factor );
		final BigDecimal result = RuntimeBigDecimal_v1.fun_DDB( cost, salvage, life, period, factor );
		return context.fromBigDecimal( result );
	}

	public long fun_DDB( long _cost, long _salvage, long _life, long _period )
	{
		final BigDecimal cost = context.toBigDecimal( _cost );
		final BigDecimal salvage = context.toBigDecimal( _salvage );
		final BigDecimal life = context.toBigDecimal( _life );
		final BigDecimal period = context.toBigDecimal( _period );
		final BigDecimal factor = RuntimeBigDecimal_v1.TWO;
		final BigDecimal result = RuntimeBigDecimal_v1.fun_DDB( cost, salvage, life, period, factor );
		return context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv, long _fv, long _type, long _guess )
	{
		final BigDecimal nper = context.toBigDecimal( _nper );
		final BigDecimal pmt = context.toBigDecimal( _pmt );
		final BigDecimal pv = context.toBigDecimal( _pv );
		final BigDecimal fv = context.toBigDecimal( _fv );
		final BigDecimal type = context.toBigDecimal( _type );
		final BigDecimal guess = context.toBigDecimal( _guess );
		final BigDecimal result = RuntimeBigDecimal_v1.fun_RATE( nper, pmt, pv, fv, type, guess );
		return context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv, long _fv, long _type )
	{
		final BigDecimal nper = context.toBigDecimal( _nper );
		final BigDecimal pmt = context.toBigDecimal( _pmt );
		final BigDecimal pv = context.toBigDecimal( _pv );
		final BigDecimal fv = context.toBigDecimal( _fv );
		final BigDecimal type = context.toBigDecimal( _type );
		final BigDecimal guess = BigDecimal.valueOf( 0.1 );
		final BigDecimal result = RuntimeBigDecimal_v1.fun_RATE( nper, pmt, pv, fv, type, guess );
		return context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv, long _fv )
	{
		final BigDecimal nper = context.toBigDecimal( _nper );
		final BigDecimal pmt = context.toBigDecimal( _pmt );
		final BigDecimal pv = context.toBigDecimal( _pv );
		final BigDecimal fv = context.toBigDecimal( _fv );
		final BigDecimal type = RuntimeBigDecimal_v1.ZERO;
		final BigDecimal guess = BigDecimal.valueOf( 0.1 );
		final BigDecimal result = RuntimeBigDecimal_v1.fun_RATE( nper, pmt, pv, fv, type, guess );
		return context.fromBigDecimal( result );
	}

	public long fun_RATE( long _nper, long _pmt, long _pv )
	{
		final BigDecimal nper = context.toBigDecimal( _nper );
		final BigDecimal pmt = context.toBigDecimal( _pmt );
		final BigDecimal pv = context.toBigDecimal( _pv );
		final BigDecimal fv = RuntimeBigDecimal_v1.ZERO;
		final BigDecimal type = RuntimeBigDecimal_v1.ZERO;
		final BigDecimal guess = BigDecimal.valueOf( 0.1 );
		final BigDecimal result = RuntimeBigDecimal_v1.fun_RATE( nper, pmt, pv, fv, type, guess );
		return context.fromBigDecimal( result );
	}


	// ------------------------------------------------ Date Functions


	public long fun_DATE( long _year, long _month, long _day )
	{
		return RuntimeLong_v1.fun_DATE( _year, _month, _day, context );
	}

	public long fun_TIME( long _hour, long _minute, long _second )
	{
		return RuntimeLong_v1.fun_TIME( _hour, _minute, _second, context );
	}

	public long fun_SECOND( long _date )
	{
		return RuntimeLong_v1.fun_SECOND( _date, context );
	}

	public long fun_MINUTE( long _date )
	{
		return RuntimeLong_v1.fun_MINUTE( _date, context );
	}

	public long fun_HOUR( long _date )
	{
		return RuntimeLong_v1.fun_HOUR( _date, context );
	}

	public long fun_WEEKDAY( long _date, long _type )
	{
		return RuntimeLong_v1.fun_WEEKDAY( _date, _type, context );
	}

	public long fun_WEEKDAY( long _date )
	{
		return RuntimeLong_v1.fun_WEEKDAY( _date, context.one(), context );
	}

	public long fun_DAY( long _date )
	{
		return RuntimeLong_v1.fun_DAY( _date, context );
	}

	public long fun_MONTH( long _date )
	{
		return RuntimeLong_v1.fun_MONTH( _date, context );
	}

	public long fun_YEAR( long _date )
	{
		return RuntimeLong_v1.fun_YEAR( _date, context );
	}

	public long fun_TODAY()
	{
		return RuntimeLong_v1.fun_TODAY( context );
	}


}