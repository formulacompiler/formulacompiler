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
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;



class HelperCompilerForIterativeFold extends HelperCompiler
{
	protected final FoldContext foldContext;
	protected final Iterable<ExpressionNode> elts;
	protected final ExpressionCompiler expc;


	public HelperCompilerForIterativeFold(SectionCompiler _section, Iterable<ExpressionNode> _elts,
			FoldContext _context, Iterable<LetEntry> _closure)
	{
		super( _section, _context.node, _closure );
		this.foldContext = _context;
		this.elts = _elts;
		this.expc = expressionCompiler();
	}


	protected final ExpressionCompiler expc()
	{
		return this.expc;
	}


	@Override
	protected final void compileBody() throws CompilerException
	{
		compileFold( this.foldContext, this.elts, compileNewAccumulator() );
	}


	protected void compileFold( FoldContext _context, Iterable<ExpressionNode> _elts, int _localResult )
			throws CompilerException
	{
		ExpressionNode first;
		if (_context.node.mayReduce() && (first = expc().firstStaticElementIn( _elts )) != null) {
			expc().compile( first );
			compileFoldWithChainedInitialValue( _context, _elts, _localResult, first );
		}
		else {
			expc().compile( _context.node.initialAccumulatorValue() );
			compileFoldWithChainedInitialValue( _context, _elts, _localResult, null );
		}
	}


	protected void compileFoldWithChainedInitialValue( FoldContext _context, Iterable<ExpressionNode> _elts,
			int _localAccumulator, ExpressionNode _except ) throws CompilerException
	{
		expc().compileChainedFoldOverNonRepeatingElements( _context, _elts, _except );
		if (expc().isSubSectionIn( _elts )) {
			compileAccumulatorStore( _localAccumulator );
			compileIterativeFoldOverRepeatingElements( _context, _elts, _localAccumulator );
			compileAccumulatorLoad( _localAccumulator );
		}
	}


	protected final void compileElements( FoldContext _context, Iterable<ExpressionNode> _elts, ExpressionNode _except,
			int _localAccumulator ) throws CompilerException
	{
		if (null != expc().firstStaticElementIn( _elts )) {
			compileAccumulatorLoad( _localAccumulator );
			expc().compileChainedFoldOverNonRepeatingElements( _context, _elts, _except );
			compileAccumulatorStore( _localAccumulator );
		}
		compileIterativeFoldOverRepeatingElements( _context, _elts, _localAccumulator );
	}


	protected final void compileIterativeFoldOverRepeatingElements( final FoldContext _context,
			Iterable<ExpressionNode> _elts, final int _localAccumulator ) throws CompilerException
	{
		final int reuseLocalsAt = localsOffset();
		for (final ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				resetLocalsTo( reuseLocalsAt );
				compileIterativeFoldOverRepeatingElement( _context, _localAccumulator,
						(ExpressionNodeForSubSectionModel) elt );
			}
		}
	}

	protected void compileIterativeFoldOverRepeatingElement( final FoldContext _context, final int _localAccumulator,
			final ExpressionNodeForSubSectionModel _elt ) throws CompilerException
	{
		final SubSectionCompiler subSection = sectionInContext().subSectionCompiler( _elt.getSectionModel() );
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, objectInContext() );
		sectionInContext().compileCallToGetterFor( mv, subSection );
		expc().compile_scanArray( new ExpressionCompiler.ForEachElementCompilation()
		{

			public void compile( int _xi ) throws CompilerException
			{
				final SectionCompiler oldSection = sectionInContext();
				final int oldObject = objectInContext();
				try {
					setObjectInContext( subSection, _xi );
					compileElements( _context, _elt.arguments(), null, _localAccumulator );
				}
				finally {
					setObjectInContext( oldSection, oldObject );
				}
			}

		} );
	}

	final int compileNewAccumulator()
	{
		return newLocal( this.foldContext.accumulatorType.getSize() );
	}

	final void compileAccumulatorStore( int _local )
	{
		mv().visitVarInsn( this.foldContext.accumulatorType.getOpcode( Opcodes.ISTORE ), _local );
	}

	final void compileAccumulatorLoad( int _local )
	{
		mv().visitVarInsn( this.foldContext.accumulatorType.getOpcode( Opcodes.ILOAD ), _local );
	}

}
