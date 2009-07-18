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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.saver;

import static org.formulacompiler.runtime.internal.spreadsheet.CellAddressImpl.BROKEN_REF;

import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;


/**
 * @author Igor Didyuk
 */
class WorkbookWriter extends ContentXmlWriter
{

	private static final String CONTENT_TYPE = "sheet.main+xml";
	private static final String RELATIONSHIP_TYPE = "officeDocument";

	WorkbookWriter( ZipOutputStream _outputStream, String _path ) throws XMLStreamException, IOException
	{
		super( _outputStream, _path, XMLConstants.Main.XMLNS, XMLConstants.DocumentRelationships.XMLNS );
	}

	private void writeWorksheet( Spreadsheet.Sheet _sheet, int _index ) throws XMLStreamException
	{
		writeStartElement( XMLConstants.Main.SHEET );
		writeAttribute( XMLConstants.Main.NAME, _sheet == null ? " " : _sheet.getName() );
		writeAttribute( XMLConstants.Main.SHEET_ID, Integer.toString( _index ) );
		writeAttribute( XMLConstants.DocumentRelationships.ID, "rId" + Integer.toString( _index ) );
		writeEndElement( XMLConstants.Main.SHEET );
	}

	void write( Spreadsheet _spreadsheet ) throws XMLStreamException, SpreadsheetException
	{
		writeStartElement( XMLConstants.Main.WORKBOOK );

		writeStartElement( XMLConstants.Main.SHEETS );
		final Spreadsheet.Sheet[] sheets = _spreadsheet.getSheets();
		if (sheets.length == 0)
			writeWorksheet( null, 1 );
		else
			for (int si = 0; si != sheets.length; si++) {
				final Spreadsheet.Sheet sheet = sheets[ si ];
				writeWorksheet( sheet, si + 1 );
			}
		writeEndElement( XMLConstants.Main.SHEETS );

		writeStartElement( XMLConstants.Main.DEFINED_NAMES );
		for (Map.Entry<String, Spreadsheet.Range> range : _spreadsheet.getRangeNames().entrySet()) {
			writeStartElement( XMLConstants.Main.DEFINED_NAME );
			writeAttribute( XMLConstants.Main.NAME, range.getKey() );
			writeText( ExpressionFormatter.format( range.getValue(), BROKEN_REF ) );
			writeEndElement( XMLConstants.Main.DEFINED_NAME );
		}
		writeEndElement( XMLConstants.Main.DEFINED_NAMES );

		writeEndElement( XMLConstants.Main.WORKBOOK );
	}

	public Metadata getMetadata()
	{
		return new Metadata( getPath(), CONTENT_TYPE, XMLConstants.DocumentRelationships.XMLNS, RELATIONSHIP_TYPE );
	}
}
