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

package org.formulacompiler.decompiler.internal.bytecode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.runtime.New;


public final class ByteCodeEngineSourceImpl implements ByteCodeEngineSource
{
	private final SortedMap<String, String> classes = New.sortedMap();

	public ByteCodeEngineSourceImpl( Map<String, String> _classes )
	{
		this.classes.putAll( _classes );
	}

	public Map<String, String> getSortedClasses()
	{
		return Collections.unmodifiableMap( this.classes );
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : this.classes.entrySet()) {
			if (first) {
				first = false;
			}
			else {
				builder.append( "\n\n" );
			}
			builder.append( "// -------------------------- " ).append( entry.getKey() ).append( "\n\n" );
			builder.append( entry.getValue() );
		}
		return builder.toString();
	}


	public void saveTo( File _target ) throws IOException
	{
		for (Map.Entry<String, String> entry : this.classes.entrySet()) {
			final String name = entry.getKey();
			final String source = entry.getValue();
			final File sourceFile = new File( _target, name.replace( '.', '/' ) + ".java" );
			final File sourceFolder = sourceFile.getParentFile();
			sourceFolder.mkdirs();
			final BufferedWriter writer = new BufferedWriter( new FileWriter( sourceFile ) );
			try {
				writer.append( source );
			}
			finally {
				writer.close();
			}
		}
	}

	public void saveTo( String _targetPath ) throws IOException
	{
		saveTo( new File( _targetPath ) );
	}

}