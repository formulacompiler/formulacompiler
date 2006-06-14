package sej.internal.bytecode.compiler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import sej.EngineError;
import sej.SaveableEngine;
import sej.internal.bytecode.runtime.ByteCodeEngine;

/**
 * Only the compiler generates saveable engines. The loader does not. This is so the runtime does
 * not need to include the save support.
 * 
 * @author peo
 */
public class SaveableByteCodeEngine extends ByteCodeEngine implements SaveableEngine
{
	

	public SaveableByteCodeEngine(Map<String, byte[]> _classNamesAndBytes) throws EngineError
	{
		super( _classNamesAndBytes );
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
