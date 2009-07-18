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

package org.formulacompiler.compiler.internal.bytecode;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSubstitution;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;


@SuppressWarnings( "unqualified-field-access" )
final class ChainedFoldCompiler
{
	private final ExpressionCompiler expc;
	private final LetDictionary letDict;
	private final ExpressionNodeForFoldApply apply;
	private final ExpressionNodeForFoldDefinition fold;

	public ChainedFoldCompiler( ExpressionCompiler _expressionCompiler, ExpressionNodeForFoldApply _applyNode )
	{
		super();
		this.expc = _expressionCompiler;
		this.letDict = _expressionCompiler.letDict();
		this.apply = _applyNode;
		this.fold = _applyNode.fold();
	}


	public final boolean compile() throws CompilerException
	{
		final Iterable<ExpressionNode> elts = firstVectorOf( apply.elements() );
		if (!elts.iterator().hasNext()) {
			final ExpressionNode whenEmpty = fold.isSpecialWhenEmpty() ? fold.whenEmpty() : fold.accuInit( 0 );
			expc.compile( whenEmpty );
			return true;
		}
		if (isChainable( fold ) && !ExpressionCompiler.isSubSectionIn( elts )) {
			final ExpressionNode initial = fold.mayReduce() ? elts.iterator().next() : fold.accuInit( 0 );
			expc.compile( initial );
			final String accName = fold.accuName( 0 );
			letDict.let( accName, fold.accuInit( 0 ).getDataType(), expc.TOP_OF_STACK );
			compileFoldOverLocalValues( elts, initial );
			letDict.unlet( accName );
			return true;
		}
		return false;
	}


	public final void compileFoldOverLocalValues( Iterable<ExpressionNode> _elts, ExpressionNode _except )
			throws CompilerException
	{
		assert isChainable( fold );
		final int reuseLocalsAt = expc.localsOffset();
		for (final ExpressionNode elt : _elts) {
			if ((elt != _except) && !(elt instanceof ExpressionNodeForSubSectionModel)) {
				final String eltName = fold.eltName( 0 );
				letDict.let( eltName, elt.getDataType(), elt );
				expc.resetLocalsTo( reuseLocalsAt );
				expc.compile( fold.accuStep( 0 ) );
				letDict.unlet( eltName );
			}
		}
	}


	static boolean isChainable( ExpressionNodeForFoldDefinition _fold )
	{
		return _fold.accuCount() == 1 && _fold.eltCount() == 1 && !_fold.isIndexed() && !_fold.isCounted();
	}

	static Iterable<ExpressionNode> firstVectorOf( Iterable<ExpressionNode> _elts )
	{
		Iterable<ExpressionNode> vec0 = _elts;
		ExpressionNode elt0;
		while ((elt0 = vec0.iterator().next()) instanceof ExpressionNodeForSubstitution
				|| elt0 instanceof ExpressionNodeForArrayReference) {
			vec0 = elt0.arguments();
		}
		return vec0;
	}

}
