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
package sej.internal.templates;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import sej.internal.runtime.RuntimeBigDecimal_v1;
import sej.runtime.SEJRuntime;


@SuppressWarnings("unqualified-field-access")
public final class ExpressionTemplatesForBigDecimals
{
	final boolean isScaled;
	final int fixedScale;
	final int roundingMode;

	public ExpressionTemplatesForBigDecimals(int _scale, int _roundingMode)
	{
		super();
		this.isScaled = (_scale != SEJRuntime.UNDEFINED_SCALE);
		this.fixedScale = _scale;
		this.roundingMode = _roundingMode;
	}


	// ------------------------------------------------ Utils


	@ReturnsAdjustedValue
	BigDecimal util_adjustValue( BigDecimal a )
	{
		return a.setScale( fixedScale, roundingMode );
	}


	BigDecimal util_round( BigDecimal a, int _maxFrac )
	{
		return RuntimeBigDecimal_v1.round( a, _maxFrac );
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
		return RuntimeBigDecimal_v1.toNum( a );
	}

	BigDecimal util_fromBigInteger( BigInteger a )
	{
		return a == null ? BigDecimal.ZERO : new BigDecimal( a );
	}

	BigDecimal util_fromNumber( Number a )
	{
		return a == null ? BigDecimal.ZERO : new BigDecimal( a.toString() );
	}

	BigDecimal util_fromBoolean( boolean a )
	{
		return RuntimeBigDecimal_v1.booleanToNum( a );
	}

	BigDecimal util_fromDate( Date a )
	{
		return RuntimeBigDecimal_v1.dateToNum( a );
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
		return RuntimeBigDecimal_v1.booleanFromNum( a );
	}

	@ReturnsAdjustedValue
	char util_toCharacter( BigDecimal a )
	{
		return (char) a.intValue();
	}

	@ReturnsAdjustedValue
	Date util_toDate( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.dateFromNum( a );
	}

	@ReturnsAdjustedValue
	String util_toString( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.toExcelString( a );
	}


	BigDecimal util_fromScaledLong( long a, int _scale )
	{
		return RuntimeBigDecimal_v1.fromScaledLong( a, _scale );
	}

	@ReturnsAdjustedValue
	long util_toScaledLong( BigDecimal a, int _scale )
	{
		return RuntimeBigDecimal_v1.toScaledLong( a, _scale );
	}


	// ------------------------------------------------ Array Fold


	BigDecimal foldArray( BigDecimal[] _a )
	{
		final BigDecimal[] a = _a;
		BigDecimal acc = foldInitial();
		int i = 1;
		for (BigDecimal ai : a) {
			acc = foldElement( acc, ai, i );
			i++;
		}
		return acc;
	}
	
	private BigDecimal foldInitial() // abstract, really
	{
		return null;
	}

	private BigDecimal foldElement( BigDecimal _acc, BigDecimal _d, int _i ) // abstract, really
	{
		return null;
	}


	// ------------------------------------------------ Operators


	@ReturnsAdjustedValue
	public BigDecimal op_PLUS( BigDecimal a, BigDecimal b )
	{
		return a.add( b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_MINUS( BigDecimal a, BigDecimal b )
	{
		return a.subtract( b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_MINUS( BigDecimal a )
	{
		return a.negate();
	}

	public BigDecimal op_TIMES( BigDecimal a, BigDecimal b )
	{
		return a.multiply( b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_DIV__if_needsValueAdjustment( BigDecimal a, BigDecimal b )
	{
		return a.divide( b, fixedScale, roundingMode );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_DIV( BigDecimal a, BigDecimal b )
	{
		return a.divide( b );
	}

	public BigDecimal op_EXP( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v1.fun_POWER( a, b );
	}

	public BigDecimal op_PERCENT( BigDecimal a )
	{
		return a.movePointLeft( 2 );
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
		return RuntimeBigDecimal_v1.min( a, b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_INTERNAL_MAX( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v1.max( a, b );
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
		return RuntimeBigDecimal_v1.fun_ACOS( a );
	}

	public BigDecimal fun_ASIN( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_ASIN( a );
	}

	public BigDecimal fun_ATAN( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_ATAN( a );
	}

	public BigDecimal fun_ATAN2( BigDecimal x, BigDecimal y )
	{
		return RuntimeBigDecimal_v1.fun_ATAN2( x, y );
	}

	public BigDecimal fun_COS( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_COS( a );
	}

	public BigDecimal fun_SIN( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_SIN( a );
	}

	public BigDecimal fun_TAN( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_TAN( a );
	}

	public BigDecimal fun_DEGREES( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_DEGREES( a );
	}

	public BigDecimal fun_RADIANS( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_RADIANS( a );
	}

	public BigDecimal fun_PI()
	{
		return RuntimeBigDecimal_v1.fun_PI();
	}

	public BigDecimal fun_ROUND( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v1.fun_ROUND( a, b );
	}

	public BigDecimal fun_TRUNC( BigDecimal a, BigDecimal b )
	{
		return RuntimeBigDecimal_v1.fun_TRUNC( a, b );
	}

	public BigDecimal fun_TRUNC( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_TRUNC( a );
	}

	public BigDecimal fun_EVEN( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_EVEN( a );
	}

	public BigDecimal fun_ODD( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_ODD( a );
	}

	public BigDecimal fun_INT( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_INT( a );
	}

	public BigDecimal fun_EXP( BigDecimal p )
	{
		return BigDecimal.valueOf( Math.exp( p.doubleValue() ) );
	}

	public BigDecimal fun_POWER( BigDecimal n, BigDecimal p )
	{
		return RuntimeBigDecimal_v1.fun_POWER( n, p );
	}

	public BigDecimal fun_LN( BigDecimal p )
	{
		return RuntimeBigDecimal_v1.fun_LN( p );
	}

	public BigDecimal fun_LOG( BigDecimal p )
	{
		return RuntimeBigDecimal_v1.fun_LOG10( p );
	}

	public BigDecimal fun_LOG( BigDecimal n, BigDecimal x )
	{
		return RuntimeBigDecimal_v1.fun_LOG( n, x );
	}

	public BigDecimal fun_LOG10( BigDecimal p )
	{
		return RuntimeBigDecimal_v1.fun_LOG10( p );
	}

	public BigDecimal fun_MOD( BigDecimal n, BigDecimal d )
	{
		return RuntimeBigDecimal_v1.fun_MOD( n, d );
	}

	public BigDecimal fun_SQRT( BigDecimal n )
	{
		return RuntimeBigDecimal_v1.fun_SQRT( n );
	}


	// ------------------------------------------------ Combinatorics


	public BigDecimal fun_FACT( BigDecimal a )
	{
		return RuntimeBigDecimal_v1.fun_FACT( a );
	}


	// ------------------------------------------------ Financials


	public BigDecimal fun_IRR__if_needsValueAdjustment( BigDecimal[] _values, BigDecimal _guess )
	{
		return RuntimeBigDecimal_v1.fun_IRR( _values, _guess, fixedScale, roundingMode );
	}

	public BigDecimal fun_IRR( BigDecimal[] _values, BigDecimal _guess )
	{
		return RuntimeBigDecimal_v1.fun_IRR( _values, _guess );
	}

	public BigDecimal fun_DB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period, BigDecimal _month )
	{
		return RuntimeBigDecimal_v1.fun_DB( _cost, _salvage, _life, _period, _month );
	}

	public BigDecimal fun_DB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period )
	{
		return RuntimeBigDecimal_v1.fun_DB( _cost, _salvage, _life, _period, BigDecimal.valueOf( 12 ) );
	}

	public BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period, BigDecimal _factor )
	{
		return RuntimeBigDecimal_v1.fun_DDB( _cost, _salvage, _life, _period, _factor );
	}

	public BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period )
	{
		return RuntimeBigDecimal_v1.fun_DDB( _cost, _salvage, _life, _period, RuntimeBigDecimal_v1.TWO );
	}

	public BigDecimal fun_PMT( BigDecimal _rate, BigDecimal _nper, BigDecimal _pv, BigDecimal _fv, BigDecimal _type )
	{
		return RuntimeBigDecimal_v1.fun_PMT( _rate, _nper, _pv, _fv, _type );
	}

	public BigDecimal fun_PMT( BigDecimal _rate, BigDecimal _nper, BigDecimal _pv, BigDecimal _fv )
	{
		return RuntimeBigDecimal_v1.fun_PMT( _rate, _nper, _pv, _fv, RuntimeBigDecimal_v1.ZERO );
	}

	public BigDecimal fun_PMT( BigDecimal _rate, BigDecimal _nper, BigDecimal _pv )
	{
		return RuntimeBigDecimal_v1.fun_PMT( _rate, _nper, _pv, RuntimeBigDecimal_v1.ZERO, RuntimeBigDecimal_v1.ZERO );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv, BigDecimal _type, BigDecimal _guess )
	{
		return RuntimeBigDecimal_v1.fun_RATE( _nper, _pmt, _pv, _fv, _type, _guess );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv, BigDecimal _type )
	{
		return RuntimeBigDecimal_v1.fun_RATE( _nper, _pmt, _pv, _fv, _type, BigDecimal.valueOf( 0.1 ) );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv )
	{
		return RuntimeBigDecimal_v1.fun_RATE( _nper, _pmt, _pv, _fv, RuntimeBigDecimal_v1.ZERO, BigDecimal.valueOf( 0.1 ) );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv )
	{
		return RuntimeBigDecimal_v1.fun_RATE( _nper, _pmt, _pv, RuntimeBigDecimal_v1.ZERO, RuntimeBigDecimal_v1.ZERO, BigDecimal.valueOf( 0.1 ) );
	}


	// ------------------------------------------------ Date Functions


	public BigDecimal fun_TODAY()
	{
		return RuntimeBigDecimal_v1.fun_TODAY();
	}


}
