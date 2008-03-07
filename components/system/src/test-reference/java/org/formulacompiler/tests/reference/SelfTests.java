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

package org.formulacompiler.tests.reference;

import org.formulacompiler.tests.reference.base.AbstractEngineCompilingTestSuite;
import org.formulacompiler.tests.reference.base.Context;
import org.formulacompiler.tests.reference.base.EngineRunningTestCase;
import org.formulacompiler.tests.reference.base.RowSetup;
import org.formulacompiler.tests.reference.base.SheetSuiteSetup;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestSuite;

public class SelfTests extends SheetSuiteSetup
{

	public static Test suite() throws Exception
	{
		TestSuite result = new TestSuite( "Files" );

		result.addTest( badResultSuite() );
		result.addTest( badAlternativeResultSuite() );

		return result;
	}

	private static Test badResultSuite() throws Exception
	{
		final Context loaderCx = newSheetContext( "BadResult" );
		final TestSuite loader = newLoader( loaderCx );
		final Context rowCx = new Context( loaderCx );
		rowCx.setRow( 1 );
		final RowSetup rowSetup = rowCx.getRowSetup();
		rowSetup.makeInput();
		rowSetup.makeOutput();
		rowSetup.setupValues();
		rowCx.setInputBindingBits( -1 );

		loader.addTest( new AbstractEngineCompilingTestSuite( rowCx )
		{

			@Override
			protected String getOwnName()
			{
				return "Compile row 2 and check for failure";
			}

			@Override
			protected void addTests() throws Exception
			{
				addTest( new EngineRunningTestCase( rowCx, false )
				{

					@Override
					protected void runTest() throws Throwable
					{
						try {
							super.runTest();
							fail( "Failing test did not fail" );
						}
						catch (AssertionFailedError e) {
							final String msg = e.getMessage();
							assertContains( msg, "expected:<4.0> but was:<3.0>" );
						}
					}

				}.init() );
			}

		}.init() );

		return loader;
	}

	private static Test badAlternativeResultSuite() throws Exception
	{
		final Context loaderCx = newSheetContext( "BadAlternativeResult" );
		final TestSuite loader = newLoader( loaderCx );
		final Context rowCx = new Context( loaderCx );
		rowCx.setRow( 1 );
		final RowSetup rowSetup = rowCx.getRowSetup();
		rowSetup.makeInput();
		rowSetup.makeOutput();
		rowSetup.setupValues();
		rowCx.setInputBindingBits( -1 );

		loader.addTest( new AbstractEngineCompilingTestSuite( rowCx )
		{

			@Override
			protected String getOwnName()
			{
				return "Compile row 2";
			}

			@Override
			protected void addTests() throws Exception
			{
				final Context inpCx = new Context( cx() );
				inpCx.setRow( 2 );
				inpCx.getRowSetup().makeInput();

				addTest( new EngineRunningTestCase( inpCx, true )
				{

					@Override
					protected void runTest() throws Throwable
					{
						try {
							super.runTest();
							fail( "Failing test did not fail" );
						}
						catch (AssertionFailedError e) {
							final String msg = e.getMessage();
							assertContains( msg, "expected:<3.0> but was:<7.0>" );
						}
					}

				}.init() );
			}

		}.init() );

		return loader;
	}

}
