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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.Style;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

class SheetWriter extends ElementWriter
{
	private final TimeZone timeZone;
	private final Set<Style> styles;

	public SheetWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter,
			final TimeZone _timeZone, final Set<Style> _styles )
	{
		super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Table.TABLE );
		this.timeZone = _timeZone;
		this.styles = _styles;
	}

	public void write( final SheetImpl _sheet ) throws XMLStreamException, SpreadsheetException
	{
		final Map<QName, String> attributes = New.map();
		if (_sheet != null) {
			final String sheetName = _sheet.getName();
			if (sheetName != null) {
				attributes.put( XMLConstants.Table.NAME, sheetName );
			}

			final String styleName = _sheet.getStyleName();
			if (styleName != null && !"".equals( styleName )) {
				attributes.put( XMLConstants.Table.STYLE_NAME, styleName );
			}
		}

		startElement( attributes );

		final ElementWriter columnWriter = new ElementWriter( getXmlEventFactory(), getXmlEventWriter(), XMLConstants.Table.TABLE_COLUMN );
		columnWriter.startElement( null );
		columnWriter.endElement();

		final RowWriter rowWriter = new RowWriter( getXmlEventFactory(), getXmlEventWriter(), this.timeZone, this.styles );
		boolean rowWritten = false;
		if (_sheet != null) {
			final List<RowImpl> rows = _sheet.getRowList();
			for (RowImpl row : rows) {
				rowWriter.write( row );
				rowWritten = true;
			}
		}
		if (!rowWritten) {
			rowWriter.write( null );
		}
		endElement();
	}
}
