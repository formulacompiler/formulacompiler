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

import java.io.InputStream;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.formulacompiler.runtime.New;

/**
 * @author Vladimir Korenev
 */
public class Parser
{
	private final Map<QName, ? extends ElementListener> listeners;
	private XMLEventListener eventListener;

	public Parser( Map<QName, ? extends ElementListener> _listeners )
	{
		assert _listeners != null;
		this.listeners = _listeners;
	}

	public void setEventListener( final XMLEventListener _eventListener )
	{
		this.eventListener = _eventListener;
	}

	public void parse( InputStream _inputStream ) throws XMLStreamException
	{
		final XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
		final XMLEventReader eventReader = xmlInputFactory.createXMLEventReader( _inputStream );
		while (eventReader.hasNext()) {
			final XMLEvent event = eventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				final ElementListener listener = this.listeners.get( startElement.getName() );
				if (listener != null) {
					parseElement( eventReader, listener, startElement );
				}
				else {
					fireXMLEvent( event );
				}
			}
			else {
				fireXMLEvent( event );
			}
		}
	}

	private void parseElement( final XMLEventReader _eventReader, ElementListener _elementHandler, StartElement _startElement ) throws XMLStreamException
	{
		final LinkedList<QName> startedElements = new LinkedList<QName>();
		final QName elementName = _startElement.getName();

		startedElements.addLast( elementName );
		fireXMLEvent( _elementHandler, _startElement );
		final Map<QName, ElementListener> handlers = New.map();
		_elementHandler.elementStarted( _startElement, handlers );

		while (_eventReader.hasNext()) {
			final XMLEvent event = _eventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				final QName name = startElement.getName();
				final ElementListener elementHandler = handlers.get( name );
				if (elementHandler != null) {
					parseElement( _eventReader, elementHandler, startElement );
				}
				else {
					fireXMLEvent( _elementHandler, event );
					startedElements.addLast( name );
				}
			}
			else {
				if (event.isEndElement()) {
					final EndElement endElement = event.asEndElement();
					final QName endedElementName = endElement.getName();
					final QName lastStarted = startedElements.removeLast();
					assert endedElementName.equals( lastStarted );
					if (startedElements.isEmpty()) {
						_elementHandler.elementEnded( endElement );
						fireXMLEvent( _elementHandler, event );
						break;
					}
				}
				fireXMLEvent( _elementHandler, event );
			}
		}
	}

	private void fireXMLEvent( final ElementListener _elementHandler, final XMLEvent _event ) throws XMLStreamException
	{
		if (_elementHandler instanceof XMLEventListener) {
			((XMLEventListener) _elementHandler).process( _event );
		}
	}

	private void fireXMLEvent( final XMLEvent _event ) throws XMLStreamException
	{
		if (this.eventListener != null) {
			this.eventListener.process( _event );
		}
	}


}
