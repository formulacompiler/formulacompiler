package sej.internal.bytecode.runtime;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import sej.Engine;
import sej.EngineError;
import sej.internal.EngineLoader;

public final class ByteCodeEngineLoader implements EngineLoader
{

	public Engine loadEngineData( InputStream _stream ) throws IOException, EngineError
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

		return new ByteCodeEngine( classNamesAndBytes );
	}

}
