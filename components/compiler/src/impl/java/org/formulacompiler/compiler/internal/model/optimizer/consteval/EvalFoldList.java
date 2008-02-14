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
