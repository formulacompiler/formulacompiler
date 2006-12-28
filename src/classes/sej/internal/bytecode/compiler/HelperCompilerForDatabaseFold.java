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

import java.util.List;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ArrayDescriptor;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForDatabaseFold;
import sej.internal.expressions.LetDictionary.LetEntry;
import sej.internal.model.ExpressionNodeForSectionModel;

final class HelperCompilerForDatabaseFold extends HelperCompilerForIterativeFold
{
	private final ExpressionNodeForArrayReference table;
	private final ArrayDescriptor tableDescriptor;
	private final String[] colNames;
	private final ExpressionNode filterExpr;
	private final boolean isReduce;
	private final boolean isZeroForEmptySelection;

	public HelperCompilerForDatabaseFold(SectionCompiler _section, ExpressionNodeForDatabaseFold _node,
			FoldContext _foldContext, Iterable<LetEntry> _closure)
	{
		super( _section, _node.elements(), _foldContext, _closure );
		this.table = _node.table();
		this.tableDescriptor = _node.table().arrayDescriptor();
		this.colNames = _node.filterColumnNames();
		this.filterExpr = _node.filter();
		this.isReduce = _node.isReduce();
		this.isZeroForEmptySelection = this.isReduce || _node.isZeroForEmptySelection();
	}


	private int localResult;

	@Override
	protected void compileFold( FoldContext _context, Iterable<ExpressionNode> _elts, int _localResult )
			throws CompilerException
	{
		this.localResult = _localResult;
		final ExpressionNodeForDatabaseFold node = (ExpressionNodeForDatabaseFold) _context.node;
		final int colIdx = node.staticFoldedColumnIndex();
		if (colIdx >= 0) {
			compileFold( _context, colIdx );
		}
		else {
			final ExpressionNode colIdxExpr = node.foldedColumnIndex();
			compileSwitchedFold( _context, colIdxExpr );
		}
	}


	private MethodCompiler matcher;
	private int haveMatchBooleanVar;

	private final void compileFold( FoldContext _context, int _foldedCol ) throws CompilerException
	{
		final List<ExpressionNode> elts = this.table.arguments();
		this.haveMatchBooleanVar = (this.isZeroForEmptySelection) ? newLocal( 1 ) : -1;

		if (this.isZeroForEmptySelection) {
			compileSetHaveMatch( false );
		}
		expc().compile( _context.node.initialAccumulatorValue() );
		compileAccumulatorStore( this.localResult );

		final int nElt = elts.size();
		int iElt = 0;
		while (iElt < nElt) {
			iElt = compileRowFold( _context, _foldedCol, elts, iElt );
		}

		if (this.isZeroForEmptySelection) {
			compileReturnZeroIfNoMatch();
		}
		compileAccumulatorLoad( this.localResult );
	}

	private int compileRowFold( FoldContext _context, int _foldedCol, List<ExpressionNode> _elts, int _iElt )
			throws CompilerException
	{
		final ExpressionNode elt = _elts.get( _iElt );
		if (elt instanceof ExpressionNodeForArrayReference) {
			compileRowFold( _context, _foldedCol, elt.arguments(), 0 );
			return _iElt + 1;
		}
		else if (elt instanceof ExpressionNodeForSectionModel) {
			compileSubSectionFold( _context, _foldedCol, (ExpressionNodeForSectionModel) elt );
			return _iElt + 1;
		}
		else {
			final GeneratorAdapter mv = mv();
			final int iFoldedElt = _iElt + _foldedCol;
			final ExpressionNode foldedElt = _elts.get( iFoldedElt );
			final Label noMatch = mv.newLabel();

			compileCallToMatcherAndBuildItInFirstPass( _elts, _iElt );
			compileSkipFoldIfNoMatch( noMatch );
			if (this.isReduce) {
				compileFirstMatchCheckAndInit( noMatch, foldedElt );
			}

			compileElementFold( _context, foldedElt );

			if (!this.isReduce && this.isZeroForEmptySelection) {
				compileSetHaveMatch( true );
			}
			mv.mark( noMatch );

			return _iElt + this.tableDescriptor.getNumberOfColumns();
		}
	}

	private void compileSubSectionFold( final FoldContext _context, final int _foldedCol,
			final ExpressionNodeForSectionModel _sub ) throws CompilerException
	{
		final int reuseLocalsAt = localsOffset();
		final SubSectionCompiler subSection = sectionInContext().subSectionCompiler( _sub.getSectionModel() );
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
					compileRowFold( _context, _foldedCol, _sub.arguments(), 0 );
				}
				finally {
					setObjectInContext( oldSection, oldObject );
				}
			}

		} );

		resetLocalsTo( reuseLocalsAt );
	}

	private void compileCallToMatcherAndBuildItInFirstPass( List<ExpressionNode> _elts, int _iElt )
			throws CompilerException
	{
		final int nCols = this.tableDescriptor.getNumberOfColumns();
		final GeneratorAdapter mv = mv();
		try {
			for (int iCol = 0; iCol < nCols; iCol++) {
				final ExpressionNode elt = _elts.get( _iElt + iCol );
				letDict().let( this.colNames[ iCol ], elt.getDataType(), elt );
			}
			final Iterable<LetEntry> closure = closureOf( this.filterExpr );
			mv().loadThis();
			compileClosure( closure );
			if (this.matcher == null) {
				this.matcher = new HelperCompilerForDatabaseMatch( section(), this.filterExpr, closure );
				this.matcher.compile();
			}
			mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, section().classInternalName(), this.matcher.methodName(),
					this.matcher.methodDescriptor() );
		}
		finally {
			letDict().unlet( nCols );
		}
	}

	private void compileSkipFoldIfNoMatch( final Label _noMatch )
	{
		mv().ifZCmp( Opcodes.IFEQ, _noMatch );
	}

	private void compileFirstMatchCheckAndInit( Label _noMatch, ExpressionNode _foldedElt ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label haveMatch = mv.newLabel();
		mv.visitVarInsn( Opcodes.ILOAD, this.haveMatchBooleanVar );
		mv.ifZCmp( Opcodes.IFNE, haveMatch );
		mv.push( true );
		mv.visitVarInsn( Opcodes.ISTORE, this.haveMatchBooleanVar );
		expc().compile( _foldedElt );
		compileAccumulatorStore( this.localResult );
		mv.goTo( _noMatch );
		mv.mark( haveMatch );
	}

	private void compileElementFold( FoldContext _context, ExpressionNode _foldedElt ) throws CompilerException
	{
		final String accName = _context.node.accumulatorName();
		final DataType accType = _context.node.initialAccumulatorValue().getDataType();
		compileAccumulatorLoad( this.localResult );
		letDict().let( accName, accType, ExpressionCompiler.CHAINED_FIRST_ARG );
		try {
			final int reuseLocalsAt = localsOffset();
			expc().compileElementFold( _context, _foldedElt );
			resetLocalsTo( reuseLocalsAt );
		}
		finally {
			letDict().unlet( accName );
		}
		compileAccumulatorStore( this.localResult );
	}

	private void compileSetHaveMatch( boolean _value )
	{
		final GeneratorAdapter mv = mv();
		mv.push( _value );
		mv.visitVarInsn( Opcodes.ISTORE, this.haveMatchBooleanVar );
	}

	private void compileReturnZeroIfNoMatch() throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label haveMatch = mv.newLabel();
		mv.visitVarInsn( Opcodes.ILOAD, this.haveMatchBooleanVar );
		mv.ifZCmp( Opcodes.IFNE, haveMatch );
		numericCompiler().compileZero();
		mv.visitInsn( typeCompiler().returnOpcode() );
		mv.mark( haveMatch );
	}


	private void compileSwitchedFold( final FoldContext _context, ExpressionNode _colIdxExpr ) throws CompilerException
	{
		final ExpressionCompilerForNumbers num = numericCompiler();
		final ExpressionNodeForDatabaseFold node = (ExpressionNodeForDatabaseFold) _context.node;

		num.compile( _colIdxExpr );
		num.compileConversionToInt();
		compileTableSwitch( node.foldableColumnKeys(), new TableSwitchGenerator()
		{

			@Override
			protected void generateCase( int _key, Label _end ) throws CompilerException
			{
				final int iCol = _key - 1;
				compileFold( _context, iCol );
				mv().goTo( _end );
			}

			@Override
			protected void generateDefault() throws CompilerException
			{
				num.compileZero();
			}

		} );

	}


}
