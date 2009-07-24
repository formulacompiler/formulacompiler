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

package org.formulacompiler.decompiler.internal.bytecode;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Map;

import net.sf.jode.bytecode.ClassPath;
import net.sf.jode.decompiler.Decompiler;

import org.formulacompiler.decompiler.ByteCodeEngineDecompiler;
import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.bytecode.ByteCodeEngine;

public class ByteCodeEngineDecompilerImpl implements ByteCodeEngineDecompiler
{
	private final ByteCodeEngine engine;


	public ByteCodeEngineDecompilerImpl( Config _config )
	{
		super();
		this.engine = (ByteCodeEngine) _config.engine;
	}

	public static final class Factory implements ByteCodeEngineDecompiler.Factory
	{
		public ByteCodeEngineDecompiler newInstance( Config _config )
		{
			return new ByteCodeEngineDecompilerImpl( _config );
		}
	}


	public ByteCodeEngineSource decompile() throws IOException
	{
		final Map<String, String> classes = New.map();

		final ByteCodeEngineLocation location = new ByteCodeEngineLocation( this.engine );
		final Decompiler decompiler = new Decompiler();
		decompiler.setOption( "style", "sun" );
		decompiler.setOption( "tabwidth", "100" );
		decompiler.setOption( "indent", "4" );
		final ByteCodeEngineLocation engineLocation = location;

		decompiler.setClassPath( new ClassPath( new ClassPath.Location[] { engineLocation,
				ClassPath.createLocation( "reflection:" ) } ) );

		for (String className : this.engine.getClassNamesAndBytes().keySet()) {
			final StringWriter writer = new StringWriter();
			decompiler.decompile( className, writer, null );
			final String source = stripHeaderCommentFrom( writer.toString() );
			classes.put( className, source );
		}

		return new ByteCodeEngineSourceImpl( classes );
	}


	private String stripHeaderCommentFrom( String _source )
	{
		final int endOfHeaderComment = _source.indexOf( "*/" );
		if (endOfHeaderComment >= 0) {
			return _source.substring( endOfHeaderComment + 2 ).trim();
		}
		return _source;
	}


	private static final class ByteCodeEngineLocation extends ClassPath.Location
	{
		private final Map<String, byte[]> classFileNameAndBytes = New.map();

		public ByteCodeEngineLocation( ByteCodeEngine _engine )
		{
			super();
			transferClassBytes( _engine.getClassNamesAndBytes() );
		}

		private void transferClassBytes( Map<String, byte[]> _classNamesAndBytes )
		{
			for (Map.Entry<String, byte[]> entry : _classNamesAndBytes.entrySet()) {
				final String className = entry.getKey();
				final byte[] classBytes = entry.getValue();
				final String fileName = className.replace( ".", "/" ) + ".class";
				this.classFileNameAndBytes.put( fileName, classBytes );
			}
		}

		@Override
		protected boolean exists( String _file )
		{
			return this.classFileNameAndBytes.containsKey( _file );
		}

		@Override
		protected InputStream getFile( String _file ) throws IOException
		{
			return new ByteArrayInputStream( this.classFileNameAndBytes.get( _file ) );
		}

	}

}
