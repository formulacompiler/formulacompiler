/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
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
	protected void traverse( TypedResult[] _args, int _firstFoldedArg ) throws CompilerException
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
		final TypedResult[] elts = new TypedResult[ _vectors.length ];
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
		ExpressionNode[] firstElts = _dynArgs.iterator().next();
		for (int i = 0; i < eltCount; i++) {
			vectors[ i ] = new ExpressionNodeForArrayReference( desc );
			vectors[ i ].setDataType( firstElts[ i ].getDataType() );
			_apply.addArgument( vectors[ i ] );
		}
		for (ExpressionNode[] elts : _dynArgs) {
			assert elts.length == eltCount;
			for (int i = 0; i < eltCount; i++)
				vectors[ i ].addArgument( elts[ i ] );
		}
	}

}
