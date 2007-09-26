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
package org.formulacompiler.compiler.internal.model.rewriting;

import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;


abstract class AbstractExpressionRewriter
{
	private int nextSanitizingId = 1;


	protected final String newSanitizingSuffix()
	{
		return String.valueOf( this.nextSanitizingId++ );
	}


	protected final ExpressionNode fold( String _acc, ExpressionNode _init, String _x, ExpressionNode _fold,
			boolean _canInlineFirst, ExpressionNode _xs )
	{
		return new ExpressionNodeForFold( _acc, _init, _x, _fold, _canInlineFirst, _xs );
	}


	protected final ExpressionNode reduce( String _acc, String _xi, ExpressionNode _reduce, ExpressionNode _empty,
			ExpressionNode _xs )
	{
		return new ExpressionNodeForReduce( _acc, _xi, _reduce, _empty, _xs );
	}


	protected final ExpressionNode folda( String _acc, ExpressionNode _init, String _x, String _i, ExpressionNode _fold,
			ExpressionNode _xs )
	{
		return new ExpressionNodeForFoldArray( _acc, _init, _x, _i, _fold, _xs );
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
