/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tests.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.formulacompiler.compiler.internal.IOUtil;


public class Util extends IOUtil
{
	public static final Properties BUILD_PROPS = readBuildProps();

	public static Properties readBuildProps()
	{
		final Properties ps = new Properties();
		for (String fn : new String[] { "../../build.default.properties", "../../build.properties" }) {
			final File f = new File( fn );
			if (f.exists()) {
				try {
					ps.load( new BufferedInputStream( new FileInputStream( f ) ) );
				}
				catch (Exception e) {
					throw new RuntimeException( e );
				}
			}
		}
		return ps;
	}

	public static boolean isBuildPropTrue( String _name )
	{
		return BUILD_PROPS.getProperty( _name, "false" ).equalsIgnoreCase( "true" );
	}


	public static String trimTrailingZerosAndPoint( String _string )
	{
		String result = _string;
		if (result.contains( "." )) {
			int l = result.length();
			while ('0' == result.charAt( l - 1 ))
				l--;
			if ('.' == result.charAt( l - 1 )) l--;
			result = result.substring( 0, l );
		}
		return result;
	}


	public static String disassemble( File _file ) throws IOException, InterruptedException
	{
		final StringBuilder sb = new StringBuilder();
		for (String className : new String[] { "$Factory", "$Root", "$Sect0", "$Sect1", "$Sect2" }) {
			disassemble( _file, className, sb );
		}
		return sb.toString();
	}

	private static void disassemble( File _file, final String _className, final StringBuilder _builder )
			throws IOException, InterruptedException
	{
		_builder.append( "\n" );
		_builder.append( exec( "javap", "-c", "-verbose", "-private", "-classpath", _file.getAbsolutePath(),
				"org/formulacompiler/gen/" + _className ) );
	}

}
