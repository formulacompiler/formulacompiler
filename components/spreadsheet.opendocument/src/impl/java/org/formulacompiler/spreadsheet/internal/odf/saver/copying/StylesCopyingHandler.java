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

package org.formulacompiler.spreadsheet.internal.odf.saver.copying;

import java.util.Map;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.Style;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.CopyingXMLEventHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

/**
 * Copies styles and adds new if necessary.
 *
 * @author Vladimir Korenev
 */
class StylesCopyingHandler extends CopyingXMLEventHandler implements ElementListener
{
	private final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	private final Set<Style> styles;

	/**
	 * @param _xmlEventWriter output.
	 * @param _styles         styles that should be written to output.
	 */
	public StylesCopyingHandler( final XMLEventWriter _xmlEventWriter, final Set<Style> _styles )
	{
		super( _xmlEventWriter );
		this.styles = _styles;
	}


	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers ) throws XMLStreamException
	{
		_handlers.put( XMLConstants.Style.STYLE, new StyleParser( getXMLEventWriter() ) );
	}

	public void elementEnded( final EndElement _endElement ) throws XMLStreamException
	{
		final StyleWriter styleWriter = new StyleWriter( this.xmlEventFactory, getXMLEventWriter() );
		for (Style style : this.styles) {
			styleWriter.write( style );
		}
	}

	private class StyleParser extends CopyingXMLEventHandler implements ElementListener
	{
		public StyleParser( final XMLEventWriter _xmlEventWriter )
		{
			super( _xmlEventWriter );
		}

		public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers ) throws XMLStreamException
		{
			final Attribute nameAttribute = _startElement.getAttributeByName( XMLConstants.Style.NAME );
			final Attribute familyAttribute = _startElement.getAttributeByName( XMLConstants.Style.FAMILY );
			if (nameAttribute != null && familyAttribute != null) {
				StylesCopyingHandler.this.styles.remove( new Style( nameAttribute.getValue(), familyAttribute.getValue() ) );
			}
		}

		public void elementEnded( final EndElement _endElement ) throws XMLStreamException
		{
			//Nothing to do.
		}
	}

	private class StyleWriter extends ElementWriter
	{
		public StyleWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter )
		{
			super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Style.STYLE );
		}

		public void write( final Style _style ) throws XMLStreamException
		{
			final Map<QName, String> attributes = New.map();
			attributes.put( XMLConstants.Style.NAME, _style.getName() );
			attributes.put( XMLConstants.Style.FAMILY, _style.getFamily() );
			startElement( attributes );
			endElement();
		}
	}

}
