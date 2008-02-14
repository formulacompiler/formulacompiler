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