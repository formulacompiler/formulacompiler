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
package sej.engine.expressions;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

import sej.describable.DescriptionBuilder;

public class ExpressionNodeForFunction extends ExpressionNode
{

	private Function function = null;


	public ExpressionNodeForFunction(Function _function, ExpressionNode... _args)
	{
		super( _args );
		setFunction( _function );
	}


	public ExpressionNodeForFunction(Function _function, Collection _args)
	{
		super( _args );
		setFunction( _function );
	}


	public Function getFunction()
	{
		return this.function;
	}


	public void setFunction( Function _function )
	{
		this.function = _function;
	}


	@Override
	public Object evaluate( EvaluationContext _context ) throws EvaluationFailed, InvocationTargetException
	{
		switch (getArguments().size()) {

		case 0:
			return this.function.evaluate();
		case 1:
			return this.function.evaluate( evaluateArgument( _context, 0 ) );
		case 2:
			return this.function.evaluate( evaluateArgument( _context, 0 ), evaluateArgument(
					_context, 1 ) );
		case 3:
			return this.function.evaluate( evaluateArgument( _context, 0 ), evaluateArgument(
					_context, 1 ), evaluateArgument( _context, 2 ) );

		default:
			Object[] vals = new Object[ getArguments().size() ];
			int i = 0;
			for (ExpressionNode arg : getArguments()) {
				vals[ i++ ] = arg.evaluate( _context );
			}
			return this.function.evaluate( vals );

		}

	}


	@Override
	public ExpressionNode cloneWithoutArguments()
	{
		return new ExpressionNodeForFunction( this.function );
	}


	@Override
	public void describeTo( DescriptionBuilder _to ) throws IOException
	{
		_to.append( this.function.getName() );
		describeArgumentListTo( _to );
	}

}
