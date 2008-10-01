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

package org.formulacompiler.spreadsheet.internal.excel.xlsx;

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

public class XlsxSpreadsheetComparator implements SpreadsheetComparator
{
	private static class Content
	{
		private String expected;
		private String actual;
	}

	public void assertEqualSpreadsheets( String _message, InputStream _expected, InputStream _actual ) throws Exception
	{
		final Map<String, Content> result = New.map();
		readArchive( result, _expected, false );
		readArchive( result, _actual, true );
		for (Map.Entry<String, Content> entry : result.entrySet()) {
			final String path = entry.getKey();
			final Content content = entry.getValue();
			if (content.expected == null) {
				Assert.fail( path + " is missing in expected file." );
			}
			final String msg = _message == null ? path : (_message + " in zipped file " + path);
			XMLAssert.assertXMLEqual( msg, content.expected, content.actual );
		}
	}

	private static void readArchive( Map<String, Content> _map, InputStream _inputStream, boolean _actual ) throws IOException
	{
		final ZipInputStream zipInputStream = new ZipInputStream( _inputStream );
		final BufferedReader reader = new BufferedReader( new InputStreamReader( zipInputStream ) );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			final StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				stringBuilder.append( line ).append( '\n' );
			}
			
			final String path = zipEntry.getName();
			final String xml = stringBuilder.toString();
			Content content = _map.get( path );
			if (content == null) {
				content = new Content();
				if (_actual)
					content.actual = xml;
				else
					content.expected = xml;
				_map.put( path, content );
			}
			else
				if (_actual)
					content.actual = xml;
				else
					content.expected = xml;
		}
	}
	
	public static final class Factory implements SpreadsheetComparator.Factory
	{
		public SpreadsheetComparator getInstance()
		{
			return new XlsxSpreadsheetComparator();
		}

		public boolean canHandle( String _fileExtension )
		{
			return _fileExtension.equalsIgnoreCase( ".xlsx" );
		}
	}

}