package sej.internal.bytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.expressions.LetDictionary;
import sej.internal.model.ExpressionNodeForSubSectionModel;


final class HelperCompilerForIterativeFold1st extends HelperCompilerForIterativeFold
{
	private final ExpressionNodeForFold1st fold;
	private boolean needFirstDetection = true;

	public HelperCompilerForIterativeFold1st(SectionCompiler _section, Iterable<ExpressionNode> _elts,
			FoldContext _context, LetDictionary<Object> _outerLets)
	{
		super( _section, _elts, _context, _outerLets );
		this.fold = (ExpressionNodeForFold1st) _context.node;
	}


	@Override
	protected void compileFold( FoldContext _context, Iterable<ExpressionNode> _elts, int _localResult )
			throws CompilerException
	{
		final ExpressionNode first = expc().firstStaticElementIn( _elts );
		if (first != null) {
			this.needFirstDetection = false;
			expc().compileElementAccess( _context, this.fold.firstName(), first, this.fold.firstValue() );
			compileFoldWithChainedInitialValue( _context, _elts, _localResult, first );
		}
		else {

			final GeneratorAdapter mv = mv();
			_context.localHaveFirst = newLocal( 1 ); // boolean
			mv.visitInsn( Opcodes.ICONST_1 );
			mv.visitVarInsn( Opcodes.ISTORE, _context.localHaveFirst );

			expc().compile( this.fold.initialAccumulatorValue() );
			compileAccumulatorStore( _localResult );
			compileIterativeFoldOverRepeatingElements( _context, _elts, _localResult );
			compileAccumulatorLoad( _localResult );
		}
	}


	@Override
	protected void compileIterativeFoldOverRepeatingElement( FoldContext _context, int _localAccumulator,
			ExpressionNodeForSubSectionModel _elt ) throws CompilerException
	{
		if (this.needFirstDetection) {
			final ExpressionNode first = expc().firstStaticElementIn( _elt.arguments() );
			if (first != null) {
				compileSingleIterativeFoldWithDetectionOfFirst( _context, _localAccumulator, _elt, first );
			}
			else {
				super.compileIterativeFoldOverRepeatingElement( _context, _localAccumulator, _elt );
			}
		}
		else {
			super.compileIterativeFoldOverRepeatingElement( _context, _localAccumulator, _elt );
		}
	}


	private void compileSingleIterativeFoldWithDetectionOfFirst( final FoldContext _context,
			final int _localAccumulator, final ExpressionNodeForSubSectionModel _elt,
			final ExpressionNode _firstNonRepeatingElement ) throws CompilerException
	{
		final SubSectionCompiler subSection = _context.section.subSectionCompiler( _elt.getSectionModel() );
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, _context.localThis );
		_context.section.compileCallToGetterFor( mv, subSection );
		final Iterable<ExpressionNode> subElts = _elt.arguments();
		final int haveFirst = _context.localHaveFirst;
		final ExpressionNodeForFold1st fold = this.fold;

		expc().compile_scanArrayWithFirst( new ExpressionCompiler.ForEachElementWithFirstCompilation()
		{

			public void compileIsFirst() throws CompilerException
			{
				mv.visitVarInsn( Opcodes.ILOAD, haveFirst );
			}

			public void compileHaveFirst() throws CompilerException
			{
				mv.visitInsn( Opcodes.ICONST_0 );
				mv.visitVarInsn( Opcodes.ISTORE, haveFirst );
			}

			public void compileFirst( int _x0 ) throws CompilerException
			{
				final FoldContext subContext = new FoldContext( _context, subSection, _x0 );
				
				HelperCompilerForIterativeFold1st.this.needFirstDetection = false;
				
				expc().compileElementAccess( subContext, fold.firstName(), _firstNonRepeatingElement, fold.firstValue() );
				expc().compileChainedFoldOverNonRepeatingElements( subContext, subElts, _firstNonRepeatingElement );
				compileAccumulatorStore( _localAccumulator );
				compileIterativeFoldOverRepeatingElements( subContext, subElts, _localAccumulator );
			}

			public void compileElement( int _xi ) throws CompilerException
			{
				compileElements( new FoldContext( _context, subSection, _xi ), subElts, null, _localAccumulator );
			}

		} );

		this.needFirstDetection = true;
	}

}
