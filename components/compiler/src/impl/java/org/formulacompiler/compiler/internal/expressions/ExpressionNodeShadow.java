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

package org.formulacompiler.compiler.internal.expressions;

import java.util.List;

import org.formulacompiler.runtime.New;


public abstract class ExpressionNodeShadow
{
	private final ExpressionNode node;
	private final List<ExpressionNodeShadow> arguments = New.list();

	public ExpressionNodeShadow( ExpressionNode _node )
	{
		super();
		this.node = _node;
	}

	public ExpressionNode node()
	{
		return this.node;
	}

	public List<ExpressionNodeShadow> arguments()
	{
		return this.arguments;
	}

	public static ExpressionNodeShadow shadow( ExpressionNode _node, Builder _builder )
	{
		if (_node == null) {
			return null;
		}
		else {
			final ExpressionNodeShadow result = _builder.shadow( _node );
			final List<ExpressionNodeShadow> resultArgs = result.arguments();
			for (ExpressionNode argNode : _node.arguments()) {
				resultArgs.add( shadow( argNode, _builder ) );
			}
			return result;
		}
	}

	public static interface Builder
	{
		ExpressionNodeShadow shadow( ExpressionNode _node );
	}

}
