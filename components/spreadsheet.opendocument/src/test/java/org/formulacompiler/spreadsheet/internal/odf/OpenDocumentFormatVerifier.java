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

package org.formulacompiler.spreadsheet.internal.odf;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.formulacompiler.spreadsheet.internal.odf.saver.io.NonClosableInputStream;
import org.iso_relax.verifier.Schema;
import org.iso_relax.verifier.Verifier;
import org.iso_relax.verifier.VerifierConfigurationException;
import org.iso_relax.verifier.VerifierFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class OpenDocumentFormatVerifier
{
	public static final String RELAXNG_NS_URI = "http://relaxng.org/ns/structure/1.0";

	public static void verify( InputStream _odsInputStream ) throws Exception
	{
		final VerifierFactory factory = VerifierFactory.newInstance( RELAXNG_NS_URI );
		final Verifier manifestVerifier = newVerifier( factory, "OpenDocument-manifest-schema-v1.1.rng" );
		final Verifier verifier = newVerifier( factory, "OpenDocument-strict-schema-v1.1.rng" );

		final ZipInputStream zipInputStream = new ZipInputStream( _odsInputStream );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			final String name = zipEntry.getName();
			final InputStream inputStream = new NonClosableInputStream( zipInputStream );
			if ("content.xml".equals( name )
					|| "meta.xml".equals( name ) || "settings.xml".equals( name ) || "styles.xml".equals( name )) {
				verifier.verify( new InputSource( inputStream ) );
			}
			else if ("META-INF/manifest.xml".equals( name )) {
				manifestVerifier.verify( new InputSource( inputStream ) );
			}
		}
	}

	private static Verifier newVerifier( final VerifierFactory _factory, final String _schemaName )
			throws VerifierConfigurationException, SAXException, IOException
	{
		final File file = new File( "src/test/data/schema/" + _schemaName );
		final Schema schema = _factory.compileSchema( file );
		return schema.newVerifier();
	}


}