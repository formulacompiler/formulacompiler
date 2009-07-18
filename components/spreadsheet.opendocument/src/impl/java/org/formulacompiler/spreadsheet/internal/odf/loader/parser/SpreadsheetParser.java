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
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementHandler;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementListener;

/**
 * @author Vladimir Korenev
 */
public class SpreadsheetParser implements ElementListener
{
	private final SpreadsheetImpl spreadsheet;
	private final SpreadsheetLoader.Config config;

	public SpreadsheetParser( final SpreadsheetImpl _spreadsheet, final SpreadsheetLoader.Config _config )
	{
		this.spreadsheet = _spreadsheet;
		this.config = _config;
	}

	public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
	{
		_handlers.put( XMLConstants.Table.TABLE, new TableParser( this.spreadsheet, this.config ) );
		_handlers.put( XMLConstants.Table.NAMED_EXPRESSIONS, new ElementHandler()
		{
			@Override
			public void elementStarted( final StartElement _startElement, final Map<QName, ElementListener> _handlers )
			{
				_handlers.put( XMLConstants.Table.NAMED_RANGE, new NamedRangeParser( SpreadsheetParser.this.spreadsheet ) );
			}
		} );
	}

	public void elementEnded( final EndElement _endElement )
	{
		this.spreadsheet.trim();
	}
}
