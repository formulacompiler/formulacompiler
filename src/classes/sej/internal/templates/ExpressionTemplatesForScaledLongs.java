/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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

import sej.internal.runtime.RuntimeLong_v1;


@SuppressWarnings("unqualified-field-access")
public class ExpressionTemplatesForScaledLongs
{
	RuntimeLong_v1.Context context;
	int scale;
	long one;


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
		return RuntimeLong_v1.fromNumber( a, context );
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
		return a != 0;
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


	// ------------------------------------------------ Operators


	long op_NOOP( long a )
	{
		return a;
	}

	long op_PLUS( long a, long b )
	{
		return a + b;
	}

	long op_MINUS( long a, long b )
	{
		return a - b;
	}

	long op_MINUS( long a )
	{
		return -a;
	}

	long op_TIMES__if_isScaled( long a, long b )
	{
		return a * b / one;
	}

	long op_TIMES( long a, long b )
	{
		return a * b;
	}

	long op_DIV__if_isScaled( long a, long b )
	{
		return a * one / b;
	}

	long op_DIV( long a, long b )
	{
		return a / b;
	}

	long op_EXP( long a, long b )
	{
		return RuntimeLong_v1.op_EXP( a, b, context );
	}

	long op_PERCENT( long a )
	{
		return a / 100;
	}

	long op_MIN( long a, long b )
	{
		return (a < b) ? a : b;
	}

	long op_MAX( long a, long b )
	{
		return (a > b) ? a : b;
	}

	long op_AND( long a, long b )
	{
		return (a != 0) && (b != 0) ? 1 : 0;
	}

	long op_OR( long a, long b )
	{
		return (a != 0) || (b != 0) ? 1 : 0;
	}


	// ------------------------------------------------ Numeric Functions


	long fun_ABS( long a )
	{
		return (a < 0) ? -a : a;
	}

	long fun_ROUND( long a, long b )
	{
		return RuntimeLong_v1.fun_ROUND( a, b, context );
	}


	// ------------------------------------------------ Date Functions


	long fun_TODAY()
	{
		return RuntimeLong_v1.fun_TODAY( context );
	}


}
