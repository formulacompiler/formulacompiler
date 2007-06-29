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
package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;

import junit.framework.TestCase;

public class Runtime_v1Test extends TestCase
{


	public void testStringFromBigDecimal() throws Exception
	{
		assertEquals( "1.2", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 1.2 ) ) );
		assertEquals( "12", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 12 ) ) );
		assertEquals( "120", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 120 ) ) );
		assertEquals( "12000000000000000000", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 1.2e19 ) ) );
		assertEquals( "1.2E+20", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 1.2e20 ) ) );
		assertEquals( "12340000000000000000", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 12.34e18 ) ) );
		assertEquals( "1.234E+20", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( 12.34e19 ) ) );
		assertEquals( "-12340000000000000000", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( -12.34e18 ) ) );
		assertEquals( "-1.234E+20", Runtime_v1.stringFromBigDecimal( BigDecimal.valueOf( -12.34e19 ) ) );
	}


}
