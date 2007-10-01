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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.bytecode.ExpressionCompilerForNumbers_Base.FoldArrayCompilation;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldArray;
import org.formulacompiler.compiler.internal.expressions.LetDictionary;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.objectweb.asm.Opcodes;


final class HelperCompilerForArrayFold extends HelperCompiler
{
	protected final FoldContext foldContext;
	protected final ExpressionNode array;


	public HelperCompilerForArrayFold( SectionCompiler _section, ExpressionNode _array, FoldContext _context,
			Iterable<LetEntry> _closure )
	{
		super( _section, _context.node, _closure );
		this.foldContext = _context;
		this.array = _array;
	}


	@Override
	protected final void compileBody() throws CompilerException
	{
		final ExpressionCompilerForNumbers expc = numericCompiler();
		final ExpressionNodeForFoldArray foldNode = (ExpressionNodeForFoldArray) this.foldContext.node;

		expc.compile( foldNode.array() );

		expc.compile_foldArray( new FoldArrayCompilation()
		{

			public void compileInitial() throws CompilerException
			{
				expc.compile( foldNode.initialAccumulatorValue() );
			}

			public void compileFold( int _acc, int _xi, int _i ) throws CompilerException
			{
				compileFold( new LocalValueRef( _acc ), new LocalValueRef( _xi ), _i );
			}

			public void compileFold( LocalRef _acc, LocalRef _xi, int _i ) throws CompilerException
			{
				final LetDictionary letDict = expc.letDict();
				final String accName = foldNode.accumulatorName();
				final String eltName = foldNode.elementName();
				final String idxName = foldNode.indexName();

				mv().visitIntInsn( Opcodes.ILOAD, _i );
				expc.compileConversionFromInt();
				final LocalRef i_num = expc.compileStoreToNewLocal( false );

				letDict.let( accName, foldNode.initialAccumulatorValue().getDataType(), _acc );
				letDict.let( eltName, foldNode.array().getDataType(), _xi );
				letDict.let( idxName, DataType.NUMERIC, i_num );
				try {
					expc.compile( foldNode.accumulatingStep() );
				}
				finally {
					letDict.unlet( idxName );
					letDict.unlet( eltName );
					letDict.unlet( accName );
				}
			}

		} );
	}

}
