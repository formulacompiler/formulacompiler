/*
 * Copyright (c) 2006 Peter Arrenbrecht
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions 
 * are met:
 *
 * - Redistributions of source code must retain the above copyright 
 *   notice, this list of conditions and the following disclaimer.
 *   
 * - Redistributions in binary form must reproduce the above copyright 
 *   notice, this list of conditions and the following disclaimer in the 
 *   documentation and/or other materials provided with the distribution.
 *   
 * - The names of the contributors may not be used to endorse or promote 
 *   products derived from this software without specific prior written 
 *   permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS 
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT 
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR 
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT 
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, 
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY 
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE 
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * Contact information:
 * Peter Arrenbrecht
 * http://www.arrenbrecht.ch/jcite
 */
package sej.internal.build.bytecode;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.regex.Pattern;


/**
 * Utility methods.
 * 
 * @author peo
 */
public class Util
{


	public static String readStringFrom( File _source ) throws IOException
	{
		StringBuffer sb = new StringBuffer( 1024 );
		BufferedReader reader = new BufferedReader( new FileReader( _source ) );
		try {
			char[] chars = new char[ 1024 ];
			int red;
			while ((red = reader.read( chars )) > -1) {
				sb.append( String.valueOf( chars, 0, red ) );
			}
		}
		finally {
			reader.close();
		}
		return sb.toString();
	}


	public static void writeStringTo( String _value, File _target ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( _target ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}


	static interface FileVisitor
	{
		void visit( File _inputFile, File _outputFile ) throws IOException;
	}


	static void iterateFiles( File _inputFolder, String _pattern, File _outputFolder, boolean _recurse,
			FileVisitor _visitor ) throws IOException
	{
		final StringBuilder src = new StringBuilder();
		for (int i = 0; i < _pattern.length(); i++) {
			char c = _pattern.charAt( i );
			switch (c) {
				case '*':
					src.append( ".*" );
					break;
				case '?':
					src.append( "." );
					break;
				default:
					src.append( "\\x" );
					src.append( Integer.toHexString( c ) );
			}
		}
		final Pattern pattern = Pattern.compile( src.toString() );

		final File[] inputFiles = _inputFolder.listFiles( new FilenameFilter()
		{

			public boolean accept( File _dir, String _name )
			{
				return pattern.matcher( _name ).matches();
			}

		} );

		for (File inputFile : inputFiles) {
			if (inputFile.isFile()) {
				final File outputFile = new File( _outputFolder, inputFile.getName() );
				_visitor.visit( inputFile, outputFile );
			}
		}

		if (_recurse) {
			for (File dirOrFile : _inputFolder.listFiles()) {
				if (dirOrFile.isDirectory() && dirOrFile.getName() != "." && dirOrFile.getName() != "..") {
					final File subInputFolder = dirOrFile;
					final File subOutputFolder = new File( _outputFolder, subInputFolder.getName() );
					iterateFiles( subInputFolder, _pattern, subOutputFolder, _recurse, _visitor );
				}
			}
		}
	}

}
