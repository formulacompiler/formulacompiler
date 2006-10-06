/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.bytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFold;
import sej.internal.expressions.LetDictionary;
import sej.internal.model.ExpressionNodeForSubSectionModel;


@SuppressWarnings("unqualified-field-access")
final class HelperCompilerForIterativeFold extends HelperCompiler
{
	private final FoldContext foldContext2;
	private final ExpressionNodeForFold foldNode;
	private final Iterable<ExpressionNode> elts;
	private final LetDictionary<Object> outerLets;


	public HelperCompilerForIterativeFold(SectionCompiler _section, Iterable<ExpressionNode> _elts,
			FoldContext _context, LetDictionary<Object> _outerLets)
	{
		super( _section, _context.node );
		this.foldContext2 = _context;
		this.foldNode = _context.node;
		this.elts = _elts;
		this.outerLets = _outerLets;
	}


	@Override
	protected final void compileBody() throws CompilerException
	{
		final ExpressionCompiler expc = expressionCompiler();

		// This handles outer lets such as in "var(xs) = (let m = avg(xs) in fold(... ei = xi - m))".
		// Ensures that the let is available and computed before the loop.
		expc.copyAndForcePendingLetsFrom( this.outerLets );

		final int localResult = compileNewAccumulator();
		expc.compile( foldContext2.node.initialAccumulatorValue() );
		compileElements( foldContext2, elts, localResult );
	}


	private void compileElements( FoldContext _context, Iterable<ExpressionNode> _elts, int _localAccumulator )
			throws CompilerException
	{
		final ExpressionCompiler expc = expressionCompiler();
		if (expc.isSubSectionIn( _elts )) {
			compileAccumulatorStore( _localAccumulator );
			compileIterativeFoldOverRepeatingElements( _context, _elts, _localAccumulator );
			compileAccumulatorLoad( _localAccumulator );
		}
		expc.compileChainedFoldOverNonRepeatingElements( _context, _elts );
	}


	final void compileIterativeFoldOverRepeatingElements( final FoldContext _context, Iterable<ExpressionNode> _elts,
			final int _localAccumulator ) throws CompilerException
	{
		final SectionCompiler section = section();
		final ExpressionCompiler expc = expressionCompiler();
		final int reuseLocalsAt = localsOffset();
		for (final ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				final ExpressionNodeForSubSectionModel subElt = (ExpressionNodeForSubSectionModel) elt;
				final SubSectionCompiler subSection = _context.section.subSectionCompiler( subElt.getSectionModel() );
				final GeneratorAdapter mv = mv();
				resetLocalsTo( reuseLocalsAt );
				mv.visitVarInsn( Opcodes.ALOAD, _context.localThis );
				_context.section.compileCallToGetterFor( mv, subSection );
				expc.compile_scanArray( new ExpressionCompiler.ForEachElementCompilation()
				{

					public void compile( int localElement ) throws CompilerException
					{
						compileAccumulatorLoad( _localAccumulator );
						compileElements( new FoldContext( _context, subSection, localElement ), elt.arguments(), _localAccumulator );
						compileAccumulatorStore( _localAccumulator );
					}

				} );
			}
		}
	}

	final int compileNewAccumulator()
	{
		return newLocal( foldContext2.accumulatorType.getSize() );
	}

	final void compileAccumulatorStore( int _local )
	{
		mv().visitVarInsn( foldContext2.accumulatorType.getOpcode( Opcodes.ISTORE ), _local );
	}

	final void compileAccumulatorLoad( int _local )
	{
		mv().visitVarInsn( foldContext2.accumulatorType.getOpcode( Opcodes.ILOAD ), _local );
	}

}
