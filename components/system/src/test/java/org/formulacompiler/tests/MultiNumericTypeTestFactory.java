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

package org.formulacompiler.tests;

import java.util.Enumeration;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public final class MultiNumericTypeTestFactory
{

	public static Test testSuite( final Class<? extends SpreadsheetNumericTypeTest> _testCaseClass )
	{
		final TestSuite suite = new TestSuite( _testCaseClass.getName() );
		suite.addTest( new MultiNumericTypeTestSuite( _testCaseClass, FormulaCompiler.DOUBLE ) );
		suite.addTest( new MultiNumericTypeTestSuite( _testCaseClass, FormulaCompiler.BIGDECIMAL128 ) );
		suite.addTest( new MultiNumericTypeTestSuite( _testCaseClass, FormulaCompiler.BIGDECIMAL_SCALE8 ) );
		suite.addTest( new MultiNumericTypeTestSuite( _testCaseClass, FormulaCompiler.LONG_SCALE6 ) );
		return suite;
	}

	private static class MultiNumericTypeTestSuite extends TestSuite
	{
		public MultiNumericTypeTestSuite( final Class<? extends SpreadsheetNumericTypeTest> _class, final NumericType _numericType )
		{
			super( _class, _numericType.toString() );
			final Enumeration tests = tests();
			while (tests.hasMoreElements()) {
				SpreadsheetNumericTypeTest testCase = (SpreadsheetNumericTypeTest) tests.nextElement();
				testCase.setNumericType( _numericType );
			}
		}
	}

	public static abstract class SpreadsheetNumericTypeTest extends TestCase
	{
		protected SpreadsheetNumericTypeTest()
		{
			// Nothing to do here.
		}

		protected SpreadsheetNumericTypeTest( final String name )
		{
			super( name );
		}

		public abstract void setNumericType( final NumericType _spreadsheetExtension );
	}

	public static abstract class SpreadsheetNumericTypeTestCase extends SpreadsheetNumericTypeTest
	{
		private NumericType numericType;

		protected SpreadsheetNumericTypeTestCase()
		{
			// Nothing to do here.
		}

		protected SpreadsheetNumericTypeTestCase( final String name )
		{
			super( name );
		}

		public NumericType getNumericType()
		{
			return this.numericType;
		}

		public void setNumericType( final NumericType _numericType )
		{
			this.numericType = _numericType;
		}
	}
}