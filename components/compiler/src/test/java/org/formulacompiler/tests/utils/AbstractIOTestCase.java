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
import org.formulacompiler.describable.Describable;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractIOTestCase extends TestCase
{
	private static final File JAR_PATH = new File( "src/test/data/enginejars/jre-" + Util.jdkVersionSuffix() );
	private static final boolean UPDATE_YAML_IN_PLACE = Util.isBuildPropTrue( "test-ref-update-yaml" );

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
					assertEqualStreams( "Comparing engines for " + _id + "; actual engine written to ...-actual.jar",
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


	protected void assertYaml( File _path, String _expectedFileBaseName, Describable _actual, String _actualFileName )
			throws Exception
	{
		final String have = _actual.describe();
		final File specificFile = new File( _path, _actualFileName + ".yaml" );
		final File genericFile = new File( _path, _expectedFileBaseName + ".yaml" );
		final File expectedFile = (specificFile.exists()) ? specificFile : genericFile;
		if (expectedFile.exists()) {
			String want = Util.readStringFrom( expectedFile );
			if (_actualFileName.endsWith( ".ods" )) {
				want = want.replaceAll( "- err: #DIV/0\\!", "- const: \"#DIV/0!\"" );
				want = want.replaceAll( "- err: #N/A", "- const: \"#N/A\"" );
				want = want.replaceAll( "- err: #VALUE\\!", "- const: \"#VALUE!\"" );
				want = want.replaceAll( "- err: #REF\\!", "- const: \"#REF!\"" );
				want = want.replaceAll( "- err: #NUM\\!", "- const: \"#NUM!\"" );
			}
			if (!want.equals( have )) {
				if (UPDATE_YAML_IN_PLACE) {
					final File actualFile = (_actualFileName.toLowerCase().endsWith( ".xls" ) && !specificFile.exists())
							? genericFile : specificFile;
					Util.writeStringTo( have, actualFile );
				}
				else {
					final File actualFile = new File( _path, _actualFileName + "-actual.yaml" );
					Util.writeStringTo( have, actualFile );
					assertEquals( "YAML bad for " + _actualFileName + "; actual YAML written to ...-actual.yaml", want, have );
				}
			}
		}
		else {
			Util.writeStringTo( have, expectedFile );
		}
	}

	protected void assertEqualStreams( String _message, InputStream _expected, InputStream _actual ) throws Exception
	{
		int offset = 0;
		while (true) {
			final int e = _expected.read();
			final int a = _actual.read();
			if (e != a) {
				assertEquals( _message + " at offset " + offset, e, a );
			}
			offset++;
			if (e < 0) break;
		}
	}


	protected void assertEqualFiles( String _nameOfExpectedFile, String _nameOfActualFile ) throws Exception
	{
		assertEqualFiles( new File( _nameOfExpectedFile ), new File( _nameOfActualFile ) );
	}

	protected void assertEqualFiles( File _nameOfExpectedFile, File _nameOfActualFile ) throws Exception
	{
		final InputStream exp = new BufferedInputStream( new FileInputStream( _nameOfExpectedFile ) );
		final InputStream act = new BufferedInputStream( new FileInputStream( _nameOfActualFile ) );
		assertEqualStreams( "Comparing files " + _nameOfExpectedFile + " and " + _nameOfActualFile, exp, act );
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
