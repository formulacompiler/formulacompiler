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

import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLetVar;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitch;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForSwitchCase;
import org.formulacompiler.compiler.internal.expressions.InnerExpressionException;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;


final class ExpressionRewriter extends AbstractExpressionRewriter
{
	private static final ExpressionNodeForConstantValue ZERO_NODE = new ExpressionNodeForConstantValue( 0 );
	private static final ExpressionNodeForConstantValue ONE_NODE = new ExpressionNodeForConstantValue( 1 );
	private static final ExpressionNodeForConstantValue EMPTY_STRING_NODE = new ExpressionNodeForConstantValue( "",
			DataType.STRING );
	private final GeneratedFunctionRewriter generatedRules;
	private final InterpretedNumericType numericType;


	public ExpressionRewriter( InterpretedNumericType _type )
	{
		super();
		this.numericType = _type;
		this.generatedRules = new GeneratedFunctionRewriter();
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
			case DSUM:
				return new FunctionRewriterForDSUM( model(), _fun, this.numericType ).rewrite();
			case DPRODUCT:
				return new FunctionRewriterForDPRODUCT( model(), _fun, this.numericType ).rewrite();
			case DCOUNT:
				return new FunctionRewriterForDCOUNT( model(), _fun, this.numericType ).rewrite();
			case DMIN:
				return new FunctionRewriterForDMIN( model(), _fun, this.numericType ).rewrite();
			case DMAX:
				return new FunctionRewriterForDMAX( model(), _fun, this.numericType ).rewrite();
			case ISNONTEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.STRING != arg.getDataType()? ExpressionNode.TRUENODE : ExpressionNode.FALSENODE;
			}
			case ISNUMBER: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.NUMERIC == arg.getDataType()? ExpressionNode.TRUENODE : ExpressionNode.FALSENODE;
			}
			case ISTEXT: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				return DataType.STRING == arg.getDataType()? ExpressionNode.TRUENODE : ExpressionNode.FALSENODE;
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
					return new ExpressionNodeForConstantValue( this.numericType.zero(), DataType.NUMERIC );
				}
			}
			case T: {
				final ExpressionNode arg = _fun.argument( 0 );
				TypeAnnotator.annotateExpr( arg );
				if (DataType.STRING == arg.getDataType()) {
					return arg;
				}
				else {
					return EMPTY_STRING_NODE;
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


	/**
	 * Rewrites {@code LOOKUP( x, xs, ys [,type] )} to {@code INDEX( ys, MATCH( x, xs [,type] ))}.
	 */
	private ExpressionNode rewriteVectorLookup( ExpressionNodeForFunction _fun )
	{
		// LATER Don't rewrite when over large repeating sections.
		final ExpressionNode match;
		final ExpressionNode x = _fun.argument( 0 );
		final ExpressionNode xs = _fun.argument( 1 );
		final ExpressionNode ys = _fun.argument( 2 );
		if (_fun.cardinality() >= 4) {
			final ExpressionNode type = _fun.argument( 3 );
			match = new ExpressionNodeForFunction( Function.INTERNAL_MATCH_INT, x, xs, type );
		}
		else {
			match = new ExpressionNodeForFunction( Function.INTERNAL_MATCH_INT, x, xs );
		}
		return new ExpressionNodeForFunction( Function.INDEX, ys, match );
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
			lookupFun = Function.HLOOKUP;
			index = rows;
		}
		else {
			lookupFun = Function.VLOOKUP;
			index = cols;
		}
		return new ExpressionNodeForFunction( lookupFun, _fun.argument( 0 ), _fun.argument( 1 ),
				new ExpressionNodeForConstantValue( index ), ONE_NODE );
	}


	private ExpressionNode rewriteHVLookup( ExpressionNodeForFunction _fun )
	{
		final Function fun = _fun.getFunction();
		final ExpressionNode valueNode = _fun.argument( 0 );
		final ExpressionNodeForArrayReference arrayNode = (ExpressionNodeForArrayReference) _fun.argument( 1 );
		final ExpressionNode indexNode = _fun.argument( 2 );
		final ExpressionNode lookupArrayNode = getHVLookupSubArray( fun, arrayNode, 0 );

		final ExpressionNode matchNode;
		final Function matchFun = (indexNode instanceof ExpressionNodeForConstantValue)? Function.INTERNAL_MATCH_INT
				: Function.MATCH;
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
			return new ExpressionNodeForFunction( Function.INDEX, valueArrayNode, matchNode );
		}
		else {
			final String matchRefName = "x";
			final ExpressionNode matchRefNode = new ExpressionNodeForLetVar( matchRefName );
			final ExpressionNode selectorNode = indexNode;
			final ExpressionNode defaultNode = ZERO_NODE;

			final ArrayDescriptor desc = arrayNode.arrayDescriptor();
			final int nArrays = (fun == Function.HLOOKUP)? desc.numberOfRows() : desc.numberOfColumns();
			final ExpressionNodeForSwitchCase[] caseNodes = new ExpressionNodeForSwitchCase[ nArrays ];
			for (int iArray = 0; iArray < nArrays; iArray++) {
				final ExpressionNode valueArrayNode = getHVLookupSubArray( fun, arrayNode, iArray );
				final ExpressionNode lookupNode = new ExpressionNodeForFunction( Function.INDEX, valueArrayNode,
						matchRefNode );
				caseNodes[ iArray ] = new ExpressionNodeForSwitchCase( lookupNode, iArray + 1 );
			}
			final ExpressionNode switchNode = new ExpressionNodeForSwitch( selectorNode, defaultNode, caseNodes );
			final ExpressionNodeForLet matchLetNode = new ExpressionNodeForLet( matchRefName, matchNode, switchNode );
			matchLetNode.setShouldCache( false ); // passed as a param to switch helper just once
			return matchLetNode;
		}
	}

	private ExpressionNode getHVLookupSubArray( Function _fun, ExpressionNodeForArrayReference _arrayNode, int _index )
	{
		final ArrayDescriptor desc = _arrayNode.arrayDescriptor();
		if (_fun == Function.HLOOKUP) {
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
			if (arg instanceof ExpressionNodeForFunction
					&& ((ExpressionNodeForFunction) arg).getFunction() == Function.MATCH) {
				final ExpressionNode newArg = new ExpressionNodeForFunction( Function.INTERNAL_MATCH_INT );
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
		final ExpressionNodeForSwitch result = new ExpressionNodeForSwitch( _fun.argument( 0 ), ZERO_NODE );
		for (int iCase = 1; iCase < _fun.cardinality(); iCase++) {
			result.addArgument( new ExpressionNodeForSwitchCase( _fun.argument( iCase ), iCase ) );
		}
		return result;
	}


}
