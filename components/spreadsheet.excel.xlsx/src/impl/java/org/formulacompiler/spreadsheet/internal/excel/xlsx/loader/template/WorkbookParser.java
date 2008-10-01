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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.PackageLoader;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.RelationshipParser;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.XmlParser;

/**
 * @author Igor Didyuk
 */
final class WorkbookParser extends XmlParser
{
	private final PackageLoader loader;
	private final String entryPath;

	private StylesheetParser stylesheet = null;

	WorkbookParser( final PackageLoader _loader, final String _entryPath ) throws XMLStreamException
	{
		super( _loader.getEntry( _entryPath ) );
		this.loader = _loader;
		this.entryPath = _entryPath;
	}

	void parse() throws XMLStreamException, IOException
	{
		final RelationshipParser relationships = parseRelationships();

		final String stylesheetEntryPath = relationships.findByType( XMLConstants.STYLESHEET_RELATIONSHIP_TYPE );
		this.stylesheet = parseStylesheet( stylesheetEntryPath );

		StartElement se = find( XMLConstants.WORKBOOK_SHEETS_PATH );
		if (se != null) {
			final int sheetsContext = getContext();
			while ((se = find( XMLConstants.Main.SHEET, sheetsContext )) != null) {
				final Attribute sheetRelationshipId = se.getAttributeByName( XMLConstants.DocumentRelationships.ID );

				final WorksheetParser parser = new WorksheetParser( this.loader,
						relationships.findById( sheetRelationshipId.getValue() ), this.stylesheet );
				try {
					parser.parse();
				}
				finally {
					parser.close();
				}
			}
		}
	}

	private RelationshipParser parseRelationships() throws XMLStreamException, IOException
	{
		final InputStream input = this.loader.getRelationship( this.entryPath );
		try {
			return new RelationshipParser( input, this.entryPath );
		}
		finally {
			input.close();
		}
	}

	private StylesheetParser parseStylesheet( final String _entryPath ) throws XMLStreamException, IOException
	{
		if (_entryPath == null)
			return null;

		final StylesheetParser stylesheetParser = new StylesheetParser( loader.getEntry( _entryPath ) );
		stylesheetParser.close();
		return stylesheetParser;
	}

	Stylesheet getStylesheet()
	{
		return this.stylesheet;
	}
}
