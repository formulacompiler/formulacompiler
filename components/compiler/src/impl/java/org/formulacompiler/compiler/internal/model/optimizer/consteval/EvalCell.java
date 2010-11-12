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

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.spreadsheet.CellAddress;

public class EvalCell extends EvalShadow
{

	public EvalCell( ExpressionNodeForCellModel _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}

	@Override
	protected final TypedResult evaluateToConst( TypedResult... _args ) throws CompilerException
	{
		final CellModel cellModel = ((ExpressionNodeForCellModel) node()).getCellModel();
		if (null == cellModel) {
			return ConstResult.NULL;
		}
		if (cellModel.isInput()) {
			return node();
		}

		TypedResult evaluated = cellModel.getCachedResult();
		if (null == evaluated) {
			evaluated = doEvaluate( cellModel );
			cellModel.setCachedResult( evaluated );
		}

		if (evaluated instanceof ExpressionNodeForConstantValue) {
			return evaluated;
		}
		else if (evaluated instanceof ExpressionNode) {
			// Not constant, so return the cell as such.
			// But use the simplified expression.
			assert node().arguments().size() == 0;
			return node();
		}
		else {
			return evaluated;
		}
	}

	private TypedResult doEvaluate( CellModel cellModel ) throws CompilerException
	{
		final Object constantValue = cellModel.getConstantValue();
		if (null != constantValue) {
			if (constantValue instanceof Boolean) {
				boolean bool = ((Boolean) constantValue).booleanValue();
				return new ConstResult( type().adjustConstantValue( Double.valueOf( bool ? 1 : 0 ) ),
						cellModel.getDataType() );
			}
			return cellModel;
		}

		final ExpressionNode expression = cellModel.getExpression();
		if (null != expression) {
			final Object source = cellModel.getSource();
			final CellAddress cellAddress = source instanceof CellAddress ? (CellAddress) source : null;
			return EvalShadow.evaluate( expression, type(), cellAddress );
		}

		return ConstResult.NULL;
	}

}
