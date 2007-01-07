/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.tests.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import sej.CallFrame;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.internal.Settings;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

public abstract class AbstractTestBase extends TestCase
{


	protected CallFrame getInput( String _name ) throws SecurityException, NoSuchMethodException
	{
		return new CallFrame( Inputs.class.getMethod( _name ) );
	}


	protected CallFrame getOutput( String _name ) throws SecurityException, NoSuchMethodException
	{
		return new CallFrame( Outputs.class.getMethod( _name ) );
	}


	protected void checkSpreadsheetStream( Spreadsheet _expected, InputStream _stream, String _typeExtensionOrFileName )
			throws Exception
	{
		Spreadsheet actual = SEJ.loadSpreadsheet( _typeExtensionOrFileName, _stream );
		touchExpressions( actual );
		assertEquals( _expected.describe(), actual.describe() );
	}


	protected void touchExpressions( Spreadsheet _ss ) throws Exception
	{
		for (Spreadsheet.Sheet s : _ss.getSheets()) {
			for (Spreadsheet.Row r : s.getRows()) {
				for (Spreadsheet.Cell c : r.getCells()) {
					c.getExpressionText();
				}
			}
		}
	}


	private static final File JAR_PATH = new File( "src/testdata/enginejars/jre-" + jdkVersion() );

	private static final String jdkVersion()
	{
		final String ver = System.getProperty( "java.version" );
		return ver.substring( 0, 3 );
	}

	private int nextCheckId = 1;

	protected void checkEngine( SaveableEngine _engine ) throws Exception
	{
		checkEngine( _engine, "default_" + Integer.toString( this.nextCheckId++ ) );
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
					writeStreamToFile( new ByteArrayInputStream( actualBytes ), new File( jars, _id + "-actual.jar" ) );
					throw t;
				}
			}
			finally {
				expected.close();
			}
		}
		else {
			writeStreamToFile( actual, expectedFile );
		}
	}


	private void writeStreamToFile( InputStream _actual, File _file ) throws FileNotFoundException, IOException
	{
		final OutputStream expected = new BufferedOutputStream( new FileOutputStream( _file ) );
		try {
			int red;
			while ((red = _actual.read()) >= 0)
				expected.write( red );
		}
		finally {
			expected.close();
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


	protected void assertEqualToFile( String _nameOfExpectedFile, String _actual ) throws Exception
	{
		final String expected = normalizeLineEndings( readStringFrom( new File( _nameOfExpectedFile ) ) );
		final String actual = normalizeLineEndings( _actual );
		assertEquals( _nameOfExpectedFile, expected, actual );
	}


	protected static String readStringFrom( File _source ) throws IOException
	{
		StringBuffer sb = new StringBuffer( 1024 );
		BufferedReader reader = new BufferedReader( new FileReader( _source ) );
		try {
			char[] chars = new char[ 1024 ];
			int red;
			while ((red = reader.read( chars )) > -1) {
				sb.append( String.valueOf( chars, 0, red ) );
			}
		}
		finally {
			reader.close();
		}
		return sb.toString();
	}

	protected static void writeStringTo( String _value, File _target ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( _target ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}

	protected static String normalizeLineEndings( String _s )
	{
		return _s.replace( "\r\n", "\n" ).replace( '\r', '\n' );
	}


}
