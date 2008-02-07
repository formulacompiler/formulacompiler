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
