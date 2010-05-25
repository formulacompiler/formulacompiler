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

package org.formulacompiler.spreadsheet.internal.odf.saver.writer;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Namespace;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.Style;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.DocumentWriter;

public class DocumentContentWriter extends DocumentWriter
{
	private final BaseSpreadsheet spreadsheet;
	private final TimeZone timeZone;
	private final Set<Style> styles = New.sortedSet();

	public DocumentContentWriter( final BaseSpreadsheet _spreadsheet, final TimeZone _timeZone )
	{
		this.spreadsheet = _spreadsheet;
		this.timeZone = _timeZone;
	}

	public Set<Style> getStyles()
	{
		return this.styles;
	}

	@Override
	protected StartElement getRootElement( final XMLEventFactory _xmlEventFactory )
	{
		final Namespace officeNamespace = _xmlEventFactory.createNamespace( XMLConstants.Office.PREFIX, XMLConstants.Office.XMLNS );
		final Namespace tableNamespace = _xmlEventFactory.createNamespace( XMLConstants.Table.PREFIX, XMLConstants.Table.XMLNS );
		final Namespace textNamespace = _xmlEventFactory.createNamespace( XMLConstants.Text.PREFIX, XMLConstants.Text.XMLNS );
		final List<Namespace> namespaces = Arrays.asList( officeNamespace, tableNamespace, textNamespace );

		final StartElement startElement = _xmlEventFactory.createStartElement( XMLConstants.Office.DOCUMENT_CONTENT, null, namespaces.iterator() );
		return startElement;
	}

	@Override
	protected void writeBody( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter ) throws XMLStreamException, SpreadsheetException
	{
		_xmlEventWriter.add( _xmlEventFactory.createStartElement( XMLConstants.Office.BODY, null, null ) );

		final SpreadsheetWriter spreadsheetWriter = new SpreadsheetWriter( _xmlEventFactory, _xmlEventWriter, this.timeZone, this.styles );
		spreadsheetWriter.write( this.spreadsheet );

		_xmlEventWriter.add( _xmlEventFactory.createEndElement( XMLConstants.Office.BODY, null ) );
	}
}
