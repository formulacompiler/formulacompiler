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
package org.formulacompiler.tests.reference.base;

import java.math.BigDecimal;
import java.util.Date;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationException;

public class EngineRunningTestCase extends AbstractContextTestCase
{
	static final double DBL_EPSILON = 0.0000001;
	static final BigDecimal BIG_EPSILON = BigDecimal.valueOf( DBL_EPSILON );

	public EngineRunningTestCase( String _name, Context _cx )
	{
		super( _name, _cx );
	}

	@Override
	protected void runTest() throws Throwable
	{
		cx().getDocumenter().sameEngineRow( cx() );
		try {
			runProtected();
		}
		catch (Throwable t) {
			final SaveableEngine engine = cx().getEngine();
			if (null != engine) {
				cx().reportFailedEngineAndRethrow( this, engine, t );
			}
		}
	}


	private void runProtected() throws Throwable
	{
		final Inputs exp = cx().getExpected();
		try {
			assertValueResult( exp );
		}
		catch (ComputationException e) {
			assertExceptionResult( exp.get( 0 ), e );
		}
		catch (ArithmeticException e) {
			assertExceptionResult( exp.get( 0 ), e );
		}
	}

	private void assertValueResult( final Inputs _exp ) throws Exception
	{
		final Outputs act = (Outputs) cx().getFactory().newComputation( cx().getInputs() );

		final BindingType type = _exp.type( 0 );
		switch (type) {

			case DOUBLE: {
				double have = act.dbl();
				assertNoException( _exp, have );
				double want = _exp.dbl( 0 );
				assertEquals( want, have, DBL_EPSILON );
				break;
			}

			case BIGDEC_PREC:
			case BIGDEC_SCALE: {
				BigDecimal have = act.bdec();
				assertNoException( _exp, have );
				BigDecimal want = _exp.bdec( 0 );
				if (want.subtract( have ).abs().compareTo( BIG_EPSILON ) > 0) {
					assertEquals( want.toPlainString(), have.toPlainString() );
				}
				break;
			}

			case LONG: {
				long have = act.lng();
				assertNoException( _exp, have );
				long want = _exp.lng( 0 );
				if (Math.abs( want - have ) > 1) {
					assertEquals( want, have );
				}
				break;
			}

			case BOOLEAN: {
				boolean have = act.bool();
				assertNoException( _exp, have );
				boolean want = _exp.bool( 0 );
				assertEquals( want, have );
				break;
			}

			case DATE: {
				Date have = act.date();
				assertNoException( _exp, have );
				if (Inputs.NOW == _exp.get( 0 )) {
					assertNow( have );
				}
				else {
					Date want = _exp.date( 0 );
					assertEquals( want, have );
				}
				break;
			}

			case STRING: {
				String have = act.str();
				assertNoException( _exp, have );
				String want = _exp.str( 0 );
				assertEquals( want, have );
				break;
			}

			default:
				throw new IllegalArgumentException( "No value check implemented for " + type );
		}
	}

	private void assertNow( Date _have )
	{
		final long notBefore = System.currentTimeMillis() / Inputs.MS_PER_SEC;
		final long actual = _have.getTime() / Inputs.MS_PER_SEC;
		final long notAfter = System.currentTimeMillis() / Inputs.MS_PER_SEC;
		assertTrue( actual >= notBefore );
		assertTrue( actual <= notAfter );
	}

	private void assertNoException( Inputs _exp, Object _actual )
	{
		final Object want = _exp.get( 0 );
		if (want instanceof Class) {
			assertEquals( want, _actual );
		}
	}

	@SuppressWarnings( "unchecked" )
	private void assertExceptionResult( Object _expected, RuntimeException _exception )
	{
		if (_expected instanceof Class) {
			final Class<RuntimeException> expectedClass = (Class<RuntimeException>) _expected;
			if (expectedClass.isAssignableFrom( _exception.getClass() )) {
				return;
			}
		}
		throw _exception;
	}


}
