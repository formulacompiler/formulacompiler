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
package org.formulacompiler.spreadsheet.internal.odf.saver.writer;

import java.util.Collections;
import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

class TextWriter extends ElementWriter
{
	public TextWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter )
	{
		super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Text.P );
	}

	public void write( String _text ) throws XMLStreamException
	{
		final int length = _text.length();
		final CharWriter charWriter = new CharWriter();
		final SpaceWriter spaceWriter = new SpaceWriter();
		final ElementWriter tabWriter = new ElementWriter(
				getXmlEventFactory(), getXmlEventWriter(), XMLConstants.Text.TAB );
		startElement( null );
		for (int i = 0; i < length; i++) {
			final char c = _text.charAt( i );
			switch (c) {
				case '\n':
				case '\r':
					spaceWriter.flush();
					charWriter.flush();
					endElement();
					startElement( null );
					break;
				case '\t':
					spaceWriter.flush();
					charWriter.flush();
					tabWriter.startElement( null );
					tabWriter.endElement();
					break;
				case ' ':
					charWriter.flush();
					spaceWriter.add();
					break;
				default:
					spaceWriter.flush();
					charWriter.add( c );
			}
		}
		spaceWriter.flush();
		charWriter.flush();
		endElement();
	}

	private class CharWriter
	{
		private final StringBuilder sb = new StringBuilder();

		public void flush() throws XMLStreamException
		{
			if (this.sb.length() > 0) {
				getXmlEventWriter().add( getXmlEventFactory().createCharacters( this.sb.toString() ) );
				this.sb.setLength( 0 );
			}
		}

		public void add( char c )
		{
			this.sb.append( c );
		}
	}

	private class SpaceWriter
	{
		private int count = 0;
		private ElementWriter writer = new ElementWriter(
				getXmlEventFactory(), getXmlEventWriter(), XMLConstants.Text.S );

		public void flush() throws XMLStreamException
		{
			if (this.count > 0) {
				final Map<QName, String> attributes = this.count == 1 ? null :
						Collections.singletonMap( XMLConstants.Text.C, Integer.toString( this.count ) );
				this.writer.startElement( attributes );
				this.writer.endElement();
				this.count = 0;
			}
		}

		public void add()
		{
			this.count++;
		}
	}
}
