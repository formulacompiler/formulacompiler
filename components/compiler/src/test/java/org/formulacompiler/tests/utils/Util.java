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
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;


public class Util
{

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


	public static String exec( String... _command ) throws IOException, InterruptedException
	{
		final ProcessBuilder pb = new ProcessBuilder( _command );
		final Process p = pb.start();
		p.waitFor();
		final StringWriter writer = new StringWriter();
		writeStream( p.getInputStream(), writer );
		return writer.toString();
	}

	private static void writeStream( InputStream _from, Writer _printTo ) throws IOException
	{
		final Reader in = new BufferedReader( new InputStreamReader( new BufferedInputStream( _from ) ) );
		while (in.ready())
			_printTo.write( in.read() );
	}

}
