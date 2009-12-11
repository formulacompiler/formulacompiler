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

package org.formulacompiler.tests.time;

import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormatTestFactory;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;


public class TimeTest
{
	public static Test suite()
	{
		final TestSuite testSuite = new ActiveTestSuite( TimeTest.class.getName() );
		addTestClass( testSuite, DoubleCachedTimeTest.class );
		addTestClass( testSuite, DoubleNonCachedTimeTest.class );
		addTestClass( testSuite, ScaledBigDecimalCachedTimeTest.class );
		addTestClass( testSuite, ScaledBigDecimalNonCachedTimeTest.class );
		addTestClass( testSuite, ScaledLongCachedTimeTest.class );
		addTestClass( testSuite, ScaledLongNonCachedTimeTest.class );
		return testSuite;
	}

	private static void addTestClass( final TestSuite _testSuite,
			final Class<? extends MultiFormatTestFactory.SpreadsheetFormatTest> _testClass )
	{
		final ActiveTestSuite activeTestSuite = new ActiveTestSuite( _testClass.getName() );
		activeTestSuite.addTest( MultiFormatTestFactory.testSuite( _testClass ) );
		_testSuite.addTest( activeTestSuite );
	}

	public static class DoubleNonCachedTimeTest extends AbstractNonCachedTimeTest
	{
		public DoubleNonCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.DOUBLE );
		}
	}

	public static class PrecisionBigDecimalNonCachedTimeTest extends AbstractNonCachedTimeTest
	{
		public PrecisionBigDecimalNonCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.BIGDECIMAL64 );
		}
	}

	public static class ScaledBigDecimalNonCachedTimeTest extends AbstractNonCachedTimeTest
	{
		public ScaledBigDecimalNonCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.BIGDECIMAL_SCALE8 );
		}
	}

	public static class ScaledLongNonCachedTimeTest extends AbstractNonCachedTimeTest
	{
		public ScaledLongNonCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.LONG_SCALE6 );
		}
	}

	public static class DoubleCachedTimeTest extends AbstractCachedTimeTest
	{
		public DoubleCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.DOUBLE );
		}
	}

	public static class PrecisionBigDecimalCachedTimeTest extends AbstractCachedTimeTest
	{
		public PrecisionBigDecimalCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.BIGDECIMAL64 );
		}
	}

	public static class ScaledBigDecimalCachedTimeTest extends AbstractCachedTimeTest
	{
		public ScaledBigDecimalCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.BIGDECIMAL_SCALE8 );
		}
	}

	public static class ScaledLongCachedTimeTest extends AbstractCachedTimeTest
	{
		public ScaledLongCachedTimeTest( String _name )
		{
			super( _name, SpreadsheetCompiler.LONG_SCALE6 );
		}
	}
}
