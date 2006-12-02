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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import sej.describable.DescriptionBuilder;

public final class ArrayValue extends ArrayDescriptor implements Iterable<Object>
{
	private final List<Object> values = new ArrayList<Object>();


	public ArrayValue(ArrayDescriptor _template)
	{
		super( _template );
	}


	public void add( Object _value )
	{
		this.values.add( _value );
	}


	public int size()
	{
		return this.values.size();
	}


	public Object get( int _index )
	{
		return this.values.get( _index );
	}


	public Iterator<Object> iterator()
	{
		return this.values.iterator();
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		super.describeTo( _to );
		_to.append( '{' );
		boolean isFirst = true;
		for (Object val : this.values) {
			if (isFirst) isFirst = false;
			else _to.append( ", " );
			_to.append( val.toString() );
		}
		_to.append( '}' );
	}

}
