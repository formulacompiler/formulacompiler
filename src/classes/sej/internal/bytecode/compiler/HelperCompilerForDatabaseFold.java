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


	@Override
	protected void compileFold( FoldContext _context, Iterable<ExpressionNode> _elts, int _localResult )
			throws CompilerException
	{
		final ExpressionNodeForDatabaseFold node = (ExpressionNodeForDatabaseFold) _context.node;
		final int colIdx = node.staticFoldedColumnIndex();
		if (colIdx >= 0) {
			compileFold( _context, colIdx, _localResult );
		}
		else {
			final ExpressionNode colIdxExpr = node.foldedColumnIndex();
			compileSwitchedFold( _context, colIdxExpr, _localResult );
		}
	}


	private final void compileFold( FoldContext _context, int _foldedCol, int _localResult ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final int nRows = this.tableDescriptor.getNumberOfRows();
		final int nCols = this.tableDescriptor.getNumberOfColumns();
		final List<ExpressionNode> elts = this.table.arguments();
		final int haveMatchBooleanVar = (this.isZeroForEmptySelection) ? newLocal( 1 ) : -1;

		if (this.isZeroForEmptySelection) {
			compileSetHaveMatch( haveMatchBooleanVar, false );
		}
		expc().compile( _context.node.initialAccumulatorValue() );
		compileAccumulatorStore( _localResult );

		int iElt = 0;
		MethodCompiler matcher = null;
		for (int iRow = 0; iRow < nRows; iRow++) {
			final int iFoldedElt = iElt + _foldedCol;
			final ExpressionNode foldedElt = elts.get( iFoldedElt );
			final Label noMatch = mv.newLabel();

			matcher = compileCallToMatcherAndBuildInFirstPass( nCols, elts, iElt, matcher );
			compileSkipFoldIfNoMatch( noMatch );
			if (this.isReduce) {
				compileFirstMatchCheckAndInit( _localResult, haveMatchBooleanVar, noMatch, foldedElt );
			}

			compileElementFold( _context, _localResult, foldedElt );

			if (!this.isReduce && this.isZeroForEmptySelection) {
				compileSetHaveMatch( haveMatchBooleanVar, true );
			}
			mv.mark( noMatch );

			iElt += nCols;
		}

		if (this.isZeroForEmptySelection) {
			compileReturnZeroIfNoMatch( haveMatchBooleanVar );
		}
		compileAccumulatorLoad( _localResult );
	}

	private MethodCompiler compileCallToMatcherAndBuildInFirstPass( int _nCols, List<ExpressionNode> _elts, int _iElt,
			MethodCompiler _matcher ) throws CompilerException
	{
		MethodCompiler result = _matcher;
		final GeneratorAdapter mv = mv();
		try {
			for (int iCol = 0; iCol < _nCols; iCol++) {
				final ExpressionNode elt = _elts.get( _iElt + iCol );
				letDict().let( this.colNames[ iCol ], elt.getDataType(), elt );
			}
			final Iterable<LetEntry> closure = closureOf( this.filterExpr );
			compileClosure( closure );
			if (_matcher == null) {
				result = new HelperCompilerForDatabaseMatch( section(), this.filterExpr, closure );
				result.compile();
			}
			mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, sectionInContext().classInternalName(), result.methodName(), result
					.methodDescriptor() );
		}
		finally {
			letDict().unlet( _nCols );
		}
		return result;
	}

	private void compileSkipFoldIfNoMatch( final Label _noMatch )
	{
		mv().ifZCmp( Opcodes.IFEQ, _noMatch );
	}

	private void compileFirstMatchCheckAndInit( int _localResult, int _haveMatchBooleanVar, Label _noMatch,
			ExpressionNode _foldedElt ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label haveMatch = mv.newLabel();
		mv.visitVarInsn( Opcodes.ILOAD, _haveMatchBooleanVar );
		mv.ifZCmp( Opcodes.IFNE, haveMatch );
		mv.push( true );
		mv.visitVarInsn( Opcodes.ISTORE, _haveMatchBooleanVar );
		expc().compile( _foldedElt );
		compileAccumulatorStore( _localResult );
		mv.goTo( _noMatch );
		mv.mark( haveMatch );
	}

	private void compileElementFold( FoldContext _context, int _localResult, ExpressionNode _foldedElt )
			throws CompilerException
	{
		final String accName = _context.node.accumulatorName();
		final DataType accType = _context.node.initialAccumulatorValue().getDataType();
		compileAccumulatorLoad( _localResult );
		letDict().let( accName, accType, ExpressionCompiler.CHAINED_FIRST_ARG );
		try {
			final int reuseLocalsAt = localsOffset();
			expc().compileElementFold( _context, _foldedElt );
			resetLocalsTo( reuseLocalsAt );
		}
		finally {
			letDict().unlet( accName );
		}
		compileAccumulatorStore( _localResult );
	}

	private void compileSetHaveMatch( int _haveMatchBooleanVar, boolean _value )
	{
		final GeneratorAdapter mv = mv();
		mv.push( _value );
		mv.visitVarInsn( Opcodes.ISTORE, _haveMatchBooleanVar );
	}

	private void compileReturnZeroIfNoMatch( int _haveMatchBooleanVar ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		final Label haveMatch = mv.newLabel();
		mv.visitVarInsn( Opcodes.ILOAD, _haveMatchBooleanVar );
		mv.ifZCmp( Opcodes.IFNE, haveMatch );
		numericCompiler().compileZero();
		mv.visitInsn( typeCompiler().returnOpcode() );
		mv.mark( haveMatch );
	}


	private void compileSwitchedFold( final FoldContext _context, ExpressionNode _colIdxExpr, final int _localResult )
			throws CompilerException
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
				compileFold( _context, iCol, _localResult );
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
