package sej.internal;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sej.SaveableEngine;

/**
 * Debugging class whose use is rejected in release builds.
 * 
 * @author peo
 */
public final class Debug
{

	/**
	 * Use like "if (Debug.TRUE) ..." to guard debug statements.
	 */
	public static final boolean TRUE = true;
	

	/**
	 * Use to temporarily save engines for debugging purposes.
	 * 
	 * @param _engine
	 * @param _fileName
	 * @throws IOException
	 */
	public static void saveEngine( SaveableEngine _engine, String _fileName ) throws IOException
	{
		OutputStream os = new BufferedOutputStream( new FileOutputStream( _fileName ) );
		try {
			_engine.saveTo( os );
		}
		finally {
			os.close();
		}
	}


}
