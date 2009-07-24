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
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.Style;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

class RowWriter extends ElementWriter
{

	private final TimeZone timeZone;
	private final Set<Style> styles;

	public RowWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter,
			final TimeZone _timeZone, final Set<Style> _styles )
	{
		super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Table.TABLE_ROW );
		this.timeZone = _timeZone;
		this.styles = _styles;
	}

	public void write( final RowImpl _row ) throws XMLStreamException, SpreadsheetException
	{
		final Map<QName, String> attributes = New.map();
		if (_row != null) {
			final String styleName = _row.getStyleName();
			if (styleName != null && !"".equals( styleName )) {
				attributes.put( XMLConstants.Table.STYLE_NAME, styleName );
			}
		}

		startElement( attributes );

		final CellWriter cellWriter = new CellWriter( getXmlEventFactory(), getXmlEventWriter(), this.timeZone, this.styles );
		boolean cellWritten = false;
		if (_row != null) {
			final List<CellInstance> cells = _row.getCellList();
			for (CellInstance cell : cells) {
				cellWriter.write( cell );
				cellWritten = true;
			}
		}
		if (!cellWritten) {
			cellWriter.write( null );
		}

		endElement();
	}
}
