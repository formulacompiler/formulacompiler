/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.compiler.internal.model.optimizer.consteval;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldApply;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.TypedResult;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;


@SuppressWarnings( "unqualified-field-access" )
abstract class EvalFoldApply extends EvalShadow
{
	protected final ExpressionNodeForFoldDefinition fold;
	private final boolean notCommutative;

	public EvalFoldApply( ExpressionNodeForFoldApply _node, InterpretedNumericType _type )
	{
		super( _node, _type );
		this.fold = _node.fold();
		this.notCommutative = !fold.mayRearrange();
	}


	@Override
	protected TypedResult evaluateToConst( TypedResult... _args ) throws CompilerException
	{
		throw new IllegalStateException( "EvalFoldList.evaluateToConst() should never be called" );
	}


	private EvalFoldDefinition foldEval;

	@Override
	protected final TypedResult eval() throws CompilerException
	{
		this.foldEval = (EvalFoldDefinition) arguments().get( 0 );
		final TypedResult[] args = new TypedResult[ node().arguments().size() ];
		args[ 0 ] = evaluateArgument( 0 ); // fold
		for (int i = 1; i < args.length; i++) {
			args[ i ] = evaluateArgument( i );
		}
		return evaluateToConstOrExprWithConstantArgsFixed( args, 1 );
	}


	private final Collection<ExpressionNode[]> dynArgs = New.collection();
	private TypedResult[] acc;
	private boolean canStillFold = true;
	private int index = 0;
	private int partialStepCount = 0;

	protected TypedResult evaluateToConstOrExprWithConstantArgsFixed( TypedResult[] _args, int _firstFoldedArg )
			throws CompilerException
	{
		final TypedResult[] initialAcc = initials( _args );
		this.acc = initialAcc.clone();
		if (!fold.isIndexed() && areConstant( acc )) {

			traverse( _args, _firstFoldedArg );

			if (dynArgs.size() == 0) {
				return finalize( acc );
			}
			else {
				final boolean sameAcc = Arrays.equals( acc, initialAcc );
				return partialFold( acc, !sameAcc );
			}
		}
		else {
			return evaluateToNode( _args );
		}
	}


	private TypedResult[] initials( TypedResult[] _args ) throws CompilerException
	{
		final TypedResult[] result = new TypedResult[ fold.accuCount() ];
		for (int i = 0; i < result.length; i++) {
			result[ i ] = foldEval.evaluateArgument( i );
		}
		return result;
	}


	private TypedResult finalize( TypedResult[] _acc ) throws CompilerException
	{
		final int nAcc = _acc.length;
		if (index == 0 && null != fold.whenEmpty()) {
			return foldEval.evaluateArgument( nAcc * 2 + 1 );
		}
		else if (null != fold.merge()) {
			for (int i = 0; i < nAcc; i++) {
				letDict().let( fold.accuName( i ), fold.accuInit( i ).getDataType(), _acc[ i ] );
			}
			final String countName = fold.countName();
			if (null != countName)
				letDict().let( countName, DataType.NUMERIC, new ConstResult( this.index, DataType.NUMERIC ) );
			try {
				return foldEval.evaluateArgument( nAcc * 2 );
			}
			finally {
				if (null != countName) letDict().unlet( countName );
				letDict().unlet( nAcc );
			}
		}
		else {
			return _acc[ 0 ];
		}
	}


	protected abstract void traverse( TypedResult[] _args, int _firstFoldedArg ) throws CompilerException;


	protected final void traverseElements( TypedResult... _elts ) throws CompilerException
	{
		index++;
		if (canStillFold && areConstant( _elts )) {
			foldElements( _elts );
		}
		else {
			deferElements( _elts );
		}
	}


	private void foldElements( TypedResult[] _elts ) throws CompilerException
	{
		final int nAcc = acc.length;
		for (int i = 0; i < nAcc; i++) {
			letDict().let( fold.accuName( i ), fold.accuInit( i ).getDataType(), acc[ i ] );
		}
		final int nElt = fold.eltCount();
		for (int i = 0; i < nElt; i++) {
			letDict().let( fold.eltName( i ), _elts[ i ].getDataType(), _elts[ i ] );
		}
		final String idxName = fold.indexName();
		if (null != idxName) letDict().let( idxName, DataType.NUMERIC, this.index );
		try {
			final TypedResult[] newAcc = new TypedResult[ nAcc ];
			for (int i = 0; i < nAcc; i++) {
				newAcc[ i ] = foldEval.evaluateArgument( nAcc + i );
			}
			if (areConstant( newAcc )) {
				partialStepCount++;
				System.arraycopy( newAcc, 0, acc, 0, nAcc );
			}
			else {
				deferElements( _elts );
			}
		}
		finally {
			if (null != idxName) letDict().unlet( idxName );
			letDict().unlet( nElt );
			letDict().unlet( nAcc );
		}
	}


	private void deferElements( TypedResult[] _elts )
	{
		if (notCommutative) {
			canStillFold = false;
		}
		final ExpressionNode[] dyn = new ExpressionNode[ _elts.length ];
		for (int i = 0; i < _elts.length; i++)
			dyn[ i ] = valueToNode( _elts[ i ] );
		dynArgs.add( dyn );
	}


	private TypedResult partialFold( TypedResult[] _initials, boolean _initialsChanged )
	{
		final ExpressionNodeForFoldDefinition newFold;
		if (_initialsChanged || fold.isCounted()) {
			final Iterator<ExpressionNode> foldArgs = fold.arguments().iterator();
			newFold = fold.cloneWithoutArgumentsAndForbidReduce();
			newFold.setPartiallyFoldedElementCount( partialStepCount );
			for (int i = 0; i < _initials.length; i++) {
				newFold.addArgument( valueToNode( _initials[ i ] ) );
				foldArgs.next();
			}
			while (foldArgs.hasNext())
				newFold.addArgument( foldArgs.next() );
		}
		else {
			newFold = fold;
		}

		final ExpressionNode result;
		result = node().cloneWithoutArguments();
		result.addArgument( newFold );
		addDynamicArgsToPartialFold( result, dynArgs );
		return result;
	}


	protected abstract void addDynamicArgsToPartialFold( final ExpressionNode _apply,
			Collection<ExpressionNode[]> _dynArgs );


}
