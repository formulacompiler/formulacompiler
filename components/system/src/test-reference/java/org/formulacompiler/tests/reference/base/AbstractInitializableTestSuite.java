/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

abstract class AbstractInitializableTestSuite extends TestSuite
{

	public AbstractInitializableTestSuite( String _name )
	{
		super( _name );
	}

	public final TestSuite init()
	{
		try {
			addTests();
		}
		catch (Throwable t) {
			System.err.println( getName() );
			t.printStackTrace();
			addFailure( t );
			throw new IllegalArgumentException( t );
		}
		return this;
	}

	protected abstract void addTests() throws Exception;


	@Override
	public final void run( TestResult _result )
	{
		try {
			setUp();
			try {
				super.run( _result );
			}
			finally {
				tearDown();
			}
		}
		catch (final Throwable t) {
			// System.err.println( getName() );
			// e.printStackTrace();
			_result.addError( this, t );
		}
	}

	protected void setUp() throws Throwable
	{
		// To be overridden.
	}

	protected void tearDown() throws Throwable
	{
		// To be overridden.
	}


	protected final void addFailure( final Throwable _t )
	{
		addTest( new TestCase( "FAILED" )
		{

			@Override
			protected void runTest() throws Throwable
			{
				throw _t;
			}

		} );
	}

}
