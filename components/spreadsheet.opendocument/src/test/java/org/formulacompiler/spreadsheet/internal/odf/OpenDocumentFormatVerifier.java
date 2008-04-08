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

package org.formulacompiler.spreadsheet.internal.odf;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.odf.saver.io.NonClosableInputStream;
import org.custommonkey.xmlunit.XMLAssert;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import junit.framework.Assert;

public class OpenDocumentFormatVerifier
{
	public static final String RELAXNG_NS_URI = "http://relaxng.org/ns/structure/1.0";

	public static void verify( InputStream _odsInputStream ) throws Exception
	{
		final VerifierFactory factory = VerifierFactory.newInstance( RELAXNG_NS_URI );
		final Verifier manifestVerifier = newVerifier( factory, "OpenDocument-manifest-schema-v1.1.rng" );
		final Verifier verifier = newVerifier( factory, "OpenDocument-strict-schema-v1.1.rng" );

		final ZipInputStream zipInputStream = new ZipInputStream( _odsInputStream );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			final String name = zipEntry.getName();
			final InputStream inputStream = new NonClosableInputStream( zipInputStream );
			if ("content.xml".equals( name )
					|| "meta.xml".equals( name ) || "settings.xml".equals( name ) || "styles.xml".equals( name )) {
				verifier.verify( new InputSource( inputStream ) );
			}
			else if ("META-INF/manifest.xml".equals( name )) {
				manifestVerifier.verify( new InputSource( inputStream ) );
			}
		}
	}

	private static Verifier newVerifier( final VerifierFactory _factory, final String _schemaName )
			throws VerifierConfigurationException, SAXException, IOException
	{
		final File file = new File( "src/test/data/schema/" + _schemaName );
		final Schema schema = _factory.compileSchema( file );
		return schema.newVerifier();
	}


	public static void compare( String _message, InputStream _expectedStream, InputStream _actualStream )
			throws Exception
	{
		final Map<String, String> expectedOdsContents = readArchive( _expectedStream );
		final Map<String, String> actualOdsContents = readArchive( _actualStream );
		for (String s : ODF_CONTENTS) {
			final String expectedContent = expectedOdsContents.get( s );
			if (expectedContent == null) {
				Assert.fail( s + " is missing in expected file." );
			}
			final String actualContent = actualOdsContents.get( s );
			if (actualContent == null) {
				Assert.fail( s + " is missing in generated file." );
			}
			final String msg = (_message == null) ? s : _message + " in zipped file " + s;
			if (s.equals( "mimetype" )) {
				Assert.assertEquals( msg, expectedContent, actualContent );
			}
			else if (!s.equals( "meta.xml" )) {
				XMLAssert.assertXMLEqual( msg, expectedContent, actualContent );
			}
		}
	}

	public static void assertOdsEqual( InputStream _expectedStream, InputStream _actualStream ) throws Exception
	{
		compare( null, _expectedStream, _actualStream );
	}

	private static String[] ODF_CONTENTS = { "content.xml", "meta.xml", "settings.xml", "styles.xml",
			"META-INF/manifest.xml", "mimetype" };

	private static Map<String, String> readArchive( InputStream _inputStream ) throws IOException
	{
		final Map<String, String> result = New.map();
		final ZipInputStream zipInputStream = new ZipInputStream( _inputStream );
		final BufferedReader reader = new BufferedReader( new InputStreamReader( zipInputStream ) );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			final StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append( line ).append( '\n' );
			}
			result.put( zipEntry.getName(), stringBuilder.toString() );
		}
		return result;
	}

}
