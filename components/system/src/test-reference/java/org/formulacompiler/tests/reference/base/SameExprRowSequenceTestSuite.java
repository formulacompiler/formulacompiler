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

abstract class SameExprRowSequenceTestSuite extends AbstractContextTestSuite
{

	public SameExprRowSequenceTestSuite( Context _cx )
	{
		super( _cx );
	}

	@Override
	protected String getOwnName()
	{
		final StringBuilder testSuiteName = new StringBuilder( "Row " );
		testSuiteName.append( cx().getRowIndex() + 1 );
		final String expr = cx().getOutputExpr();
		if (expr != null) {
			testSuiteName.append( ": " ).append( expr.replace( '(', '[' ).replace( ')', ']' ) );
		}
		return testSuiteName.toString();
	}


	@Override
	protected void setUp() throws Throwable
	{
		super.setUp();
		cx().getRowSetup().setupValues();
	}

	@Override
	protected void tearDown() throws Throwable
	{
		cx().releaseInputs();
		super.tearDown();
	}

}
