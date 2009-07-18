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
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


@SuppressWarnings( "unqualified-field-access" )
final class HelperCompilerForFoldChained extends HelperCompilerForFolds
{
	private final ChainedFoldCompiler chainedCompiler;


	public HelperCompilerForFoldChained( SectionCompiler _section, ExpressionNodeForFoldApply _applyNode,
			Iterable<LetEntry> _closure )
	{
		super( _section, _applyNode, _closure );
		this.chainedCompiler = new ChainedFoldCompiler( expressionCompiler(), _applyNode );
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		assert ChainedFoldCompiler.isChainable( fold ) && !fold.isSpecialWhenEmpty();
		final ExpressionNode initNode = fold.accuInit( 0 );
		final DataType initType = initNode.getDataType();
		final Iterable<ExpressionNode> elts = apply.elements();
		final ExpressionNode initialElt = fold.mayReduceAndRearrange() ? firstLocalElementIn( elts ) : null;
		final ExpressionNode initial = (null != initialElt) ? initialElt : initNode;

		expressionCompiler().compile( initial );

		final String accName = fold.accuName( 0 );
		letDict().let( accName, initType, expressionCompiler().TOP_OF_STACK );

		chainedCompiler.compileFoldOverLocalValues( elts, initialElt );

		if (expressionCompiler().isSubSectionIn( elts )) {
			setupAccumulator( initType );
			compileAccumulatorStore();

			compileFoldOverSubSections( elts );

			compileAccumulatorLoad();
		}
		letDict().unlet( accName );
	}


	private void compileFoldOverSubSections( Iterable<ExpressionNode> _elts ) throws CompilerException
	{
		final int reuseLocalsAt = localsOffset();
		for (final ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				resetLocalsTo( reuseLocalsAt );

				compileSubSectionTraversal( (ExpressionNodeForSubSectionModel) elt, new SubSectionTraversal()
				{

					public void compile( Collection<ExpressionNode> _elements ) throws CompilerException
					{
						if (null != firstLocalElementIn( _elements )) {
							compileAccumulatorLoad();
							chainedCompiler.compileFoldOverLocalValues( _elements, null );
							compileAccumulatorStore();
						}
						compileFoldOverSubSections( _elements );
					}

				} );

			}
		}
	}


	private Type accuType;
	private int accuVar;

	private void setupAccumulator( DataType _type )
	{
		accuType = section().engineCompiler().typeCompiler( _type ).type();
		accuVar = newLocal( accuType.getSize() );
	}

	private void compileAccumulatorStore()
	{
		mv().visitVarInsn( accuType.getOpcode( Opcodes.ISTORE ), accuVar );
	}

	private void compileAccumulatorLoad()
	{
		mv().visitVarInsn( accuType.getOpcode( Opcodes.ILOAD ), accuVar );
	}


}
