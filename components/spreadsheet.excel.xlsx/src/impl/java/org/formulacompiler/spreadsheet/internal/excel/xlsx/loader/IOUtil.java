package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * @author Vladimir Korenev
 */
public class IOUtil
{
	public static byte[] readBytes( InputStream _input ) throws IOException
	{
		final ByteArrayOutputStream outputStream = new ByteArrayOutputStream( 0x1000 );
		final byte[] buffer = new byte[ 0x1000 ];
		int n;
		while ((n = _input.read( buffer )) != -1) {
			outputStream.write( buffer, 0, n );
		}
		return outputStream.toByteArray();
	}

}
