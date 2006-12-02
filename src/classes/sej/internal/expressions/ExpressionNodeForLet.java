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
import java.util.Collection;

import sej.describable.DescriptionBuilder;

public final class ExpressionNodeForLet extends ExpressionNode
{
	private final String varName;

	public ExpressionNodeForLet(String _varName, ExpressionNode _value, ExpressionNode _in)
	{
		super( _value, _in );
		this.varName = _varName;
	}

	private ExpressionNodeForLet(String _varName)
	{
		super();
		this.varName = _varName;
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


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForLet( this.varName );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new IllegalStateException( "COUNT not supported over _LET" );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_LET( " ).append( varName() ).append( ": " );
		value().describeTo( _to, _cfg );
		_to.append( "; " );
		in().describeTo( _to, _cfg );
		_to.append( " )" );
	}

}
