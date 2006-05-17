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
package sej;

import junit.framework.TestCase;

public abstract class AbstractNumericTypeTest extends TestCase
{

	protected abstract NumericType getType();


	public void testConstantsToString() throws Exception
	{
		assertEquals( "", getType().valueToString( null ) );
		assertEquals( "", getType().valueToConciseString( null ) );
		assertEquals( "0", getType().valueToConciseString( getType().getZero() ) );
		assertEquals( "1", getType().valueToConciseString( getType().getOne() ) );
	}

	public void testConstantsFromString() throws Exception
	{
		assertEquals( getType().getZero(), getType().valueOf( (String) null ) );
		assertEquals( getType().getZero(), getType().valueOf( "" ) );
		assertEquals( getType().getZero(), getType().valueOf( "0" ) );
		assertEquals( getType().getOne(), getType().valueOf( "1" ) );
	}


	@SuppressWarnings("unchecked")
	protected static void assertEquals( Number _a, Number _b )
	{
		Comparable a = (Comparable) _a;
		Comparable b = (Comparable) _b;
		assertEquals( 0, a.compareTo( b ) );
	}

}
