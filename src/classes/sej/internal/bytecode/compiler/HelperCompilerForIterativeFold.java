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
import sej.internal.model.ExpressionNodeForSubSectionModel;


@SuppressWarnings("unqualified-field-access")
final class HelperCompilerForIterativeFold extends TypedMethodCompiler
{
	private static final int FIRST_PARAM = 1;

	private final FoldContext foldContext;
	private final ExpressionNodeForFold foldNode;
	private final Iterable<ExpressionNode> elts;


	public HelperCompilerForIterativeFold(SectionCompiler _section, Iterable<ExpressionNode> _elts,
			FoldContext _context)
	{
		super( _section, 0, _section.newGetterName(), constructDescriptor( _context ), _context.node.getDataType() );
		this.foldContext = _context;
		this.foldNode = _context.node;
		this.elts = _elts;
	}

	private static String constructDescriptor( FoldContext _context )
	{
		final String accDesc = _context.accumulatorType.getDescriptor();
		return "(" + accDesc + ")" + accDesc;
	}


	@Override
	protected final void compileBody() throws CompilerException
	{
		final ExpressionCompiler expc = expressionCompiler();
		if (expc.isSubSectionIn( elts )) {
			final String accName = foldNode.accumulatorName();
			final Object accOld = expc.letDict().let( accName, FIRST_PARAM );
			try {
				compileIterativeFoldOverRepeatingElements( foldContext, elts, FIRST_PARAM );
			}
			finally {
				expc.letDict().unlet( accName, accOld );
			}
		}
		compileAccumulatorLoad( foldContext, FIRST_PARAM );
		expc.compileChainedFoldOverNonRepeatingElements( foldContext, elts );
	}

	
	@Override
	protected final void endCompilation()
	{
		mv().visitInsn( foldContext.accumulatorType.getOpcode( Opcodes.IRETURN ) );
		super.endCompilation();
	}


	final void compileIterativeFoldOverRepeatingElements( FoldContext _context, Iterable<ExpressionNode> _elts,
			int _localAccumulator ) throws CompilerException
	{
		final int reuseLocalsAt = localsOffset();
		for (final ExpressionNode elt : _elts) {
			if (elt instanceof ExpressionNodeForSubSectionModel) {
				resetLocalsTo( reuseLocalsAt );
				compileIterationOver( _context, (ExpressionNodeForSubSectionModel) elt, _localAccumulator );
			}
		}
	}

	final void compileIterationOver( final FoldContext _context, final ExpressionNodeForSubSectionModel _elt,
			final int _localAccumulator ) throws CompilerException
	{
		final ExpressionCompiler expc = expressionCompiler();
		final SectionCompiler section = section();
		final SubSectionCompiler sub = section.subSectionCompiler( _elt.getSectionModel() );
		final MethodCompiler subExpr = new HelperCompilerForIterativeFold( sub, _elt.arguments(), _context );
		subExpr.compile();

		final GeneratorAdapter mv = mv();
		mv.loadThis();
		section.compileCallToGetterFor( mv, sub );
		expc.compile_scanArray( new ExpressionCompiler.InnerCompilation()
		{
			
			public void compile() throws CompilerException
			{
				compileAccumulatorLoad( _context, _localAccumulator );
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sub.classInternalName(), subExpr.methodName(), subExpr
						.methodDescriptor() );
				compileAccumulatorStore( _context, _localAccumulator );
			}
			
		} );
	}

	final int compileNewAccumulator( FoldContext _foldContext )
	{
		return newLocal( _foldContext.accumulatorType.getSize() );
	}

	final void compileAccumulatorStore( FoldContext _context, int _local )
	{
		mv().visitVarInsn( _context.accumulatorType.getOpcode( Opcodes.ISTORE ), _local );
	}

	final void compileAccumulatorLoad( FoldContext _context, int _local )
	{
		mv().visitVarInsn( _context.accumulatorType.getOpcode( Opcodes.ILOAD ), _local );
	}

}
