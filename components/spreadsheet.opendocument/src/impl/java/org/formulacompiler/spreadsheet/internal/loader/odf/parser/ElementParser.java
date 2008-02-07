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
package org.formulacompiler.spreadsheet.internal.loader.odf.parser;

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
