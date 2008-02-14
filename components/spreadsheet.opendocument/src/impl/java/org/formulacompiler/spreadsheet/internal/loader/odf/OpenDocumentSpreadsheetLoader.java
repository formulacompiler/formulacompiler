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

package org.formulacompiler.spreadsheet.internal.loader.odf;

import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.loader.SpreadsheetLoaderDispatcher;
import org.formulacompiler.spreadsheet.internal.loader.odf.parser.SpreadsheetParser;
import org.formulacompiler.spreadsheet.internal.loader.odf.parser.XMLConstants;

/**
 * @author Vladimir Korenev
 */
public class OpenDocumentSpreadsheetLoader implements SpreadsheetLoader
{

	public static final class Factory implements SpreadsheetLoaderDispatcher.Factory
	{

		public SpreadsheetLoader newInstance( Config _config )
		{
			return new OpenDocumentSpreadsheetLoader();
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".ods" );
		}

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

	private static Spreadsheet readContent( InputStream _inputStream ) throws SpreadsheetException
	{
		final SpreadsheetImpl workbook = new SpreadsheetImpl();

		try {
			final SpreadsheetParser parser = new SpreadsheetParser( workbook );

			final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
			final XMLEventReader xmlEventReader = xmlInputFactory.createXMLEventReader( _inputStream );
			while (xmlEventReader.hasNext()) {
				final XMLEvent event = xmlEventReader.nextEvent();
				if (event.isStartElement()) {
					final StartElement startElement = event.asStartElement();
					final QName qName = startElement.getName();
					if (qName.equals( XMLConstants.Office.SPREADSHEET )) {
						parser.parseElement( xmlEventReader, startElement );
					}
				}
			}
		}
		catch (XMLStreamException e) {
			throw new SpreadsheetException.LoadError( e );
		}

		workbook.trim();
		return workbook;
	}

}
