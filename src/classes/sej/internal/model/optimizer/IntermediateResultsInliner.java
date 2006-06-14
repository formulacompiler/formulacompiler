/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.model.optimizer;

import java.util.List;

import sej.CompilerError;
import sej.expressions.ExpressionNode;
import sej.expressions.ExpressionNodeForConstantValue;
import sej.internal.model.AbstractComputationModelVisitor;
import sej.internal.model.CellModel;
import sej.internal.model.ComputationModel;
import sej.internal.model.ExpressionNodeForCellModel;

final public class IntermediateResultsInliner extends AbstractComputationModelVisitor
{


	@Override
	public boolean visit( ComputationModel _model ) throws CompilerError
	{
		_model.traverse( new ReferenceCounter() );
		return true;
	}
	
	
	@Override
	public boolean visited( ComputationModel _model ) throws CompilerError
	{
		_model.traverse( new InlinedCellRemover() );
		return true;
	}


	@Override
	public boolean visit( CellModel _cell )
	{
		if (!isInlineable( _cell )) {
			ExpressionNode expr = _cell.getExpression();
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
		assert null != _expr;
		ExpressionNode result = _expr;
		while (result instanceof ExpressionNodeForCellModel) {
			ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) result;
			CellModel cellModel = cellNode.getCellModel();
			if (!isInlineable( cellModel )) return result;
			result = cellModel.getExpression();
			if (null == result) {
				result = new ExpressionNodeForConstantValue( cellModel.getConstantValue() );
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
		List<ExpressionNode> args = _expr.getArguments();
		if (args.size() > 0) {
			ExpressionNode[] sourceArgs = args.toArray( new ExpressionNode[ args.size() ] );
			args.clear();
			for (ExpressionNode sourceArg : sourceArgs) {
				args.add( inlineIntermediateResultsInto( sourceArg ) );
			}
		}
	}


}
