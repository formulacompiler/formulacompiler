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
package org.formulacompiler.tests.time;

import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.extensions.ActiveTestSuite;
import junit.framework.Test;
import junit.framework.TestSuite;


public class TimeTest
{
	public static Test suite()
	{
		final TestSuite testSuite = new ActiveTestSuite();
		testSuite.addTest( new ActiveTestSuite( DoubleCachedTimeTest.class ) );
		testSuite.addTest( new ActiveTestSuite( DoubleNonCachedTimeTest.class ) );
		testSuite.addTest( new ActiveTestSuite( ScaledBigDecimalCachedTimeTest.class ) );
		testSuite.addTest( new ActiveTestSuite( ScaledBigDecimalNonCachedTimeTest.class ) );
		testSuite.addTest( new ActiveTestSuite( ScaledLongCachedTimeTest.class ) );
		testSuite.addTest( new ActiveTestSuite( ScaledLongNonCachedTimeTest.class ) );
		return testSuite;
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
