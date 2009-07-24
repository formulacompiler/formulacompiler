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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.StartElement;

import org.formulacompiler.compiler.internal.expressions.parser.ExpressionParser;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.parser.SpreadsheetExpressionParserA1OOXML;


/**
 * @author Igor Didyuk
 */
final class WorkbookParser extends XmlParser
{
	// See Office Open XML Part 4 - Markup Language Reference, paragraph 3.2.5
	private static final String DEFINED_NAME_RESERVED_PREFIX = "_xlnm.";

	private final PackageLoader loader;
	private final String entryPath;

	private final SpreadsheetLoader.Config config;

	WorkbookParser( final PackageLoader _loader, final String _entryPath,
			final SpreadsheetLoader.Config _config ) throws XMLStreamException
	{
		super( _loader.getEntry( _entryPath ) );
		this.loader = _loader;
		this.entryPath = _entryPath;
		this.config = _config;
	}

	void parse( final SpreadsheetImpl _spreadsheet ) throws XMLStreamException, IOException, SpreadsheetException
	{
		final RelationshipParser relationships = parseRelationships();

		final String sharedStringsEntryPath = relationships.findByType( XMLConstants.SHARED_STRINGS_RELATIONSHIP_TYPE );
		final SharedStringsParser sharedStrings = parseSharedStrings( sharedStringsEntryPath );

		final String stylesheetEntryPath = relationships.findByType( XMLConstants.STYLESHEET_RELATIONSHIP_TYPE );
		final StylesheetParser stylesheet = parseStylesheet( stylesheetEntryPath );

		StartElement se = find( XMLConstants.WORKBOOK_SHEETS_PATH );
		if (se != null) {
			final int sheetsContext = getContext();
			while ((se = find( XMLConstants.Main.SHEET, sheetsContext )) != null) {
				final Attribute sheetName = se.getAttributeByName( XMLConstants.Main.NAME );
				final Attribute sheetRelationshipId = se.getAttributeByName( XMLConstants.DocumentRelationships.ID );

				final SheetImpl sheet = new SheetImpl( _spreadsheet, sheetName.getValue() );
				final WorksheetParser parser = new WorksheetParser( this.loader,
						relationships.findById( sheetRelationshipId.getValue() ), stylesheet, sharedStrings,
						this.config );
				try {
					parser.parse( sheet );
				}
				finally {
					parser.close();
				}
			}
		}

		parseDefinedNames( _spreadsheet );
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

	private SharedStringsParser parseSharedStrings( final String _entryPath ) throws XMLStreamException, IOException
	{
		if (_entryPath == null)
			return null;

		final InputStream input = this.loader.getEntry( _entryPath );
		try {
			return new SharedStringsParser( input );
		}
		finally {
			input.close();
		}
	}

	private StylesheetParser parseStylesheet( final String _entryPath ) throws XMLStreamException, IOException
	{
		if (_entryPath == null)
			return null;

		final InputStream input = this.loader.getEntry( _entryPath );
		try {
			return new StylesheetParser( input );
		}
		finally {
			input.close();
		}
	}

	private void parseDefinedNames( final SpreadsheetImpl _spreadsheet ) throws XMLStreamException, SpreadsheetException
	{
		StartElement se = find( XMLConstants.WORKBOOK_DEFINED_NAMES_PATH );
		if (se != null) {
			final int definedNamesContext = getContext();
			while ((se = find( XMLConstants.Main.DEFINED_NAME, definedNamesContext )) != null) {
				final String name = se.getAttributeByName( XMLConstants.Main.NAME ).getValue();
				if (!name.startsWith( DEFINED_NAME_RESERVED_PREFIX )) {
					final String cellRangeAddress = getText();
					final ExpressionParser parser = new SpreadsheetExpressionParserA1OOXML( cellRangeAddress, _spreadsheet );
					try {
						final CellRange cellRange = (CellRange) parser.rangeOrCellRefA1();
						_spreadsheet.defineModelRangeName( name, cellRange );
					}
					catch (org.formulacompiler.compiler.internal.expressions.parser.ParseException e) {
						throw new SpreadsheetException.LoadError( "Error parsing named range " + name, e );
					}
				}
			}
		}
	}
}
