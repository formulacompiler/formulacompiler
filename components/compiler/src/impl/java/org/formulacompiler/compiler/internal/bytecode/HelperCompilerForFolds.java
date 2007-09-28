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
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

abstract class HelperCompilerForFolds extends HelperCompiler
{
	protected final ExpressionNodeForFoldApply apply;
	protected final ExpressionNodeForFoldDefinition fold;

	public HelperCompilerForFolds( SectionCompiler _section, ExpressionNodeForFoldApply _applyNode,
			Iterable<LetEntry> _closure )
	{
		super( _section, _applyNode, _closure );
		this.apply = _applyNode;
		this.fold = _applyNode.fold();
	}


	protected static interface SubSectionTraversal
	{
		void compile( Collection<ExpressionNode> _elements ) throws CompilerException;
	}

	protected final void compileSubSectionTraversal( final ExpressionNodeForSubSectionModel _sub,
			final SubSectionTraversal _traversal ) throws CompilerException
	{
		final SubSectionCompiler subSection = sectionInContext().subSectionCompiler( _sub.getSectionModel() );
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, objectInContext() );
		sectionInContext().compileCallToGetterFor( mv, subSection );
		expressionCompiler().compile_scanArray( new ExpressionCompiler.ForEachElementCompilation()
		{

			public void compile( int _xi ) throws CompilerException
			{
				final SectionCompiler oldSection = sectionInContext();
				final int oldObject = objectInContext();
				try {
					setObjectInContext( subSection, _xi );
					_traversal.compile( _sub.arguments() );
				}
				finally {
					setObjectInContext( oldSection, oldObject );
				}
			}

		} );

	}

}
