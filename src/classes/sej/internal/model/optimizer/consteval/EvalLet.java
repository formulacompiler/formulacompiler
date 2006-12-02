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
package sej.internal.model.optimizer.consteval;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.model.util.InterpretedNumericType;

final class EvalLet extends EvalShadow
{
	private final String varName;

	public EvalLet(ExpressionNodeForLet _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.varName = _node.varName();
	}


	@Override
	protected Object eval() throws CompilerException
	{
		final Object val = evaluateArgument( 0 );
		letDict().let( this.varName, null, val );
		try {
			final Object result = evaluateArgument( 1 );
			if (isConstant( result )) {
				return result;
			}
			return evaluateToNode( new Object[] { val, result } );
		}
		finally {
			letDict().unlet( this.varName );
		}
	}

	@Override
	protected Object evaluateToConst( Object[] _args )
	{
		throw new IllegalStateException( "EvalLet.evaluateToConst() should never be called" );
	}

}
