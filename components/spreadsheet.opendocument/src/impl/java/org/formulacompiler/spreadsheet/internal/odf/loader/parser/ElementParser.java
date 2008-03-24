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

package org.formulacompiler.spreadsheet.internal.odf.loader.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

/**
 * @author Vladimir Korenev
 */
abstract class ElementParser
{
	private final Map<QName, ElementParser> elementParserMap = new HashMap<QName, ElementParser>();
	private final LinkedList<QName> startedElements = new LinkedList<QName>();

	public final void parseElement( XMLEventReader _eventReader, StartElement _startElement ) throws XMLStreamException
	{
		final QName elementName = _startElement.getName();

		elementStarted( _startElement );

		while (_eventReader.hasNext()) {
			XMLEvent event = _eventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				final QName name = startElement.getName();
				final ElementParser elementParser = this.elementParserMap.get( name );
				if (elementParser != null) {
					elementParser.parseElement( _eventReader, startElement );
				}
				else {
					this.startedElements.addLast( name );
				}
			}
			else if (event.isCharacters()) {
				processCharacters( event.asCharacters() );
			}
			else if (event.isEndElement()) {
				final EndElement endElement = event.asEndElement();
				final QName endedElementName = endElement.getName();
				if (!this.startedElements.isEmpty()) {
					final QName lastStarted = this.startedElements.removeLast();
					assert lastStarted.equals( endedElementName );
				}
				else if (endedElementName.equals( elementName )) {
					elementEnded( endElement );
					break;
				}
			}
		}
	}

	protected final void addElementParser( QName _name, ElementParser _elementParser )
	{
		this.elementParserMap.put( _name, _elementParser );
	}

	protected void elementStarted( final StartElement _startElement )
	{
		// Not needed.
	}

	protected void elementEnded( final EndElement _endElement )
	{
		// Not needed.
	}

	protected void processCharacters( final Characters _characters )
	{
		// Not needed.
	}

}
