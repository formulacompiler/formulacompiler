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

package org.formulacompiler.spreadsheet.internal.odf.saver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Set;
import java.util.TimeZone;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import javax.xml.stream.XMLStreamException;

import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.SpreadsheetSaver;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.odf.saver.copying.ManifestCopyingParser;
import org.formulacompiler.spreadsheet.internal.odf.saver.copying.StylesCopyingParser;
import org.formulacompiler.spreadsheet.internal.odf.saver.io.NonClosableInputStream;
import org.formulacompiler.spreadsheet.internal.odf.saver.writer.DocumentContentWriter;
import org.formulacompiler.spreadsheet.internal.odf.saver.writer.DocumentMetaWriter;
import org.formulacompiler.spreadsheet.internal.odf.xml.stream.CopyingParser;
import org.formulacompiler.spreadsheet.internal.saver.SpreadsheetSaverDispatcher;

public class OpenDocumentSpreadsheetSaver implements SpreadsheetSaver
{
	public static final String MIME_TYPE = "application/vnd.oasis.opendocument.spreadsheet";

	private static final String CONTENT_XML = "content.xml";
	private static final String META_XML = "meta.xml";
	private static final String STYLES_XML = "styles.xml";
	private static final String MANIFEST_XML = "META-INF/manifest.xml";
	private static final String MIMETYPE = "mimetype";

	private final BaseSpreadsheet spreadsheet;
	private final OutputStream outputStream;
	private final InputStream templateInputStream;
	private final TimeZone timeZone;

	private OpenDocumentSpreadsheetSaver( Config _config )
	{
		super();
		this.spreadsheet = (BaseSpreadsheet) _config.spreadsheet;
		this.outputStream = _config.outputStream;
		this.templateInputStream = _config.templateInputStream;
		this.timeZone = (_config.timeZone != null) ? _config.timeZone : TimeZone.getDefault();
	}

	public static final class Factory implements SpreadsheetSaverDispatcher.Factory
	{
		public SpreadsheetSaver newInstance( Config _config )
		{
			return new OpenDocumentSpreadsheetSaver( _config );
		}

		public boolean canHandle( String _fileName )
		{
			return _fileName.toLowerCase().endsWith( ".ods" );
		}
	}

	public void save() throws IOException, SpreadsheetException
	{
		final ZipOutputStream zipOutputStream = new ZipOutputStream( this.outputStream );

		final Set<Style> styles;
		{
			final ZipEntry zipEntry = new ZipEntry( CONTENT_XML );
			zipOutputStream.putNextEntry( zipEntry );

			final DocumentContentWriter writer = new DocumentContentWriter( this.spreadsheet, this.timeZone );
			writer.write( zipOutputStream );
			styles = writer.getStyles();

			zipOutputStream.closeEntry();
		}

		{
			final ZipEntry zipEntry = new ZipEntry( META_XML );
			zipOutputStream.putNextEntry( zipEntry );

			final DocumentMetaWriter writer = new DocumentMetaWriter( this.timeZone );
			writer.write( zipOutputStream );

			zipOutputStream.closeEntry();
		}

		final InputStream templateStream = this.templateInputStream != null ? this.templateInputStream :
				ClassLoader.getSystemResourceAsStream( "META-INF/templates/default.ods" );
		if (templateStream == null) {
			throw new IOException( "Default OpenDocument spreadsheet template is not found." );
		}

		copyFromTemplate( templateStream, zipOutputStream, styles );

		zipOutputStream.close();

	}

	private void copyFromTemplate( InputStream _inputStream, ZipOutputStream _zipOutputStream, final Set<Style> _styles ) throws IOException, SpreadsheetException
	{
		final ZipInputStream zipInputStream = new ZipInputStream( _inputStream );
		final InputStream inputStream = new NonClosableInputStream( zipInputStream );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			final String name = zipEntry.getName();
			if (MANIFEST_XML.equals( name )) {
				try {
					_zipOutputStream.putNextEntry( new ZipEntry( name ) );
					final CopyingParser parser = new ManifestCopyingParser();
					parser.copy( inputStream, _zipOutputStream );
					_zipOutputStream.closeEntry();
				}
				catch (XMLStreamException e) {
					final Throwable nestedException = e.getNestedException();
					if (nestedException != null) {
						e.initCause( nestedException );
					}
					throw new SpreadsheetException.SaveError( e );
				}

			}
			else if (STYLES_XML.equals( name )) {
				try {
					_zipOutputStream.putNextEntry( new ZipEntry( name ) );
					final CopyingParser parser = new StylesCopyingParser( _styles );
					parser.copy( inputStream, _zipOutputStream );
					_zipOutputStream.closeEntry();
				}
				catch (XMLStreamException e) {
					final Throwable nestedException = e.getNestedException();
					if (nestedException != null) {
						e.initCause( nestedException );
					}
					throw new SpreadsheetException.SaveError( e );
				}

			}
			else if (MIMETYPE.equals( name )) {
				_zipOutputStream.putNextEntry( new ZipEntry( name ) );
				final Writer writer = new OutputStreamWriter( _zipOutputStream, "UTF-8" );
				writer.write( MIME_TYPE );
				writer.flush();
				_zipOutputStream.closeEntry();
			}
			else if (!(CONTENT_XML.equals( name ) || META_XML.equals( name ))) {
				_zipOutputStream.putNextEntry( new ZipEntry( name ) );
				copy( zipInputStream, _zipOutputStream );
				_zipOutputStream.closeEntry();
			}
		}
	}

	private static void copy( InputStream _input, OutputStream _output ) throws IOException
	{
		byte[] buffer = new byte[4096];
		int n;
		while ((n = _input.read( buffer )) != -1) {
			_output.write( buffer, 0, n );
		}
	}
}
