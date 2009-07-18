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

package org.formulacompiler.compiler.internal.model.rewriting;

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.runtime.New;


public final class SubstitutionInliner extends AbstractComputationModelVisitor
{

	public SubstitutionInliner()
	{
		super();
	}


	@Override
	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		final ExpressionNode expr = _cell.getExpression();
		if (null != expr) {
			inline( expr );
		}
		return true;
	}


	private void inline( ExpressionNode _expr )
	{
		if (null != _expr) {
			_expr.replaceArguments( inline( _expr.arguments() ) );
		}
	}


	private List<ExpressionNode> inline( List<ExpressionNode> _list )
	{
		final List<ExpressionNode> result = New.list();
		for (final ExpressionNode node : _list) {
			inline( node, result );
		}
		return result;
	}


	private void inline( ExpressionNode _node, List<ExpressionNode> _result )
	{
		if (_node instanceof ExpressionNodeForSubstitution) {
			for (final ExpressionNode elt : _node.arguments()) {
				inline( elt, _result );
			}
		}
		else {
			inline( _node );
			_result.add( _node );
		}
	}


}
