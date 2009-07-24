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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.saver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.TimeZone;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.Relationship;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template.Stylesheet;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.loader.template.TemplateLoader;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.saver.ContentXmlWriter.Metadata;
import org.formulacompiler.spreadsheet.internal.saver.SpreadsheetSaverDispatcher;

/**
 * Spreadsheet file saver implementation for the Microsoft OOXML (Excel 2007 .xlsx) format.
 *
 * @author Igor Didyuk
 */
public final class ExcelXlsxSaver implements SpreadsheetSaver
{
	private final Spreadsheet model;
	private final OutputStream outputStream;
	private final InputStream templateInputStream;
	private final TimeZone timeZone;

	private static final String STYLESHEET_TEMPLATE = "org.formulacompiler.spreadsheet.internal.saver$styles.xml";

	public ExcelXlsxSaver( Config _config )
	{
		super();
		this.model = _config.spreadsheet;
		this.outputStream = _config.outputStream;
		this.templateInputStream = _config.templateInputStream;
		this.timeZone = (_config.timeZone != null) ? _config.timeZone : TimeZone.getDefault();
	}


	public static final class Factory implements SpreadsheetSaverDispatcher.Factory
	{
		public SpreadsheetSaver newInstance( Config _config )
		{
			return new ExcelXlsxSaver( _config );
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".xlsx" );
		}
	}

	public void save() throws IOException, SpreadsheetException
	{
		final Stylesheet template;
		if (this.templateInputStream != null) {
			template = TemplateLoader.loadWorkbook( this.templateInputStream );
		}
		else {
			final InputStream is = ClassLoader.getSystemResourceAsStream( STYLESHEET_TEMPLATE );
			try {
				template = TemplateLoader.loadStylesheet( is );
			}
			finally {
				is.close();
			}
		}

		final ZipOutputStream zos = new ZipOutputStream( this.outputStream );
		final SharedStringsWriter.StringList sharedStrings = new SharedStringsWriter.StringList();

		final List<ContentXmlWriter.Part> contentParts = New.list();

		final List<Relationship> wbRelationships = New.list();
		final List<Relationship> rootRelationships = New.list();
		try {
			// Sheets
			final Spreadsheet.Sheet[] sheets = this.model.getSheets();
			if (sheets.length == 0) {
				WorksheetWriter writer = new WorksheetWriter( zos, "xl/worksheets/sheet1.xml",
						null, null, null );
				writer.write( null );
				writer.close();

				final Metadata metadata = writer.getMetadata();
				contentParts.add( metadata.getContentPart() );
				wbRelationships.add( metadata.getRelationship() );
			}
			else {
				for (int si = 0; si != sheets.length; si++) {
					final WorksheetWriter writer = new WorksheetWriter( zos, "xl/worksheets/sheet" + (si + 1) + ".xml",
							this.timeZone, sharedStrings, template );
					writer.write( sheets[ si ] );
					writer.close();

					final Metadata metadata = writer.getMetadata();
					contentParts.add( metadata.getContentPart() );
					wbRelationships.add( metadata.getRelationship() );
				}
			}

			// Stylesheet
			{
				final StylesheetWriter writer = new StylesheetWriter( zos, "xl/styles.xml" );
				writer.write( template );
				writer.close();

				final Metadata metadata = writer.getMetadata();
				contentParts.add( metadata.getContentPart() );
				wbRelationships.add( metadata.getRelationship() );
			}

			// Shared Strings
			{
				final SharedStringsWriter writer = new SharedStringsWriter( zos, "xl/sharedStrings.xml" );
				writer.write( sharedStrings );
				writer.close();

				final Metadata metadata = writer.getMetadata();
				contentParts.add( metadata.getContentPart() );
				wbRelationships.add( metadata.getRelationship() );
			}

			// Workbook
			{
				final WorkbookWriter writer = new WorkbookWriter( zos, "xl/workbook.xml" );
				writer.write( this.model );
				writer.close();

				final Metadata metadata = writer.getMetadata();
				contentParts.add( metadata.getContentPart() );
				rootRelationships.add( metadata.getRelationship() );
			}

			// Relationships
			{
				final RelationshipSaver writer = new RelationshipSaver( zos, "xl/workbook.xml" );
				writer.write( wbRelationships );
				writer.close();
			}

			{
				final RelationshipSaver writer = new RelationshipSaver( zos, "" );
				writer.write( rootRelationships );
				writer.close();
			}

			// Content Types
			{
				final ContentTypesWriter writer = new ContentTypesWriter( zos, "[Content_Types].xml" );
				writer.write( contentParts );
				writer.close();
			}
		}
		catch (XMLStreamException e) {
			final Throwable nestedException = e.getNestedException();
			if (nestedException != null) {
				e.initCause( nestedException );
			}
			throw new SpreadsheetException.SaveError( e );
		}

		zos.close();
	}
}
