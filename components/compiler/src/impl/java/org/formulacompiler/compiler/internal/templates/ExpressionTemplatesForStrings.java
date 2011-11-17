/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.runtime.internal.Runtime_v2;


public final class ExpressionTemplatesForStrings
{
	private final ComputationMode computationMode;
	private final Environment environment;

	public ExpressionTemplatesForStrings( ComputationMode _mode, Environment _env )
	{
		this.computationMode = _mode;
		this.environment = _env;
	}


	String util_fromString( String a )
	{
		return Runtime_v2.stringFromString( a );
	}

	String util_fromNull()
	{
		return Runtime_v2.emptyString();
	}

	String util_fromInt( int a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromLong( long a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromDouble( double a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromFloat( float a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromNumber( Number a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromBigDecimal( BigDecimal a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromBigInteger( BigInteger a )
	{
		return Runtime_v2.stringFromNumber( a, this.environment );
	}

	String util_fromBoolean( boolean a )
	{
		return Runtime_v2.stringFromBoolean( a, this.environment );
	}

	String util_fromDate( Date a )
	{
		final double num = RuntimeDouble_v2.dateToNum( a, this.environment.timeZone(), this.computationMode );
		return Runtime_v2.stringFromNumber( num, this.environment );
	}

	String util_fromScaledLong( long a, int _scale )
	{
		final BigDecimal num = RuntimeBigDecimal_v2.fromScaledLong( a, _scale );
		return Runtime_v2.stringFromNumber( num, this.environment );
	}

	String util_fromMsSinceUTC1970( long a )
	{
		final double num = RuntimeDouble_v2.msSinceUTC1970ToNum( a, this.environment.timeZone(), this.computationMode );
		return Runtime_v2.stringFromNumber( num, this.environment );
	}

	String util_fromMs( long a )
	{
		final double num = RuntimeDouble_v2.msToNum( a );
		return Runtime_v2.stringFromNumber( num, this.environment );
	}

	long util_toLong( String a )
	{
		return Runtime_v2.stringToNumber( a, this.environment, this.computationMode ).longValue();
	}

	int util_toInt( String a )
	{
		return Runtime_v2.stringToNumber( a, this.environment, this.computationMode ).intValue();
	}

	short util_toShort( String a )
	{
		return Runtime_v2.stringToNumber( a, this.environment, this.computationMode ).shortValue();
	}

	byte util_toByte( String a )
	{
		return Runtime_v2.stringToNumber( a, this.environment, this.computationMode ).byteValue();
	}

	BigDecimal util_toBigDecimal( String a )
	{
		return RuntimeBigDecimal_v2.fromString( a, this.environment, this.computationMode );
	}

	BigInteger util_toBigInteger( String a )
	{
		return RuntimeBigDecimal_v2.fromString( a, this.environment, this.computationMode ).toBigInteger();
	}

	float util_toFloat( String a )
	{
		return Runtime_v2.stringToNumber( a, this.environment, this.computationMode ).floatValue();
	}

	double util_toDouble( String a )
	{
		return Runtime_v2.stringToNumber( a, this.environment, this.computationMode ).doubleValue();
	}

	boolean util_toBoolean( String a )
	{
		return Runtime_v2.stringToBoolean( a, this.environment, this.computationMode );
	}

	Date util_toDate( String a )
	{
		return Runtime_v2.stringToDate( a, this.environment, this.computationMode );
	}

	long util_toScaledLong( String a, int _scale )
	{
		return Runtime_v2.stringToScaledLong( a, _scale, this.environment, this.computationMode );
	}

	long util_toMs( String a )
	{
		return Runtime_v2.stringToMs( a, this.environment, this.computationMode );
	}

	long util_toMsSinceUTC1970( String a )
	{
		return Runtime_v2.stringToMsSinceUTC1970( a, this.environment, this.computationMode );
	}

	// Use utilFun_ here so "a" is not chained. We need it after the call to "new" for "<init>".

	StringBuilder utilFun_newBuilder( String a )
	{
		return new StringBuilder( a );
	}

	// Use utilOp_ from here because the StringBuilder is chained.

	StringBuilder utilOp_appendBuilder( StringBuilder b, String s )
	{
		return b.append( s );
	}

	String utilOp_fromBuilder( StringBuilder b )
	{
		return b.toString();
	}

	public String fun_ERROR( String _message )
	{
		Runtime_v2.fun_ERROR( _message );
		return null;
	}

	public String fun_CLEAN( String a )
	{
		return Runtime_v2.fun_CLEAN( a );
	}

	public String fun_FIXED( Number _number, int _decimals, int _no_commas )
	{
		boolean no_commas = _no_commas != 0;
		return Runtime_v2.fun_FIXED( _number, _decimals, no_commas, this.environment );
	}

	public String fun_FIXED( Number _number, int _decimals )
	{
		return Runtime_v2.fun_FIXED( _number, _decimals, false, this.environment );
	}

	public String fun_FIXED( Number _number )
	{
		return Runtime_v2.fun_FIXED( _number, 2, false, this.environment );
	}

	public String fun_CHAR( int a )
	{
		return Runtime_v2.fun_CHAR( a, this.environment );
	}

	public String fun_DOLLAR( Number _number, int _decimals )
	{
		return Runtime_v2.fun_DOLLAR( _number, _decimals, this.environment );
	}

	public String fun_DOLLAR( Number _number )
	{
		return Runtime_v2.fun_DOLLAR( _number, this.environment );
	}

	public String fun_LOWER( String a )
	{
		return Runtime_v2.fun_LOWER( a );
	}

	public String fun_ROMAN( int _number )
	{
		return Runtime_v2.fun_ROMAN( _number, 0 );
	}

	public String fun_ROMAN( int _number, int _form )
	{
		return Runtime_v2.fun_ROMAN( _number, _form );
	}

	public String fun_UPPER( String a )
	{
		return Runtime_v2.fun_UPPER( a );
	}

	public String fun_PROPER( String a )
	{
		return Runtime_v2.fun_PROPER( a );
	}

	public String fun_REPT( String a, int n )
	{
		return Runtime_v2.fun_REPT( a, n );
	}

	public String fun_TRIM( String a )
	{
		return Runtime_v2.fun_TRIM( a );
	}

	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- fun_MID
	public String fun_MID( String s, int start, int len )
	{
		return Runtime_v2.fun_MID( s, start, len );
	}
	// ---- fun_MID

	public String fun_LEFT( String s )
	{
		return Runtime_v2.fun_LEFT( s, 1 );
	}

	public String fun_LEFT( String s, int len )
	{
		return Runtime_v2.fun_LEFT( s, len );
	}

	public String fun_RIGHT( String s )
	{
		return Runtime_v2.fun_RIGHT( s, 1 );
	}

	public String fun_RIGHT( String s, int len )
	{
		return Runtime_v2.fun_RIGHT( s, len );
	}

	public String fun_SUBSTITUTE( String s, String src, String tgt )
	{
		return Runtime_v2.fun_SUBSTITUTE( s, src, tgt );
	}

	public String fun_SUBSTITUTE( String s, String src, String tgt, int occurrence )
	{
		return Runtime_v2.fun_SUBSTITUTE( s, src, tgt, occurrence );
	}

	public String fun_REPLACE( String s, int at, int len, String repl )
	{
		return Runtime_v2.fun_REPLACE( s, at, len, repl );
	}

	public String fun_TEXT( Number _num, String _format )
	{
		return Runtime_v2.fun_TEXT( _num, _format, this.environment );
	}

	public String fun_ADDRESS( int _row, int _column, int absRelType, int a1Style, String sheet )
	{
		return Runtime_v2.fun_ADDRESS( _row, _column, absRelType, a1Style != 0, sheet, this.computationMode );
	}

	public String fun_ADDRESS( int _row, int _column, int absRelType, int a1Style )
	{
		return Runtime_v2.fun_ADDRESS( _row, _column, absRelType, a1Style != 0, null, this.computationMode );
	}

}
