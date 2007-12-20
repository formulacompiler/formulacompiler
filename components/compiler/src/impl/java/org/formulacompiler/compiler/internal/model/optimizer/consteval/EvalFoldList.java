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
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


final class EvalFoldList extends EvalFoldApply
{

	public EvalFoldList( ExpressionNodeForFoldList _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected void traverse( TypedResult[] _args, int _firstFoldedArg ) throws CompilerException
	{
		for (int i = _args.length - 1; i >= _firstFoldedArg; i--) {
			final TypedResult xi = _args[ i ];
			if (isNesting( xi )) {
				traverse( ((ExpressionNode) xi).arguments() );
			}
			else {
				traverseElements( xi );
			}
		}
	}

	private void traverse( Iterable<ExpressionNode> _nodes ) throws CompilerException
	{
		for (final ExpressionNode node : _nodes) {
			if (isNesting( node )) {
				traverse( node.arguments() );
			}
			else {
				traverseElements( node );
			}
		}
	}

	private boolean isNesting( TypedResult _elt )
	{
		return _elt instanceof ExpressionNodeForSubstitution || _elt instanceof ExpressionNodeForArrayReference;
	}


	@Override
	protected void addDynamicArgsToPartialFold( ExpressionNode _apply, Collection<ExpressionNode[]> _dynArgs )
	{
		for (ExpressionNode[] elts : _dynArgs) {
			assert elts.length == 1;
			_apply.addArgument( elts[ 0 ] );
		}
	}

}
