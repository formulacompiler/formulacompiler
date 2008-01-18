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
