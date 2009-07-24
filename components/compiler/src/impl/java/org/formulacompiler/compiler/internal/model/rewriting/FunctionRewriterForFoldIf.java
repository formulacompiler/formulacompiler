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

package org.formulacompiler.compiler.internal.model.rewriting;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDatabase;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;

final class FunctionRewriterForFoldIf extends AbstractFunctionRewriterForConditionalFold
{
	private final ExpressionNodeForArrayReference folded;
	private final ExpressionNodeForArrayReference tested;
	private final ExpressionNode test;
	private final ExpressionNode fold;

	public FunctionRewriterForFoldIf( ComputationModel _model, ExpressionNodeForFunction _fun,
			InterpretedNumericType _type, NameSanitizer _sanitizer, ExpressionNode _fold )
	{
		super( _model, _type, _sanitizer );
		this.tested = (ExpressionNodeForArrayReference) _fun.argument( 0 );
		this.test = _fun.argument( 1 );
		this.folded = (ExpressionNodeForArrayReference) (_fun.cardinality() > 2 ? _fun.argument( 2 ) : null);
		this.fold = _fold;
	}


	public final ExpressionNode rewrite() throws CompilerException
	{
		// We need the type info to properly treat by-example criteria contained in strings.
		final ExpressionNodeForArrayReference tested = makeVertical( this.tested );
		TypeAnnotator.annotateExpr( tested );

		final FilterBuilder filterBuilder = new FilterBuilder();
		final DataType testedType = tested.getDataType();
		final ExpressionNode filter = filterBuilder.buildFilterByExample( 0, this.test, testedType );

		final ExpressionNodeForFoldDatabase apply;
		if (this.folded == null) {
			apply = new ExpressionNodeForFoldDatabase( this.fold, New.array( testedType ), this.colPrefix(), filter, 0,
					null, null, tested );
		}
		else {
			TypeAnnotator.annotateExpr( this.folded );
			final DataType foldedType = this.folded.getDataType();
			apply = new ExpressionNodeForFoldDatabase( this.fold, New.array( testedType, foldedType ), this.colPrefix(),
					filter, 1, null, null, vectorsToMatrix( tested, makeVertical( this.folded ) ) );
		}

		return filterBuilder.encloseFoldInLets( apply );
	}


	private ExpressionNodeForArrayReference makeVertical( ExpressionNodeForArrayReference _vector )
	{
		final ArrayDescriptor desc = _vector.arrayDescriptor();
		assert desc.numberOfSheets() == 1;
		if (desc.numberOfRows() > 1) {
			assert desc.numberOfColumns() == 1;
			return _vector;
		}
		else {
			assert desc.numberOfRows() == 1;
			final ArrayDescriptor newDesc = new ArrayDescriptor( desc.origin(), new ArrayDescriptor.Point( 1, desc
					.numberOfColumns(), 1 ) );
			final ExpressionNodeForArrayReference newVector = new ExpressionNodeForArrayReference( newDesc );
			newVector.arguments().addAll( _vector.arguments() );
			return newVector;
		}
	}


	private ExpressionNodeForArrayReference vectorsToMatrix( ExpressionNodeForArrayReference... _vectors )
	{
		final int rows = _vectors[ 0 ].arguments().size();
		final int cols = _vectors.length;
		final ArrayDescriptor desc = new ArrayDescriptor( 1, rows, cols );
		final ExpressionNodeForArrayReference result = new ExpressionNodeForArrayReference( desc );

		for (int row = 0; row < rows; row++)
			for (int col = 0; col < cols; col++)
				result.addArgument( _vectors[ col ].argument( row ) );

		return result;
	}

}
