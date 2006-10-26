package sej.internal.bytecode.compiler;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForFold1st;
import sej.internal.expressions.LetDictionary.LetEntry;
import sej.internal.model.ExpressionNodeForSubSectionModel;


final class HelperCompilerForIterativeFold1st extends HelperCompilerForIterativeFold
{
	private final ExpressionNodeForFold1st fold;
	private boolean needFirstDetection = true;

	public HelperCompilerForIterativeFold1st(SectionCompiler _section, Iterable<ExpressionNode> _elts,
			FoldContext _context, Iterable<LetEntry> _closure)
	{
		super( _section, _elts, _context, _closure );
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
		final SubSectionCompiler subSection = sectionInContext().subSectionCompiler( _elt.getSectionModel() );
		final GeneratorAdapter mv = mv();
		mv.visitVarInsn( Opcodes.ALOAD, objectInContext() );
		sectionInContext().compileCallToGetterFor( mv, subSection );
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
				final SectionCompiler oldSection = sectionInContext();
				final int oldObject = objectInContext();
				try {
					setObjectInContext( subSection, _x0 );

					HelperCompilerForIterativeFold1st.this.needFirstDetection = false;

					expc().compileElementAccess( _context, fold.firstName(), _firstNonRepeatingElement, fold.firstValue() );
					expc().compileChainedFoldOverNonRepeatingElements( _context, subElts, _firstNonRepeatingElement );
					compileAccumulatorStore( _localAccumulator );
					compileIterativeFoldOverRepeatingElements( _context, subElts, _localAccumulator );
					
				}
				finally {
					setObjectInContext( oldSection, oldObject );
				}
			}

			public void compileElement( int _xi ) throws CompilerException
			{
				final SectionCompiler oldSection = sectionInContext();
				final int oldObject = objectInContext();
				try {
					setObjectInContext( subSection, _xi );
					compileElements( _context, subElts, null, _localAccumulator );
				}
				finally {
					setObjectInContext( oldSection, oldObject );
				}
			}

		} );

		this.needFirstDetection = true;
	}

}
