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
