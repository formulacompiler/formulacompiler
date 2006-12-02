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

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.model.util.InterpretedNumericType;

final class EvalFold1st extends EvalAbstractFold
{
	private static final Object NO_VALUE = new Object();

	private final String firstName;
	private EvalShadow initialEval;

	public EvalFold1st(ExpressionNodeForFold1st _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.firstName = _node.firstName();
	}


	@Override
	protected int evalFixedArgs( Object[] _args, int _i0 ) throws CompilerException
	{
		int i0 = super.evalFixedArgs( _args, _i0 );

		// Temporarily undefine the names so they don't capture outer defs.
		letDict().let( this.firstName, null, EvalLetVar.UNDEF );
		try {
			_args[ i0++ ] = evaluateArgument( 2 ); // initial
		}
		finally {
			letDict().unlet( this.firstName );
		}

		this.initialEval = shadow( (ExpressionNode) _args[ i0 - 1 ], type() );
		return i0;
	}


	@Override
	protected Object initial( Object[] _args )
	{
		return NO_VALUE;
	}


	@Override
	protected Object foldOne( Object _acc, Object _val, Collection<ExpressionNode> _dynArgs ) throws CompilerException
	{
		if (_acc == NO_VALUE) {
			letDict().let( this.firstName, null, _val );
			try {
				final Object val = this.initialEval.evalIn( context() );
				if (isConstant( val )) {
					return val;
				}
				else {
					_dynArgs.add( (ExpressionNode) val );
					return _acc;
				}
			}
			finally {
				letDict().unlet( this.firstName );
			}
		}
		else {
			return super.foldOne( _acc, _val, _dynArgs );
		}
	}


	@Override
	protected ExpressionNode partialFold( Object _acc, boolean _accChanged, Object[] _args,
			Collection<ExpressionNode> _dynArgs )
	{
		if (_acc != NO_VALUE) {
			ExpressionNodeForFold result = new ExpressionNodeForFold( this.accName, new ExpressionNodeForConstantValue(
					_acc ), this.eltName, node().argument( 1 ).clone(), false );
			result.arguments().addAll( _dynArgs );
			return result;
		}
		else {
			ExpressionNodeForFold1st result = (ExpressionNodeForFold1st) node().cloneWithoutArguments();
			result.addArgument( valueToNode( _args[ 0 ] ) ); // empty
			result.addArgument( valueToNode( _args[ 1 ] ) ); // fold
			result.addArgument( valueToNode( _args[ 2 ] ) ); // initial
			result.arguments().addAll( _dynArgs );
			return result;
		}
	}

}
