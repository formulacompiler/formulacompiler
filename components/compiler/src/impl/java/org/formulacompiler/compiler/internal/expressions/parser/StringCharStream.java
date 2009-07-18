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

package org.formulacompiler.compiler.internal.expressions.parser;

import java.io.IOException;


final class StringCharStream implements CharStream
{
	private final String source;
	private final int len;
	private int tokenStartsAt = 0;
	private int nextCharAt = 0;

	public StringCharStream( String _source )
	{
		super();
		this.source = _source;
		this.len = _source.length();
	}


	public char readChar() throws IOException
	{
		if (this.nextCharAt < this.len) {
			return this.source.charAt( this.nextCharAt++ );
		}
		throw new IOException( "EOF" );
	}

	public char BeginToken() throws IOException
	{
		this.tokenStartsAt = this.nextCharAt;
		return readChar();
	}

	public String GetImage()
	{
		return this.source.substring( this.tokenStartsAt, this.nextCharAt );
	}

	public char[] GetSuffix( int _len )
	{
		final char[] suffix = new char[ _len ];
		this.source.getChars( this.nextCharAt - _len, this.nextCharAt, suffix, 0 );
		return suffix;
	}

	public void backup( int _amount )
	{
		this.nextCharAt -= _amount;
	}

	public int getBeginColumn()
	{
		return this.tokenStartsAt + 1; // columns are 1-based
	}

	public int getEndColumn()
	{
		return this.nextCharAt + 1; // columns are 1-based
	}

	@Deprecated
	public int getColumn()
	{
		return getEndColumn();
	}

	public int getBeginLine()
	{
		return 1;
	}

	public int getEndLine()
	{
		return getBeginLine();
	}

	@Deprecated
	public int getLine()
	{
		return getEndLine();
	}

	public void Done()
	{
		// No cleanup here.
	}

}
