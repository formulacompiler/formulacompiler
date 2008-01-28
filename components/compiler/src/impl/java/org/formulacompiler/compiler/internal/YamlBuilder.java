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

import java.util.Date;



public final class YamlBuilder
{
	private static final CharSequence VAL_SEP = ": ";
	private static final CharSequence LIST_SEP = ":";
	private static final CharSequence LIST_PREFIX = "- ";

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
