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

package org.formulacompiler.spreadsheet.internal.odf.saver.copying;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.OpenDocumentSpreadsheetSaver;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.CopyingXMLEventHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;

/**
 * Copies styles and adds new if necessary.
 *
 * @author Vladimir Korenev
 */
class FileEntryCopyingHandler extends CopyingXMLEventHandler implements ElementListener
{
	/**
	 * @param _xmlEventWriter output.
	 */
	public FileEntryCopyingHandler( final XMLEventWriter _xmlEventWriter )
	{
		super( _xmlEventWriter );
	}


	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers ) throws XMLStreamException
	{
		//Nothing to do.
	}

	public void elementEnded( final EndElement _endElement ) throws XMLStreamException
	{
		//Nothing to do.
	}

	@Override
	public void process( final XMLEvent _event ) throws XMLStreamException
	{
		if (_event.isStartElement()) {
			final StartElement startElement = _event.asStartElement();
			if (startElement.getName().equals( XMLConstants.Manifest.FILE_ENTRY )) {
				final Attribute fullPathAttribute = startElement.getAttributeByName( XMLConstants.Manifest.FULL_PATH );
				if (fullPathAttribute != null && "/".equals( fullPathAttribute.getValue() )) {
					final XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
					final Attribute mediaTypeAttribute = xmlEventFactory.createAttribute( XMLConstants.Manifest.MEDIA_TYPE, OpenDocumentSpreadsheetSaver.MIME_TYPE );
					final List<Attribute> attributes = Arrays.asList( mediaTypeAttribute, fullPathAttribute );
					final StartElement newStartElement = xmlEventFactory.createStartElement( XMLConstants.Manifest.FILE_ENTRY, attributes.iterator(), null );
					super.process( newStartElement );
					return;
				}
			}
		}
		super.process( _event );    //To change body of overridden methods use File | Settings | File Templates.
	}

}