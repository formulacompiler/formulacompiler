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
		final Outputs act = (Outputs) cx().getFactory().newComputation( cx().getInputs() );

		final BindingType type = exp.type( 0 );
		switch (type) {

			case DOUBLE: {
				double want = exp.dbl( 0 );
				double have = act.dbl();
				assertEquals( want, have, DBL_EPSILON );
				break;
			}

			case BIGDEC_PREC:
			case BIGDEC_SCALE: {
				BigDecimal want = exp.bdec( 0 );
				BigDecimal have = act.bdec();
				if (want.subtract( have ).abs().compareTo( BIG_EPSILON ) > 0) {
					assertEquals( want.toPlainString(), have.toPlainString() );
				}
				break;
			}

			case LONG: {
				long want = exp.lng( 0 );
				long have = act.lng();
				if (Math.abs( want - have ) > 1) {
					assertEquals( want, have );
				}
				break;
			}

			case BOOLEAN: {
				boolean want = exp.bool( 0 );
				boolean have = act.bool();
				assertEquals( want, have );
				break;
			}

			case DATE: {
				Date have = act.date();
				if (Inputs.NOW == exp.get( 0 )) {
					assertNow( have );
				}
				else {
					Date want = exp.date( 0 );
					assertEquals( want, have );
				}
				break;
			}

			case STRING: {
				String want = exp.str( 0 );
				String have = act.str();
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


}
