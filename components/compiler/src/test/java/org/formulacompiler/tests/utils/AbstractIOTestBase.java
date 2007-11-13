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
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Settings;
import org.formulacompiler.describable.Describable;

import junit.framework.AssertionFailedError;

public abstract class AbstractIOTestBase extends AbstractTestBase
{
	private static final File JAR_PATH = new File( "src/test/data/enginejars/jre-" + Util.jdkVersionSuffix() );

	protected AbstractIOTestBase()
	{
		super();
	}

	protected AbstractIOTestBase( String _name )
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
					writeStreamToFile( new ByteArrayInputStream( actualBytes ), actualFile );
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
			writeStreamToFile( actual, expectedFile );
		}
	}


	protected void assertYaml( File _path, String _expectedFileBaseName, Describable _actual, String _actualFileName )
			throws Exception
	{
		final String have = _actual.describe();
		final File expectedFile = new File( _path, _expectedFileBaseName + ".yaml" );
		if (expectedFile.exists()) {
			final String want = Util.readStringFrom( expectedFile );
			if (!want.equals( have )) {
				final File actualFile = new File( _path, _actualFileName + "-actual.yaml" );
				Util.writeStringTo( have, actualFile );
				assertEquals( "YAML bad for " + _actualFileName + "; actual YAML written to ...-actual.yaml", want, have );
			}
		}
		else {
			Util.writeStringTo( have, expectedFile );
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


}
