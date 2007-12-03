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
	private final Type type;
	private final String varName;

	public static enum Type {
		BYVAL, BYNAME, SYMBOLIC
	}

	public ExpressionNodeForLet( Type _type, String _varName )
	{
		super();
		this.varName = _varName;
		this.type = _type;
	}

	public ExpressionNodeForLet( Type _type, String _varName, ExpressionNode _value )
	{
		this( _type, _varName );
		addArgument( _value );
	}

	public ExpressionNodeForLet( Type _type, String _varName, ExpressionNode _value, ExpressionNode _in )
	{
		this( _type, _varName, _value );
		addArgument( _in );
	}

	public ExpressionNodeForLet( String _varName, ExpressionNode _value, ExpressionNode _in )
	{
		this( Type.BYVAL, _varName, _value, _in );
	}


	public Type type()
	{
		return this.type;
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
		return new ExpressionNodeForLet( this.type, this.varName );
	}


	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new IllegalStateException( "COUNT not supported over _LET" );
	}


	@Override
	protected void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "(let" ).append( type() == Type.BYVAL? "" : "/" + type().toString().toLowerCase() ).append( " " ).append( varName() ).append(
				" = " );
		value().describeTo( _to, _cfg );
		_to.append( " in " );
		in().describeTo( _to, _cfg );
		_to.append( " )" );
	}

}
