/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.describable;


/**
 * A specialized string builder that supports multiple lines with proper indentation. This class
 * really ought to implement <code>Appendable</code>, but unfortunately, that interface is not
 * available in Java 1.4.
 * 
 * @author peo
 */
public class DescriptionBuilder
{
	private final StringBuilder builder = new StringBuilder();
	private final String indent;
	private final int indentLength;
	private StringBuilder indentation = new StringBuilder();
	private boolean indentPending = true;


	public DescriptionBuilder()
	{
		this( "\t" );
	}


	public DescriptionBuilder(final String _indent)
	{
		super();
		this.indent = _indent;
		this.indentLength = _indent.length();
	}


	public StringBuilder getIndentation()
	{
		return this.indentation;
	}


	public void indent()
	{
		this.indentation.append( this.indent );
	}


	public void outdent()
	{
		final int l = this.indentation.length();
		if (this.indentLength <= l) {
			this.indentation.setLength( l - this.indentLength );
		}
	}


	public void newLine()
	{
		this.builder.append( '\n' );
		this.indentPending = true;
	}


	private void addIndentationIfPending()
	{
		if (this.indentPending) addIndentation();
	}


	private void addIndentation()
	{
		this.builder.append( this.indentation );
		this.indentPending = false;
	}


	@Override
	public String toString()
	{
		return this.builder.toString();
	}


	public void appendLine( String _string )
	{
		append( _string );
		newLine();
	}


	public DescriptionBuilder append( boolean _b )
	{
		addIndentationIfPending();
		this.builder.append( _b );
		return this;
	}


	public DescriptionBuilder append( char _c )
	{
		addIndentationIfPending();
		this.builder.append( _c );
		return this;
	}


	public DescriptionBuilder append( char[] _str, int _offset, int _len )
	{
		addIndentationIfPending();
		this.builder.append( _str, _offset, _len );
		return this;
	}


	public DescriptionBuilder append( char[] _str )
	{
		addIndentationIfPending();
		this.builder.append( _str );
		return this;
	}


	public DescriptionBuilder append( CharSequence _s, int _start, int _end )
	{
		addIndentationIfPending();
		this.builder.append( _s, _start, _end );
		return this;
	}


	public DescriptionBuilder append( CharSequence _s )
	{
		addIndentationIfPending();
		this.builder.append( _s );
		return this;
	}


	public DescriptionBuilder append( double _d )
	{
		addIndentationIfPending();
		this.builder.append( _d );
		return this;
	}


	public DescriptionBuilder append( float _f )
	{
		addIndentationIfPending();
		this.builder.append( _f );
		return this;
	}


	public DescriptionBuilder append( int _i )
	{
		addIndentationIfPending();
		this.builder.append( _i );
		return this;
	}


	public DescriptionBuilder append( long _lng )
	{
		addIndentationIfPending();
		this.builder.append( _lng );
		return this;
	}


	public DescriptionBuilder append( Object _obj )
	{
		addIndentationIfPending();
		this.builder.append( _obj );
		return this;
	}


	public DescriptionBuilder append( String _str )
	{
		addIndentationIfPending();
		this.builder.append( _str );
		return this;
	}


	public DescriptionBuilder append( StringBuffer _sb )
	{
		addIndentationIfPending();
		this.builder.append( _sb );
		return this;
	}


	public void ensureCapacity( int _minimumCapacity )
	{
		this.builder.ensureCapacity( _minimumCapacity );
	}


	public int length()
	{
		return this.builder.length();
	}


}
