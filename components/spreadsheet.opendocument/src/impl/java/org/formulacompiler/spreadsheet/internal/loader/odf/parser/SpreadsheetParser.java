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

package org.formulacompiler.spreadsheet.internal.loader.odf.parser;

import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

/**
 * @author Vladimir Korenev
 */
public class SpreadsheetParser extends ElementParser
{
	private final SpreadsheetImpl spreadsheet;

	public SpreadsheetParser( final SpreadsheetImpl _spreadsheet )
	{
		this.spreadsheet = _spreadsheet;
	}

	@Override
	protected void elementStarted( final StartElement _startElement )
	{
		addElementParser( XMLConstants.Table.TABLE, new TableParser( this.spreadsheet ) );
		addElementParser( XMLConstants.Table.NAMED_EXPRESSIONS, new ElementParser()
		{
			@SuppressWarnings( "unqualified-field-access" )
			@Override
			protected void elementStarted( final StartElement _startElement )
			{
				addElementParser( XMLConstants.Table.NAMED_RANGE, new NamedRangeParser( spreadsheet ) );
			}
		} );
	}
}
