package sej.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import sej.compiler.SaveableEngine;
import sej.decompiler.SEJByteCode;

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
	 * Use to temporarily save engines for debugging purposes. Using this instead of
	 * {@link SaveableEngine#saveTo(OutputStream)} ensures that debugging code does not compile in
	 * release builds.
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


	/**
	 * Use to temporarily decompile engines for debugging purposes. Using this instead of
	 * {@link SEJByteCode#decompile(sej.runtime.Engine)} ensures that debugging code does not compile
	 * in release builds.
	 */
	public static void decompileEngine( SaveableEngine _engine, String _folderName ) throws IOException
	{
		SEJByteCode.decompile( _engine ).saveTo( new File( _folderName ) );
	}


}
