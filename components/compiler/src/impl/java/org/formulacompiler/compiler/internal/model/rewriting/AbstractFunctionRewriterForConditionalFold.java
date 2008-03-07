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

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;


abstract class AbstractFunctionRewriterForConditionalFold extends AbstractExpressionRewriter
{
	private final ComputationModel model;
	private final InterpretedNumericType numericType;
	private final String colPrefix;
	private final String critPrefix;

	public AbstractFunctionRewriterForConditionalFold( ComputationModel _model, InterpretedNumericType _type,
			NameSanitizer _sanitizer )
	{
		super( _sanitizer );
		this.model = _model;
		this.numericType = _type;
		this.colPrefix = "col" + newSanitizingSuffix() + "_";
		this.critPrefix = "crit" + newSanitizingSuffix() + "_";
	}

	public String colPrefix()
	{
		return this.colPrefix;
	}

	public String critPrefix()
	{
		return this.critPrefix;
	}


	protected static final int FREE_FORM = -1;

	protected final class FilterBuilder
	{
		private ExpressionNodeForLet firstLet = null;
		private ExpressionNodeForLet lastLet = null;
		private int nextCritID = 0;


		public ExpressionNode buildFilter( int[] _critCols, List<ExpressionNode> _critData, DataType[] _colTypes,
				List<CellModel> _firstRow ) throws CompilerException
		{
			return join( Function.OR, buildRowFilters( _critCols, _critData.iterator(), _colTypes, _firstRow ) );
		}

		private Collection<ExpressionNode> buildRowFilters( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes, List<CellModel> _firstRow ) throws CompilerException
		{
			final Collection<ExpressionNode> result = New.collection();
			while (_critIterator.hasNext()) {
				result.add( buildRowFilter( _critCols, _critIterator, _colTypes, _firstRow ) );
			}
			return result;
		}

		private ExpressionNode buildRowFilter( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes, List<CellModel> _firstRow ) throws CompilerException
		{
			return join( Function.AND, buildColFilters( _critCols, _critIterator, _colTypes, _firstRow ) );
		}

		private Collection<ExpressionNode> buildColFilters( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes, List<CellModel> _firstRow ) throws CompilerException
		{
			final int len = _critCols.length;
			final Collection<ExpressionNode> result = New.list( len );
			for (int iCrit = 0; iCrit < len; iCrit++) {
				final int iCol = _critCols[ iCrit ];
				final ExpressionNode criterion = _critIterator.next();
				final ExpressionNode colFilter = (iCol == FREE_FORM) ? buildFreeFormFilter( criterion, _firstRow )
						: buildFilterByExample( iCol, criterion, _colTypes[ iCol ] );
				if (null != colFilter) {
					result.add( colFilter );
				}
			}
			return result;
		}

		public ExpressionNode buildFilterByExample( int _tableCol, ExpressionNode _criterion, DataType _type )
				throws CompilerException
		{
			final Object cst = constantValueOf( _criterion );
			if (null == cst) {
				return null;
			}
			else if (cst instanceof String) {
				return buildFilterByExample( _tableCol, (String) cst, _type );
			}
			else {
				final ExpressionNode critExpr = expressionOf( _criterion );
				if (critExpr instanceof ExpressionNodeForOperator) {
					ExpressionNodeForOperator op = (ExpressionNodeForOperator) critExpr;
					switch (op.getOperator()) {
						case CONCAT: {
							final Object cst0 = constantValueOf( op.argument( 0 ) );
							if (cst0 instanceof String) {
								final List<ExpressionNode> args = op.arguments();
								args.remove( 0 );
								return buildFilterByExample( _tableCol, (String) cst0, args, _criterion );
							}
						}
					}
				}
			}
			return buildComparison( Operator.EQUAL, _tableCol, _criterion );
		}

		private ExpressionNode buildFilterByExample( int _tableCol, final String _criterion, DataType _type )
				throws CompilerException
		{
			Operator comparison = Operator.EQUAL;
			int valueStartsAt = 0;
			if (_criterion.startsWith( "=" )) {
				valueStartsAt = 1;
			}
			else if (_criterion.startsWith( "<>" )) {
				comparison = Operator.NOTEQUAL;
				valueStartsAt = 2;
			}
			else if (_criterion.startsWith( "<=" )) {
				comparison = Operator.LESSOREQUAL;
				valueStartsAt = 2;
			}
			else if (_criterion.startsWith( ">=" )) {
				comparison = Operator.GREATEROREQUAL;
				valueStartsAt = 2;
			}
			else if (_criterion.startsWith( "<" )) {
				comparison = Operator.LESS;
				valueStartsAt = 1;
			}
			else if (_criterion.startsWith( ">" )) {
				comparison = Operator.GREATER;
				valueStartsAt = 1;
			}

			final String compareToStr = _criterion.substring( valueStartsAt );
			Object compareTo;
			switch (_type) {
				case NUMERIC:
					try {
						compareTo = numericType().fromString( compareToStr, environment() );
					}
					catch (ParseException e) {
						throw new CompilerException.UnsupportedExpression( e );
					}
					break;
				default:
					compareTo = compareToStr;
			}

			return buildComparison( comparison, _tableCol, cst( compareTo ) );
		}

		private ExpressionNode buildFilterByExample( int _tableCol, String _comparison, List<ExpressionNode> _args,
				ExpressionNode _criterion )
		{
			Operator comparison;
			if (_comparison.equals( "=" )) comparison = Operator.EQUAL;
			else if (_comparison.equals( "<>" )) comparison = Operator.NOTEQUAL;
			else if (_comparison.equals( "<=" )) comparison = Operator.LESSOREQUAL;
			else if (_comparison.equals( ">=" )) comparison = Operator.GREATEROREQUAL;
			else if (_comparison.equals( "<" )) comparison = Operator.LESS;
			else if (_comparison.equals( ">" )) comparison = Operator.GREATER;
			else {
				return op( Operator.EQUAL, var( colPrefix() + _tableCol ), _criterion );
			}
			if (_args.size() == 1) {
				/*
				 * We assume that the intention of constructs like
				 * 
				 * =">" & C11
				 * 
				 * was _not_ to cast C11 to string.
				 */
				return buildComparison( comparison, _tableCol, _args.iterator().next() );
			}
			else {
				return buildComparison( comparison, _tableCol, new ExpressionNodeForOperator( Operator.CONCAT, _args ) );
			}
		}

		private ExpressionNode buildComparison( Operator _comparison, int _tableCol, ExpressionNode _criterion )
		{
			final String colName = colPrefix() + _tableCol;

			// Unfortunately, constant folding has not been done, because the folder relies on the
			// rewriter having run first.
			if (NOT_CONST != constantValueOf( _criterion )) {
				return op( _comparison, var( colName ), _criterion );
			}
			else {
				final String critName = critPrefix() + this.nextCritID++;

				final ExpressionNodeForLet newLet = new ExpressionNodeForLet( ExpressionNodeForLet.Type.BYNAME, critName,
						_criterion );
				if (this.lastLet == null) {
					this.firstLet = this.lastLet = newLet;
				}
				else {
					this.lastLet.addArgument( newLet );
					this.lastLet = newLet;
				}

				return op( _comparison, var( colName ), var( critName ) );
			}
		}


		private ExpressionNode buildFreeFormFilter( ExpressionNode _criterion, List<CellModel> _firstRow )
				throws CompilerException
		{
			final ExpressionNode critExpr = expressionOf( _criterion );
			return replaceTableCellsByColRefs( critExpr, _firstRow );
		}

		private ExpressionNode replaceTableCellsByColRefs( ExpressionNode _node, List<CellModel> _firstRow )
				throws CompilerException
		{
			if (_node instanceof ExpressionNodeForCellModel) {
				final ExpressionNodeForCellModel cellNode = (ExpressionNodeForCellModel) _node;
				final CellModel cellModel = cellNode.getCellModel();
				final int iCol = _firstRow.indexOf( cellModel );
				if (iCol >= 0) {
					return var( colPrefix() + iCol );
				}
				else {
					return _node;
				}
			}
			else {
				final ExpressionNode clone = _node.cloneWithoutArguments();
				for (ExpressionNode arg : _node.arguments()) {
					clone.addArgument( replaceTableCellsByColRefs( arg, _firstRow ) );
				}
				return clone;
			}
		}


		private ExpressionNode join( Function _joinBy, Collection<ExpressionNode> _nodes )
		{
			if (_nodes.size() == 1) {
				return _nodes.iterator().next();
			}
			else {
				return new ExpressionNodeForFunction( _joinBy, _nodes );
			}
		}


		public ExpressionNode encloseFoldInLets( ExpressionNode _fold )
		{
			if (this.firstLet != null) {
				this.lastLet.addArgument( _fold );
				return this.firstLet;
			}
			else {
				return _fold;
			}
		}


	}


	protected final InterpretedNumericType numericType()
	{
		return this.numericType;
	}

	protected final Environment environment()
	{
		return this.model.getEnvironment();
	}

}
