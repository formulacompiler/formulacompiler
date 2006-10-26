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

public final class LetDictionary<V>
{
	private final Stack<LetEntry<V>> stack = new Stack<LetEntry<V>>();

	public LetDictionary()
	{
		super();
	}


	public final void clear()
	{
		this.stack.clear();
	}


	public final void let( String _name, V _value )
	{
		this.stack.push( new LetEntry<V>( _name, _value ) );
	}

	public final void set( String _name, V _value )
	{
		for (int i = this.stack.size() - 1; i >= 0; i--) {
			final LetEntry<V> entry = this.stack.get( i );
			if (_name.equals( entry.name )) {
				this.stack.set( i, new LetEntry<V>( _name, _value ) );
				return;
			}
		}
		throw new IllegalArgumentException( "Name not found - missing let?" );
	}

	public final void unlet( String _name )
	{
		final LetEntry was = this.stack.pop();
		if (was.name != _name) throw new IllegalArgumentException( "Name mismatch - unbalanced let/unlet?" );
	}


	public final V lookup( String _name )
	{
		for (int i = this.stack.size() - 1; i >= 0; i--) {
			final LetEntry<V> entry = this.stack.get( i );
			if (_name.equals( entry.name )) {
				return entry.value;
			}
		}
		return null;
	}
	
	
	public final Iterable<LetEntry<V>> entries()
	{
		return this.stack;
	}


	public static final class LetEntry<V>
	{
		public final String name;
		public final V value;

		public LetEntry(String _name, V _value)
		{
			super();
			this.name = _name;
			this.value = _value;
		}
	}

}
