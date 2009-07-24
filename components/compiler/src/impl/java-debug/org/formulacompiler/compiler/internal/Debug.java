/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
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
	 * Like {@link #decompileEngine(SaveableEngine)}, but runs the result with the supplied editor.
	 * 
	 * @param _editor is, for example, "notepad" or "gedit".
	 */
	public static void decompileAndShowEngine( SaveableEngine _engine, String _editor ) throws IOException
	{
		decompileEngine( _engine );
		Runtime.getRuntime().exec( _editor + " " + TEMP_DECOMPILED + "/org/formulacompiler/gen/$Root.java" );
	}


}
