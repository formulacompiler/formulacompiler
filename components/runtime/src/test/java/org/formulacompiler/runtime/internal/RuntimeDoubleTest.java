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

import junit.framework.TestCase;

public class RuntimeDoubleTest extends TestCase
{


	public void testRound()
	{
		assertEquals( 1.22, RuntimeDouble_v2.round( 1.224, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.225, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.229, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.230, 2 ) );
		assertEquals( 1.23, RuntimeDouble_v2.round( 1.234, 2 ) );
		assertEquals( 1.24, RuntimeDouble_v2.round( 1.235, 2 ) );
		assertEquals( 1.24, RuntimeDouble_v2.round( 1.239999, 2 ) );

		assertEquals( 1.2, RuntimeDouble_v2.round( 1.234, 1 ) );
		assertEquals( 1.3, RuntimeDouble_v2.round( 1.25, 1 ) );

		assertEquals( 1.0, RuntimeDouble_v2.round( 1.4, 0 ) );
		assertEquals( 2.0, RuntimeDouble_v2.round( 1.5, 0 ) );

		assertEquals( 10.0, RuntimeDouble_v2.round( 14, -1 ) );
		assertEquals( 20.0, RuntimeDouble_v2.round( 15, -1 ) );

		assertEquals( -12.01, RuntimeDouble_v2.round( -12.005, 2 ) );
	}


}
