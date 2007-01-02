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

import sej.internal.expressions.ExpressionNodeForLetVar;
import sej.internal.model.util.InterpretedNumericType;

final class EvalLetVar extends EvalShadow
{
	static final Object UNDEF = new Object();

	private final String varName;

	public EvalLetVar(ExpressionNodeForLetVar _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.varName = _node.varName();
	}


	@Override
	protected Object eval()
	{
		final Object val = letDict().lookup( this.varName );
		if (val == UNDEF) {
			if (LOG.e()) LOG.a( "Lookup " ).a( this.varName ).a( " is undefined. " ).lf();
			return node(); // No need to clone leaf node.
		}
		else if (isConstant( val )) {
			if (LOG.e()) LOG.a( "Lookup " ).a( this.varName ).a( " <- " ).a( val ).lf();
			return val;
		}
		else {
			if (LOG.e()) LOG.a( "Lookup " ).a( this.varName ).a( " = " ).a( val ).lf();
			return node(); // No need to clone leaf node.
		}
	}

	@Override
	protected Object evaluateToConst( Object... _args )
	{
		throw new IllegalStateException( "EvalLetVar.evaluateToConst() should never be called" );
	}

}
