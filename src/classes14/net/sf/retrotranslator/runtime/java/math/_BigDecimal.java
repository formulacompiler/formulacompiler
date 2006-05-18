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
package net.sf.retrotranslator.runtime.java.math;

import java.math.BigDecimal;

public final class _BigDecimal
{

	public static BigDecimal valueOf( long _value )
	{
		return new BigDecimal( _value );
	}

	public static BigDecimal valueOf( double _value )
	{
		return new BigDecimal( _value );
	}

	public static BigDecimal pow( final BigDecimal x, int n )
	{
		if (n < 0 || n > 999999999) throw new ArithmeticException( "Invalid operation" );
		BigDecimal result = new BigDecimal( 1 );
		while (0 < n--) {
			result = result.multiply( x );
		}
		return result;
	}

	public static String toPlainString( final BigDecimal x )
	{
		return x.toString().trim();
	}

	public static BigDecimal setScale( final BigDecimal x, int _newScale, int _roundingMode )
	{
		if (_newScale < 0) {
			final BigDecimal shifted = x.movePointLeft( -_newScale );
			final BigDecimal truncated = shifted.setScale( 0, _roundingMode );
			return truncated.movePointRight( -_newScale );
		}
		return x.setScale( _newScale, _roundingMode );
	}

	public static int precision( final BigDecimal x )
	{
		throw new UnsupportedOperationException( "BigDecimal.precision() not supported on JRE 1.4" );
	}

}
