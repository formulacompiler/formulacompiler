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

package org.formulacompiler.compiler.internal.model.rewriting;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;


public final class ModelRewriter extends AbstractComputationModelVisitor
{
	private final ExpressionRewriter rewriter;
	private ComputationModel model;

	public ModelRewriter( InterpretedNumericType _type )
	{
		super();
		this.rewriter = new ExpressionRewriter( _type, new NameSanitizer() );
	}


	@Override
	protected boolean visitModel( ComputationModel _model ) throws CompilerException
	{
		this.model = _model;
		return super.visitModel( _model );
	}

	@Override
	protected boolean visitedModel( ComputationModel _model ) throws CompilerException
	{
		this.model = null;
		return super.visitedModel( _model );
	}


	@Override
	protected boolean visitCell( CellModel _cell ) throws CompilerException
	{
		final ExpressionNode expr = _cell.getExpression();
		if (null != expr) {
			_cell.setExpression( this.rewriter.rewrite( this.model, expr ) );
		}
		return true;
	}

}
