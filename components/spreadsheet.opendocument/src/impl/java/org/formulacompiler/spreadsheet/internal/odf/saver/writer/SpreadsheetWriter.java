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
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.Style;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

class SpreadsheetWriter extends ElementWriter
{
	private final TimeZone timeZone;
	private final Set<Style> styles;


	public SpreadsheetWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter,
			final TimeZone _timeZone, final Set<Style> _styles )
	{
		super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Office.SPREADSHEET );
		this.timeZone = _timeZone;
		this.styles = _styles;
	}

	public void write( final SpreadsheetImpl _spreadsheet ) throws XMLStreamException, SpreadsheetException
	{
		startElement( null );

		final List<SheetImpl> sheets = _spreadsheet.getSheetList();
		if (sheets.size() > 0) {
			final SheetWriter sheetWriter = new SheetWriter( getXmlEventFactory(), getXmlEventWriter(), this.timeZone, this.styles );
			for (SheetImpl sheet : sheets) {
				sheetWriter.write( sheet );
			}
		}

		final Map<String, CellRange> modelRangeNames = _spreadsheet.getModelRangeNames();
		if (modelRangeNames != null && !modelRangeNames.isEmpty()) {
			final ElementWriter elementWriter = new ElementWriter( getXmlEventFactory(), getXmlEventWriter(),
					XMLConstants.Table.NAMED_EXPRESSIONS );
			elementWriter.startElement( null );

			{
				final NamedRangeWriter namedRangeWriter = new NamedRangeWriter( getXmlEventFactory(), getXmlEventWriter() );
				for (Map.Entry<String, CellRange> nameDefinition : modelRangeNames.entrySet()) {
					namedRangeWriter.write( nameDefinition.getKey(), nameDefinition.getValue() );
				}
			}

			elementWriter.endElement();
		}

		endElement();
	}
}