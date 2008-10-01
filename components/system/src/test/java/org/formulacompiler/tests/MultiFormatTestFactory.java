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

package org.formulacompiler.tests;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestResult;
import junit.framework.TestSuite;

public final class MultiFormatTestFactory
{

	public static Test testSuite( final Class<? extends SpreadsheetFormatTest> _testCaseClass )
	{
		final TestSuite suite = new TestSuite( _testCaseClass.getName() );
		suite.addTest( new MultiFormatTestSuite( _testCaseClass, ".xls" ) );
		suite.addTest( new MultiFormatTestSuite( _testCaseClass, ".xlsx" ) );
		suite.addTest( new MultiFormatTestSuite( _testCaseClass, ".ods" ) );
		return suite;
	}

	private static class MultiFormatTestSuite extends TestSuite
	{
		public MultiFormatTestSuite( final Class<? extends SpreadsheetFormatTest> _class, final String _spreadsheetExtension )
		{
			super( _class, _spreadsheetExtension );
			final Enumeration tests = tests();
			while (tests.hasMoreElements()) {
				SpreadsheetFormatTest testCase = (SpreadsheetFormatTest) tests.nextElement();
				testCase.setSpreadsheetExtension( _spreadsheetExtension );
			}
		}

		//LATER Remove this after Saver for XLSX implementation
		private void skipTest( Test _test, TestResult _result )
		{
			_result.startTest( _test );
			_result.endTest( _test );
		}

		//LATER Remove this after Saver for XLSX implementation
		@Override
		public void runTest( Test _test, TestResult _result )
		{
			MultiFormatTestFactory.SpreadsheetFormatTestCase spreadsheetTest = (MultiFormatTestFactory.SpreadsheetFormatTestCase) _test;
			if (spreadsheetTest.spreadsheetExtension.equals( ".xlsx" )) {
				if (spreadsheetTest.getName().equals( "testSavingDateConstant" )) {
					skipTest( _test, _result );
					return;
				}
				if (spreadsheetTest.getName().equals( "testGenerateFile" )) {
					skipTest( _test, _result );
					return;
				}
				if (spreadsheetTest.getName().equals( "testGenerateStream" )) {
					skipTest( _test, _result );
					return;
				}
				if (spreadsheetTest.getName().equals( "testGenerateTemplatedFile" )) {
					skipTest( _test, _result );
					return;
				}
				if (spreadsheetTest.getName().equals( "testGenerateTemplatedStream" )) {
					skipTest( _test, _result );
					return;
				}
			}
			super.runTest( _test, _result );
		}

	}

	public static interface SpreadsheetFormatTest
	{
		public void setSpreadsheetExtension( final String _spreadsheetExtension );
	}

	public static abstract class SpreadsheetFormatTestCase extends TestCase implements SpreadsheetFormatTest
	{
		private String spreadsheetExtension;

		protected SpreadsheetFormatTestCase()
		{
			// Nothing here.
		}

		protected SpreadsheetFormatTestCase( final String name )
		{
			super( name );
		}

		public String getSpreadsheetExtension()
		{
			return this.spreadsheetExtension;
		}

		public void setSpreadsheetExtension( final String _spreadsheetExtension )
		{
			this.spreadsheetExtension = _spreadsheetExtension;
		}
	}
}
