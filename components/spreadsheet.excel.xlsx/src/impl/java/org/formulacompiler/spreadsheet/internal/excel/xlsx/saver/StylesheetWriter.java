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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template.Stylesheet;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.saver.ContentXmlWriter.Metadata;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.xml.XmlNode;


/**
 * @author Igor Didyuk
 */
final class StylesheetWriter extends XmlWriter
{

	private static final String CONTENT_TYPE = "styles+xml";
	private static final String RELATIONSHIP_TYPE = "styles";

	StylesheetWriter( ZipOutputStream _outputStream, String _path ) throws IOException, XMLStreamException
	{
		super( _outputStream, _path, XMLConstants.Main.XMLNS );
	}

	private static void skipCurrentEvent( XMLEventReader _xmlEventReader ) throws XMLStreamException
	{
		int level = 0;
		do {
			final XMLEvent event = _xmlEventReader.nextEvent();
			if (event.isStartElement())
				level++;
			else if (event.isEndElement())
				level--;
		}
		while (level >= 0);
	}

	private void write( final XmlNode _node ) throws XMLStreamException
	{
		writeStartElement( _node.getName() );
		for (XmlNode.Attribute attribute : _node.getAttributes())
			writeAttribute( attribute.getName(), attribute.getValue() );
		for (XmlNode child : _node.getChildren())
			write( child );
		writeEndElement( _node.getName() );
	}

	private void write( final List<XmlNode> _styles ) throws XMLStreamException
	{
		final int count = _styles.size();
		for (int i = 0; i != count; i++) {
			final XmlNode node = _styles.get( i );
			write( node );
		}
	}

	private void writeCellStyleXfs( final List<XmlNode> _styles ) throws XMLStreamException
	{
		writeStartElement( XMLConstants.Main.CELL_STYLE_FORMATS );
		writeAttribute( XMLConstants.Main.COUNT, Integer.toString( _styles.size() ) );
		write( _styles );
		writeEndElement( XMLConstants.Main.CELL_STYLE_FORMATS );
	}

	private void writeCellXfs( final List<XmlNode> _styles ) throws XMLStreamException
	{
		writeStartElement( XMLConstants.Main.CELL_FORMATS );
		writeAttribute( XMLConstants.Main.COUNT, Integer.toString( _styles.size() ) );
		write( _styles );
		writeEndElement( XMLConstants.Main.CELL_FORMATS );
	}

	private void writeCellStyles( final List<XmlNode> _styles ) throws XMLStreamException
	{
		if (_styles.isEmpty())
			return;

		writeStartElement( XMLConstants.Main.NAMED_STYLES );
		writeAttribute( XMLConstants.Main.COUNT, Integer.toString( _styles.size() ) );
		write( _styles );
		writeEndElement( XMLConstants.Main.NAMED_STYLES );
	}

	void write( Stylesheet _stylesheet ) throws XMLStreamException
	{
		final ByteArrayInputStream bais = new ByteArrayInputStream( _stylesheet.getStylesheetSource() );
		final XMLEventReader xmlEventReader = XMLInputFactory.newInstance().createXMLEventReader( bais );

		boolean headerProcessed = false;
		while (xmlEventReader.hasNext()) {
			final XMLEvent event = xmlEventReader.nextEvent();

			// Skip all content till the first start element
			if (!headerProcessed) {
				if (!event.isStartElement())
					continue;
				headerProcessed = true;
			}

			if (event.isStartElement() && event.asStartElement().getName().equals( XMLConstants.Main.CELL_STYLE_FORMATS )) {
				skipCurrentEvent( xmlEventReader );

				writeCellStyleXfs( _stylesheet.getCellStyleXfs() );
				writeCellXfs( _stylesheet.getCellXfs() );
				writeCellStyles( _stylesheet.getCellStyles() );
			}
			else
			if (event.isStartElement() && event.asStartElement().getName().equals( XMLConstants.Main.CELL_FORMATS )) {
				skipCurrentEvent( xmlEventReader );
			}
			else
			if (event.isStartElement() && event.asStartElement().getName().equals( XMLConstants.Main.NAMED_STYLES )) {
				skipCurrentEvent( xmlEventReader );
			}
			else
				writeEvent( event );
		}
		xmlEventReader.close();
	}

	public Metadata getMetadata()
	{
		return new Metadata( getPath(), CONTENT_TYPE, XMLConstants.DocumentRelationships.XMLNS, RELATIONSHIP_TYPE );
	}
}
