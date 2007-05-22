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
package sej.internal.runtime;

import junit.framework.TestCase;

public class RuntimeLong_v1Test extends TestCase
{
	
	public void testFromDouble()
	{
		final RuntimeLong_v1.Context context2 = new RuntimeLong_v1.Context( 2 );

		assertEquals( -2, RuntimeLong_v1.fromDouble( -0.0150001, context2 ) );
		assertEquals( -1, RuntimeLong_v1.fromDouble( -0.0149999, context2 ) );
		assertEquals( -1, RuntimeLong_v1.fromDouble( -0.0050001, context2 ) );
		assertEquals( 0, RuntimeLong_v1.fromDouble( -0.0049999, context2 ) );
		assertEquals( 0, RuntimeLong_v1.fromDouble( 0.0049999, context2 ) );
		assertEquals( 1, RuntimeLong_v1.fromDouble( 0.0050001, context2 ) );
		assertEquals( 1, RuntimeLong_v1.fromDouble( 0.0149999, context2 ) );
		assertEquals( 2, RuntimeLong_v1.fromDouble( 0.0150001, context2 ) );

		final RuntimeLong_v1.Context context0 = new RuntimeLong_v1.Context( 0 );

		assertEquals( -2, RuntimeLong_v1.fromDouble( -1.50001, context0 ) );
		assertEquals( -1, RuntimeLong_v1.fromDouble( -1.49999, context0 ) );
		assertEquals( -1, RuntimeLong_v1.fromDouble( -0.50001, context0 ) );
		assertEquals( 0, RuntimeLong_v1.fromDouble( -0.49999, context0 ) );
		assertEquals( 0, RuntimeLong_v1.fromDouble( 0.49999, context0 ) );
		assertEquals( 1, RuntimeLong_v1.fromDouble( 0.50001, context0 ) );
		assertEquals( 1, RuntimeLong_v1.fromDouble( 1.49999, context0 ) );
		assertEquals( 2, RuntimeLong_v1.fromDouble( 1.50001, context0 ) );
	}
	
}
