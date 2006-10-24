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
package sej.internal.bytecode.compiler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import sej.SaveableEngine;
import sej.internal.Settings;
import sej.internal.bytecode.runtime.ByteCodeEngine;
import sej.runtime.EngineException;

/**
 * Only the compiler generates saveable engines. The loader does not. This is so the runtime does
 * not need to include the save support.
 * 
 * @author peo
 */
final class SaveableByteCodeEngine extends ByteCodeEngine implements SaveableEngine
{


	public SaveableByteCodeEngine(ClassLoader _parentClassLoader, Map<String, byte[]> _classNamesAndBytes)
			throws EngineException
	{
		super( _parentClassLoader, _classNamesAndBytes );
	}


	public void saveTo( OutputStream _stream ) throws IOException
	{
		final ZipOutputStream jarStream = new JarOutputStream( _stream );
		try {
			jarStream.setMethod( JarOutputStream.DEFLATED );
			for (Entry<String, byte[]> classNameAndBytes : this.classNamesAndBytes.entrySet()) {
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
