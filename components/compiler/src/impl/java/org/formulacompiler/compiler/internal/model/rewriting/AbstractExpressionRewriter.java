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

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldArray;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForReduce;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.runtime.New;


abstract class AbstractExpressionRewriter
{
	private int nextSanitizingId = 1;


	protected final String newSanitizingSuffix()
	{
		return String.valueOf( this.nextSanitizingId++ );
	}


	protected final ExpressionNode substitution( ExpressionNode _expr )
	{
		if (_expr instanceof ExpressionNodeForSubstitution) {
			return _expr;
		}
		return new ExpressionNodeForSubstitution( _expr );
	}

	protected final ExpressionNode substitution( Collection<ExpressionNode> _exprs )
	{
		if (_exprs.size() == 1) {
			return substitution( _exprs.iterator().next() );
		}
		return new ExpressionNodeForSubstitution( _exprs );
	}

	protected final ExpressionNode substitution( Iterator<ExpressionNode> _exprs )
	{
		Collection<ExpressionNode> coll = New.collection();
		while (_exprs.hasNext())
			coll.add( _exprs.next() );
		return substitution( coll );
	}


	protected final ExpressionNodeForOperator op( Operator _op, String _a, String _b )
	{
		return new ExpressionNodeForOperator( _op, var( _a ), var( _b ) );
	}


	protected final ExpressionNodeForOperator op( Operator _op, ExpressionNode... _args )
	{
		return new ExpressionNodeForOperator( _op, _args );
	}


	protected final ExpressionNodeForFunction fun( Function _fun, ExpressionNode... _args )
	{
		return new ExpressionNodeForFunction( _fun, _args );
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


	protected final ExpressionNode let( String _n, ExpressionNode _value, ExpressionNode _in )
	{
		return new ExpressionNodeForLet( _n, _value, _in );
	}

	protected final ExpressionNode symbolicLet( String _n, ExpressionNode _value, ExpressionNode _in )
	{
		final ExpressionNodeForLet let = new ExpressionNodeForLet( _n, _value, _in );
		let.setSymbolic( true );
		return let;
	}


	protected final ExpressionNodeForLetVar var( String _a )
	{
		return new ExpressionNodeForLetVar( _a );
	}


	protected final ExpressionNodeForConstantValue cst( Object _value )
	{
		return new ExpressionNodeForConstantValue( _value );
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
