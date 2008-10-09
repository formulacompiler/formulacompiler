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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.saver;

import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * @author Igor Didyuk
 */
abstract class XmlWriter
{
	private final String path;

	private final XMLEventWriter eventWriter;
	private final XMLEventFactory eventFactory;
	private final String[] namespaces;

	private boolean rootElementWritten = false;

	XmlWriter( ZipOutputStream _outputStream, String _path, String... _namespaces ) throws XMLStreamException, IOException
	{
		ZipEntry zipEntry = new ZipEntry( _path );
		_outputStream.putNextEntry( zipEntry );

		this.path = _path;

		this.eventWriter = XMLOutputFactory.newInstance().createXMLEventWriter( _outputStream, "UTF-8" );
		this.eventFactory = XMLEventFactory.newInstance();
		this.namespaces = _namespaces;

		for (int i = 0; i != this.namespaces.length; i++) {
			final String prefix;
			if (i == 0)
				prefix = javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
			else
				prefix = "ns" + i;
			this.eventWriter.setPrefix( prefix, this.namespaces[ i ] );
		}
		this.eventWriter.setPrefix( XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI );

		this.eventWriter.add( this.eventFactory.createStartDocument( "UTF-8", "1.0", true ) );
		this.eventWriter.add( this.eventFactory.createIgnorableSpace( "\n" ) );
	}

	final void close() throws XMLStreamException
	{
		this.eventWriter.add( this.eventFactory.createEndDocument() );
		this.eventWriter.close();
	}

	protected final String getPath()
	{
		return this.path;
	}

	protected final void writeEvent( final XMLEvent _event ) throws XMLStreamException
	{
		this.eventWriter.add( _event );
	}

	protected final void writeStartElement( final QName _element ) throws XMLStreamException
	{
		final String localPart = _element.getLocalPart();
		final String namespaceUri = _element.getNamespaceURI();
		final String prefix = this.eventWriter.getPrefix( namespaceUri );
		final StartElement se = eventFactory.createStartElement( prefix, namespaceUri, localPart );
		this.eventWriter.add( se );
		if (!this.rootElementWritten) {
			for (int i = 0; i != this.namespaces.length; i++) {
				final String nsUri = XmlWriter.this.namespaces[ i ];
				final Namespace namespace = eventFactory.createNamespace( this.eventWriter.getPrefix( nsUri ), nsUri );
				this.eventWriter.add( namespace );
			}
			this.rootElementWritten = true;
		}
	}

	protected final void writeEndElement( final QName _element ) throws XMLStreamException
	{
		this.eventWriter.add( eventFactory.createEndElement( _element, null ) );
	}

	protected final void writeAttribute( final QName _element, final String _value ) throws XMLStreamException
	{
		final String localPart = _element.getLocalPart();
		final String namespaceUri = _element.getNamespaceURI();
		final String prefix = this.eventWriter.getPrefix( namespaceUri );
		final Attribute a = eventFactory.createAttribute( prefix, namespaceUri, localPart, _value );
		this.eventWriter.add( a );
	}

	protected final void writeText( final String _value ) throws XMLStreamException
	{
		final Characters c = eventFactory.createCharacters( _value );
		this.eventWriter.add( c );
	}
}
