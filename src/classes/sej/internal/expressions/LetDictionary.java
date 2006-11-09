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
package sej.internal.expressions;

import java.util.Stack;

import sej.internal.Settings;
import sej.internal.logging.Log;

public final class LetDictionary
{
	public static final Log LOG = Settings.LOG_LETVARS;

	private final Stack<LetEntry> stack = new Stack<LetEntry>();

	public LetDictionary()
	{
		super();
	}


	public final void clear()
	{
		this.stack.clear();
	}


	public final void let( String _name, DataType _type, Object _value )
	{
		this.stack.push( new LetEntry( _name, _type, _value ) );
		if (LOG.e()) LOG.a( "Letting " ).a( _name ).a( " = " ).a( _value ).lf().i();
	}

	public final void set( String _name, Object _value )
	{
		if (LOG.e()) LOG.o().a( "Reletting " ).a( _name ).a( " = " ).a( _value ).lf().i();

		for (int i = this.stack.size() - 1; i >= 0; i--) {
			final LetEntry entry = this.stack.get( i );
			if (_name.equals( entry.name )) {
				this.stack.set( i, new LetEntry( _name, entry.type, _value ) );
				return;
			}
		}
		throw new IllegalArgumentException( "Name not found - missing let?" );
	}

	public final void unlet( String _name )
	{
		final LetEntry was = this.stack.pop();
		if (was.name != _name) throw new IllegalArgumentException( "Name mismatch - unbalanced let/unlet?" );

		if (LOG.e()) LOG.o().a( "Unletting " ).a( _name ).lf();
	}


	public final Object lookup( String _name )
	{
		final LetEntry found = find( _name );
		return (null != found) ? found.value : null;
	}

	public DataType lookupType( String _name )
	{
		final LetEntry found = find( _name );
		return (null != found) ? found.type : null;
	}

	public final LetEntry find( String _name )
	{
		for (int i = this.stack.size() - 1; i >= 0; i--) {
			final LetEntry entry = this.stack.get( i );
			if (_name.equals( entry.name )) {
				return entry;
			}
		}
		return null;
	}


	public final Iterable<LetEntry> entries()
	{
		return this.stack;
	}


	public static final class LetEntry
	{
		public final String name;
		public final DataType type;
		public final Object value;

		public LetEntry(String _name, DataType _type, Object _value)
		{
			super();
			this.name = _name;
			this.type = _type;
			this.value = _value;
		}
	}


}
