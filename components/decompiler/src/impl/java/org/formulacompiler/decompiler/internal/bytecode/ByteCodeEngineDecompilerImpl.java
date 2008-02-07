/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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
