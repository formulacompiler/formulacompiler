/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.engine.compiler.model.optimizer;

import sej.engine.compiler.model.AbstractEngineModelVisitor;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.ExpressionNodeForCellModel;
import sej.engine.compiler.model.ExpressionNodeForParentSectionModel;
import sej.engine.expressions.ExpressionNode;

public final class ReferenceCounter extends AbstractEngineModelVisitor
{


	@Override
	public boolean visit( CellModel _cell )
	{
		if (_cell.isOutput()) {
			reference( _cell );
		}
		return true;
	}


	void reference( CellModel _cell )
	{
		if (null == _cell) throw new IllegalArgumentException();
		boolean notAnalyzedYet = (0 == _cell.getReferenceCount());
		_cell.addReference();
		if (notAnalyzedYet) {
			ExpressionNode expr = _cell.getExpression();
			if (null != expr) {
				addRefToEverythingReferencedBy( expr, false );
			}
		}
	}


	void addRefToEverythingReferencedBy( ExpressionNode _expr, boolean _accessedBySubBand )
	{
		if (_expr instanceof ExpressionNodeForCellModel) {
			ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) _expr;
			CellModel cellModel = cellNode.getCellModel();
			if (null != cellModel) {
				reference( cellModel );
				if (_accessedBySubBand) cellModel.addReference();
			}
		}
		else {
			if (_expr instanceof ExpressionNodeForParentSectionModel) {
				_accessedBySubBand = true;
			}
			for (ExpressionNode arg : _expr.getArguments()) {
				addRefToEverythingReferencedBy( arg, _accessedBySubBand );
			}
		}
	}
}