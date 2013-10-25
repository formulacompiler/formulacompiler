/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.compiler.internal.model.analysis;

import java.util.LinkedHashSet;
import java.util.Set;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;

public class CircularReferencesChecker extends AbstractComputationModelVisitor
{
	@Override
	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		final ExpressionNode expression = _cell.getExpression();
		if (expression != null) {
			final Object source = _cell.getSource();
			final Set<Object> path = new LinkedHashSet<Object>();
			path.add( source );
			checkExpression( expression, source, path );
		}
		return true;
	}

	private void checkExpression( ExpressionNode _expression, Object _referencedBy, Set<Object> _path ) throws CompilerException
	{
		if (_expression instanceof ExpressionNodeForCellModel) {
			final ExpressionNodeForCellModel cellModelExpr = (ExpressionNodeForCellModel) _expression;
			final CellModel cellModel = cellModelExpr.getCellModel();
			if (cellModel != null) {
				final Object source = cellModel.getSource();
				if (source.equals( _referencedBy )) {
					throw new CompilerException.CyclicReferenceException( _path );
				}
				final ExpressionNode expression = cellModel.getExpression();
				if (expression != null && !_path.contains( source )) {
					final Set<Object> newPath = new LinkedHashSet<Object>( _path );
					newPath.add( source );
					checkExpression( expression, _referencedBy, newPath );
				}
			}
		}
		for (ExpressionNode arg : _expression.arguments()) {
			if (arg != null) {
				checkExpression( arg, _referencedBy, _path );
			}
		}
	}
}
