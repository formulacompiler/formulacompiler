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
package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForAbstractFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;


abstract class EvalAbstractFold extends EvalShadow
{
	protected final String accName;
	protected final String eltName;
	protected EvalShadow foldEval;

	public EvalAbstractFold(ExpressionNodeForAbstractFold _node, InterpretedNumericType _type)
	{
		super( _node, _type );
		this.accName = _node.accumulatorName();
		this.eltName = _node.elementName();
	}


	@Override
	protected final Object eval() throws CompilerException
	{
		final Object[] args = new Object[ node().arguments().size() ];
		final int i0 = evalFixedArgs( args, 0 );
		for (int i = i0; i < args.length; i++) {
			args[ i ] = evaluateArgument( i );
		}
		return evaluateToConstOrExprWithConstantArgsFixed( args, i0 );
	}


	protected Object evaluateToConstOrExprWithConstantArgsFixed( Object[] _args, int _firstFoldedArg )
			throws CompilerException
	{
		final Collection<ExpressionNode> dynArgs = New.collection();
		final Object initialAcc = initial( _args );
		Object acc = initialAcc;
		if (isConstant( acc )) {
			for (int i = _args.length - 1; i >= _firstFoldedArg; i--) {
				final Object xi = _args[ i ];
				if (xi instanceof ExpressionNodeForSubstitution) {
					acc = foldMany( acc, ((ExpressionNodeForSubstitution) xi).arguments(), dynArgs );
				}
				else if (isConstant( xi )) {
					acc = foldOne( acc, xi, dynArgs );
				}
				else {
					dynArgs.add( (ExpressionNode) xi );
				}
			}
			if (dynArgs.size() == 0) {
				return acc;
			}
			else {
				final boolean sameAcc = (acc == initialAcc) || (acc != null && acc.equals( initialAcc ));
				return partialFold( acc, !sameAcc, _args, dynArgs );
			}
		}
		else {
			return evaluateToNode( _args );
		}
	}


	protected int evalFixedArgs( final Object[] _args, final int _i0 ) throws CompilerException
	{
		int i0 = _i0;
		_args[ i0++ ] = evaluateArgument( 0 ); // initial
		_args[ i0++ ] = evalFoldingStep( _args );
		this.foldEval = shadow( (ExpressionNode) _args[ i0 - 1 ], type() );
		return i0;
	}


	protected Object evalFoldingStep( final Object[] _args ) throws CompilerException
	{
		// Temporarily undefine the names so they don't capture outer defs.
		letDict().let( this.accName, null, EvalLetVar.UNDEF );
		letDict().let( this.eltName, null, EvalLetVar.UNDEF );
		try {
			return evaluateArgument( 1 ); // fold
		}
		finally {
			letDict().unlet( this.eltName );
			letDict().unlet( this.accName );
		}
	}


	protected Object initial( final Object[] _args )
	{
		return _args[ 0 ];
	}


	private final Object foldMany( final Object _acc, final Iterable<ExpressionNode> _nodes,
			Collection<ExpressionNode> _dynArgs ) throws CompilerException
	{
		Object acc = _acc;
		for (final ExpressionNode node : _nodes) {
			if (node instanceof ExpressionNodeForConstantValue) {
				final ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) node;
				acc = foldOne( acc, constNode.value(), _dynArgs );
			}
			else if (node instanceof ExpressionNodeForSubstitution) {
				acc = foldMany( acc, node.arguments(), _dynArgs );
			}
			else {
				_dynArgs.add( node );
			}
		}
		return acc;
	}


	protected Object foldOne( final Object _acc, final Object _val, Collection<ExpressionNode> _dynArgs )
			throws CompilerException
	{
		letDict().let( this.accName, null, _acc );
		letDict().let( this.eltName, null, _val );
		try {
			final Object newAcc = this.foldEval.evalIn( context() );
			if (isConstant( newAcc )) {
				return newAcc;
			}
			else {
				_dynArgs.add( new ExpressionNodeForConstantValue( _val ) );
				return _acc;
			}
		}
		finally {
			letDict().unlet( this.eltName );
			letDict().unlet( this.accName );
		}
	}


	protected abstract ExpressionNode partialFold( Object _acc, boolean _accChanged, Object[] _args,
			Collection<ExpressionNode> _dynArgs );


	@Override
	protected Object evaluateToConst( Object... _args )
	{
		throw new IllegalStateException( "EvalFold.evaluateToConst() should never be called" );
	}

}
