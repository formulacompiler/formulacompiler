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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.formulacompiler.runtime.New;
import org.formulacompiler.tests.utils.SpreadsheetComparator;
import org.custommonkey.xmlunit.XMLAssert;

import junit.framework.Assert;

public class OpenDocumentSpreadsheetComparator implements SpreadsheetComparator
{
	private static final String[] ODF_CONTENTS = { "content.xml", "meta.xml", "settings.xml", "styles.xml",
			"META-INF/manifest.xml", "mimetype" };

	public void assertEqualSpreadsheets( String _message, InputStream _expected, InputStream _actual ) throws Exception
	{
		final Map<String, String> expectedOdsContents = readArchive( _expected );
		final Map<String, String> actualOdsContents = readArchive( _actual );
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

	private Map<String, String> readArchive( InputStream _inputStream ) throws IOException
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

	public static final class Factory implements SpreadsheetComparator.Factory
	{
		public SpreadsheetComparator getInstance()
		{
			return new OpenDocumentSpreadsheetComparator();
		}

		public boolean canHandle( String _fileExtension )
		{
			return _fileExtension.equalsIgnoreCase( ".ods" );
		}
	}

}