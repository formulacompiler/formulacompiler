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

package org.formulacompiler.compiler.internal;

import java.util.Date;
import java.util.Iterator;


public final class YamlBuilder
{
	private static final CharSequence VAL_SEP = ": ";
	private static final CharSequence LIST_SEP = ":";
	private static final CharSequence LIST_PREFIX = "- ";
	private static final CharSequence LINE_LIST_SEP = ", ";
	private static final CharSequence LINE_LIST_START = "[ ";
	private static final CharSequence LINE_LIST_END = " ]";

	private final DescriptionBuilder desc;


	public YamlBuilder( DescriptionBuilder _desc )
	{
		this.desc = _desc;
	}


	public final DescriptionBuilder desc()
	{
		return this.desc;
	}


	@Override
	public String toString()
	{
		return desc().toString();
	}


	public final YamlBuilder ln( CharSequence _name )
	{
		desc().append( _name ).append( LIST_SEP );
		return this;
	}


	public final <D extends Yamlizable> YamlBuilder l( Iterable<D> _elts )
	{
		for (Yamlizable elt : _elts) {
			desc().onNewLine().append( LIST_PREFIX ).indent();
			v( elt );
			desc().outdent();
		}
		return this;
	}

	public final YamlBuilder l( CharSequence _name, Object... _elts )
	{
		for (Object elt : _elts) {
			desc().onNewLine().append( LIST_PREFIX ).indent();
			nv( _name, elt );
			desc().outdent();
		}
		return this;
	}

	public final <D extends Yamlizable> YamlBuilder lSpaced( Iterable<D> _elts )
	{
		for (Yamlizable elt : _elts) {
			desc().onNewLine().newLine().append( LIST_PREFIX ).indent();
			v( elt );
			desc().outdent();
		}
		return this;
	}

	public final YamlBuilder lOneLine( Iterable<String> _elts )
	{
		desc().append( LINE_LIST_START );
		Iterator<String> i = _elts.iterator();
		boolean hasNext = i.hasNext();
		while (hasNext) {
			String elt = i.next();
			v( elt );
			hasNext = i.hasNext();
			if (hasNext)
				desc().append( LINE_LIST_SEP );
		}
		desc().append( LINE_LIST_END );
		return this;
	}


	public final YamlBuilder s( CharSequence _string )
	{
		desc().append( _string );
		return this;
	}

	public final YamlBuilder s( char _c )
	{
		desc().append( _c );
		return this;
	}

	public final YamlBuilder i( int _i )
	{
		desc().append( _i );
		return this;
	}


	public final YamlBuilder vn( CharSequence _name )
	{
		return s( _name ).s( VAL_SEP );
	}

	public final YamlBuilder nv( CharSequence _name, Object _value )
	{
		if (null != _value) {
			return vn( _name ).v( _value ).lf();
		}
		return this;
	}

	public final YamlBuilder nv( CharSequence _name, CharSequence _valuePrefix, Object _value )
	{
		if (null != _value) {
			return vn( _name ).s( _valuePrefix ).v( _value ).lf();
		}
		return this;
	}

	public final YamlBuilder v( Object _value, boolean quoteStrings )
	{
		if (null == _value) desc().appendNull();
		else if (_value instanceof Yamlizable) return v( (Yamlizable) _value );
		else if (_value instanceof AbstractDescribable) return v( (AbstractDescribable) _value );
		else if (_value instanceof String) return quoteStrings ? qv( (String) _value ) : v( (String) _value );
		else if (_value instanceof Date) return v( (Date) _value );
		else if (_value instanceof Double) return v( ((Double) _value).doubleValue() );
		else desc().append( _value );
		return this;
	}

	public final YamlBuilder v( Object _value )
	{
		return v( _value, false );
	}

	public final YamlBuilder v( AbstractDescribable _value )
	{
		if (null == _value) desc().appendNull();
		else _value.describeTo( desc() );
		return this;
	}

	public final YamlBuilder v( Yamlizable _value )
	{
		if (null == _value) desc().appendNull();
		else _value.yamlTo( this );
		return this;
	}

	public final YamlBuilder v( AbstractYamlizable _value )
	{
		if (null == _value) desc().appendNull();
		else _value.yamlTo( this );
		return this;
	}

	public final YamlBuilder v( String _value )
	{
		desc().append( _value );
		return this;
	}

	/**
	 * Appends a quoted string to a given buffer.
	 * 
	 * @param s the string to be added.
	 * @return this decription builder.
	 */
	public final YamlBuilder qv( final String s )
	{
		s( "\"" );
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt( i );
			if (c == '\n') {
				s( "\\n" );
			}
			else if (c == '\r') {
				s( "\\r" );
			}
			else if (c == '\\') {
				s( "\\\\" );
			}
			else if (c == '"') {
				s( "\\\"" );
			}
			else if (c < 0x20) {
				s( "\\u" );
				if (c < 0x10) {
					s( "000" );
				}
				else {
					s( "00" );
				}
				s( Integer.toHexString( c ) );
			}
			else {
				desc().append( c );
			}
		}
		s( "\"" );

		return this;
	}

	public final YamlBuilder v( Date _value )
	{
		desc().append( "Date( " ).append( _value.getTime() ).append( " )" );
		return this;
	}

	public final YamlBuilder v( double _value )
	{
		if (Double.isNaN( _value )) return s( ".NAN" );
		if (Double.isInfinite( _value )) return s( _value == Double.POSITIVE_INFINITY ? ".Inf" : "-.Inf" );
		desc().append( _value );
		return this;
	}

	public final YamlBuilder v( long _value )
	{
		desc().append( _value );
		return this;
	}

	public final YamlBuilder v( int _value )
	{
		desc().append( _value );
		return this;
	}

	public final YamlBuilder v( char _value )
	{
		desc().append( _value );
		return this;
	}

	public final YamlBuilder v( boolean _value )
	{
		desc().append( _value );
		return this;
	}


	public final YamlBuilder lf()
	{
		desc().newLine();
		return this;
	}


}
