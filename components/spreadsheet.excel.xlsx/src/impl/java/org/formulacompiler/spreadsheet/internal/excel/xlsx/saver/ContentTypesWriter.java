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
import java.util.List;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
final class ContentTypesWriter extends XmlWriter
{

	ContentTypesWriter( ZipOutputStream _outputStream, String _path ) throws XMLStreamException, IOException
	{
		super( _outputStream, _path, XMLConstants.ContentTypes.XMLNS );
	}

	void write( List<ContentXmlWriter.Part> _parts ) throws XMLStreamException
	{
		writeStartElement( XMLConstants.ContentTypes.TYPES );

		writeStartElement( XMLConstants.ContentTypes.DEFAULT );
		writeAttribute( XMLConstants.ContentTypes.EXTENSION, "rels" );
		writeAttribute( XMLConstants.ContentTypes.CONTENT_TYPE, "application/vnd.openxmlformats-package.relationships+xml" );
		writeEndElement( XMLConstants.ContentTypes.DEFAULT );

		writeStartElement( XMLConstants.ContentTypes.DEFAULT );
		writeAttribute( XMLConstants.ContentTypes.EXTENSION, "xml" );
		writeAttribute( XMLConstants.ContentTypes.CONTENT_TYPE, "application/xml" );
		writeEndElement( XMLConstants.ContentTypes.DEFAULT );

		for (ContentXmlWriter.Part part : _parts) {
			writeStartElement( XMLConstants.ContentTypes.OVERRIDE );
			writeAttribute( XMLConstants.ContentTypes.PART_NAME, part.getPartName() );
			writeAttribute( XMLConstants.ContentTypes.CONTENT_TYPE, part.getContentType() );
			writeEndElement( XMLConstants.ContentTypes.OVERRIDE );
		}

		writeEndElement( XMLConstants.ContentTypes.TYPES );
	}
}
