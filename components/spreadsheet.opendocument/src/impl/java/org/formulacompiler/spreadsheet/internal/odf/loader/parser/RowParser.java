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

package org.formulacompiler.spreadsheet.internal.odf.loader.parser;

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;

/**
 * @author Vladimir Korenev
 */
class RowParser extends ElementHandler
{
	private final SheetImpl sheet;
	private final SpreadsheetLoader.Config config;

	public RowParser( SheetImpl _sheet, SpreadsheetLoader.Config _config )
	{
		this.sheet = _sheet;
		this.config = _config;
	}

	@Override
	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
	{
		final int numberRowsRepeated;
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Table.NUMBER_ROWS_REPEATED );
			if (attribute != null) {
				numberRowsRepeated = Integer.parseInt( attribute.getValue() );
			}
			else {
				numberRowsRepeated = 1;
			}
		}
		final RowImpl row = createRow( numberRowsRepeated );
		final CellParser cellParser = new CellParser( row, this.config );
		_handlers.put( XMLConstants.Table.TABLE_CELL, cellParser );
		_handlers.put( XMLConstants.Table.COVERED_TABLE_CELL, cellParser );
	}

	private RowImpl createRow( int _numberRowsRepeated )
	{
		final RowImpl row = new RowImpl( this.sheet );
		for (int i = 1; i < _numberRowsRepeated; i++) {
			new RowImpl( row );
		}
		return row;
	}
}
