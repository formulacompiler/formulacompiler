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

import java.util.Collection;

import sej.compiler.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForFoldArray;
import sej.internal.model.interpreter.InterpretedNumericType;

final class EvalFoldArray extends EvalAbstractFold
{
	private final String idxName;


	public EvalFoldArray(ExpressionNodeForFoldArray _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.idxName = _node.indexName();
	}


	@Override
	protected Object evalFoldingStep( Object[] _args ) throws CompilerException
	{
		letDict().let( this.idxName, null, EvalLetVar.UNDEF );
		try {
			return super.evalFoldingStep( _args );
		}
		finally {
			letDict().unlet( this.idxName );
		}
	}


	@Override
	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args, int _firstFoldedArg )
			throws CompilerException
	{
		if ((isConstant( _args[ 0 ] ) && isConstant( _args[ 2 ] ))
				&& _args[ 1 ] instanceof ExpressionNode && _args[ 2 ] instanceof ExpressionNodeForArrayReference) {
			final Object result = fold( _args[ 0 ], (ExpressionNode) _args[ 1 ],
					(ExpressionNodeForArrayReference) _args[ 2 ] );
			if (result != null) {
				return result;
			}
		}
		return evaluateToNode( _args );
	}


	private final Object fold( Object _initial, ExpressionNode _step, ExpressionNodeForArrayReference _array )
			throws CompilerException
	{
		Object result = _initial;
		int i = 1;
		for (ExpressionNode ai : _array.arguments()) {

			letDict().let( this.accName, null, result );
			letDict().let( this.eltName, null, valueOf( ai ) );
			letDict().let( this.idxName, null, i++ );
			try {
				result = this.foldEval.evalIn( context() );
				if (result instanceof ExpressionNode) return null; // bail out
			}
			finally {
				letDict().unlet( this.idxName );
				letDict().unlet( this.eltName );
				letDict().unlet( this.accName );
			}

		}
		return result;
	}


	@Override
	protected ExpressionNode partialFold( Object _acc, boolean _accChanged, Object[] _args,
			Collection<ExpressionNode> _dynArgs )
	{
		throw new AbstractMethodError( "EvalFoldArray.partialFold" );
	}

}
