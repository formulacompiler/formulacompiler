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

package org.formulacompiler.tests.utils;

import java.util.Enumeration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public final class MultiFormatTestFactory
{

	public static Test testSuite( final Class<? extends SpreadsheetFormatTest> _testCaseClass )
	{
		final TestSuite suite = new TestSuite( _testCaseClass.getName() );
		suite.addTest( new MultiFormatTestSuite( _testCaseClass, ".xls", ".xlt" ) );
		suite.addTest( new MultiFormatTestSuite( _testCaseClass, ".xlsx", ".xltx" ) );
		suite.addTest( new MultiFormatTestSuite( _testCaseClass, ".ods", ".ots" ) );
		return suite;
	}

	private static class MultiFormatTestSuite extends TestSuite
	{
		public MultiFormatTestSuite( final Class<? extends SpreadsheetFormatTest> _class, final String _spreadsheetExtension, final String _templateExtension )
		{
			super( _class, _spreadsheetExtension );
			final Enumeration tests = tests();
			while (tests.hasMoreElements()) {
				SpreadsheetFormatTest testCase = (SpreadsheetFormatTest) tests.nextElement();
				testCase.setSpreadsheetExtension( _spreadsheetExtension );
				testCase.setSpreadsheetTemplateExtension( _templateExtension );
			}
		}

	}

	public static abstract class SpreadsheetFormatTest extends TestCase
	{
		protected SpreadsheetFormatTest()
		{
			// Nothing to do here.
		}

		protected SpreadsheetFormatTest( final String name )
		{
			super( name );
		}

		public abstract void setSpreadsheetExtension( final String _spreadsheetExtension );

		public abstract void setSpreadsheetTemplateExtension( final String _extension );
	}

	public static abstract class SpreadsheetFormatTestCase extends SpreadsheetFormatTest
	{
		private String spreadsheetExtension;

		private String spreadsheetTemplateExtension;

		protected SpreadsheetFormatTestCase()
		{
			// Nothing to do here.
		}

		protected SpreadsheetFormatTestCase( final String name )
		{
			super( name );
		}

		public String getSpreadsheetExtension()
		{
			return this.spreadsheetExtension;
		}

		public String getSpreadsheetTemplateExtension()
		{
			return this.spreadsheetTemplateExtension;
		}

		public void setSpreadsheetExtension( final String _spreadsheetExtension )
		{
			this.spreadsheetExtension = _spreadsheetExtension;
		}

		public void setSpreadsheetTemplateExtension( final String _extension )
		{
			this.spreadsheetTemplateExtension = _extension;
		}
	}
}
