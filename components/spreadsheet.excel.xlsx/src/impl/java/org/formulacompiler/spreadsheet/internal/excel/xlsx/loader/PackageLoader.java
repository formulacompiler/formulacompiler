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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.internal.excel.xlsx.Relationship;


/**
 * @author Igor Didyuk
 */
public final class PackageLoader
{
	private final Map<String, byte[]> entries = New.hashMap();

	public PackageLoader( final InputStream _stream ) throws IOException
	{
		final ZipInputStream zipInputStream = new ZipInputStream( _stream );
		ZipEntry zipEntry;
		while ((zipEntry = zipInputStream.getNextEntry()) != null) {
			final byte[] bytes = IOUtil.readBytes( zipInputStream );
			this.entries.put( zipEntry.getName(), bytes );
		}
	}

	public InputStream getEntry( final String _entryPath )
	{
		return new ByteArrayInputStream( this.entries.get( _entryPath ) );
	}

	public byte[] getEntryBytes( final String _entryPath )
	{
		return this.entries.get( _entryPath );
	}

	public InputStream getRelationship( final String _entryPath )
	{
		return getEntry( Relationship.getRelationshipPath( _entryPath ) );
	}
}
