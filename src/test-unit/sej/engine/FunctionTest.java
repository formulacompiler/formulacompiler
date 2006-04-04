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
package sej.engine;

import sej.engine.expressions.Function;
import sej.engine.expressions.RangeValue;
import junit.framework.TestCase;

public class FunctionTest extends TestCase
{


	public void testRound()
	{
		assertEquals( 1.22, Runtime_v1.round( 1.224, 2 ) );
		assertEquals( 1.23, Runtime_v1.round( 1.225, 2 ) );
		assertEquals( 1.23, Runtime_v1.round( 1.229, 2 ) );
		assertEquals( 1.23, Runtime_v1.round( 1.230, 2 ) );
		assertEquals( 1.23, Runtime_v1.round( 1.234, 2 ) );
		assertEquals( 1.24, Runtime_v1.round( 1.235, 2 ) );
		assertEquals( 1.24, Runtime_v1.round( 1.239999, 2 ) );

		assertEquals( 1.2, Runtime_v1.round( 1.234, 1 ) );
		assertEquals( 1.3, Runtime_v1.round( 1.25, 1 ) );

		assertEquals( 1.0, Runtime_v1.round( 1.4, 0 ) );
		assertEquals( 2.0, Runtime_v1.round( 1.5, 0 ) );

		assertEquals( 10.0, Runtime_v1.round( 14, -1 ) );
		assertEquals( 20.0, Runtime_v1.round( 15, -1 ) );

		assertEquals( -12.01, Runtime_v1.round( -12.005, 2 ) );
	}


	public void testROUND()
	{
		assertNull( Function.ROUND.evaluate( null, 2.0 ) );
		assertEquals( 1.0, Function.ROUND.evaluate( 1.4, 0.0 ) );
		assertEquals( 2.0, Function.ROUND.evaluate( 1.5, 0.0 ) );
	}


	public void testMatch()
	{
		RangeValue asc = new RangeValue( 1, 1, 3 );
		asc.add( 1.0 );
		asc.add( 2.0 );
		asc.add( 4.0 );

		assertEquals( -1, Function.match( 0.0, asc, 1 ) );
		assertEquals( +0, Function.match( 1.0, asc, 1 ) );
		assertEquals( +1, Function.match( 2.0, asc, 1 ) );
		assertEquals( +1, Function.match( 3.0, asc, 1 ) );
		assertEquals( +2, Function.match( 4.0, asc, 1 ) );
		assertEquals( +2, Function.match( 5.0, asc, 1 ) );

		assertEquals( -1, Function.match( 0.0, asc, 0 ) );
		assertEquals( +0, Function.match( 1.0, asc, 0 ) );
		assertEquals( +1, Function.match( 2.0, asc, 0 ) );
		assertEquals( -1, Function.match( 3.0, asc, 0 ) );
		assertEquals( +2, Function.match( 4.0, asc, 0 ) );
		assertEquals( -1, Function.match( 5.0, asc, 0 ) );

		RangeValue desc = new RangeValue( 1, 1, 3 );
		desc.add( 4.0 );
		desc.add( 2.0 );
		desc.add( 1.0 );

		assertEquals( -1, Function.match( 5.0, desc, -1 ) );
		assertEquals( +0, Function.match( 4.0, desc, -1 ) );
		assertEquals( +0, Function.match( 3.0, desc, -1 ) );
		assertEquals( +1, Function.match( 2.0, desc, -1 ) );
		assertEquals( +2, Function.match( 1.0, desc, -1 ) );
		assertEquals( +2, Function.match( 0.0, desc, -1 ) );
	}


}
