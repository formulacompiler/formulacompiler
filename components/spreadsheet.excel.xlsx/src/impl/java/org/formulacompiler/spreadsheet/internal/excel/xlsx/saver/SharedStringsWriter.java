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
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
final class SharedStringsWriter extends ContentXmlWriter
{

	private static final String CONTENT_TYPE = "sharedStrings+xml";
	private static final String RELATIONSHIP_TYPE = "sharedStrings";

	private static final QName XML_SPACE = new QName( javax.xml.XMLConstants.XML_NS_URI, "space" );

	static final class StringList
	{

		private final List<String> list = New.list();

		int add( String _s )
		{
			int i = this.list.indexOf( _s );
			if (i == -1) {
				i = this.list.size();
				this.list.add( _s );
			}
			return i;
		}

		String get( int _index )
		{
			return this.list.get( _index );
		}

		int size()
		{
			return this.list.size();
		}
	}

	SharedStringsWriter( ZipOutputStream _outputStream, String _path ) throws XMLStreamException, IOException
	{
		super( _outputStream, _path, XMLConstants.Main.XMLNS );
	}

	void write( StringList _sharedStrings ) throws XMLStreamException
	{
		writeStartElement( XMLConstants.Main.SST );

		final int size = _sharedStrings.size();
		writeAttribute( XMLConstants.Main.COUNT, String.valueOf( size ) );
		writeAttribute( XMLConstants.Main.UNIQUE_COUNT, String.valueOf( size ) );

		for (int i = 0; i != size; i++) {
			writeStartElement( XMLConstants.Main.STRING_ITEM );
			writeStartElement( XMLConstants.Main.TEXT );
			final String text = _sharedStrings.get( i );
			if (text != null && text.length() != 0 &&
					(Character.isWhitespace( text.charAt( 0 ) ) || Character.isWhitespace( text.charAt( text.length() - 1 ) )))
				writeAttribute( XML_SPACE, "preserve" );
			writeText( text );
			writeEndElement( XMLConstants.Main.TEXT );
			writeEndElement( XMLConstants.Main.STRING_ITEM );
		}

		writeEndElement( XMLConstants.Main.SST );
	}

	public Metadata getMetadata()
	{
		return new Metadata( getPath(), CONTENT_TYPE, XMLConstants.DocumentRelationships.XMLNS, RELATIONSHIP_TYPE );
	}
}
