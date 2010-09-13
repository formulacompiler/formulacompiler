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

package org.formulacompiler.compiler.internal.model.optimizer;

import java.util.Map;
import java.util.Stack;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ConstantExpressionCellListenerSupport;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.compiler.internal.model.optimizer.consteval.ConstResult;
import org.formulacompiler.compiler.internal.model.optimizer.consteval.EvalShadow;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.spreadsheet.CellAddress;


final public class ConstantSubExpressionEliminator extends AbstractComputationModelVisitor
{
	private final InterpretedNumericType numericType;
	private final ConstantExpressionCellListenerSupport listenerSupport;
	private final Stack<Map<CellModel, TypedResult>> savedCaches = New.stack();


	private ConstantSubExpressionEliminator( InterpretedNumericType _type,
			ConstantExpressionCellListenerSupport _listenerSupport )
	{
		super();
		this.numericType = _type;
		this.listenerSupport = _listenerSupport;
	}

	public ConstantSubExpressionEliminator( NumericType _type, ComputationMode _mode, Environment _env,
			ConstantExpressionCellListenerSupport _listenerSupport )
	{
		this( InterpretedNumericType.typeFor( _type, _mode, _env ), _listenerSupport );
	}

	public ConstantSubExpressionEliminator( NumericType _type )
	{
		this( InterpretedNumericType.typeFor( _type, ComputationMode.EXCEL, Environment.DEFAULT ), null );
		Util.assertTesting();
	}


	public InterpretedNumericType getNumericType()
	{
		return this.numericType;
	}


	/**
	 * Reset results cached by reference from outside the section. They might be constant when seen
	 * from within, but not from without.
	 */
	@Override
	protected boolean visitSection( SectionModel _section ) throws CompilerException
	{
		final Map<CellModel, TypedResult> saved = New.map();
		this.savedCaches.push( saved );
		_section.traverse( new AbstractComputationModelVisitor()
		{
			@Override
			protected boolean visitCell( CellModel _cell ) throws CompilerException
			{
				saved.put( _cell, _cell.getCachedResult() );
				_cell.setCachedResult( null );
				return true;
			}
		} );
		return true;
	}

	/**
	 * Restore former cached values after processing the section so outer refs don't see inner cached
	 * values.
	 */
	@Override
	protected boolean visitedSection( SectionModel _section ) throws CompilerException
	{
		final Map<CellModel, TypedResult> saved = this.savedCaches.pop();
		_section.traverse( new AbstractComputationModelVisitor()
		{
			@Override
			protected boolean visitCell( CellModel _cell ) throws CompilerException
			{
				_cell.setCachedResult( saved.get( _cell ) );
				return true;
			}
		} );
		return true;
	}


	@Override
	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		ExpressionNode sourceExpr = _cell.getExpression();
		if (null != sourceExpr) {
			try {
				TypedResult optimizedResult = eliminateConstantsFrom( sourceExpr, _cell );
				assert (optimizedResult.getDataType() == _cell.getDataType() || optimizedResult.getDataType() == DataType.NULL);
				if (optimizedResult.hasConstantValue()) {
					_cell.setExpression( null );
					final Object value = optimizedResult.getConstantValue();
					_cell.setConstantValue( value );
					if (this.listenerSupport != null) {
						this.listenerSupport.constantExpressionEliminated( _cell, value );
					}
				}
				else {
					ExpressionNode optimizedExpr = (ExpressionNode) optimizedResult;
					_cell.setExpression( optimizedExpr );
				}
			}
			catch (Throwable t) {
				throw new CompilerException.UnsupportedExpression( t );
			}
		}
		return true;
	}

	private TypedResult eliminateConstantsFrom( ExpressionNode _expr, CellModel _cell ) throws CompilerException
	{
		if (null == _expr) return ConstResult.NULL;
		final TypedResult cached = _cell.getCachedResult();
		if (null != cached) return cached;
		final Object source = _cell.getSource();
		final CellAddress cellAddress = source instanceof CellAddress ? (CellAddress) source : null;
		return EvalShadow.evaluate( _expr, getNumericType(), cellAddress );
	}


}
