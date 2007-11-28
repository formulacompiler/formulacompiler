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
