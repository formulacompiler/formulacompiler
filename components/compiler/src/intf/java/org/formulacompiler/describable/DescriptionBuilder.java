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
package org.formulacompiler.describable;

import java.io.IOException;
import java.util.Date;


/**
 * A specialized string builder that supports multiple lines with proper indentation. Geared towards
 * emitting <a href="http://yaml.org/">YAML</a>.
 * 
 * @author peo
 */
public class DescriptionBuilder implements Appendable
{
	public static final CharSequence DEFAULT_INDENT = "  ";

	private static final CharSequence VAL_SEP = ": ";
	private static final CharSequence LIST_SEP = ":";
	private static final CharSequence LIST_PREFIX = "- ";

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


	public final StringBuilder getIndentation()
	{
		return this.indentation;
	}


	public final DescriptionBuilder indent()
	{
		this.indentation.append( this.indent );
		return this;
	}


	public final DescriptionBuilder indent( int _n )
	{
		for (int i = _n; i > 0; i--)
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


	@Override
	public String toString()
	{
		return this.builder.toString();
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

	public final DescriptionBuilder append( Describable _desc ) throws IOException
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


	public final DescriptionBuilder ln( CharSequence _name )
	{
		return append( _name ).append( LIST_SEP );
	}

	public final <D extends Describable> DescriptionBuilder l( Iterable<D> _descs ) throws IOException
	{
		for (Describable desc : _descs) {
			onNewLine().append( LIST_PREFIX ).indent().append( desc ).outdent();
		}
		return this;
	}

	public final <D extends Describable> DescriptionBuilder lSpaced( Iterable<D> _descs ) throws IOException
	{
		for (Describable desc : _descs) {
			onNewLine().newLine().append( LIST_PREFIX ).indent().append( desc ).outdent();
		}
		return this;
	}


	public final DescriptionBuilder vn( CharSequence _name )
	{
		return append( _name ).append( VAL_SEP );
	}

	public final DescriptionBuilder nv( CharSequence _name, Object _value ) throws IOException
	{
		if (null != _value) {
			return vn( _name ).v( _value ).lf();
		}
		return this;
	}

	public final DescriptionBuilder nv( CharSequence _name, CharSequence _valuePrefix, Object _value ) throws IOException
	{
		if (null != _value) {
			return vn( _name ).append( _valuePrefix ).v( _value ).lf();
		}
		return this;
	}


	public final DescriptionBuilder v( Object _value ) throws IOException
	{
		if (null == _value) return appendNull();
		if (_value instanceof Describable) return v( (Describable) _value );
		if (_value instanceof String) return v( (String) _value );
		if (_value instanceof Date) return v( (Date) _value );
		if (_value instanceof Double) return v( ((Double) _value).doubleValue() );
		return append( _value );
	}

	public final DescriptionBuilder v( Describable _value ) throws IOException
	{
		return append( _value );
	}

	public final DescriptionBuilder v( String _value )
	{
		return append( _value );
	}

	public final DescriptionBuilder v( Date _value )
	{
		return append( "Date( " ).append( _value.getTime() ).append( " )" );
	}

	public final DescriptionBuilder v( double _value )
	{
		if (Double.isNaN( _value )) return append( ".NAN" );
		if (Double.isInfinite( _value )) return append( _value == Double.POSITIVE_INFINITY? ".Inf" : "-.Inf" );
		return append( _value );
	}

	public final DescriptionBuilder v( long _value )
	{
		return append( _value );
	}

	public final DescriptionBuilder v( int _value )
	{
		return append( _value );
	}

	public final DescriptionBuilder v( char _value )
	{
		return append( _value );
	}

	public final DescriptionBuilder v( boolean _value )
	{
		return append( _value );
	}


	public final DescriptionBuilder lf()
	{
		return newLine();
	}


	public final void ensureCapacity( int _minimumCapacity )
	{
		this.builder.ensureCapacity( _minimumCapacity );
	}


	public final int length()
	{
		return this.builder.length();
	}

}
