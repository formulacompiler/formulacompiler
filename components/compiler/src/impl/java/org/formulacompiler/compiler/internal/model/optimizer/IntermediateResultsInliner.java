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
package org.formulacompiler.compiler.internal.model.optimizer;

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;


final public class IntermediateResultsInliner extends AbstractComputationModelVisitor
{


	@Override
	protected boolean visitModel( ComputationModel _model ) throws CompilerException
	{
		_model.traverse( new ReferenceCounter() );
		return true;
	}


	@Override
	protected boolean visitedModel( ComputationModel _model ) throws CompilerException
	{
		_model.traverse( new InlinedCellRemover() );
		return true;
	}


	@Override
	protected boolean visitCell( CellModel _cell )
	{
		if (!isInlineable( _cell )) {
			final ExpressionNode expr = _cell.getExpression();
			if (null != expr) {
				_cell.setExpression( inlineIntermediateResultsInto( expr ) );
			}
		}
		return true;
	}


	static boolean isInlineable( CellModel _cell )
	{
		return !_cell.isInput() && !_cell.isOutput() && !(_cell.getReferenceCount() > 1);
	}


	private ExpressionNode inlineIntermediateResultsInto( ExpressionNode _expr )
	{
		if (_expr == null) return null;

		ExpressionNode result = _expr;
		while (result instanceof ExpressionNodeForCellModel) {
			final ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) result;
			final CellModel cellModel = cellNode.getCellModel();
			if (!isInlineable( cellModel )) return result;
			result = cellModel.getExpression();
			if (null == result) {
				result = new ExpressionNodeForConstantValue( cellModel.getConstantValue(), cellModel.getDataType() );
			}
			else {
				cellModel.setExpression( null );
			}
		}
		inlineIntermediateResultsIntoArgsOf( result );
		return result;
	}


	private void inlineIntermediateResultsIntoArgsOf( ExpressionNode _expr )
	{
		if (null == _expr) {
			throw new IllegalArgumentException();
		}
		final List<ExpressionNode> args = _expr.arguments();
		if (args.size() > 0) {
			final ExpressionNode[] sourceArgs = args.toArray( new ExpressionNode[ args.size() ] );
			args.clear();
			for (ExpressionNode sourceArg : sourceArgs) {
				args.add( inlineIntermediateResultsInto( sourceArg ) );
			}
		}
	}


}
