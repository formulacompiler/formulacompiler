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

import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.model.util.InterpretedNumericType;

final class EvalFold1st extends EvalAbstractFold
{
	private static final Object NO_VALUE = new Object();
	
	private final String firstName;

	public EvalFold1st(ExpressionNodeForFold1st _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.firstName = _node.firstName();
	}


	@Override
	protected int evalFixedArgs( Object[] _args, int _i0 )
	{
		int i0 = super.evalFixedArgs( _args, _i0 );
		_args[ i0++ ] = node().argument( 2 ); // first
		return i0;
	}
	
	
	@Override
	protected Object initial( Object[] _args )
	{
		return NO_VALUE;
	}
	
	
	private boolean haveFirst = false;

	@Override
	protected Object fold( Object _acc, Object _val )
	{
		if (this.haveFirst) {
			return super.fold( _acc, _val );
		}
		else {
			this.haveFirst = true;
			letDict().let( this.firstName, null, _val );
			try {
				return evaluateArgument( 2 ); // first
			}
			finally {
				letDict().unlet( this.firstName );
			}
		}
	}


	@Override
	protected void insertPartialFold( Object _acc )
	{
		if (_acc != NO_VALUE) {
			node().addArgument( new ExpressionNodeForConstantValue( _acc ) );
		}
	}


}
