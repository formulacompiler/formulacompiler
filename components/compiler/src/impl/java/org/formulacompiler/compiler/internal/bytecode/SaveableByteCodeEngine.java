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

package org.formulacompiler.compiler.internal.bytecode;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.Settings;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.internal.bytecode.ByteCodeEngine;


/**
 * Only the compiler generates saveable engines. The loader does not. This is so the runtime does
 * not need to include the save support.
 * 
 * @author peo
 */
final class SaveableByteCodeEngine extends ByteCodeEngine implements SaveableEngine
{


	public SaveableByteCodeEngine( ClassLoader _parentClassLoader, Map<String, byte[]> _classNamesAndBytes )
			throws EngineException
	{
		super( _parentClassLoader, _classNamesAndBytes );
	}


	public void saveTo( OutputStream _stream ) throws IOException
	{
		final ZipOutputStream jarStream = new JarOutputStream( _stream );
		try {
			jarStream.setMethod( JarOutputStream.DEFLATED );
			for (Entry<String, byte[]> classNameAndBytes : this.getClassNamesAndBytes().entrySet()) {
				final String fileName = classNameAndBytes.getKey().replace( '.', '/' ) + ".class";
				final byte[] fileData = classNameAndBytes.getValue();
				ZipEntry jarEntry = new ZipEntry( fileName );
				if (Settings.isDebugCompilationEnabled()) {
					jarEntry.setTime( 0 );
				}
				jarStream.putNextEntry( jarEntry );
				jarStream.write( fileData );
				jarStream.closeEntry();
			}
		}
		finally {
			jarStream.finish();
		}
	}

}
