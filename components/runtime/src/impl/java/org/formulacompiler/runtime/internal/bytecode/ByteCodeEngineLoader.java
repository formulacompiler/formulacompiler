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


	public ByteCodeEngineLoader(EngineLoader.Config _config)
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
