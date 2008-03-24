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

import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;

/**
 * @author Vladimir Korenev
 */
class TableParser extends ElementParser
{
	private final SpreadsheetImpl spreadsheet;

	public TableParser( SpreadsheetImpl _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
	}

	@Override
	protected void elementStarted( final StartElement _startElement )
	{
		final SheetImpl sheet;
		{
			final Attribute attribute = _startElement.getAttributeByName( XMLConstants.Table.NAME );
			if (attribute != null) {
				final String tableName = attribute.getValue();
				sheet = new SheetImpl( this.spreadsheet, tableName );
			}
			else {
				sheet = new SheetImpl( this.spreadsheet );
			}
		}
		addElementParser( XMLConstants.Table.TABLE_ROW, new RowParser( sheet ) );
	}

}
