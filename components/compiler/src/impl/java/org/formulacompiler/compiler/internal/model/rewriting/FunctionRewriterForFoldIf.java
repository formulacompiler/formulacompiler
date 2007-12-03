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
		this.folded = (ExpressionNodeForArrayReference) (_fun.cardinality() > 2? _fun.argument( 2 ) : null);
		this.fold = _fold;
	}


	public final ExpressionNode rewrite() throws CompilerException
	{
		// We need the type info to properly treat by-example criteria contained in strings.
		TypeAnnotator.annotateExpr( this.tested );

		final FilterBuilder filterBuilder = new FilterBuilder();
		final DataType testedType = this.tested.getDataType();
		final ExpressionNode filter = filterBuilder.buildFilterByExample( 0, this.test, testedType );

		final ExpressionNodeForFoldDatabase apply;
		if (this.folded == null) {
			apply = new ExpressionNodeForFoldDatabase( this.fold, New.array( testedType ), this.colPrefix(), filter, 0,
					null, null, this.tested );
		}
		else {
			TypeAnnotator.annotateExpr( this.folded );
			final DataType foldedType = this.folded.getDataType();
			apply = new ExpressionNodeForFoldDatabase( this.fold, New.array( testedType, foldedType ), this.colPrefix(),
					filter, 1, null, null, vectorsToMatrix( this.tested, this.folded ) );
		}

		return filterBuilder.encloseFoldInLets( apply );
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
