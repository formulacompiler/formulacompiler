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

package org.formulacompiler.spreadsheet.internal.odf.loader.parser;

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.loader.builder.SheetBuilder;
import org.formulacompiler.spreadsheet.internal.loader.builder.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;

/**
 * @author Vladimir Korenev
 */
class TableParser extends ElementHandler
{
	private final SpreadsheetBuilder spreadsheetBuilder;
	private final SpreadsheetLoader.Config config;

	public TableParser( SpreadsheetBuilder _spreadsheetBuilder, SpreadsheetLoader.Config _config )
	{
		this.spreadsheetBuilder = _spreadsheetBuilder;
		this.config = _config;
	}

	@Override
	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
	{
		final SheetBuilder sheetBuilder;
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Table.NAME );
			if (attribute != null) {
				final String tableName = attribute.getValue();
				sheetBuilder = this.spreadsheetBuilder.beginSheet( tableName );
			}
			else {
				sheetBuilder = this.spreadsheetBuilder.beginSheet();
			}
		}
		_handlers.put( XMLConstants.Table.TABLE_ROW, new RowParser( sheetBuilder, this.config ) );
	}

	@Override
	public void elementEnded( final EndElement _endElement ) throws XMLStreamException
	{
		this.spreadsheetBuilder.endSheet();
	}
}
