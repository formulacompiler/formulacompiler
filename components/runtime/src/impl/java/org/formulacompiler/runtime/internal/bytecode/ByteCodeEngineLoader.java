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

package org.formulacompiler.runtime.internal.bytecode;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.EngineLoader;


public final class ByteCodeEngineLoader implements EngineLoader
{
	private final ClassLoader parentClassLoader;


	public ByteCodeEngineLoader( EngineLoader.Config _config )
	{
		super();
		this.parentClassLoader = _config.parentClassLoader;
	}

	public static final class Factory implements EngineLoader.Factory
	{
		public EngineLoader newInstance( Config _config )
		{
			return new ByteCodeEngineLoader( _config );
		}
	}


	public Engine loadEngineData( InputStream _stream ) throws IOException, EngineException
	{
		final Map<String, byte[]> classNamesAndBytes = new HashMap<String, byte[]>();

		final ZipInputStream jarStream = new ZipInputStream( _stream );
		try {
			ZipEntry jarEntry;
			while ((jarEntry = jarStream.getNextEntry()) != null) {
				final String className = jarEntry.getName().replace( ".class", "" ).replace( '/', '.' );
				final ByteArrayOutputStream classDataStream = new ByteArrayOutputStream( 1024 );
				final byte[] buffer = new byte[ 1024 ];
				int red;
				while ((red = jarStream.read( buffer, 0, buffer.length )) > 0) {
					classDataStream.write( buffer, 0, red );
				}
				final byte[] classData = classDataStream.toByteArray();
				jarStream.closeEntry();

				classNamesAndBytes.put( className, classData );
			}
		}
		finally {
			jarStream.close();
		}

		return new ByteCodeEngine( this.parentClassLoader, classNamesAndBytes );
	}

}
