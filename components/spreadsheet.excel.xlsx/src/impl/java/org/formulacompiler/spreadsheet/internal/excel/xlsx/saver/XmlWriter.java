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
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;


/**
 * @author Igor Didyuk
 */
abstract class XmlWriter
{
	private final String path;

	private final XMLStreamWriter streamWriter;
	private final String[] namespaces;

	private boolean rootElementWritten = false;

	XmlWriter( ZipOutputStream _outputStream, String _path, String... _namespaces ) throws XMLStreamException, IOException
	{
		ZipEntry zipEntry = new ZipEntry( _path );
		_outputStream.putNextEntry( zipEntry );

		this.path = _path;

		this.streamWriter = XMLOutputFactory.newInstance().createXMLStreamWriter( _outputStream, "UTF-8" );
		this.namespaces = _namespaces;

		for (int i = 0; i != this.namespaces.length; i++) {
			if (i == 0) {
				this.streamWriter.setDefaultNamespace( this.namespaces[ i ] );
			}
			else {
				final String prefix = "ns" + i;
				this.streamWriter.setPrefix( prefix, this.namespaces[ i ] );
			}
		}
		this.streamWriter.setPrefix( XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI );

		this.streamWriter.writeStartDocument( "UTF-8", "1.0" );
		this.streamWriter.writeCharacters( "\n" );
	}

	final void close() throws XMLStreamException
	{
		this.streamWriter.writeEndDocument();
		this.streamWriter.close();
	}

	protected final String getPath()
	{
		return this.path;
	}

	protected final void writeEvent( final XMLEvent _event ) throws XMLStreamException
	{
		final int eventType = _event.getEventType();
		switch (eventType) {
			case XMLEvent.START_ELEMENT:
				writeStartElement( _event.asStartElement() );
				break;
			case XMLEvent.END_ELEMENT:
				this.streamWriter.writeEndElement();
				break;
			case XMLEvent.PROCESSING_INSTRUCTION:
				final ProcessingInstruction instruction = (ProcessingInstruction) _event;
				this.streamWriter.writeProcessingInstruction( instruction.getTarget(), instruction.getData() );
				break;
			case XMLEvent.CHARACTERS:
				Characters characters = _event.asCharacters();
				if (characters.isCData()) {
					this.streamWriter.writeCData( characters.getData() );
				}
				else {
					this.streamWriter.writeCharacters( characters.getData() );
				}
				break;
			case XMLEvent.COMMENT:
				this.streamWriter.writeComment( ((Comment) _event).getText() );
				break;
			case XMLEvent.START_DOCUMENT:
				final String encoding = ((StartDocument) _event).getCharacterEncodingScheme();
				final String version = ((StartDocument) _event).getVersion();
				this.streamWriter.writeStartDocument( encoding, version );
				break;
			case XMLEvent.END_DOCUMENT:
				this.streamWriter.writeEndDocument();
				break;
			case XMLEvent.ENTITY_REFERENCE:
				this.streamWriter.writeEntityRef( ((EntityReference) _event).getName() );
				break;
			case XMLEvent.ATTRIBUTE:
				final Attribute attribute = (Attribute) _event;
				writeAttribute( attribute );
				break;
			case XMLEvent.DTD:
				this.streamWriter.writeDTD( ((DTD) _event).getDocumentTypeDeclaration() );
				break;
			case XMLEvent.NAMESPACE:
				break;
		}
	}

	@SuppressWarnings( "unchecked" )
	private void writeStartElement( StartElement _startElement ) throws XMLStreamException
	{
		final QName name = _startElement.getName();
		writeStartElement( name );

		final Iterator<Attribute> attributes = _startElement.getAttributes();
		while (attributes.hasNext()) {
			final Attribute attribute = attributes.next();
			writeAttribute( attribute );
		}
	}

	private void writeAttribute( final Attribute _attribute ) throws XMLStreamException
	{
		writeAttribute( _attribute.getName(), _attribute.getValue() );
	}

	protected final void writeStartElement( final QName _name ) throws XMLStreamException
	{
		final String localPart = _name.getLocalPart();
		final String namespaceUri = _name.getNamespaceURI();
		this.streamWriter.writeStartElement( namespaceUri, localPart );
		if (!this.rootElementWritten) {
			for (int i = 0; i != this.namespaces.length; i++) {
				final String nsUri = XmlWriter.this.namespaces[ i ];
				final String prefix = this.streamWriter.getPrefix( nsUri );
				this.streamWriter.writeNamespace( prefix, nsUri );
			}
			this.rootElementWritten = true;
		}
	}

	protected final void writeEndElement( final QName _name ) throws XMLStreamException
	{
		this.streamWriter.writeEndElement();
	}

	protected final void writeAttribute( final QName _name, final String _value ) throws XMLStreamException
	{
		final String localPart = _name.getLocalPart();
		final String namespaceUri = _name.getNamespaceURI();
		if (XMLConstants.XML_NS_URI.equals( namespaceUri )) {
			// Workaround Sun's StAX bug.
			this.streamWriter.writeAttribute( XMLConstants.XML_NS_PREFIX, namespaceUri, localPart, _value );
		}
		else if (namespaceUri == null || "".equals( namespaceUri )) {
			this.streamWriter.writeAttribute( localPart, _value );
		}
		else {
			this.streamWriter.writeAttribute( namespaceUri, localPart, _value );
		}
	}

	protected final void writeText( final String _value ) throws XMLStreamException
	{
		this.streamWriter.writeCharacters( _value );
	}
}
