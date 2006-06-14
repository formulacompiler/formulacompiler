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
package sej.internal;

import sej.NumericType;


public class ScaledLongTypeTest extends AbstractNumericTypeTest
{
	private final NumericType type = NumericType.LONG4;

	@Override
	protected NumericType getType()
	{
		return this.type;
	}


	public void testStringToScaledLong()
	{
		final NumericTypeImpl.AbstractLongType dec2 = new NumericTypeImpl.ScaledLongType( 2 );

		assertEquals( 12345, dec2.parse( "123.45" ) );
		assertEquals( 12345, dec2.parse( "123.452" ) );
		assertEquals( 12340, dec2.parse( "123.4" ) );
		assertEquals( 12340, dec2.parse( "123.400" ) );
		assertEquals( 12300, dec2.parse( "123" ) );
		assertEquals( 12300, dec2.parse( "123.0" ) );
		assertEquals( -12300, dec2.parse( "-123" ) );
		assertEquals( -12300, dec2.parse( "-123.0" ) );
		assertEquals( -12345, dec2.parse( "-123.452" ) );
		assertEquals( -12340, dec2.parse( "-123.4" ) );

		assertEquals( 12345, dec2.parse( "123.454" ) );
		assertEquals( 12345, dec2.parse( "123.455" ) );
		assertEquals( 12345, dec2.parse( "123.456" ) );

		assertEquals( -12345, dec2.parse( "-123.454" ) );
		assertEquals( -12345, dec2.parse( "-123.455" ) );
		assertEquals( -12345, dec2.parse( "-123.456" ) );
	}


	public void testScaledLongToString()
	{
		final NumericTypeImpl.AbstractLongType dec2 = new NumericTypeImpl.ScaledLongType( 2 );

		assertEquals( "123.45", dec2.format( 12345 ) );
		assertEquals( "123.40", dec2.format( 12340 ) );
		assertEquals( "123.00", dec2.format( 12300 ) );
		assertEquals( "-123.45", dec2.format( -12345 ) );
		assertEquals( "-123.40", dec2.format( -12340 ) );
		assertEquals( "-123.00", dec2.format( -12300 ) );
		
		assertEquals( "0.00", dec2.format( 0 ) );
		assertEquals( "0.01", dec2.format( 1 ) );
		assertEquals( "0.10", dec2.format( 10 ) );
		assertEquals( "1.00", dec2.format( 100 ) );

		assertEquals( "0.00", dec2.format( -0 ) );
		assertEquals( "-0.01", dec2.format( -1 ) );
		assertEquals( "-0.10", dec2.format( -10 ) );
		assertEquals( "-1.00", dec2.format( -100 ) );
	}


}
