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
	private final ExpressionNode filter;


	public HelperCompilerForDatabaseFold(SectionCompiler _section, ExpressionNodeForDatabaseFold _node,
			FoldContext _foldContext, Iterable<LetEntry> _closure)
	{
		super( _section, _node.elements(), _foldContext, _closure );
		this.table = _node.table();
		this.tableDescriptor = _node.table().arrayDescriptor();
		this.colNames = _node.filterColumnNames();
		this.filter = _node.filter();
	}


	@Override
	protected void compileFold( FoldContext _context, Iterable<ExpressionNode> _elts, int _localResult )
			throws CompilerException
	{
		final int nRows = this.tableDescriptor.getNumberOfRows();
		final int nCols = this.tableDescriptor.getNumberOfColumns();
		final List<ExpressionNode> elts = this.table.arguments();
		final String accName = _context.node.accumulatorName();
		final DataType accType = _context.node.initialAccumulatorValue().getDataType();

		expc().compile( _context.node.initialAccumulatorValue() );
		compileAccumulatorStore( _localResult );

		int iElt = 0;
		MethodCompiler matcher = null;
		for (int iRow = 0; iRow < nRows; iRow++) {

			try {
				for (int iCol = 0; iCol < nCols; iCol++) {
					final ExpressionNode elt = elts.get( iElt++ );
					letDict().let( this.colNames[ iCol ], elt.getDataType(), elt );
				}
				final Iterable<LetEntry> closure = closureOf( this.filter );
				compileClosure( closure );
				if (matcher == null) {
					matcher = new HelperCompilerForDatabaseMatch( section(), this.filter, closure );
					matcher.compile();
				}
				mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, sectionInContext().classInternalName(), matcher.methodName(),
						matcher.methodDescriptor() );
			}
			finally {
				letDict().unlet( nCols );
			}

			Label noMatch = mv().newLabel();
			mv().ifZCmp( Opcodes.IFEQ, noMatch );

			compileAccumulatorLoad( _localResult );
			letDict().let( accName, accType, ExpressionCompiler.CHAINED_FIRST_ARG );
			try {
				final int reuseLocalsAt = localsOffset();
				final ExpressionNode foldedElt = elts.get( iRow * nCols + 4 ); // FIXME
				expc().compileElementFold( _context, foldedElt );
				resetLocalsTo( reuseLocalsAt );
			}
			finally {
				letDict().unlet( accName );
			}
			compileAccumulatorStore( _localResult );

			mv().mark( noMatch );
		}

		compileAccumulatorLoad( _localResult );
	}


}
