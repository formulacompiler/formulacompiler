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

package org.formulacompiler.compiler.internal.model.rewriting;

import static org.formulacompiler.compiler.Function.*;
import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;


final class ExpressionRewriter extends AbstractExpressionRewriter
{
	private final GeneratedFunctionRewriter generatedRules;
	private final InterpretedNumericType numericType;


	public ExpressionRewriter( InterpretedNumericType _type, NameSanitizer _sanitizer )
	{
		super( _sanitizer );
		this.numericType = _type;
		this.generatedRules = new GeneratedFunctionRewriter( _sanitizer );
	}


	private ComputationModel model;

	public ComputationModel model()
	{
		return this.model;
	}


	public final ExpressionNode rewrite( ComputationModel _model, ExpressionNode _expr ) throws CompilerException
	{
		this.model = _model;
		try {
			final boolean[] haveRewritten = new boolean[] { false };
			return rewrite( _expr, haveRewritten );
		}
		finally {
			this.model = null;
		}
	}


	protected final ExpressionNode rewrite( ExpressionNode _expr, boolean[] _haveRewritten ) throws CompilerException
	{
		assert this.model != null;
		ExpressionNode result = _expr;
		try {
			if (result instanceof ExpressionNodeForFunction) {
				result = rewriteFun( (ExpressionNodeForFunction) result, _haveRewritten );
			}
		}
		catch (InnerExpressionException e) {
			throw e;
		}
		catch (CompilerException e) {
			throw new InnerExpressionException( _expr, e );
		}
		return rewriteArgsOf( result, _haveRewritten );
	}


	private ExpressionNode rewriteArgsOf( ExpressionNode _expr, boolean[] _haveRewritten ) throws CompilerException
	{
		if (null == _expr) {
			return null;
		}
		else {
			final List<ExpressionNode> args = _expr.arguments();
			final boolean[] argRewritten = new boolean[] { false };
			for (int iArg = 0; iArg < args.size(); iArg++) {
				final ExpressionNode arg = args.get( iArg );
				argRewritten[ 0 ] = false;
				final ExpressionNode rewritten = rewrite( arg, argRewritten );
				if (rewritten != arg) {
					args.set( iArg, rewritten );
				}
				if (argRewritten[ 0 ]) _haveRewritten[ 0 ] = true;
			}
			if (_haveRewritten[ 0 ]) {
				_expr.setDataType( null ); // force the typer to run this again
			}
			return _expr;
		}
	}


	private ExpressionNode rewriteFun( ExpressionNodeForFunction _fun, boolean[] _haveRewritten )
			throws CompilerException
	{
		ExpressionNodeForFunction curr = _fun;
		ExpressionNode rewritten = rewriteFunOnce( curr );
		while (rewritten != curr && rewritten instanceof ExpressionNodeForFunction) {
			curr = (ExpressionNodeForFunction) rewritten;
			rewritten = rewriteFunOnce( curr );
		}
		// The next line assumes function node rewrites are never in-place.
		// This is true even for rewrites adding default values for omitted parameters.
		if (rewritten != _fun) _haveRewritten[ 0 ] = true;
		return rewritten;
	}


	private ExpressionNode rewriteFunOnce( ExpressionNodeForFunction _fun ) throws CompilerException
	{
		switch (_fun.getFunction()) {

			case CHITEST: {
				if (_fun.cardinality() < 6) {
					final ArrayDescriptor descX = ((ExpressionNodeForArrayReference) _fun.argument( 0 )).arrayDescriptor();
					final int colsX = descX.numberOfColumns();
					final int rowsX = descX.numberOfRows();
					final ArrayDescriptor descY = ((ExpressionNodeForArrayReference) _fun.argument( 1 )).arrayDescriptor();
					final int colsY = descY.numberOfColumns();
					final int rowsY = descY.numberOfRows();
					return fun( CHITEST, _fun.argument( 0 ), _fun.argument( 1 ), cst( colsX, DataType.NUMERIC ), cst( rowsX,
							DataType.NUMERIC ), cst( colsY, DataType.NUMERIC ), cst( rowsY, DataType.NUMERIC ) );
				}
				break;
			}
			case DCOUNT:
			case DCOUNTA:
				return rewriteDAgg( _fun, fold_count() );
			case DSUM:
				return rewriteDAgg( _fun, this.generatedRules.fold_sum() );
			case DPRODUCT:
				return rewriteDAgg( _fun, this.generatedRules.fold_product() );
			case DMIN:
				return rewriteDAgg( _fun, this.generatedRules.fold_min() );
			case DMAX:
				return rewriteDAgg( _fun, this.generatedRules.fold_max() );
			case DAVERAGE:
				return rewriteDAgg( _fun, this.generatedRules.fold_average() );
			case DVARP:
				return rewriteDAgg( _fun, this.generatedRules.fold_varp() );
			case DVAR:
				return rewriteDAgg( _fun, this.generatedRules.fold_var() );
			case DSTDEVP:
				return fun( Function.SQRT, rewriteDAgg( _fun, this.generatedRules.fold_varp() ) );
			case DSTDEV:
				return fun( Function.SQRT, rewriteDAgg( _fun, this.generatedRules.fold_var() ) );
			case DGET:
				return rewriteDAgg( _fun, this.generatedRules.fold_get() );

			case SUMIF:
				return rewriteAggIf( _fun, this.generatedRules.fold_sum() );
			case COUNTIF:
				return rewriteAggIf( _fun, fold_count() );

			case ISNONTEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.STRING != arg.getDataType() ? TRUE : FALSE;
			}
			case ISNUMBER: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.NUMERIC == arg.getDataType() ? TRUE : FALSE;
			}
			case ISTEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.STRING == arg.getDataType() ? TRUE : FALSE;
			}
			case VALUE: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				if (DataType.NUMERIC == arg.getDataType()) {
					return arg;
				}
				break;
			}
			case N: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				if (DataType.NUMERIC == arg.getDataType()) {
					return arg;
				}
				else {
					return ZERO;
				}
			}
			case T: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				if (DataType.STRING == arg.getDataType()) {
					return arg;
				}
				else {
					return EMPTY_STRING;
				}
			}
			case TEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				if (DataType.STRING == arg.getDataType()) {
					return arg;
				}
				break;
			}

			case LOOKUP: {
				switch (_fun.cardinality()) {
					case 2:
						return rewriteArrayLookup( _fun );
					case 3:
					case 4:
						return rewriteVectorLookup( _fun );
				}
				break;
			}
			case HLOOKUP:
			case VLOOKUP:
				return rewriteHVLookup( _fun );
			case INDEX:
				return rewriteIndex( _fun );
			case CHOOSE:
				return rewriteChoose( _fun );

		}
		return this.generatedRules.rewrite( _fun );
	}


	private ExpressionNode rewriteDAgg( ExpressionNodeForFunction _fun, ExpressionNode _fold ) throws CompilerException
	{
		return new FunctionRewriterForDatabaseFold( model(), _fun, this.numericType, sanitizer(), _fold ).rewrite();
	}

	private ExpressionNode rewriteAggIf( ExpressionNodeForFunction _fun, ExpressionNode _fold ) throws CompilerException
	{
		return new FunctionRewriterForFoldIf( model(), _fun, this.numericType, sanitizer(), _fold ).rewrite();
	}


	private static final String[] NO_NAMES = new String[ 0 ];
	private static final ExpressionNode[] NO_EXPRS = new ExpressionNode[ 0 ];

	private ExpressionNode fold_count()
	{
		return new ExpressionNodeForFoldDefinition( NO_NAMES, NO_EXPRS, null, New.array( "xi" ), NO_EXPRS, "n",
				var( "n" ), ZERO, true, true );
	}


	/**
	 * Rewrites {@code LOOKUP( x, xs, ys [,type] )} to {@code INDEX( ys, MATCH( x, xs [,type] ))}.
	 */
	private ExpressionNode rewriteVectorLookup( ExpressionNodeForFunction _fun )
	{
		// LATER Don't rewrite when over large repeating sections.
		final ExpressionNode x, xs, ys, match;
		x = _fun.argument( 0 );
		xs = _fun.argument( 1 );
		ys = _fun.argument( 2 );
		if (_fun.cardinality() >= 4) {
			final ExpressionNode type = _fun.argument( 3 );
			match = fun( INTERNAL_MATCH_INT, x, xs, type );
		}
		else {
			match = fun( INTERNAL_MATCH_INT, x, xs );
		}
		return fun( INDEX, ys, match );
	}


	private ExpressionNode rewriteArrayLookup( ExpressionNodeForFunction _fun )
	{
		final ExpressionNodeForArrayReference array = (ExpressionNodeForArrayReference) _fun.argument( 1 );
		final ArrayDescriptor desc = array.arrayDescriptor();
		final int cols = desc.numberOfColumns();
		final int rows = desc.numberOfRows();
		final Function lookupFun;
		final int index;
		if (cols > rows) {
			lookupFun = HLOOKUP;
			index = rows;
		}
		else {
			lookupFun = VLOOKUP;
			index = cols;
		}
		return fun( lookupFun, _fun.argument( 0 ), _fun.argument( 1 ), cst( index, DataType.NUMERIC ), ONE );
	}


	private ExpressionNode rewriteHVLookup( ExpressionNodeForFunction _fun )
	{
		final Function fun = _fun.getFunction();
		final ExpressionNode valueNode = _fun.argument( 0 );
		final ExpressionNodeForArrayReference arrayNode = (ExpressionNodeForArrayReference) _fun.argument( 1 );
		final ExpressionNode indexNode = _fun.argument( 2 );
		final ExpressionNode lookupArrayNode = getHVLookupSubArray( fun, arrayNode, 0 );

		final ExpressionNode matchNode;
		final Function matchFun = (indexNode instanceof ExpressionNodeForConstantValue) ? INTERNAL_MATCH_INT : MATCH;
		if (_fun.cardinality() >= 4) {
			final ExpressionNode typeNode = _fun.argument( 3 );
			matchNode = new ExpressionNodeForFunction( matchFun, valueNode, lookupArrayNode, typeNode );
		}
		else {
			matchNode = new ExpressionNodeForFunction( matchFun, valueNode, lookupArrayNode );
		}

		if (indexNode instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue constIndex = (ExpressionNodeForConstantValue) indexNode;
			final int index = this.numericType.toInt( constIndex.value(), -1 ) - 1;
			final ExpressionNode valueArrayNode = getHVLookupSubArray( fun, arrayNode, index );
			return fun( INDEX, valueArrayNode, matchNode );
		}
		else {
			final String matchRefName = "x";
			final ExpressionNode matchRefNode = var( matchRefName );
			final ExpressionNode selectorNode = indexNode;
			final ExpressionNode defaultNode = err( "#VALUE/REF! because index is out of range in H/VLOOKUP" );

			final ArrayDescriptor desc = arrayNode.arrayDescriptor();
			final int nArrays = (fun == HLOOKUP) ? desc.numberOfRows() : desc.numberOfColumns();
			final ExpressionNodeForSwitchCase[] caseNodes = new ExpressionNodeForSwitchCase[ nArrays ];
			for (int iArray = 0; iArray < nArrays; iArray++) {
				final ExpressionNode valueArrayNode = getHVLookupSubArray( fun, arrayNode, iArray );
				final ExpressionNode lookupNode = fun( INDEX, valueArrayNode, matchRefNode );
				caseNodes[ iArray ] = new ExpressionNodeForSwitchCase( lookupNode, iArray + 1 );
			}
			final ExpressionNode switchNode = new ExpressionNodeForSwitch( selectorNode, defaultNode, caseNodes );
			final ExpressionNode matchLetNode = letByName( matchRefName, matchNode, switchNode );
			return matchLetNode;
		}
	}

	private ExpressionNode getHVLookupSubArray( Function _fun, ExpressionNodeForArrayReference _arrayNode, int _index )
	{
		final ArrayDescriptor desc = _arrayNode.arrayDescriptor();
		if (_fun == HLOOKUP) {
			final int cols = desc.numberOfColumns();
			return _arrayNode.subArray( _index, 1, 0, cols );
		}
		else {
			final int rows = desc.numberOfRows();
			return _arrayNode.subArray( 0, rows, _index, 1 );
		}
	}


	/**
	 * Rewrites an inner MATCH to MATCH_INT in the first argument to get rid of unnecessary casts.
	 */
	private ExpressionNode rewriteIndex( ExpressionNodeForFunction _fun )
	{
		final List<ExpressionNode> newArgs = New.list();
		newArgs.addAll( _fun.arguments() );
		boolean rewritten = false;
		for (int iArg = 1; iArg <= 2 && iArg < _fun.cardinality(); iArg++) {
			final ExpressionNode arg = _fun.argument( iArg );
			if (arg instanceof ExpressionNodeForFunction && ((ExpressionNodeForFunction) arg).getFunction() == MATCH) {
				final ExpressionNode newArg = fun( INTERNAL_MATCH_INT );
				newArg.arguments().addAll( arg.arguments() );
				newArgs.set( iArg, newArg );
				rewritten = true;
			}
		}
		if (rewritten) {
			final ExpressionNode newFun = _fun.cloneWithoutArguments();
			newFun.arguments().addAll( newArgs );
			return newFun;
		}
		return _fun;
	}


	/**
	 * Rewrites CHOOSE to SWITCH.
	 */
	private ExpressionNode rewriteChoose( ExpressionNodeForFunction _fun )
	{
		final ExpressionNodeForSwitch result = new ExpressionNodeForSwitch( _fun.argument( 0 ),
				err( "#VALUE! because index to CHOOSE is out of range" ) );
		for (int iCase = 1; iCase < _fun.cardinality(); iCase++) {
			result.addArgument( new ExpressionNodeForSwitchCase( _fun.argument( iCase ), iCase ) );
		}
		return result;
	}


}
