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

import java.util.Map;
import java.util.Stack;

import org.formulacompiler.runtime.New;


/**
 * A specialized string builder wrapper that supports multiple lines with proper indentation.
 * 
 * @author peo
 */
public class DescriptionBuilder
{
	public static final CharSequence DEFAULT_INDENT = "  ";

	private final StringBuilder builder = new StringBuilder();
	private final CharSequence indent;
	private final int indentLength;
	private StringBuilder indentation = new StringBuilder();
	private boolean indentPending = true;


	public DescriptionBuilder( CharSequence _indent )
	{
		super();
		this.indent = _indent;
		this.indentLength = _indent.length();
	}

	public DescriptionBuilder()
	{
		this( DEFAULT_INDENT );
	}


	@Override
	public String toString()
	{
		return this.builder.toString();
	}

	public final void ensureCapacity( int _minimumCapacity )
	{
		this.builder.ensureCapacity( _minimumCapacity );
	}

	public final int length()
	{
		return this.builder.length();
	}


	public final DescriptionBuilder indent()
	{
		this.indentation.append( this.indent );
		return this;
	}

	public final DescriptionBuilder indent( int _levels )
	{
		for (int i = 0; i < _levels; i++)
			indent();
		return this;
	}

	public final DescriptionBuilder outdent()
	{
		final int l = this.indentation.length();
		if (this.indentLength <= l) {
			this.indentation.setLength( l - this.indentLength );
		}
		return this;
	}


	public final DescriptionBuilder newLine()
	{
		this.builder.append( '\n' );
		this.indentPending = true;
		return this;
	}

	public final DescriptionBuilder onNewLine()
	{
		if (!this.indentPending) newLine();
		return this;
	}


	private final void addIndentationIfPending()
	{
		if (this.indentPending) addIndentation();
	}


	private final void addIndentation()
	{
		this.builder.append( this.indentation );
		this.indentPending = false;
	}


	public final DescriptionBuilder appendLine( String _string )
	{
		return append( _string ).newLine();
	}


	public final DescriptionBuilder appendUnindented( String _string )
	{
		if (null != _string) this.builder.append( _string );
		return this;
	}


	public final DescriptionBuilder append( boolean _b )
	{
		addIndentationIfPending();
		this.builder.append( _b );
		return this;
	}

	public final DescriptionBuilder append( char _c )
	{
		addIndentationIfPending();
		this.builder.append( _c );
		return this;
	}

	public final DescriptionBuilder append( char[] _str, int _offset, int _len )
	{
		addIndentationIfPending();
		if (null == _str) appendNull();
		else this.builder.append( _str, _offset, _len );
		return this;
	}

	public final DescriptionBuilder append( char[] _str )
	{
		addIndentationIfPending();
		if (null == _str) appendNull();
		else this.builder.append( _str );
		return this;
	}

	public final DescriptionBuilder append( CharSequence _s, int _start, int _end )
	{
		addIndentationIfPending();
		if (null == _s) appendNull();
		else this.builder.append( _s, _start, _end );
		return this;
	}

	public final DescriptionBuilder append( CharSequence _s )
	{
		addIndentationIfPending();
		if (null == _s) appendNull();
		else this.builder.append( _s );
		return this;
	}

	public final DescriptionBuilder append( double _d )
	{
		addIndentationIfPending();
		this.builder.append( _d );
		return this;
	}

	public final DescriptionBuilder append( float _f )
	{
		addIndentationIfPending();
		this.builder.append( _f );
		return this;
	}

	public final DescriptionBuilder append( int _i )
	{
		addIndentationIfPending();
		this.builder.append( _i );
		return this;
	}

	public final DescriptionBuilder append( long _lng )
	{
		addIndentationIfPending();
		this.builder.append( _lng );
		return this;
	}

	public final DescriptionBuilder append( Object _obj )
	{
		if (_obj instanceof AbstractDescribable) return append( (AbstractDescribable) _obj );
		addIndentationIfPending();
		if (null == _obj) appendNull();
		else this.builder.append( _obj );
		return this;
	}

	public final DescriptionBuilder append( String _str )
	{
		addIndentationIfPending();
		if (null == _str) appendNull();
		else this.builder.append( _str );
		return this;
	}

	public final DescriptionBuilder append( StringBuffer _sb )
	{
		addIndentationIfPending();
		this.builder.append( _sb );
		return this;
	}

	public final DescriptionBuilder append( AbstractDescribable _desc )
	{
		if (null == _desc) appendNull();
		else _desc.describeTo( this );
		return this;
	}


	public final DescriptionBuilder appendNull()
	{
		addIndentationIfPending();
		this.builder.append( "null" );
		return this;
	}


	private Map<Class, Object> contextMap = New.map();
	private Stack<Object> contextStack = New.stack();

	@SuppressWarnings( "unchecked" )
	public final <C> C getContext( Class<C> _class )
	{
		return (C) this.contextMap.get( _class );
	}

	public final DescriptionBuilder pushContext( Object _context )
	{
		assert null != _context;
		this.contextStack.push( getContext( _context.getClass() ) );
		this.contextStack.push( _context.getClass() );
		this.contextMap.put( _context.getClass(), _context );
		return this;
	}

	public final DescriptionBuilder popContext()
	{
		final Class clazz = (Class) this.contextStack.pop();
		final Object was = this.contextStack.pop();
		this.contextMap.put( clazz, was );
		return this;
	}


}
