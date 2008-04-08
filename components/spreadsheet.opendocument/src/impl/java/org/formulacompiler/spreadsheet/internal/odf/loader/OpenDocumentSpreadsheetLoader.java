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

package org.formulacompiler.spreadsheet.internal.odf.loader;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.loader.SpreadsheetLoaderDispatcher;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.loader.parser.SpreadsheetParser;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.Parser;

/**
 * @author Vladimir Korenev
 */
public class OpenDocumentSpreadsheetLoader implements SpreadsheetLoader
{

	public static final class Factory implements SpreadsheetLoaderDispatcher.Factory
	{

		public SpreadsheetLoader newInstance( Config _config )
		{
			return new OpenDocumentSpreadsheetLoader( _config );
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".ods" );
		}

	}


	private final Config config;

	public OpenDocumentSpreadsheetLoader( final Config _config )
	{
		config = _config;
	}


	public Spreadsheet loadFrom( final String _originalFileName, final InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		ZipInputStream zipInputStream = new ZipInputStream( _stream );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			if ("content.xml".equals( zipEntry.getName() )) {
				return readContent( zipInputStream );
			}
		}
		throw new SpreadsheetException.LoadError( "<content.xml> is missing in <" + _originalFileName + ">" );
	}

	private Spreadsheet readContent( InputStream _inputStream ) throws SpreadsheetException
	{
		final SpreadsheetImpl workbook = new SpreadsheetImpl();

		try {
			final SpreadsheetParser spreadsheetParser = new SpreadsheetParser( workbook, this.config );
			final Parser parser = new Parser( Collections.singletonMap( XMLConstants.Office.SPREADSHEET, spreadsheetParser ) );
			parser.parse( _inputStream );
		}
		catch (XMLStreamException e) {
			final Throwable nestedException = e.getNestedException();
			if (nestedException != null) {
				e.initCause( nestedException );
			}
			throw new SpreadsheetException.LoadError( e );
		}

		return workbook;
	}

}
