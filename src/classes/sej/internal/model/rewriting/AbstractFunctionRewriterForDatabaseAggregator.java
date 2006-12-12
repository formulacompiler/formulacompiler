/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.model.rewriting;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import sej.CompilerException;
import sej.Function;
import sej.Operator;
import sej.internal.expressions.ArrayDescriptor;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForDatabaseFold;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.CellModel;
import sej.internal.model.ExpressionNodeForCellModel;
import sej.internal.model.analysis.TypeAnnotator;
import sej.internal.model.util.InterpretedNumericType;

abstract class AbstractFunctionRewriterForDatabaseAggregator extends AbstractExpressionRewriter
{
	private final InterpretedNumericType numericType;
	private final ExpressionNodeForArrayReference table;
	private final ExpressionNode valueColumn;
	private final ExpressionNodeForArrayReference criteria;

	public AbstractFunctionRewriterForDatabaseAggregator(ExpressionNodeForFunction _fun, InterpretedNumericType _type)
	{
		super();
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
		// We need the type info to properly treat by-example criteria contained in strings.
		TypeAnnotator.annotateExpr( this.table );

		final ArrayDescriptor tableDescriptor = this.table.arrayDescriptor();
		final Iterator<ExpressionNode> tableIterator = this.table.arguments().iterator();
		final List<String> tableLabels = getLabelsFromTable( tableDescriptor, tableIterator );
		final DataType[] colTypes = new DataType[ tableLabels.size() ];
		for (int i = 0; i < colTypes.length; i++) {
			colTypes[ i ] = DataType.NULL;
		}
		final List<CellModel> firstRow = new ArrayList<CellModel>( colTypes.length );
		final ExpressionNode data = getShapedDataAndTypesAndFirstRowFromTable( tableDescriptor, tableIterator, colTypes,
				firstRow );
		final int[] foldableColumnKeys = getFoldableColumnKeys( colTypes );

		final ArrayDescriptor critDescriptor = this.criteria.arrayDescriptor();
		final Iterator<ExpressionNode> critIterator = this.criteria.arguments().iterator();
		final List<String> critLabels = getLabelsFromTable( critDescriptor, critIterator );
		final int[] critCols = associateCriteriaColumnsWithTableLabels( tableLabels, critLabels );

		final int foldedColumnIndex = buildFoldedColumnIndex( this.valueColumn, tableLabels );
		if (foldedColumnIndex < 0) {
			if (DataType.NUMERIC != TypeAnnotator.annotateExpr( this.valueColumn )) {
				throw new CompilerException.UnsupportedExpression(
						"The value column must either be a constant name or an index. It cannot be a computed name." );
			}
		}

		final FilterBuilder filterBuilder = new FilterBuilder();
		final ExpressionNode filter = filterBuilder.buildFilter( critCols, critIterator, colTypes, firstRow );

		final ExpressionNodeForDatabaseFold fold = new ExpressionNodeForDatabaseFold( tableDescriptor, "col", filter,
				"r", initialAccumulatorValue(), "xi", foldingStep( "r", "xi" ), foldedColumnIndex, foldableColumnKeys,
				this.valueColumn, isReduce(), isZeroForEmptySelection(), data );

		return filterBuilder.encloseFoldInLets( fold );
	}


	private List<String> getLabelsFromTable( ArrayDescriptor _tableDesc, Iterator<ExpressionNode> _tableElements )
			throws CompilerException
	{
		final List<String> result = new ArrayList<String>();
		final int nCol = _tableDesc.getNumberOfColumns();
		for (int iCol = 0; iCol < nCol; iCol++) {
			final Object cst = constantValueOf( _tableElements.next() );
			if (NOT_CONST == cst) {
				throw new CompilerException.UnsupportedExpression(
						"Database table/criteria labels must be constant values." );
			}
			result.add( cst.toString() );
		}
		return result;
	}


	private ExpressionNodeForArrayReference getShapedDataAndTypesAndFirstRowFromTable( ArrayDescriptor _tableDesc,
			Iterator<ExpressionNode> _tableElements, DataType[] _colTypes, Collection<CellModel> _firstRow )
			throws CompilerException
	{
		final ArrayDescriptor td = _tableDesc;
		final int cols = td.getNumberOfColumns();
		final ArrayDescriptor dd = new ArrayDescriptor( td.getNumberOfSheets(), td.getNumberOfRows() - 1, cols );
		final ExpressionNodeForArrayReference result = new ExpressionNodeForArrayReference( dd, false );

		boolean inFirstRow = true;
		int iCol = 0;
		while (_tableElements.hasNext()) {
			final ExpressionNode elt = _tableElements.next();
			result.addArgument( elt );

			final DataType eltType = elt.getDataType();
			final DataType colType = _colTypes[ iCol ];
			if (colType == DataType.NULL) {
				_colTypes[ iCol ] = eltType;
			}
			else if (eltType != DataType.NULL & colType != eltType) {
				throw new CompilerException.UnsupportedExpression(
						"Database table columns must have a consistent data type; in column "
								+ (iCol + 1) + " the value " + elt.describe() + " is not a " + colType + "." );
			}

			if (inFirstRow) {
				if (elt instanceof ExpressionNodeForCellModel) {
					ExpressionNodeForCellModel cellElt = (ExpressionNodeForCellModel) elt;
					_firstRow.add( cellElt.getCellModel() );
				}
			}

			if (++iCol == cols) {
				iCol = 0;
				inFirstRow = false;
			}
		}

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


		public ExpressionNode buildFilter( int[] _critCols, Iterator<ExpressionNode> _critIterator, DataType[] _colTypes,
				List<CellModel> _firstRow ) throws CompilerException
		{
			return join( Function.OR, buildRowFilters( _critCols, _critIterator, _colTypes, _firstRow ) );
		}

		private Collection<ExpressionNode> buildRowFilters( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes, List<CellModel> _firstRow ) throws CompilerException
		{
			final Collection<ExpressionNode> result = new ArrayList<ExpressionNode>();
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
			final Collection<ExpressionNode> result = new ArrayList<ExpressionNode>( len );
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

		private ExpressionNode buildFilterByExample( int _tableCol, ExpressionNode _criterion, DataType _type )
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
					compareTo = numericType().fromString( compareToStr );
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
				final String critName = "-crit" + this.nextCritID++;

				final ExpressionNodeForLet newLet = new ExpressionNodeForLet( critName, false, _criterion );
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


}