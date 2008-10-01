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

import java.io.IOException;
import java.io.InputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.XMLConstants;
import org.formulacompiler.spreadsheet.internal.loader.SpreadsheetLoaderDispatcher;


/**
 * Spreadsheet file loader implementation for the Microsoft OOXML (Excel 2007 .xlsx) format.
 *
 * @author Igor Didyuk
 */
public class ExcelXlsxLoader implements SpreadsheetLoader
{

	public static final class Factory implements SpreadsheetLoaderDispatcher.Factory
	{

		public SpreadsheetLoader newInstance( Config _config )
		{
			return new ExcelXlsxLoader( _config );
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".xlsx" );
		}

	}

	private final Config config;

	private ExcelXlsxLoader( Config _config )
	{
		this.config = _config;
	}

	public Spreadsheet loadFrom( final String _originalFileName, final InputStream _stream ) throws IOException,
			SpreadsheetException
	{
		PackageLoader loader = new PackageLoader( _stream );
		try {
			final String workbook;
			final InputStream input = loader.getRelationship( "" );
			try {
				final RelationshipParser parser = new RelationshipParser( input, "" );
				workbook = parser.findByType( XMLConstants.WORKBOOK_RELATIONSHIP_TYPE );
				if (workbook == null)
					throw new SpreadsheetException.LoadError( "officeDocument relationship was not found in the root relationshp list" );
			}
			finally {
				input.close();
			}

			final WorkbookParser parser = new WorkbookParser( loader, workbook, this.config );
			try {
				final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
				parser.parse( spreadsheet );
				spreadsheet.trim();
				return spreadsheet;
			}
			finally {
				parser.close();
			}
		}
		catch (XMLStreamException e) {
			final Throwable nestedException = e.getNestedException();
			if (nestedException != null) {
				e.initCause( nestedException );
			}
			throw new SpreadsheetException.LoadError( "Error loading " + _originalFileName, e );
		}
	}
}
