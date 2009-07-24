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

import java.util.Map;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.odf.XMLConstants;
import org.formulacompiler.spreadsheet.internal.odf.saver.util.RefFormatter;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.ElementWriter;

class NamedRangeWriter extends ElementWriter
{
	public NamedRangeWriter( final XMLEventFactory _xmlEventFactory, final XMLEventWriter _xmlEventWriter )
	{
		super( _xmlEventFactory, _xmlEventWriter, XMLConstants.Table.NAMED_RANGE );
	}

	public void write( String _name, CellRange _range ) throws XMLStreamException
	{
		final Map<QName, String> attributes = New.map();
		attributes.put( XMLConstants.Table.NAME, _name );
		attributes.put( XMLConstants.Table.BASE_CELL_ADDRESS, RefFormatter.format( _range.getFrom().getAbsoluteIndex( true, true ), null ) );
		attributes.put( XMLConstants.Table.CELL_RANGE_ADDRESS, RefFormatter.format( _range, null ) );
		startElement( attributes );
		endElement();
	}
}
