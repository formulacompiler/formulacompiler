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

package org.formulacompiler.compiler.internal.model;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLogging;

/**
 * @author Vladimir Korenev
 */
public class LoggingVisitor extends AbstractComputationModelVisitor
{
	@Override
	protected boolean visitCell( final CellModel _cell ) throws CompilerException
	{
		final ExpressionNode expr = _cell.getExpression();
		if (expr != null) {
			final Object source = _cell.getSource();
			final String definedName = _cell.getName();
			final boolean input = _cell.isInput();
			final boolean output = _cell.isOutput();
			final ExpressionNodeForLogging loggingExpr = new ExpressionNodeForLogging( expr, source, definedName, input, output );
			_cell.setExpression( loggingExpr );
		}
		return true;
	}
}
