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

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;


abstract class AbstractExpressionRewriter
{
	private final NameSanitizer sanitizer;

	public AbstractExpressionRewriter( NameSanitizer _sanitizer )
	{
		super();
		this.sanitizer = _sanitizer;
	}


	protected final NameSanitizer sanitizer()
	{
		return this.sanitizer;
	}

	protected final String newSanitizingSuffix()
	{
		return this.sanitizer.newSanitizingSuffix();
	}


	protected static final Object NOT_CONST = new Object();

	protected final Object constantValueOf( ExpressionNode _node )
	{
		if (_node instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue constNode = (ExpressionNodeForConstantValue) _node;
			return constNode.value();
		}
		else if (_node instanceof ExpressionNodeForCellModel) {
			final ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) _node;
			final CellModel cell = cellNode.getCellModel();
			if (null == cell) {
				return null;
			}
			else if (!cell.isInput()) {
				if (null == cell.getExpression()) {
					return cell.getConstantValue();
				}
				else {
					return constantValueOf( cell.getExpression() );
				}
			}
		}
		return NOT_CONST;
	}

	protected final ExpressionNode expressionOf( ExpressionNode _node )
	{
		if (_node instanceof ExpressionNodeForCellModel) {
			final ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) _node;
			final CellModel cell = cellNode.getCellModel();
			if (null != cell && !cell.isInput() && null != cell.getExpression()) {
				return expressionOf( cell.getExpression() );
			}
		}
		return _node;
	}


}
