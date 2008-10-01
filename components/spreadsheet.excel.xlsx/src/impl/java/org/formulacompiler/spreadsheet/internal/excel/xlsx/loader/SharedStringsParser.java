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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.io.InputStream;
import java.util.List;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
final class SharedStringsParser extends XmlParser
{
	private final List<String> items = New.arrayList();

	public SharedStringsParser( final InputStream _input ) throws XMLStreamException
	{
		super( _input );

		while (find( XMLConstants.STRING_ITEM_PATH ) != null)
			this.items.add( parseItem() );
	}

	private String getTextNotNull() throws XMLStreamException
	{
		final String s = getText();
		return s != null ? s : "";
	}

	private String parseItem() throws XMLStreamException
	{
		final int siContext = getContext();
		final StartElement se = findAny( siContext );
		final QName name = se.getName();
		if (name.equals( XMLConstants.Main.TEXT ))
			return getTextNotNull();
		if (name.equals( XMLConstants.Main.RICH_TEXT )) {
			final StringBuilder sb = new StringBuilder();
			do {
				find( XMLConstants.Main.TEXT, getContext() );
				sb.append( getTextNotNull() );
			}
			while (find( XMLConstants.Main.RICH_TEXT, siContext ) != null);
			return sb.toString();
		}
		return "";
	}

	public String getString( final int _index )
	{
		return this.items.get( _index );
	}
}
