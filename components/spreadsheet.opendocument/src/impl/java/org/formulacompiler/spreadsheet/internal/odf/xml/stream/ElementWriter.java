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

package org.formulacompiler.spreadsheet.internal.odf.xml.stream;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;

public class ElementWriter
{
	private final XMLEventFactory xmlEventFactory;
	private final XMLEventWriter xmlEventWriter;
	private final QName name;

	public ElementWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter, final QName _name )
	{
		this.xmlEventFactory = _xmlEventFactory;
		this.xmlEventWriter = _xmlEventWriter;
		this.name = _name;
	}

	protected XMLEventFactory getXmlEventFactory()
	{
		return this.xmlEventFactory;
	}

	protected XMLEventWriter getXmlEventWriter()
	{
		return this.xmlEventWriter;
	}

	public void startElement( Map<QName, String> attributesMap ) throws XMLStreamException
	{
		final Iterator<Attribute> attributesIterator;
		if (attributesMap == null || attributesMap.isEmpty()) {
			attributesIterator = null;
		}
		else {
			final Collection<Attribute> attributes = New.collection( attributesMap.size() );
			for (Map.Entry<QName, String> attrEntry : attributesMap.entrySet()) {
				final Attribute attribute = this.xmlEventFactory.createAttribute( attrEntry.getKey(), attrEntry.getValue() );
				attributes.add( attribute );
			}
			attributesIterator = attributes.iterator();
		}
		final StartElement startElement = this.xmlEventFactory.createStartElement( this.name, attributesIterator, null );
		this.xmlEventWriter.add( startElement );
	}

	public void endElement() throws XMLStreamException
	{
		final EndElement endElement = this.xmlEventFactory.createEndElement( this.name, null );
		this.xmlEventWriter.add( endElement );
	}
}
