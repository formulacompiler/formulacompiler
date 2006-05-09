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
package sej.engine.compiler.model.util;

import java.math.BigDecimal;


public class Util
{

	public static String valueToString( Object _arg )
	{
		if (null == _arg) {
			return "";
		}
		else if (_arg instanceof Number) {
			String result = _arg.toString();
			if (result.endsWith( ".0" )) {
				return result.substring( 0, result.length() - 2 );
			}
			else {
				return result;
			}
		}
		else {
			return _arg.toString();
		}
	}


	public static double valueToDouble( Object _value, double _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).doubleValue();
		if (_value instanceof String) return Double.valueOf( (String) _value );
		return _ifNull;
	}


	public static double valueToDoubleOrZero( Object _value )
	{
		return valueToDouble( _value, 0.0 );
	}


	public static int valueToInt( Object _value, int _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).intValue();
		if (_value instanceof String) return Integer.valueOf( (String) _value );
		return _ifNull;
	}


	public static int valueToIntOrZero( Object _value )
	{
		return valueToInt( _value, 0 );
	}


	public static int valueToIntOrOne( Object _value )
	{
		return valueToInt( _value, 1 );
	}


	public static long valueToLong( Object _value, long _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).intValue();
		if (_value instanceof String) return Long.valueOf( (String) _value );
		return _ifNull;
	}


	public static boolean valueToBoolean( Object _value, boolean _ifNull )
	{
		if (_value instanceof Boolean) return (Boolean) _value;
		if (_value instanceof Number) return (0 != ((Number) _value).intValue());
		if (_value instanceof String) return Boolean.parseBoolean( (String) _value );
		return _ifNull;
	}


	private static BigDecimal valueToBigDecimal( Object _value, BigDecimal _ifNull )
	{
		if (_value instanceof BigDecimal) return (BigDecimal) _value;
		if (_value instanceof Double) return BigDecimal.valueOf( (Double) _value );
		if (_value instanceof String) return new BigDecimal( (String) _value );
		return _ifNull;
	}


	public static BigDecimal valueToBigDecimalOrZero( Object _value )
	{
		return valueToBigDecimal( _value, BigDecimal.ZERO );
	}


}
