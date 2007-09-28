/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.model.rewriting;

import static org.formulacompiler.compiler.Function.*;
import static org.formulacompiler.compiler.Operator.*;
import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldDefinition;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldList;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldVectors;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFoldWrapping;
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
			return rewrite( _expr );
		}
		finally {
			this.model = null;
		}
	}


	protected final ExpressionNode rewrite( ExpressionNode _expr ) throws CompilerException
	{
		assert this.model != null;
		ExpressionNode result = _expr;
		try {
			if (_expr instanceof ExpressionNodeForFunction) {
				result = rewriteFun( (ExpressionNodeForFunction) _expr );
			}
		}
		catch (InnerExpressionException e) {
			throw e;
		}
		catch (CompilerException e) {
			throw new InnerExpressionException( _expr, e );
		}
		return rewriteArgsOf( result );
	}


	private ExpressionNode rewriteArgsOf( ExpressionNode _expr ) throws CompilerException
	{
		if (null == _expr) {
			return null;
		}
		else {
			final List<ExpressionNode> args = _expr.arguments();
			for (int iArg = 0; iArg < args.size(); iArg++) {
				final ExpressionNode arg = args.get( iArg );
				final ExpressionNode rewritten = rewrite( arg );
				if (rewritten != arg) {
					args.set( iArg, rewritten );
				}
			}
			return _expr;
		}
	}


	private ExpressionNode rewriteFun( ExpressionNodeForFunction _fun ) throws CompilerException
	{
		ExpressionNodeForFunction curr = _fun;
		ExpressionNode rewritten = rewriteFunOnce( curr );
		while (rewritten != curr && rewritten instanceof ExpressionNodeForFunction) {
			curr = (ExpressionNodeForFunction) rewritten;
			rewritten = rewriteFunOnce( curr );
		}
		return rewritten;
	}


	private ExpressionNode rewriteFunOnce( ExpressionNodeForFunction _fun ) throws CompilerException
	{
		switch (_fun.getFunction()) {

			// TODO Define externally.
			case SUM:
				return rewriteAgg( _fun, foldSUM() );
			case PRODUCT:
				return rewriteAgg( _fun, foldPRODUCT() );
			case MIN:
				return rewriteAgg( _fun, foldMIN() );
			case MAX:
				return rewriteAgg( _fun, foldMAX() );
			case VAR:
				return rewriteAgg( _fun, foldVAR() );
			case VARP:
				return rewriteAgg( _fun, foldVARP() );
			case COVAR:
				return new ExpressionNodeForFoldVectors( foldCOVAR(), substitution( _fun.argument( 0 ) ),
						substitution( _fun.argument( 1 ) ) );
			case NPV:
				final ExpressionNodeForFoldWrapping wrap = foldNPV( substitution( _fun.argument( 0 ) ) );
				return wrap.inject( new ExpressionNodeForFoldVectors( wrap.fold(), substitution( _fun.argument( 1 ) ) ) );

			case DSUM:
				return rewriteDAgg( _fun, foldSUM() );
			case DPRODUCT:
				return rewriteDAgg( _fun, foldPRODUCT() );
			case DMIN:
				return rewriteDAgg( _fun, foldMIN() );
			case DMAX:
				return rewriteDAgg( _fun, foldMAX() );
			case DCOUNT:
				return rewriteDAgg( _fun, foldCOUNT() );

			case ISNONTEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.STRING != arg.getDataType()? TRUE : FALSE;
			}
			case ISNUMBER: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.NUMERIC == arg.getDataType()? TRUE : FALSE;
			}
			case ISTEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.STRING == arg.getDataType()? TRUE : FALSE;
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


	private ExpressionNodeForFoldList rewriteAgg( ExpressionNodeForFunction _fun, final ExpressionNode _fold )
	{
		return new ExpressionNodeForFoldList( _fold, substitution( _fun.arguments() ) );
	}

	private ExpressionNode rewriteDAgg( ExpressionNodeForFunction _fun, ExpressionNode _fold ) throws CompilerException
	{
		return new FunctionRewriterForDatabaseFold( model(), _fun, this.numericType, sanitizer(), _fold ).rewrite();
	}


	private ExpressionNode foldSUM()
	{
		return new ExpressionNodeForFoldDefinition( "acc", ZERO, null, "xi", op( PLUS, var( "acc" ), var( "xi" ) ), true );
	}

	private ExpressionNode foldPRODUCT()
	{
		return new ExpressionNodeForFoldDefinition( "acc", ONE, null, "xi", op( TIMES, var( "acc" ), var( "xi" ) ), ZERO,
				true );
	}

	private ExpressionNode foldMIN()
	{
		return foldMINMAX( INTERNAL_MIN, this.numericType.maxValue() );
	}

	private ExpressionNode foldMAX()
	{
		return foldMINMAX( INTERNAL_MAX, this.numericType.minValue() );
	}

	private ExpressionNode foldMINMAX( Operator _step, Object _initial )
	{
		return new ExpressionNodeForFoldDefinition( "acc", cst( _initial ), null, "xi", op( _step, var( "acc" ),
				var( "xi" ) ), ZERO, true );
	}

	private static final String[] NO_NAMES = new String[ 0 ];
	private static final ExpressionNode[] NO_EXPRS = new ExpressionNode[ 0 ];

	private ExpressionNode foldCOUNT()
	{
		return new ExpressionNodeForFoldDefinition( NO_NAMES, NO_EXPRS, null, New.array( "xi" ), NO_EXPRS, "n",
				var( "n" ), ZERO, true );
	}

	private ExpressionNode foldVAR()
	{
		final ExpressionNode sx0, sxx0, n, sx, sxi, xi, sxx, sxxi, merge;
		sx0 = sxx0 = ZERO;
		n = var( "n" );
		sx = var( "sx" );
		sxx = var( "sxx" );
		xi = var( "xi" );
		sxi = op( PLUS, sx, xi );
		sxxi = op( PLUS, sxx, op( TIMES, xi, xi ) );
		merge = op( DIV, op( MINUS, sxx, op( DIV, op( TIMES, sx, sx ), n ) ), op( MINUS, n, ONE ) );
		return new ExpressionNodeForFoldDefinition( New.array( "sx", "sxx" ), New.array( sx0, sxx0 ), null, New
				.array( "xi" ), New.array( sxi, sxxi ), "n", merge, ZERO, true );
	}

	private ExpressionNode foldVARP()
	{
		final ExpressionNode sx0, sxx0, n, sx, sxi, xi, sxx, sxxi, merge;
		sx0 = sxx0 = ZERO;
		n = var( "n" );
		sx = var( "sx" );
		sxx = var( "sxx" );
		xi = var( "xi" );
		sxi = op( PLUS, sx, xi );
		sxxi = op( PLUS, sxx, op( TIMES, xi, xi ) );
		merge = op( DIV, op( MINUS, sxx, op( DIV, op( TIMES, sx, sx ), n ) ), n );
		return new ExpressionNodeForFoldDefinition( New.array( "sx", "sxx" ), New.array( sx0, sxx0 ), null, New
				.array( "xi" ), New.array( sxi, sxxi ), "n", merge, ZERO, true );
	}

	private ExpressionNode foldCOVAR()
	{
		final ExpressionNode sx0, sy0, sxy0, n, sx, sxi, xi, sy, syi, yi, sxy, sxyi, merge, fold;
		sx0 = sy0 = sxy0 = ZERO;
		n = var( "n" );
		sx = var( "sx" );
		sy = var( "sy" );
		sxy = var( "sxy" );
		xi = var( "xi" );
		yi = var( "yi" );
		sxi = op( PLUS, sx, xi );
		syi = op( PLUS, sy, yi );
		sxyi = op( PLUS, sxy, op( TIMES, xi, yi ) );
		merge = op( DIV, op( MINUS, sxy, op( DIV, op( TIMES, sx, sy ), n ) ), n );
		fold = new ExpressionNodeForFoldDefinition( New.array( "sx", "sy", "sxy" ), New.array( sx0, sy0, sxy0 ), null,
				New.array( "xi", "yi" ), New.array( sxi, syi, sxyi ), "n", merge, ZERO, true );
		return fold;
	}

	private ExpressionNodeForFoldWrapping foldNPV( ExpressionNode _rate )
	{
		final ExpressionNode rate1, r, i, vi, init, step, let;
		final ExpressionNodeForFoldDefinition fold;
		final ExpressionNodeForFoldWrapping wrap;
		rate1 = var( "rate1" );
		r = var( "r" );
		i = var( "i" );
		vi = var( "vi" );
		init = ZERO;
		step = op( PLUS, r, op( DIV, vi, op( Operator.EXP, rate1, i ) ) );
		fold = new ExpressionNodeForFoldDefinition( "r", init, "i", "vi", step, true );

		// wrap it
		let = let( "rate1", op( PLUS, _rate, ONE ), fold );
		wrap = new ExpressionNodeForFoldWrapping( let, 1 );
		return wrap;
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
		final Function matchFun = (indexNode instanceof ExpressionNodeForConstantValue)? INTERNAL_MATCH_INT : MATCH;
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
			final ExpressionNode defaultNode = ZERO;

			final ArrayDescriptor desc = arrayNode.arrayDescriptor();
			final int nArrays = (fun == HLOOKUP)? desc.numberOfRows() : desc.numberOfColumns();
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
		final ExpressionNodeForSwitch result = new ExpressionNodeForSwitch( _fun.argument( 0 ), ZERO );
		for (int iCase = 1; iCase < _fun.cardinality(); iCase++) {
			result.addArgument( new ExpressionNodeForSwitchCase( _fun.argument( iCase ), iCase ) );
		}
		return result;
	}


}
