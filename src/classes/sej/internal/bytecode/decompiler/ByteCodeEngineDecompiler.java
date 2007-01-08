/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.bytecode.decompiler;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import net.sf.jode.bytecode.ClassPath;
import net.sf.jode.decompiler.Decompiler;
import sej.EngineDescription;
import sej.describable.AbstractDescribable;
import sej.describable.DescriptionBuilder;
import sej.internal.bytecode.runtime.ByteCodeEngine;
import sej.internal.engine.compiler.EngineDecompiler;
import sej.util.New;

public class ByteCodeEngineDecompiler implements EngineDecompiler
{
	private final ByteCodeEngine engine;


	public ByteCodeEngineDecompiler(Config _config)
	{
		super();
		this.engine = (ByteCodeEngine) _config.engine;
	}


	public EngineDescription decompile() throws IOException
	{
		final Map<String, String> classes = New.newMap();

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

		return new ByteCodeEngineDescription( classes );
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
		private final Map<String, byte[]> classFileNameAndBytes = New.newMap();

		public ByteCodeEngineLocation(ByteCodeEngine _engine)
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


	public static final class ByteCodeEngineDescription extends AbstractDescribable implements EngineDescription
	{
		private final SortedMap<String, String> classes = New.newSortedMap();

		public ByteCodeEngineDescription(Map<String, String> _classes)
		{
			this.classes.putAll( _classes );
		}
		
		public Map<String, String> getSortedClasses()
		{
			return Collections.unmodifiableMap( this.classes );
		}

		@Override
		public void describeTo( DescriptionBuilder _to ) throws IOException
		{
			boolean first = true;
			for (Map.Entry<String, String> entry : this.classes.entrySet()) {
				if (first) {
					first = false;
				}
				else {
					_to.newLine();
					_to.newLine();
				}
				_to.append( "// -------------------------- " );
				_to.appendLine( entry.getKey() );
				_to.newLine();
				_to.append( entry.getValue() );
			}
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

	}

}
