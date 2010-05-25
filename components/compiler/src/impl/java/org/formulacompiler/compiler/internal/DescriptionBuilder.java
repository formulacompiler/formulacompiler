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
		return internalPushContext( _context, _context.getClass() );
	}

	public final <C> DescriptionBuilder pushContext( C _context, Class<? super C> _class )
	{
		return internalPushContext( _context, _class );
	}

	private final DescriptionBuilder internalPushContext( Object
			_context, Class<?> _class )
	{
		assert null != _context;
		this.contextStack.push( getContext( _class ) );
		this.contextStack.push( _class );
		this.contextMap.put( _class, _context );
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
