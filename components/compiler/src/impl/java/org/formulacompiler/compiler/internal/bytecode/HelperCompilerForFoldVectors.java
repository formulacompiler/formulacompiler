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

import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;


@SuppressWarnings( "unqualified-field-access" )
final class HelperCompilerForFoldVectors extends HelperCompilerForFoldApply
{

	public HelperCompilerForFoldVectors( SectionCompiler _section, ExpressionNodeForFoldApply _applyNode,
			Iterable<LetEntry> _closure )
	{
		super( _section, _applyNode, _closure );
	}

	@Override
	protected boolean argumentsAreVectors()
	{
		return true;
	}


	@Override
	protected final void compileTraversal() throws CompilerException
	{
		final int eltCount = fold.eltCount();

		final Iterator<ExpressionNode> eltsIter = apply.elements().iterator();
		final ExpressionNode[] elts = new ExpressionNode[ eltCount ];
		for (int i = 0; i < eltCount; i++)
			elts[ i ] = eltsIter.next();

		final Iterator<ExpressionNode>[] vecs = getVectorArray( elts, eltCount );
		compileTraversalOf( vecs );
	}


	private void compileTraversalOf( Iterator<ExpressionNode>[] _vecs ) throws CompilerException
	{
		final int eltCount = fold.eltCount();
		final ExpressionNode[] elts = new ExpressionNode[ eltCount ];
		final int reuseLocalsAt = localsOffset();
		while (_vecs[ 0 ].hasNext()) {
			resetLocalsTo( reuseLocalsAt );
			for (int iVec = 0; iVec < eltCount; iVec++) {
				elts[ iVec ] = _vecs[ iVec ].next();
			}
			if (elts[ 0 ] instanceof ExpressionNodeForSubSectionModel) {
				verifyAllElementsReferenceTheSameSubSection( elts );
				compileSubSectionTraversal( (ExpressionNodeForSubSectionModel) elts[ 0 ], new SubSectionTraversal()
				{

					public void compile( Collection<ExpressionNode> _elements ) throws CompilerException
					{
						final Iterator<ExpressionNode>[] subVecs = getVectorArray( elts, eltCount );
						compileTraversalOf( subVecs );
					}

				} );
			}
			else {
				for (int iVec = 0; iVec < eltCount; iVec++) {
					final ExpressionNode elt = elts[ iVec ];
					letDict().let( fold.eltName( iVec ), elt.getDataType(), elt );
				}

				compileFoldStepsWithEltsBound();

				letDict().unlet( eltCount );
			}

		}
	}

	@SuppressWarnings( "unchecked" )
	private Iterator<ExpressionNode>[] getVectorArray( ExpressionNode[] _vecs, int _eltCount )
	{
		final Iterator<ExpressionNode>[] result = new Iterator[ _eltCount ];
		int iVec = 0;
		for (ExpressionNode vec : _vecs) {
			result[ iVec++ ] = vec.arguments().iterator();
		}
		assert iVec == result.length;
		return result;
	}


	private void verifyAllElementsReferenceTheSameSubSection( ExpressionNode[] _elts ) throws CompilerException
	{
		final ExpressionNodeForSubSectionModel sub = (ExpressionNodeForSubSectionModel) _elts[ 0 ];
		for (int iVec = 1; iVec < _elts.length; iVec++) {
			final ExpressionNode elt = _elts[ iVec ];
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				final ExpressionNodeForSubSectionModel sub2 = (ExpressionNodeForSubSectionModel) elt;
				if (sub.getSectionModel() != sub2.getSectionModel()) {
					throw differentSubSections( sub, sub2 );
				}
			}
			else throw differentSubSections( sub, elt );
		}
	}

	private CompilerException differentSubSections( ExpressionNode _a, ExpressionNode _b )
	{
		return new CompilerException.ParallelVectorsSpanDifferentSubSections(
				"Cannot aggregate parallel vectors crossing subsections differently at " + _a + " and " + _b );
	}

}
