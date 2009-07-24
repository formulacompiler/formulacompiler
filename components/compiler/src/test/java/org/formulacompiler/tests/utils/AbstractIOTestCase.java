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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.IOUtil;
import org.formulacompiler.compiler.internal.Settings;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractIOTestCase extends TestCase
{
	private static final File JAR_PATH = new File( "src/test/data/enginejars/jre-" + Util.jdkVersionSuffix() );

	protected AbstractIOTestCase()
	{
		super();
	}

	protected AbstractIOTestCase( String _name )
	{
		super( _name );
	}


	private int nextEngineCheckId = 1;

	protected void checkEngine( SaveableEngine _engine ) throws Exception
	{
		checkEngine( _engine, "default_" + Integer.toString( this.nextEngineCheckId++ ) );
	}

	protected void checkEngine( SaveableEngine _engine, String _id ) throws Exception
	{
		final File jars = new File( JAR_PATH, getClass().getSimpleName() + '/' + getName() );
		jars.mkdirs();

		// Make sure dates in .zip files are 0ed out so .zips are comparable:
		Settings.setDebugCompilationEnabled( true );

		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		_engine.saveTo( outputStream );
		final byte[] actualBytes = outputStream.toByteArray();
		final InputStream actual = new ByteArrayInputStream( actualBytes );
		final File expectedFile = new File( jars, _id + ".jar" );
		if (expectedFile.exists()) {
			final InputStream expected = new BufferedInputStream( new FileInputStream( expectedFile ) );
			try {
				try {
					IOAssert.assertEqualStreams( "Comparing engines for " + _id + "; actual engine written to ...-actual.jar",
							expected, actual );
				}
				catch (AssertionFailedError t) {
					final File actualFile = new File( jars, _id + "-actual.jar" );
					IOUtil.writeStreamToFile( new ByteArrayInputStream( actualBytes ), actualFile );
					final String actualDisasm = Util.disassemble( actualFile );
					final String expectedDisasm = Util.disassemble( expectedFile );
					assertEquals( t.getMessage(), expectedDisasm, actualDisasm );
					throw t;
				}
			}
			finally {
				expected.close();
			}
		}
		else {
			IOUtil.writeStreamToFile( actual, expectedFile );
		}
	}


	protected void assertEqualReaders( String _message, Reader _expected, Reader _actual ) throws Exception
	{
		int line = 1;
		final BufferedReader expected = _expected instanceof BufferedReader ? (BufferedReader) _expected
				: new BufferedReader( _expected );
		final BufferedReader actual = _actual instanceof BufferedReader ? (BufferedReader) _actual : new BufferedReader(
				_actual );
		while (true) {
			final String e = expected.readLine();
			final String a = actual.readLine();
			if (e == null && a == null) break;
			if (e == null || !e.equals( a )) {
				assertEquals( _message + " at line " + line, e, a );
			}
			line++;
		}
	}


	protected void assertEqualTextFiles( String _nameOfExpectedFile, String _nameOfActualFile ) throws Exception
	{
		assertEqualTextFiles( new File( _nameOfExpectedFile ), new File( _nameOfActualFile ) );
	}

	protected void assertEqualTextFiles( File _nameOfExpectedFile, File _nameOfActualFile ) throws Exception
	{
		final Reader exp = new FileReader( _nameOfExpectedFile );
		final Reader act = new FileReader( _nameOfActualFile );
		assertEqualReaders( "Comparing files " + _nameOfExpectedFile + " and " + _nameOfActualFile, exp, act );
	}


	protected void assertEqualToFile( String _nameOfExpectedFile, String _actual ) throws Exception
	{
		final String expected = normalizeLineEndings( readStringFrom( new File( _nameOfExpectedFile ) ) );
		final String actual = normalizeLineEndings( _actual );
		assertEquals( _nameOfExpectedFile, expected, actual );
	}


	protected static String readStringFrom( File _source ) throws IOException
	{
		return IOUtil.readStringFrom( _source );
	}

	protected static void writeStringTo( String _value, File _target ) throws IOException
	{
		IOUtil.writeStringTo( _value, _target );
	}

	protected static String normalizeLineEndings( String _s )
	{
		return IOUtil.normalizeLineEndings( _s );
	}

}
