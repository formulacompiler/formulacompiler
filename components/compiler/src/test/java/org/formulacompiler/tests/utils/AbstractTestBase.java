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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

import junit.framework.TestCase;

public abstract class AbstractTestBase extends TestCase
{


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
		final BufferedReader expected = _expected instanceof BufferedReader ? (BufferedReader) _expected : new BufferedReader( _expected );
		final BufferedReader actual = _actual instanceof BufferedReader ? (BufferedReader) _actual : new BufferedReader( _actual );
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
