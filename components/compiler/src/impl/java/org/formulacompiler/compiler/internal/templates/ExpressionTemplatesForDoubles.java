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

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v1;


public final class ExpressionTemplatesForDoubles
{
	public Environment environment = null;


	// ------------------------------------------------ Utils


	double util_round( double a, int _maxFrac )
	{
		return RuntimeDouble_v1.round( a, _maxFrac );
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
		return RuntimeDouble_v1.numberToNum( a );
	}

	double util_fromBoolean( boolean a )
	{
		return RuntimeDouble_v1.booleanToNum( a );
	}

	double util_fromDate( Date a )
	{
		return RuntimeDouble_v1.dateToNum( a, environment.timeZone );
	}


	byte util_toByte( double a )
	{
		return (byte) a;
	}

	short util_toShort( double a )
	{
		return (short) a;
	}

	int util_toInt( double a )
	{
		return (int) a;
	}

	long util_toLong( double a )
	{
		return (long) a;
	}

	double util_toDouble( double a )
	{
		return a;
	}

	float util_toFloat( double a )
	{
		return (float) a;
	}

	BigInteger util_toBigInteger( double a )
	{
		return BigInteger.valueOf( (long) a );
	}

	BigDecimal util_toBigDecimal( double a )
	{
		return BigDecimal.valueOf( a );
	}

	boolean util_toBoolean( double a )
	{
		return RuntimeDouble_v1.booleanFromNum( a );
	}

	char util_toCharacter( double a )
	{
		return (char) a;
	}

	Date util_toDate( double a )
	{
		return RuntimeDouble_v1.dateFromNum( a, environment.timeZone );
	}

	String util_toString( double a )
	{
		return RuntimeDouble_v1.toExcelString( a );
	}
	
	Number util_toNumber( double a )
	{
		return a;
	}


	double util_fromScaledLong( long a, long _scalingFactor )
	{
		return RuntimeDouble_v1.fromScaledLong( a, _scalingFactor );
	}

	long util_toScaledLong( double a, long _scalingFactor )
	{
		return RuntimeDouble_v1.toScaledLong( a, _scalingFactor );
	}


	// ------------------------------------------------ Array Fold


	double foldArray( double[] _a )
	{
		final double[] a = _a;
		double acc = foldInitial();
		int i = 1;
		for (double ai : a) {
			acc = foldElement( acc, ai, i );
			i++;
		}
		return acc;
	}
	
	private double foldInitial() // abstract, really
	{
		return 0;
	}

	private double foldElement( double _acc, double _d, int _i ) // abstract, really
	{
		return 0;
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
		return RuntimeDouble_v1.min( a, b );
	}

	public double op_INTERNAL_MAX( double a, double b )
	{
		return RuntimeDouble_v1.max( a, b );
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
		return RuntimeDouble_v1.fun_ACOS( a );
	}

	public double fun_ASIN( double a )
	{
		return RuntimeDouble_v1.fun_ASIN( a );
	}

	public double fun_ATAN( double a )
	{
		return Math.atan( a );
	}

	public double fun_ATAN2( double x, double y )
	{
		return Math.atan2( y, x );
	}

	public double fun_COS( double a )
	{
		return Math.cos( a );
	}

	public double fun_SIN( double a )
	{
		return Math.sin( a );
	}

	public double fun_TAN( double a )
	{
		return Math.tan( a );
	}

	public double fun_DEGREES( double a )
	{
		return Math.toDegrees( a );
	}

	public double fun_RADIANS( double a )
	{
		return Math.toRadians( a );
	}

	public double fun_PI()
	{
		return Math.PI;
	}

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ROUND
	public double fun_ROUND( double a, double b )
	{
		return RuntimeDouble_v1.round( a, (int) b );
	}
	// ---- fun_ROUND

	public double fun_TRUNC( double a, double b )
	{
		return RuntimeDouble_v1.trunc( a, (int) b );
	}

	public double fun_TRUNC( double a )
	{
		return RuntimeDouble_v1.fun_TRUNC( a );
	}

	public double fun_EVEN( double a )
	{
		return RuntimeDouble_v1.fun_EVEN( a );
	}

	public double fun_ODD( double a )
	{
		return RuntimeDouble_v1.fun_ODD( a );
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
		return RuntimeDouble_v1.fun_POWER( n, p );
	}

	public double fun_LN( double p )
	{
		return RuntimeDouble_v1.fun_LN( p );
	}

	public double fun_LOG( double p )
	{
		return RuntimeDouble_v1.fun_LOG10( p );
	}

	public double fun_LOG( double n, double x )
	{
		return RuntimeDouble_v1.fun_LOG( n, x );
	}

	public double fun_LOG10( double p )
	{
		return RuntimeDouble_v1.fun_LOG10( p );
	}

	public double fun_MOD( double n, double d )
	{
		return RuntimeDouble_v1.fun_MOD( n, d );
	}

	public double fun_SQRT( double n )
	{
		return RuntimeDouble_v1.fun_SQRT( n );
	}

	// ------------------------------------------------ Combinatorics


	public double fun_FACT( double a )
	{
		return RuntimeDouble_v1.fun_FACT( a );
	}


	// ------------------------------------------------ Financials


	public double fun_IRR( double[] _values, double _guess )
	{
		return RuntimeDouble_v1.fun_IRR( _values, _guess );
	}

	public double fun_DB( double _cost, double _salvage, double _life, double _period, double _month )
	{
		return RuntimeDouble_v1.fun_DB( _cost, _salvage, _life, _period, _month );
	}

	public double fun_DB( double _cost, double _salvage, double _life, double _period )
	{
		return RuntimeDouble_v1.fun_DB( _cost, _salvage, _life, _period, 12 );
	}

	public double fun_DDB( double _cost, double _salvage, double _life, double _period, double _factor )
	{
		return RuntimeDouble_v1.fun_DDB( _cost, _salvage, _life, _period, _factor );
	}

	public double fun_DDB( double _cost, double _salvage, double _life, double _period )
	{
		return RuntimeDouble_v1.fun_DDB( _cost, _salvage, _life, _period, 2 );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv, double _fv, double _type, double _guess )
	{
		return RuntimeDouble_v1.fun_RATE( _nper, _pmt, _pv, _fv, _type, _guess );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv, double _fv, double _type )
	{
		return RuntimeDouble_v1.fun_RATE( _nper, _pmt, _pv, _fv, _type, 0.1 );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv, double _fv )
	{
		return RuntimeDouble_v1.fun_RATE( _nper, _pmt, _pv, _fv, 0, 0.1 );
	}

	public double fun_RATE( double _nper, double _pmt, double _pv )
	{
		return RuntimeDouble_v1.fun_RATE( _nper, _pmt, _pv, 0, 0, 0.1 );
	}


	// ------------------------------------------------ Date Functions


	public double fun_DATE( double _year, double _month, double _day )
	{
		return RuntimeDouble_v1.excelDateToNum( (int) _year, (int) _month, (int) _day );
	}

	public double fun_TIME( double _hour, double _minute, double _second )
	{
		return RuntimeDouble_v1.fun_TIME( _hour, _minute, _second );
	}

	public double fun_SECOND( double _date )
	{
		return RuntimeDouble_v1.fun_SECOND( _date );
	}

	public double fun_MINUTE( double _date )
	{
		return RuntimeDouble_v1.fun_MINUTE( _date );
	}

	public double fun_HOUR( double _date )
	{
		return RuntimeDouble_v1.fun_HOUR( _date );
	}

	public double fun_WEEKDAY( double _date, double _type )
	{
		return RuntimeDouble_v1.getWeekDayFromNum( _date, (int) Math.round( _type ) );
	}

	public double fun_WEEKDAY( double _date )
	{
		return RuntimeDouble_v1.getWeekDayFromNum( _date, 1 );
	}

	public double fun_DAY( double _date )
	{
		return RuntimeDouble_v1.getDayFromNum( _date );
	}

	public double fun_MONTH( double _date )
	{
		return RuntimeDouble_v1.getMonthFromNum( _date );
	}

	public double fun_YEAR( double _date )
	{
		return RuntimeDouble_v1.getYearFromNum( _date );
	}

	public double fun_TODAY()
	{
		return RuntimeDouble_v1.fun_TODAY( this.environment );
	}


	// ------------------------------------------------ Conversions Functions


	// TODO Parse date and time values
	public double fun_VALUE( String _text )
	{
		return RuntimeDouble_v1.fun_VALUE( _text, this.environment );
	}


}
