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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;


@SuppressWarnings( "unqualified-field-access" )
final class HelperCompilerForFoldList extends HelperCompilerForFoldApply
{

	public HelperCompilerForFoldList( SectionCompiler _section, ExpressionNodeForFoldApply _applyNode,
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
		compileTraversalOf( apply.elements() );
	}


	private void compileTraversalOf( final Iterable<ExpressionNode> _elts ) throws CompilerException
	{
		final int reuseLocalsAt = localsOffset();
		final String eltName = fold.eltName( 0 );
		for (ExpressionNode elt : _elts) {
			resetLocalsTo( reuseLocalsAt );
			if (elt instanceof ExpressionNodeForSubSectionModel) {

				compileSubSectionTraversal( (ExpressionNodeForSubSectionModel) elt, new SubSectionTraversal()
				{

					public void compile( Collection<ExpressionNode> _elements ) throws CompilerException
					{
						compileTraversalOf( _elements );
					}

				} );

			}
			else {
				letDict().let( eltName, elt.getDataType(), elt );

				compileFoldStepsWithEltsBound();

				letDict().unlet( eltName );
			}
		}
	}


}
