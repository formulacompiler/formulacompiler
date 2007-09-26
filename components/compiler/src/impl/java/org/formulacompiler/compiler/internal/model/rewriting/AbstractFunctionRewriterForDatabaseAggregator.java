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

import java.text.ParseException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForDatabaseFold;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForLet;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForSubSectionModel;
import org.formulacompiler.compiler.internal.model.analysis.TypeAnnotator;
import org.formulacompiler.compiler.internal.model.interpreter.InterpretedNumericType;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.Environment;


abstract class AbstractFunctionRewriterForDatabaseAggregator extends AbstractExpressionRewriter
{
	private final ComputationModel model;
	private final ExpressionNodeForFunction fun;
	private final InterpretedNumericType numericType;
	private final ExpressionNodeForArrayReference table;
	private final ExpressionNode valueColumn;
	private final ExpressionNodeForArrayReference criteria;

	public AbstractFunctionRewriterForDatabaseAggregator( ComputationModel _model, ExpressionNodeForFunction _fun,
			InterpretedNumericType _type )
	{
		super();
		this.model = _model;
		this.fun = _fun;
		this.numericType = _type;
		this.table = (ExpressionNodeForArrayReference) _fun.argument( 0 );
		this.valueColumn = _fun.argument( 1 );
		this.criteria = (ExpressionNodeForArrayReference) _fun.argument( 2 );
	}

	final ExpressionNodeForArrayReference table()
	{
		return this.table;
	}


	protected boolean isReduce()
	{
		return false;
	}

	protected boolean isZeroForEmptySelection()
	{
		return isReduce();
	}

	protected ExpressionNode initialAccumulatorValue()
	{
		return cst( 0.0 );
	}

	protected ExpressionNode foldingStep( String _accumulatorName, String _elementName )
	{
		return op( foldingOperator(), var( _accumulatorName ), var( _elementName ) );
	}

	protected abstract Operator foldingOperator();


	public final ExpressionNode rewrite() throws CompilerException
	{
		final ArrayDescriptor tableDescriptor = this.table.arrayDescriptor();
		if (tableDescriptor.numberOfColumns() == ArrayDescriptor.DYNAMIC) {
			throw new CompilerException.SectionOrientation( "The function "
					+ this.fun.getFunction() + " can only operate on vertically repeating sections." );
		}

		// We need the type info to properly treat by-example criteria contained in strings.
		TypeAnnotator.annotateExpr( this.table );

		final List<ExpressionNode> tableElements = this.table.arguments();
		final List<String> tableLabels = getLabelsFromTable( tableDescriptor, tableElements );
		final List<ExpressionNode> dataElements = stripLabelsFromTable( tableDescriptor, tableElements );
		final List<CellModel> firstRow = getFirstRowFromTable( tableDescriptor, dataElements );
		final DataType[] colTypes = getColTypesFromFirstRow( firstRow );
		final int[] foldableColumnKeys = getFoldableColumnKeys( colTypes );
		final ExpressionNode data = getShapedDataFromTable( tableDescriptor, dataElements );

		final ArrayDescriptor critDescriptor = this.criteria.arrayDescriptor();
		final List<ExpressionNode> critElements = this.criteria.arguments();
		final List<String> critLabels = getLabelsFromTable( critDescriptor, critElements );
		final List<ExpressionNode> critData = stripLabelsFromTable( critDescriptor, critElements );
		final int[] critCols = associateCriteriaColumnsWithTableLabels( tableLabels, critLabels );

		final int foldedColumnIndex = buildFoldedColumnIndex( this.valueColumn, tableLabels );
		if (foldedColumnIndex < 0) {
			if (DataType.NUMERIC != TypeAnnotator.annotateExpr( this.valueColumn )) {
				throw new CompilerException.UnsupportedExpression(
						"The value column must either be a constant name or an index. It cannot be a computed name." );
			}
		}

		final FilterBuilder filterBuilder = new FilterBuilder();
		final ExpressionNode filter = filterBuilder.buildFilter( critCols, critData, colTypes, firstRow );

		final ExpressionNodeForDatabaseFold fold = new ExpressionNodeForDatabaseFold( tableDescriptor, "col", filter,
				"r", initialAccumulatorValue(), "xi", foldingStep( "r", "xi" ), foldedColumnIndex, foldableColumnKeys,
				this.valueColumn, colTypes, isReduce(), isZeroForEmptySelection(), data );

		return filterBuilder.encloseFoldInLets( fold );
	}


	private List<String> getLabelsFromTable( ArrayDescriptor _tableDesc, List<ExpressionNode> _tableElements )
			throws CompilerException
	{
		final ExpressionNode first = _tableElements.get( 0 );
		if (first instanceof ExpressionNodeForArrayReference) {
			return getLabelsFromTable( _tableDesc, first.arguments() );
		}
		else {
			final int nCol = _tableDesc.numberOfColumns();
			final List<String> result = New.list( nCol );
			final Iterator<ExpressionNode> iElt = _tableElements.iterator();
			for (int iCol = 0; iCol < nCol; iCol++) {
				final Object cst = constantValueOf( iElt.next() );
				if (NOT_CONST == cst) {
					throw new CompilerException.UnsupportedExpression(
							"Database table/criteria labels must be constant values." );
				}
				result.add( cst.toString() );
			}
			return result;
		}
	}

	private List<ExpressionNode> stripLabelsFromTable( ArrayDescriptor _tableDesc, List<ExpressionNode> _tableElements )
	{
		final List<ExpressionNode> result = New.list( _tableElements.size() - 1 ); // heuristic
		stripLabelsFromTableInto( _tableDesc, _tableElements, result );
		return result;
	}

	private void stripLabelsFromTableInto( ArrayDescriptor _tableDesc, List<ExpressionNode> _tableElements,
			List<ExpressionNode> _result )
	{
		final Iterator<ExpressionNode> iElt = _tableElements.iterator();
		final ExpressionNode first = iElt.next();
		if (first instanceof ExpressionNodeForArrayReference) {
			final ExpressionNodeForArrayReference firstArr = (ExpressionNodeForArrayReference) first;
			final ArrayDescriptor firstArrDesc = firstArr.arrayDescriptor();
			final int firstArrRows = firstArrDesc.numberOfRows();
			if (firstArrRows > 1) {
				final ExpressionNodeForArrayReference newFirstArr = new ExpressionNodeForArrayReference(
						new ArrayDescriptor( firstArrDesc, +0, 0, -1, 0 ) );
				stripLabelsFromTableInto( firstArrDesc, firstArr.arguments(), newFirstArr.arguments() );
				_result.add( newFirstArr );
			}
		}
		else {
			final int nCol = _tableDesc.numberOfColumns();
			for (int iCol = 1; iCol < nCol; iCol++) {
				iElt.next();
			}
		}
		while (iElt.hasNext()) {
			_result.add( iElt.next() );
		}
	}

	private List<CellModel> getFirstRowFromTable( ArrayDescriptor _tableDesc, List<ExpressionNode> _dataElements )
			throws CompilerException
	{
		final int nCol = _tableDesc.numberOfColumns();
		final List<CellModel> result = New.list( nCol );
		getFirstRowFromTableInto( 0, nCol, _dataElements.iterator(), result );
		return result;
	}

	private int getFirstRowFromTableInto( int _iCol, int _nCol, Iterator<ExpressionNode> _dataElements,
			Collection<CellModel> _row ) throws CompilerException
	{
		int iCol = _iCol;
		while (iCol < _nCol && _dataElements.hasNext()) {
			final ExpressionNode next = _dataElements.next();
			if (next instanceof ExpressionNodeForArrayReference || next instanceof ExpressionNodeForSubSectionModel) {
				iCol = getFirstRowFromTableInto( iCol, _nCol, next.arguments().iterator(), _row );
			}
			else {
				if (!(next instanceof ExpressionNodeForCellModel)) {
					throw new CompilerException.UnsupportedExpression( "Database table data must be cells." );
				}
				ExpressionNodeForCellModel cellElt = (ExpressionNodeForCellModel) next;
				_row.add( cellElt.getCellModel() );
				iCol++;
			}
		}
		return iCol;
	}


	private DataType[] getColTypesFromFirstRow( List<CellModel> _firstRow )
	{
		final DataType[] result = new DataType[ _firstRow.size() ];
		int i = 0;
		for (CellModel cell : _firstRow) {
			result[ i++ ] = cell.getDataType();
		}
		return result;
	}


	private ExpressionNodeForArrayReference getShapedDataFromTable( ArrayDescriptor _tableDesc,
			List<ExpressionNode> _tableElements )
	{
		final ArrayDescriptor dd = new ArrayDescriptor( _tableDesc, +1, 0, -1, 0 );
		final ExpressionNodeForArrayReference result = new ExpressionNodeForArrayReference( dd );
		result.arguments().addAll( _tableElements );
		return result;
	}

	private int[] getFoldableColumnKeys( DataType[] _colTypes )
	{
		int nKeys = 0;
		for (int iCol = 0; iCol < _colTypes.length; iCol++) {
			if (_colTypes[ iCol ] == DataType.NUMERIC) {
				nKeys++;
			}
		}
		final int[] keys = new int[ nKeys ];
		int iKey = 0;
		for (int iCol = 0; iCol < _colTypes.length; iCol++) {
			if (_colTypes[ iCol ] == DataType.NUMERIC) {
				keys[ iKey++ ] = iCol + 1; // Keys are 1-based.
			}
		}
		return keys;
	}


	private static final int FREE_FORM = -1;

	private int[] associateCriteriaColumnsWithTableLabels( List<String> _tableLabels, List<String> _critLabels )
	{
		final int[] result = new int[ _critLabels.size() ];
		for (int i = 0; i < result.length; i++) {
			result[ i ] = _tableLabels.indexOf( _critLabels.get( i ) );
		}
		return result;
	}


	private int buildFoldedColumnIndex( ExpressionNode _valueColumn, List<String> _tableLabels )
			throws CompilerException
	{
		Object val = constantValueOf( _valueColumn );
		if (NOT_CONST != val) {
			if (val instanceof String) {
				return _tableLabels.indexOf( val );
			}
			else if (val instanceof Number) {
				final int iCol = numericType().toInt( val, 0 ) - 1;
				if (iCol >= 0 && iCol < _tableLabels.size()) {
					return iCol;
				}
				else {
					throw new CompilerException.UnsupportedExpression( "The constant column index must be between 1 and "
							+ _tableLabels.size() + " - it is " + (iCol + 1) + "." );
				}
			}
		}
		return -1;
	}


	private final class FilterBuilder
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
				final ExpressionNode colFilter = (iCol == FREE_FORM)? buildFreeFormFilter( criterion, _firstRow )
						: buildFilterByExample( iCol, criterion, _colTypes[ iCol ] );
				if (null != colFilter) {
					result.add( colFilter );
				}
			}
			return result;
		}

		private ExpressionNode buildFilterByExample( int _tableCol, ExpressionNode _criterion, DataType _type )
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
				return op( Operator.EQUAL, var( "col" + _tableCol ), _criterion );
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
			final String colName = "col" + _tableCol;

			// Unfortunately, constant folding has not been done, because the folder relies on the
			// rewriter having run first.
			if (NOT_CONST != constantValueOf( _criterion )) {
				return op( _comparison, var( colName ), _criterion );
			}
			else {
				final String critName = "crit" + this.nextCritID++;

				final ExpressionNodeForLet newLet = new ExpressionNodeForLet( critName, false, _criterion );
				newLet.setShouldCache( false );
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
					return var( "col" + iCol );
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


		public ExpressionNode encloseFoldInLets( ExpressionNodeForDatabaseFold _fold )
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


	private final InterpretedNumericType numericType()
	{
		return this.numericType;
	}


	protected final Environment environment()
	{
		return this.model.getEnvironment();
	}


}
