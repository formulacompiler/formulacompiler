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


public final class ExpressionNodeForMakeArray extends ExpressionNode
{

	protected ExpressionNodeForMakeArray()
	{
		super();
	}

	public ExpressionNodeForMakeArray(Collection _args)
	{
		super( _args );
	}

	public ExpressionNodeForMakeArray(ExpressionNode... _args)
	{
		super( _args );
	}


	@Override
	public ExpressionNode innerCloneWithoutArguments()
	{
		return new ExpressionNodeForMakeArray();
	}

	
	@Override
	protected int countValuesCore( LetDictionary _letDict, Collection<ExpressionNode> _uncountables )
	{
		return countArgumentValues( _letDict, _uncountables );
	}
	
	@Override
	protected int countValuesCore( Collection<ExpressionNode> _uncountables )
	{
		throw new AbstractMethodError();
	}

	
	@Override
	public void describeToWithConfig( DescriptionBuilder _to, ExpressionDescriptionConfig _cfg ) throws IOException
	{
		_to.append( "_MAKE_ARRAY " );
		describeArgumentListTo( _to, _cfg );
	}

}