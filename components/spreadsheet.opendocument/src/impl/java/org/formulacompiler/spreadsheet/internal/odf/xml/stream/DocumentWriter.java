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

package org.formulacompiler.spreadsheet.internal.odf.xml.stream;

import java.io.OutputStream;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.SpreadsheetException;

public abstract class DocumentWriter
{
	public void write( final OutputStream _outputStream )
			throws SpreadsheetException
	{
		try {
			final XMLEventWriter xmlEventWriter = Factory.createXMLEventWriter( _outputStream );
			final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();

			xmlEventWriter.add( xmlEventFactory.createStartDocument() );

			final StartElement startElement = getRootElement( xmlEventFactory );
			xmlEventWriter.add( startElement );

			writeBody( xmlEventFactory, xmlEventWriter );

			final EndElement endElement = xmlEventFactory.createEndElement( startElement.getName(), null );
			xmlEventWriter.add( endElement );

			xmlEventWriter.add( xmlEventFactory.createEndDocument() );

			xmlEventWriter.close();

		} catch (XMLStreamException e) {
			final Throwable nestedException = e.getNestedException();
			if (nestedException != null) {
				e.initCause( nestedException );
			}
			throw new SpreadsheetException.SaveError( e );
		}
	}

	protected abstract StartElement getRootElement( XMLEventFactory _xmlEventFactory );

	protected abstract void writeBody( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter ) throws XMLStreamException, SpreadsheetException;

	protected static void writeTextElement( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter, QName _elementName, String _text ) throws XMLStreamException
	{
		_xmlEventWriter.add( _xmlEventFactory.createStartElement( _elementName, null, null ) );
		_xmlEventWriter.add( _xmlEventFactory.createCharacters( _text ) );
		_xmlEventWriter.add( _xmlEventFactory.createEndElement( _elementName, null ) );
	}
}
