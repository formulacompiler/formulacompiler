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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.LetDictionary.LetEntry;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


@SuppressWarnings( "unqualified-field-access" )
abstract class HelperCompilerForFoldApply extends HelperCompilerForFolds
{
	private final boolean mustDetectEmptiness;
	private final boolean mustCount;

	public HelperCompilerForFoldApply( SectionCompiler _section, ExpressionNodeForFoldApply _applyNode,
			Iterable<LetEntry> _closure )
	{
		super( _section, _applyNode, _closure );

		/*
		 * I really hate the following snippet, but since Java does not allow me to get "vec0" before
		 * the super() call in a constructor, I see no good way to solve this using inheritance.
		 */
		boolean onlyDetectEmptyIf = true, onlyCountIf = true;
		if (argumentsAreVectors()) {
			final Iterable<ExpressionNode> vec0 = ChainedFoldCompiler.firstVectorOf( apply.elements() );
			onlyDetectEmptyIf = (null == firstLocalElementIn( vec0 ));
			onlyCountIf = ExpressionCompiler.isSubSectionIn( vec0 );
		}

		this.mustDetectEmptiness = (onlyDetectEmptyIf && fold.isSpecialWhenEmpty() && fold
				.getPartiallyFoldedElementCount() == 0);
		this.mustCount = (onlyCountIf && fold.isCounted());
	}


	protected abstract boolean argumentsAreVectors();


	@Override
	protected final void compileBody() throws CompilerException
	{
		compileSetup();
		compileTraversal();
		compileMerge();
	}


	private final void compileSetup() throws CompilerException
	{
		compileAccuSetup();
		compileIndexSetup();
	}

	private Type[] accuTypes;
	private int[] accuVars;

	private void compileAccuSetup() throws CompilerException
	{
		final int nAccus = fold.accuCount();
		accuTypes = new Type[ nAccus ];
		accuVars = new int[ nAccus ];
		for (int i = 0; i < nAccus; i++) {
			final ExpressionNode initNode = fold.accuInit( i );
			final DataType initType = initNode.getDataType();
			final Type accuType = section().engineCompiler().typeCompiler( initType ).type();
			final int accuVar = newLocal( accuType.getSize() );
			accuTypes[ i ] = accuType;
			accuVars[ i ] = accuVar;
			expressionCompiler().compile( initNode );
			compileAccumulatorStore( i );
			letDict().let( fold.accuName( i ), initType, new GeneratedRef()
			{

				public void compile( ExpressionCompiler _exp ) throws CompilerException
				{
					mv().visitVarInsn( accuType.getOpcode( Opcodes.ILOAD ), accuVar );
				}

			} );
		}
	}

	private void compileAccumulatorStore( int _iAccu )
	{
		mv().visitVarInsn( accuTypes[ _iAccu ].getOpcode( Opcodes.ISTORE ), accuVars[ _iAccu ] );
	}

	private void compileAccumulatorLoad( int _iAccu )
	{
		mv().visitVarInsn( accuTypes[ _iAccu ].getOpcode( Opcodes.ILOAD ), accuVars[ _iAccu ] );
	}

	private static final int NO_VAR = 0;
	private int indexVar = NO_VAR;
	private int staticCount = 0;

	private void compileIndexSetup()
	{
		staticCount = fold.getPartiallyFoldedElementCount();
		if (mustDetectEmptiness || mustCount || fold.isIndexed()) {
			this.indexVar = newLocal( Type.INT_TYPE.getSize() );
			mv().push( fold.getPartiallyFoldedElementCount() );
			mv().visitVarInsn( Opcodes.ISTORE, indexVar );
			letIndexVarAs( fold.indexName() );
		}
	}

	private void letIndexVarAs( String _nameOrNull )
	{
		if (null != _nameOrNull) {
			letDict().let( _nameOrNull, numericCompiler().dataType(), new GeneratedRef()
			{

				public void compile( ExpressionCompiler _exp ) throws CompilerException
				{
					mv().visitVarInsn( Opcodes.ILOAD, indexVar );
					_exp.compileConversionFrom( Integer.TYPE );
				}

			} );
		}
	}


	protected abstract void compileTraversal() throws CompilerException;

	protected final void compileFoldStepsWithEltsBound() throws CompilerException
	{
		compileIndexIncrement();
		for (int i = 0; i < fold.accuCount(); i++) {
			expressionCompiler().compile( fold.accuStep( i ) );
			compileAccumulatorStore( i );
		}
	}

	protected final void compileIndexIncrement()
	{
		staticCount++;
		if (NO_VAR != indexVar) {
			mv().visitIincInsn( indexVar, 1 );
		}
	}


	private void compileMerge() throws CompilerException
	{
		final Label exit;
		if (mustDetectEmptiness) {
			final Label notEmpty = mv().newLabel();
			mv().visitVarInsn( Opcodes.ILOAD, indexVar );
			mv().ifZCmp( Opcodes.IFGT, notEmpty );
			expressionCompiler().compile( fold.whenEmpty() );
			exit = mv().newLabel();
			mv().goTo( exit );
			mv().mark( notEmpty );
		}
		else {
			exit = null;
		}

		if (fold.isMergedExplicitly()) {
			if (mustCount) {
				letIndexVarAs( fold.countName() );
			}
			else if (fold.isCounted()) {
				letDict().let( fold.countName(), DataType.NUMERIC,
						new ExpressionNodeForConstantValue( staticCount, DataType.NUMERIC ) );
			}
			expressionCompiler().compile( fold.merge() );
		}
		else {
			assert 1 == accuVars.length;
			compileAccumulatorLoad( 0 );
		}

		if (null != exit) mv().mark( exit );
	}

}
