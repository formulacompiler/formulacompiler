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
package org.formulacompiler.compiler.internal.expressions;

import java.io.IOException;
import java.util.Collection;

import org.formulacompiler.describable.DescriptionBuilder;


public final class ExpressionNodeForLet extends ExpressionNode
{
	private final String varName;
	private final boolean mayFold;
	private boolean symbolic;
	private boolean shouldCache = true;
	

	public ExpressionNodeForLet(String _varName, ExpressionNode _value, ExpressionNode _in)
	{
		super( _value, _in );
		this.varName = _varName;
		this.mayFold = true;
	}

	public ExpressionNodeForLet(String _varName, boolean _mayFold, ExpressionNode _value)
	{
		super( _value );
		this.varName = _varName;
		this.mayFold = _mayFold;
	}

	private ExpressionNodeForLet(String _varName, boolean _mayFold, boolean _symbolic, boolean _shouldCache)
	{
		super();
		this.varName = _varName;
		this.mayFold = _mayFold;
		this.symbolic = _symbolic;
		this.shouldCache = _shouldCache;
	}


	public final String varName()
	{
		return this.varName;
	}

	public final ExpressionNode value()
	{
		return argument( 0 );
	}

	public final ExpressionNode in()
	{
		return argument( 1 );
	}

	public final boolean mayFold()
	{
		return this.mayFold;
	}
	
	public boolean isSymbolic()
	{
		return this.symbolic;
	}
	
	public void setSymbolic( boolean _symbolic )
	{
		this.symbolic = _symbolic;
	}
	
	public boolean shouldCache()
	{
		return this.shouldCache;
	}
	
	public void setShouldCache( boolean _shouldCache )
	{
		this.shouldCache = _shouldCache;
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForLet( this.varName, this.mayFold, this.symbolic, this.shouldCache );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new IllegalStateException( "COUNT not supported over _LET" );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( mayFold()? "_LET( " : "_LET_nofold( " ).append( varName() ).append( ": " );
		value().describeTo( _to, _cfg );
		_to.append( "; " );
		in().describeTo( _to, _cfg );
		_to.append( " )" );
	}

}
