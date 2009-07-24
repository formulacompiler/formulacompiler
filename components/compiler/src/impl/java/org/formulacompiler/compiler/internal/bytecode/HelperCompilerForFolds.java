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
