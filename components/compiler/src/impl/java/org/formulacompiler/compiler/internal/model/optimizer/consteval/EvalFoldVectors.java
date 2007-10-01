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
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


@SuppressWarnings( { "unqualified-field-access" } )
final class EvalFoldVectors extends EvalFoldApply
{

	public EvalFoldVectors( ExpressionNodeForFoldVectors _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@SuppressWarnings( "unchecked" )
	@Override
	protected void traverse( Object[] _args, int _firstFoldedArg ) throws CompilerException
	{
		final int eltCount = fold.eltCount();
		final Iterator<ExpressionNode>[] vectors = new Iterator[ eltCount ];
		for (int i = 0; i < eltCount; i++) {
			vectors[ i ] = ((ExpressionNode) _args[ i + _firstFoldedArg ]).arguments().iterator();
		}
		traverse( vectors );
	}

	private void traverse( Iterator<ExpressionNode>[] _vectors ) throws CompilerException
	{
		final Object[] elts = new Object[ _vectors.length ];
		while (_vectors[ 0 ].hasNext()) {
			for (int i = 0; i < _vectors.length; i++)
				elts[ i ] = _vectors[ i ].next();
			traverseElements( elts );
		}
	}


	@Override
	protected void addDynamicArgsToPartialFold( ExpressionNode _apply, Collection<ExpressionNode[]> _dynArgs )
	{
		final int eltCount = fold.eltCount();
		final ArrayDescriptor desc = new ArrayDescriptor( 1, _dynArgs.size(), 1 );
		final ExpressionNode[] vectors = new ExpressionNode[ eltCount ];
		for (int i = 0; i < eltCount; i++) {
			vectors[ i ] = new ExpressionNodeForArrayReference( desc );
			_apply.addArgument( vectors[ i ] );
		}
		for (ExpressionNode[] elts : _dynArgs) {
			assert elts.length == eltCount;
			for (int i = 0; i < eltCount; i++)
				vectors[ i ].addArgument( elts[ i ] );
		}
	}

}
