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

import java.util.Collection;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCount;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.interpreter.EvalNotPossibleException;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.interpreter.InterpreterException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.spreadsheet.CellAddress;


public class EvalFunction extends EvalShadow<ExpressionNodeForFunction>
{

	EvalFunction( ExpressionNodeForFunction _node, InterpretedNumericType _type )
	{
		super( _node, _type );
	}


	@Override
	protected TypedResult eval() throws CompilerException
	{
		final Function function = node().getFunction();
		switch (function) {

			case AND:
				return evalBooleanSequence( false );

			case OR:
				return evalBooleanSequence( true );

			case COUNT: {
				final Collection<ExpressionNode> uncountables = New.collection();
				final int staticValueCount = node().countArgumentValues( letDict(), uncountables );
				final int subCount = uncountables.size();
				if (subCount == 0) {
					return new ConstResult( staticValueCount, DataType.NUMERIC );
				}
				else {
					final SectionModel[] subs = new SectionModel[ subCount ];
					final int[] subCounts = new int[ subCount ];
					int i = 0;
					for (ExpressionNode uncountable : uncountables) {
						final ExpressionNodeForSubSectionModel sub = (ExpressionNodeForSubSectionModel) uncountable;
						subs[ i ] = sub.getSectionModel();
						final Collection<ExpressionNode> subUncountables = New.collection();
						subCounts[ i ] = sub.countArgumentValues( letDict(), subUncountables );
						if (subUncountables.size() > 0) {
							throw new CompilerException.UnsupportedExpression( "COUNT of nested sections not supported" );
						}
						i++;
					}
					final ExpressionNodeForCount res = new ExpressionNodeForCount( staticValueCount, subs, subCounts );
					res.setDataType( DataType.NUMERIC );
					return res;
				}
			}

			case COLUMN: {
				final CellAddress cellAddress = getCellAddress();
				return new ConstResult( cellAddress.getColumnIndex() + 1, DataType.NUMERIC );
			}

			case ROW: {
				final CellAddress cellAddress = getCellAddress();
				return new ConstResult( cellAddress.getRowIndex() + 1, DataType.NUMERIC );
			}

			default:
				return super.eval();

		}
	}


	private final TypedResult evalBooleanSequence( boolean _returnThisIfFound ) throws CompilerException
	{
		final InterpretedNumericType type = type();
		final Collection<ExpressionNode> dynArgs = New.collection();
		final int n = cardinality();
		for (int i = 0; i < n; i++) {
			final TypedResult arg = evaluateArgument( i );
			if (arg.hasConstantValue()) {
				final boolean value = type.toBoolean( arg.getConstantValue() );
				if (value == _returnThisIfFound) {
					return ConstResult.valueOf( _returnThisIfFound );
				}
			}
			else {
				dynArgs.add( (ExpressionNode) arg );
			}
		}
		if (dynArgs.size() > 0) {
			final ExpressionNode result = node().cloneWithoutArguments();
			result.arguments().addAll( dynArgs );
			return result;
		}
		else {
			return ConstResult.valueOf( !_returnThisIfFound );
		}
	}


	private CellAddress getCellAddress() throws CompilerException
	{
		if (node().arguments() == null || node().arguments().size() == 0) {
			final CellAddress cellAddress = context().cellAddress;
			if (cellAddress != null) {
				return cellAddress;
			}
		}
		else {
			final ExpressionNode arg0 = node().argument( 0 );
			if (arg0 instanceof ExpressionNodeForCellModel) {
				return arg0.getOriginCellAddress();
			}
		}
		throw new CompilerException.UnsupportedExpression( "ROW/COLUMN is not supported for such arguments" );
	}


	@Override
	protected TypedResult evaluateToConst( TypedResult... _args ) throws InterpreterException
	{
		final Function function = node().getFunction();
		if (function.isVolatile()) {
			return evaluateToNode( _args );
		}
		else {
			switch (function) {

				case COUNT:
					throw new IllegalStateException( "COUNT not expected in evaluateToConst" );

				default:
					try {
						return new ConstResult( type().compute( function, valuesOf( _args ) ), node().getDataType() );
					}
					catch (EvalNotPossibleException e) {
						return evaluateToNode( _args );
					}

			}
		}
	}

}
