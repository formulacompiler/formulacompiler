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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template;

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.PackageLoader;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.RelationshipParser;

/**
 * @author Igor Didyuk
 */
public abstract class TemplateLoader
{
	public static Stylesheet loadWorkbook( final InputStream _stream ) throws IOException, SpreadsheetException
	{
		PackageLoader loader = new PackageLoader( _stream );
		try {
			InputStream input;

			final String workbook;
			input = loader.getRelationship( "" );
			try {
				RelationshipParser parser = new RelationshipParser( input, "" );
				workbook = parser.findByType( XMLConstants.WORKBOOK_RELATIONSHIP_TYPE );
				if (workbook == null)
					throw new SpreadsheetException.LoadError( "officeDocument relationship was not found in the root relationshp list" );
			}
			finally {
				input.close();
			}

			WorkbookParser workbookParser = new WorkbookParser( loader, workbook );
			try {
				workbookParser.parse();
				return workbookParser.getStylesheet();
			}
			finally {
				workbookParser.close();
			}
		}
		catch (XMLStreamException e) {
			final Throwable nestedException = e.getNestedException();
			if (nestedException != null) {
				e.initCause( nestedException );
			}
			throw new SpreadsheetException.LoadError( "Error loading workbook template", e );
		}
	}

	public static Stylesheet loadStylesheet( final InputStream _stream ) throws IOException, SpreadsheetException
	{
		try {
			final StylesheetParser stylesheetParser = new StylesheetParser( _stream );
			stylesheetParser.close();
			return stylesheetParser;
		}
		catch (XMLStreamException e) {
			final Throwable nestedException = e.getNestedException();
			if (nestedException != null) {
				e.initCause( nestedException );
			}
			throw new SpreadsheetException.LoadError( "Error loading stylesheet template", e );
		}
	}
}
