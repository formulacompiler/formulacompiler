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

package org.formulacompiler.compiler.internal.expressions;

import java.util.Stack;

import org.formulacompiler.compiler.internal.Settings;
import org.formulacompiler.compiler.internal.logging.Log;


public final class LetDictionary<T>
{
	public static final Log LOG = Settings.LOG_LETVARS;

	private final Stack<LetEntry<T>> stack = new Stack<LetEntry<T>>();

	public LetDictionary()
	{
		super();
	}


	public final void clear()
	{
		this.stack.clear();
	}


	public final void let( String _name, DataType _type, T _value )
	{
		this.stack.push( new LetEntry<T>( _name, _type, _value ) );
		if (LOG.e()) LOG.a( "Letting " ).a( _name ).a( " = " ).a( _value ).lf().i();
	}

	public final void set( String _name, T _value )
	{
		if (LOG.e()) LOG.a( "Reletting " ).a( _name ).a( " = " ).a( _value ).lf();

		for (int i = this.stack.size() - 1; i >= 0; i--) {
			final LetEntry<T> entry = this.stack.get( i );
			if (_name.equals( entry.name )) {
				this.stack.set( i, new LetEntry<T>( _name, entry.type, _value ) );
				return;
			}
		}
		throw new IllegalArgumentException( "Name not found - missing let?" );
	}

	public final void unlet( String _name )
	{
		final LetEntry<T> was = this.stack.pop();
		if (was.name != _name) throw new IllegalArgumentException( "Name mismatch - unbalanced let/unlet?" );

		if (LOG.e()) LOG.o().a( "Unletting " ).a( _name ).lf();
	}

	public final void unlet( int _numberOfUnlets )
	{
		for (int i = 0; i < _numberOfUnlets; i++) {
			final LetEntry<T> was = this.stack.pop();
			if (LOG.e()) LOG.o().a( "Unletting " ).a( was.name ).lf();
		}
	}


	public final T lookup( String _name )
	{
		final LetEntry<T> found = find( _name );
		return (null != found) ? found.value : null;
	}

	public DataType lookupType( String _name )
	{
		final LetEntry<T> found = find( _name );
		return (null != found) ? found.type : null;
	}

	public final LetEntry<T> find( String _name )
	{
		for (int i = this.stack.size() - 1; i >= 0; i--) {
			final LetEntry<T> entry = this.stack.get( i );
			if (_name.equals( entry.name )) {
				return entry;
			}
		}
		return null;
	}


	public static final class LetEntry<T>
	{
		public final String name;
		public final DataType type;
		public final T value;

		public LetEntry( String _name, DataType _type, T _value )
		{
			super();
			this.name = _name;
			this.type = _type;
			this.value = _value;
		}

	}


}
