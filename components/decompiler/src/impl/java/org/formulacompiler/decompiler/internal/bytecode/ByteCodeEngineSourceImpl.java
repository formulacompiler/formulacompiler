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
package org.formulacompiler.decompiler.internal.bytecode;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;

import org.formulacompiler.decompiler.ByteCodeEngineSource;
import org.formulacompiler.runtime.New;


public final class ByteCodeEngineSourceImpl implements ByteCodeEngineSource
{
	private final SortedMap<String, String> classes = New.newSortedMap();

	public ByteCodeEngineSourceImpl(Map<String, String> _classes)
	{
		this.classes.putAll( _classes );
	}

	public Map<String, String> getSortedClasses()
	{
		return Collections.unmodifiableMap( this.classes );
	}

	@Override
	public String toString()
	{
		final StringBuilder builder = new StringBuilder();
		boolean first = true;
		for (Map.Entry<String, String> entry : this.classes.entrySet()) {
			if (first) {
				first = false;
			}
			else {
				builder.append( "\n\n" );
			}
			builder.append( "// -------------------------- " ).append( entry.getKey() ).append( "\n\n" );
			builder.append( entry.getValue() );
		}
		return builder.toString();
	}


	public void saveTo( File _target ) throws IOException
	{
		for (Map.Entry<String, String> entry : this.classes.entrySet()) {
			final String name = entry.getKey();
			final String source = entry.getValue();
			final File sourceFile = new File( _target, name.replace( '.', '/' ) + ".java" );
			final File sourceFolder = sourceFile.getParentFile();
			sourceFolder.mkdirs();
			final BufferedWriter writer = new BufferedWriter( new FileWriter( sourceFile ) );
			try {
				writer.append( source );
			}
			finally {
				writer.close();
			}
		}
	}

	public void saveTo( String _targetPath ) throws IOException
	{
		saveTo( new File( _targetPath ) );
	}

}