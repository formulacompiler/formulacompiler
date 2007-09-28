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
