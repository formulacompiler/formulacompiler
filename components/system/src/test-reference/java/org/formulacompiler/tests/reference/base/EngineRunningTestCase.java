/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.tests.reference.base;

import java.math.BigDecimal;
import java.util.Date;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationException;

public class EngineRunningTestCase extends AbstractContextTestCase
{
	private static final double DBL_EPSILON = 1e-7;
	private static final BigDecimal BIG_EPSILON = new BigDecimal( DBL_EPSILON );
	private static final BigDecimal BIG_EPSILON_SQ = BIG_EPSILON.multiply( BIG_EPSILON );

	private final boolean setupInputs;

	public EngineRunningTestCase( Context _cx, boolean _setupInputs )
	{
		super( _cx );
		this.setupInputs = _setupInputs;
	}

	@Override
	protected String getOwnName()
	{
		if (cx().getRowIndex() == cx().getOutputCell().getRow().getRowIndex()) return "Run";
		return "Run; input row " + (cx().getRowIndex() + 1);
	}


	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		if (this.setupInputs) {
			cx().getRowSetup().setupValues();
		}
	}

	@Override
	protected void tearDown() throws Exception
	{
		if (this.setupInputs) {
			cx().releaseInputs();
		}
		super.tearDown();
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
				if (Double.isNaN( want ) && Double.isNaN( have )) break;
				if (Double.isInfinite( want ) && Double.isInfinite( have )) break;
				// sqrt(1 + min(a^2, b^2)) yields 1 for "small" numbers and min(|a|,|b|) for "big" numbers.
				assertEquals( want, have, Math.sqrt( 1 + Math.min( want * want, have * have ) ) * DBL_EPSILON );
				break;
			}

			case BIGDEC_PREC:
			case BIGDEC_SCALE: {
				BigDecimal have = act.bdec();
				assertNoException( _exp, have );
				BigDecimal want = _exp.bdec( 0 );
				final BigDecimal w2 = want.multiply( want );
				final BigDecimal h2 = have.multiply( have );
				final double a = BigDecimal.ONE.add( w2.min( h2 ) ).multiply( BIG_EPSILON_SQ ).doubleValue();
				final BigDecimal eps = Double.isInfinite( a ) ?
						want.abs().min( have.abs() ).multiply( BIG_EPSILON ) :
						new BigDecimal( Math.sqrt( a ) );
				if (want.subtract( have ).abs().compareTo( eps ) > 0) {
					assertEquals( want.stripTrailingZeros().toPlainString(), have.stripTrailingZeros().toPlainString() );
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
