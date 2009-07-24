/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.io.InputStream;
import java.util.Stack;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.xml.XmlNode;


/**
 * @author Igor Didyuk
 */
public abstract class XmlParser
{
	private final XMLEventReader xmlEventReader;
	private final Stack<QName> cursor;

	protected XmlParser( final InputStream _input ) throws XMLStreamException
	{
		this.xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader( _input );
		this.cursor = new Stack<QName>();
	}

	protected final StartElement find( final QName[] _path, final int _context ) throws XMLStreamException
	{
		int matchPosition = _context;
		for (int i = _context; i != this.cursor.size(); i++) {
			if (i - _context >= _path.length || !this.cursor.get( i ).equals( _path[ i - _context ] ))
				break;
			matchPosition++;
		}

		if (matchPosition != this.cursor.size()) {
			while (this.xmlEventReader.hasNext()) {
				final XMLEvent event = this.xmlEventReader.nextEvent();
				if (event.isStartElement()) {
					final StartElement startElement = event.asStartElement();
					final QName name = startElement.getName();
					this.cursor.push( name );
				}
				else if (event.isEndElement()) {
					this.cursor.pop();
					if (this.cursor.size() == matchPosition)
						break;
				}
			}
		}

		while (this.xmlEventReader.hasNext()) {
			final XMLEvent event = this.xmlEventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				final QName name = startElement.getName();
				final int pathIndex = this.cursor.size() - _context;
				if (pathIndex < _path.length && name.equals( _path[ pathIndex ] )) {
					this.cursor.push( name );
					if (this.cursor.size() == _context + _path.length)
						return startElement;
				}
				else {
					skipElement();
				}
			}
			else if (event.isEndElement()) {
				this.cursor.pop();
				if (this.cursor.size() < _context)
					return null;
			}
		}
		return null;
	}

	private void skipElement() throws XMLStreamException
	{
		int level = 0;
		while (this.xmlEventReader.hasNext()) {
			final XMLEvent event = this.xmlEventReader.nextEvent();
			if (event.isStartElement()) level++;
			else if (event.isEndElement() && level-- == 0) return;
		}
	}

	protected final XmlNode readEvents( StartElement _se ) throws XMLStreamException
	{
		final XmlNode node = new XmlNode( _se );

		while (this.xmlEventReader.hasNext()) {
			final XMLEvent event = this.xmlEventReader.nextEvent();
			if (event.isEndElement()) {
				this.cursor.pop();
				return node;
			}
			if (event.isStartElement()) {
				StartElement se = event.asStartElement();
				this.cursor.push( se.getName() );
				node.addChild( new XmlNode( se ) );
			}
		}
		throw new XMLStreamException( "Element end not found." );
	}

	protected final StartElement findAny( final int _context ) throws XMLStreamException
	{
		while (this.xmlEventReader.hasNext()) {
			final XMLEvent event = this.xmlEventReader.nextEvent();
			if (event.isStartElement()) {
				final StartElement startElement = event.asStartElement();
				final QName name = startElement.getName();
				this.cursor.push( name );
				if (this.cursor.size() == _context + 1)
					return startElement;
			}
			else if (event.isEndElement()) {
				this.cursor.pop();
				if (this.cursor.size() < _context)
					return null;
			}
		}
		return null;
	}

	protected final StartElement find( final QName[] _path ) throws XMLStreamException
	{
		return find( _path, 0 );
	}

	protected final StartElement find( final QName _path, final int _context ) throws XMLStreamException
	{
		return find( new QName[]{ _path }, _context );
	}

	protected final int getContext()
	{
		return this.cursor.size();
	}

	protected final String getText() throws XMLStreamException
	{
		StringBuilder sb = null;
		XMLEvent event;
		while ((event = this.xmlEventReader.nextEvent()).isCharacters()) {
			if (sb == null)
				sb = new StringBuilder();
			sb.append( event.asCharacters().getData() );
		}
		if (event.isEndElement())
			this.cursor.pop();
		else
			throw new XMLStreamException( "Unexpected event type: " + event.getEventType() );
		return sb == null ? null : sb.toString();
	}

	public final void close() throws XMLStreamException
	{
		this.cursor.clear();
		this.xmlEventReader.close();
	}
}
