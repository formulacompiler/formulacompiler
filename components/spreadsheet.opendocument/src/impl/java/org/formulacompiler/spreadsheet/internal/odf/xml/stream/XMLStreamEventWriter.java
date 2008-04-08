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

import java.util.Iterator;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.Comment;
import javax.xml.stream.events.DTD;
import javax.xml.stream.events.EntityReference;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.ProcessingInstruction;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Vladimir Korenev
 */
class XMLStreamEventWriter implements XMLEventWriter
{
	private final XMLStreamWriter writer;

	private StartElement savedStart;

	public XMLStreamEventWriter( XMLStreamWriter _writer )
	{
		this.writer = _writer;
	}

	public String getPrefix( String _uri ) throws XMLStreamException
	{
		return this.writer.getPrefix( _uri );
	}

	public void setPrefix( String _prefix, String _uri ) throws XMLStreamException
	{
		this.writer.setPrefix( _prefix, _uri );
	}

	public void setDefaultNamespace( String _uri ) throws XMLStreamException
	{
		this.writer.setDefaultNamespace( _uri );
	}

	public void setNamespaceContext( NamespaceContext _context ) throws XMLStreamException
	{
		this.writer.setNamespaceContext( _context );
	}

	public NamespaceContext getNamespaceContext()
	{
		return this.writer.getNamespaceContext();
	}

	public void flush() throws XMLStreamException
	{
		this.writer.flush();
	}

	public void close() throws XMLStreamException
	{
		this.writer.close();
	}

	public void add( XMLEvent e ) throws XMLStreamException
	{
		final int eventType = e.getEventType();
		boolean processed = false;
		if (this.savedStart != null) {
			try {
				if (eventType == XMLEvent.END_ELEMENT) {
					writeStartElement( this.savedStart, true );
					processed = true;
				}
				else {
					writeStartElement( this.savedStart, false );
				}
			} finally {
				this.savedStart = null;
			}
		}
		switch (eventType) {
			case XMLEvent.START_ELEMENT:
				this.savedStart = e.asStartElement();
				break;
			case XMLEvent.END_ELEMENT:
				if (!processed) {
					this.writer.writeEndElement();
				}
				break;
			case XMLEvent.PROCESSING_INSTRUCTION:
				final ProcessingInstruction instruction = (ProcessingInstruction) e;
				this.writer.writeProcessingInstruction( instruction.getTarget(), instruction.getData() );
				break;
			case XMLEvent.CHARACTERS:
				writeCharacters( e.asCharacters() );
				break;
			case XMLEvent.COMMENT:
				this.writer.writeComment( ((Comment) e).getText() );
				break;
			case XMLEvent.START_DOCUMENT:
				writeStartDocument( (StartDocument) e );
				break;
			case XMLEvent.END_DOCUMENT:
				this.writer.writeEndDocument();
				break;
			case XMLEvent.ENTITY_REFERENCE:
				this.writer.writeEntityRef( ((EntityReference) e).getName() );
				break;
			case XMLEvent.ATTRIBUTE:
				writeAttribute( (Attribute) e );
				break;
			case XMLEvent.DTD:
				this.writer.writeDTD( ((DTD) e).getDocumentTypeDeclaration() );
				break;
			case XMLEvent.NAMESPACE:
				writeNamespace( (Namespace) e );
				break;
			default:
				throw new XMLStreamException( "Event of unsupported type: " + e );
		}
	}

	private void writeStartElement( StartElement _startElement, boolean _empty ) throws XMLStreamException
	{
		final String prefix = _startElement.getName().getPrefix();
		final String namespace = _startElement.getName().getNamespaceURI();
		final String localName = _startElement.getName().getLocalPart();
		if (_empty) {
			this.writer.writeEmptyElement( prefix, localName, namespace );
		}
		else {
			this.writer.writeStartElement( prefix, localName, namespace );
		}
		final Iterator namespaces = _startElement.getNamespaces();
		while (namespaces.hasNext()) {
			writeNamespace( (Namespace) namespaces.next() );
		}

		final Iterator attributes = _startElement.getAttributes();
		while (attributes.hasNext()) {
			writeAttribute( (Attribute) attributes.next() );
		}

	}

	private void writeCharacters( Characters _characters ) throws XMLStreamException
	{
		if (_characters.isCData()) {
			this.writer.writeCData( _characters.getData() );
		}
		else {
			this.writer.writeCharacters( _characters.getData() );
		}
	}

	private void writeStartDocument( StartDocument _startDocument ) throws XMLStreamException
	{
		final String encoding = _startDocument.getCharacterEncodingScheme();
		final String version = _startDocument.getVersion();
		this.writer.writeStartDocument( encoding, version );
	}

	private void writeAttribute( Attribute _attribute ) throws XMLStreamException
	{
		final QName qName = _attribute.getName();
		this.writer.writeAttribute( qName.getPrefix(), qName.getNamespaceURI(), qName.getLocalPart(), _attribute.getValue() );
	}


	private void writeNamespace( Namespace _namespace ) throws XMLStreamException
	{
		if (_namespace.isDefaultNamespaceDeclaration()) {
			this.writer.writeDefaultNamespace( _namespace.getNamespaceURI() );
		}
		else {
			this.writer.writeNamespace( _namespace.getPrefix(), _namespace.getNamespaceURI() );
		}
	}

	public void add( XMLEventReader _reader ) throws XMLStreamException
	{
		while (_reader.hasNext()) {
			add( _reader.nextEvent() );
		}
	}

}
