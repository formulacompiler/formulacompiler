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
