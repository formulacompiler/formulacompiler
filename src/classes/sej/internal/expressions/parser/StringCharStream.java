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
package sej.internal.expressions.parser;

import java.io.IOException;

final class StringCharStream implements CharStream
{
	private final String source;
	private final int len;
	private int tokenStartsAt = 0;
	private int nextCharAt = 0;

	public StringCharStream(String _source)
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
