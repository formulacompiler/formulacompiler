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

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.internal.ProductInfo;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.DataTypeUtil;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.DocumentWriter;

public class DocumentMetaWriter extends DocumentWriter
{
	private final TimeZone timeZone;

	public DocumentMetaWriter( final TimeZone _timeZone )
	{
		this.timeZone = _timeZone;
	}

	@Override
	protected StartElement getRootElement( final XMLEventFactory _xmlEventFactory )
	{
		final Namespace officeNamespace = _xmlEventFactory.createNamespace( XMLConstants.Office.PREFIX, XMLConstants.Office.XMLNS );
		final Namespace metaNamespace = _xmlEventFactory.createNamespace( XMLConstants.Meta.PREFIX, XMLConstants.Meta.XMLNS );
		final List<Namespace> namespaces = Arrays.asList( officeNamespace, metaNamespace );

		final StartElement startElement = _xmlEventFactory.createStartElement( XMLConstants.Office.DOCUMENT_META, null, namespaces.iterator() );
		return startElement;
	}

	@Override
	protected void writeBody( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter ) throws XMLStreamException, SpreadsheetException
	{
		_xmlEventWriter.add( _xmlEventFactory.createStartElement( XMLConstants.Office.META, null, null ) );
		writeTextElement( _xmlEventFactory, _xmlEventWriter, XMLConstants.Meta.GENERATOR, getGenerator() );
		final long time = System.currentTimeMillis() / 1000 * 1000;
		final String date = DataTypeUtil.dateToXmlFormat( new Date( time ), this.timeZone );
		writeTextElement( _xmlEventFactory, _xmlEventWriter, XMLConstants.Meta.CREATION_DATE, date );
		_xmlEventWriter.add( _xmlEventFactory.createEndElement( XMLConstants.Office.META, null ) );
	}

	private static String getGenerator()
	{
		StringBuilder builder = new StringBuilder();
		builder.append( ProductInfo.NAME.replaceAll( "\\s", "" ) );
		builder.append( '/' );
		builder.append( ProductInfo.VERSION );
		return builder.toString();
	}
}
