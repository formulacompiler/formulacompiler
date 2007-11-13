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

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForParentSectionModel;

public final class ReferenceCounter extends AbstractComputationModelVisitor
{


	@Override
	protected boolean visitCell( CellModel _cell )
	{
		if (_cell.isOutput()) {
			reference( _cell, false );
		}
		return true;
	}


	void reference( CellModel _cell, boolean _makeSureIsCallable )
	{
		if (null == _cell) throw new IllegalArgumentException();
		boolean notAnalyzedYet = (0 == _cell.getReferenceCount());
		_cell.addReference();
		if (_makeSureIsCallable) _cell.addReference();
		if (notAnalyzedYet) {
			addRefToEverythingReferencedBy( _cell.getExpression(), false );
			addRefToEverythingReferencedBy( _cell.getCallChainToCall() );
		}
	}


	void addRefToEverythingReferencedBy( ExpressionNode _expr, boolean _accessedBySubBand )
	{
		if (_expr == null) {
			// nothing to do
		}
		else if (_expr instanceof ExpressionNodeForCellModel) {
			ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) _expr;
			CellModel cellModel = cellNode.getCellModel();
			if (null != cellModel) {
				reference( cellModel, _accessedBySubBand );
			}
		}
		else {
			final boolean argsBySubBand = _accessedBySubBand || (_expr instanceof ExpressionNodeForParentSectionModel);
			for (ExpressionNode arg : _expr.arguments()) {
				addRefToEverythingReferencedBy( arg, argsBySubBand );
			}
		}
	}


	private void addRefToEverythingReferencedBy( CallFrame _callChainToCall )
	{
		if (null == _callChainToCall) return;
		for (Object arg : _callChainToCall.getArgs()) {
			if (arg instanceof CellModel) {
				CellModel cell = (CellModel) arg;
				reference( cell, true );
			}
		}
		addRefToEverythingReferencedBy( _callChainToCall.getPrev() );
	}


}