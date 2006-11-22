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

import sej.internal.runtime.RuntimeDouble_v1;

public abstract class ExpressionTemplatesForDoubles
{


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
		return a == null ? 0 : a.doubleValue();
	}

	double util_fromBoolean( boolean a )
	{
		return a ? 1 : 0;
	}

	double util_fromDate( Date a )
	{
		return RuntimeDouble_v1.dateToNum( a );
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
		// Use a local so the conditional does not span the return instruction.
		final boolean b = a != 0;
		return b;
	}

	char util_toCharacter( double a )
	{
		return (char) a;
	}

	Date util_toDate( double a )
	{
		return RuntimeDouble_v1.dateFromNum( a );
	}

	String util_toString( double a )
	{
		return RuntimeDouble_v1.toExcelString( a );
	}


	double util_fromScaledLong( long a, long _scalingFactor )
	{
		return RuntimeDouble_v1.fromScaledLong( a, _scalingFactor );
	}

	long util_toScaledLong( double a, long _scalingFactor )
	{
		return RuntimeDouble_v1.toScaledLong( a, _scalingFactor );
	}


	// ------------------------------------------------ Operators


	double op_INTERNAL_NOOP( double a )
	{
		return a;
	}

	double op_PLUS( double a, double b )
	{
		return a + b;
	}

	double op_MINUS( double a, double b )
	{
		return a - b;
	}

	double op_MINUS( double a )
	{
		return -a;
	}

	double op_TIMES( double a, double b )
	{
		return a * b;
	}

	double op_DIV( double a, double b )
	{
		return a / b;
	}

	double op_EXP( double a, double b )
	{
		return Math.pow( a, b );
	}

	double op_PERCENT( double a )
	{
		return a / 100;
	}

	double op_INTERNAL_MIN( double a, double b )
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

	double op_INTERNAL_MAX( double a, double b )
	{
		return RuntimeDouble_v1.max( a, b );
	}


	// ------------------------------------------------ Numeric Functions


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ABS
	double fun_ABS( double a )
	{
		return Math.abs( a );
	}
	// ---- fun_ABS

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_ROUND
	double fun_ROUND( double a, double b )
	{
		return RuntimeDouble_v1.round( a, (int) b );
	}
	// ---- fun_ROUND


	// ------------------------------------------------ Combinatorics


	double fun_FACT( double a )
	{
		return RuntimeDouble_v1.fun_FACT( a );
	}


	// ------------------------------------------------ Date Functions


	double fun_TODAY()
	{
		return RuntimeDouble_v1.fun_TODAY();
	}


}
