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
package org.formulacompiler.compiler.internal;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.FormulaDecompiler;


/**
 * Debugging class whose use is rejected in release builds.
 * 
 * @author peo
 */
public final class Debug
{
	private static final String TEMP_DECOMPILED = "temp/debug/decompiled";
	
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
	 * {@link FormulaDecompiler#decompile(org.formulacompiler.runtime.Engine)} ensures that debugging
	 * code does not compile in release builds.
	 * 
	 * @see #decompileEngine(SaveableEngine)
	 */
	public static void decompileEngine( SaveableEngine _engine, String _folderName ) throws IOException
	{
		FormulaDecompiler.decompile( _engine ).saveTo( new File( _folderName ) );
	}


	/**
	 * Use to temporarily decompile an engine for debugging purposes to "temp/debug/decompiled/".
	 * Using this instead of {@link FormulaDecompiler#decompile(org.formulacompiler.runtime.Engine)}
	 * ensures that debugging code does not compile in release builds.
	 * 
	 * @see #decompileEngine(SaveableEngine, String)
	 */
	public static void decompileEngine( SaveableEngine _engine ) throws IOException
	{
		decompileEngine( _engine, TEMP_DECOMPILED );
	}


	/**
	 * Like {@link #decompileEngine(SaveableEngine)}, but runs the result in "gedit" (Linux).
	 */
	public static void decompileAndShowEngine( SaveableEngine _engine ) throws IOException
	{
		decompileEngine( _engine );
		Runtime.getRuntime().exec( "gedit " + TEMP_DECOMPILED + "/org/formulacompiler/gen/$Root.java" );
	}


}
