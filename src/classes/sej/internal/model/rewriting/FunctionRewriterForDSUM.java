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
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForDatabaseFold;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForLet;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.model.analysis.TypeAnnotator;
import sej.internal.model.util.InterpretedNumericType;

final class FunctionRewriterForDSUM extends AbstractExpressionRewriter
{
	private final InterpretedNumericType numericType;
	private final ExpressionNodeForArrayReference table;
	private final ExpressionNode valueColumn;
	private final ExpressionNodeForArrayReference criteria;

	public FunctionRewriterForDSUM(ExpressionNodeForFunction _fun, InterpretedNumericType _type)
	{
		super();
		this.numericType = _type;
		this.table = (ExpressionNodeForArrayReference) _fun.argument( 0 );
		this.valueColumn = _fun.argument( 1 );
		this.criteria = (ExpressionNodeForArrayReference) _fun.argument( 2 );
	}


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
		final ExpressionNode data = getShapedDataAndTypesFromTable( tableDescriptor, tableIterator, colTypes );

		final ArrayDescriptor critDescriptor = this.criteria.arrayDescriptor();
		final Iterator<ExpressionNode> critIterator = this.criteria.arguments().iterator();
		final List<String> critLabels = getLabelsFromTable( critDescriptor, critIterator );
		final int[] critCols = associateCriteriaColumnsWithTableLabels( tableLabels, critLabels );

		final ExpressionNode folded = buildFoldedColumnIndex( this.valueColumn, tableLabels );

		final FilterBuilder filterBuilder = new FilterBuilder();
		final ExpressionNode filter = filterBuilder.buildFilter( critCols, critIterator, colTypes );

		final ExpressionNodeForDatabaseFold fold = new ExpressionNodeForDatabaseFold( "col", filter, "r", cst( 0.0 ),
				"xi", op( Operator.PLUS, var( "r" ), var( "xi" ) ), folded, data );

		return filterBuilder.encloseFoldInLets( fold );
	}


	private List<String> getLabelsFromTable( ArrayDescriptor _tableDesc, Iterator<ExpressionNode> _tableElements )
			throws CompilerException
	{
		try {
			final List<String> result = new ArrayList<String>();
			final int nCol = _tableDesc.getNumberOfColumns();
			for (int iCol = 0; iCol < nCol; iCol++) {
				result.add( constantValueOf( _tableElements.next() ).toString() );
			}
			return result;
		}
		catch (NotConstantException e) {
			throw new CompilerException.UnsupportedExpression( "Database table/criteria labels must be constant values." );
		}
	}


	private ExpressionNodeForArrayReference getShapedDataAndTypesFromTable( ArrayDescriptor _tableDesc,
			Iterator<ExpressionNode> _tableElements, DataType[] _colTypes ) throws CompilerException
	{
		final ArrayDescriptor td = _tableDesc;
		final int cols = td.getNumberOfColumns();
		final ArrayDescriptor dd = new ArrayDescriptor( td.getNumberOfSheets(), td.getNumberOfRows() - 1, cols );
		final ExpressionNodeForArrayReference result = new ExpressionNodeForArrayReference( dd );

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

			if (++iCol == cols) iCol = 0;
		}

		return result;
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


	private ExpressionNode buildFoldedColumnIndex( ExpressionNode _valueColumn, List<String> _tableLabels )
			throws CompilerException
	{
		if (_valueColumn instanceof ExpressionNodeForConstantValue) {
			final ExpressionNodeForConstantValue cst = (ExpressionNodeForConstantValue) _valueColumn;
			final Object val = cst.value();
			if (val instanceof String) {
				String colName = (String) val;
				return cst( _tableLabels.indexOf( colName ) );
			}
		}
		if (DataType.NUMERIC != TypeAnnotator.annotateExpr( _valueColumn )) {
			throw new CompilerException.UnsupportedExpression(
					"The value column must either be a constant name or an index. It cannot be a computed name." );
		}
		return _valueColumn;
	}


	private final class FilterBuilder
	{
		private ExpressionNodeForLet firstLet = null;
		private ExpressionNodeForLet lastLet = null;
		private int nextCritID = 0;


		public ExpressionNode buildFilter( int[] _critCols, Iterator<ExpressionNode> _critIterator, DataType[] _colTypes )
				throws CompilerException
		{
			return join( Function.OR, buildRowFilters( _critCols, _critIterator, _colTypes ) );
		}

		private Collection<ExpressionNode> buildRowFilters( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes ) throws CompilerException
		{
			final Collection<ExpressionNode> result = new ArrayList<ExpressionNode>();
			while (_critIterator.hasNext()) {
				result.add( buildRowFilter( _critCols, _critIterator, _colTypes ) );
			}
			return result;
		}

		private ExpressionNode buildRowFilter( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes ) throws CompilerException
		{
			return join( Function.AND, buildColFilters( _critCols, _critIterator, _colTypes ) );
		}

		private Collection<ExpressionNode> buildColFilters( int[] _critCols, Iterator<ExpressionNode> _critIterator,
				DataType[] _colTypes ) throws CompilerException
		{
			final int len = _critCols.length;
			final Collection<ExpressionNode> result = new ArrayList<ExpressionNode>( len );
			for (int i = 0; i < len; i++) {
				final ExpressionNode colFilter = buildColFilter( _critCols[ i ], _critIterator.next(), _colTypes[ i ] );
				if (null != colFilter) {
					result.add( colFilter );
				}
			}
			return result;
		}

		private ExpressionNode buildColFilter( int _tableCol, ExpressionNode _criterion, DataType _type )
				throws CompilerException
		{
			return (_tableCol == FREE_FORM) ? buildFreeFormFilter( _criterion ) : buildFilterByExample( _tableCol,
					_criterion, _type );
		}


		private ExpressionNode buildFilterByExample( int _tableCol, ExpressionNode _criterion, DataType _type )
		{
			if (_criterion instanceof ExpressionNodeForConstantValue) {
				final ExpressionNodeForConstantValue cst = (ExpressionNodeForConstantValue) _criterion;
				final Object val = cst.value();
				if (null == val) {
					return null;
				}
				else if (val instanceof String) {
					return buildFilterByExample( _tableCol, (String) val, _type );
				}
			}
			else if (_criterion instanceof ExpressionNodeForOperator) {
				ExpressionNodeForOperator op = (ExpressionNodeForOperator) _criterion;
				switch (op.getOperator()) {
					case CONCAT: {
						final ExpressionNode arg0 = op.argument( 0 );
						if (arg0 instanceof ExpressionNodeForConstantValue) {
							ExpressionNodeForConstantValue cst = (ExpressionNodeForConstantValue) arg0;
							final Object val = cst.value();
							if (val instanceof String) {
								final List<ExpressionNode> args = op.arguments();
								args.remove( 0 );
								return buildFilterByExample( _tableCol, (String) val, args, _criterion );
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
			if (_criterion instanceof ExpressionNodeForConstantValue) {
				return op( _comparison, var( colName ), _criterion );
			}
			else {
				final String critName = "-crit" + this.nextCritID++;

				final ExpressionNodeForLet newLet = new ExpressionNodeForLet( critName, _criterion );
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


		private ExpressionNode buildFreeFormFilter( ExpressionNode _criterion ) throws CompilerException
		{
			// FIXME Free-form
			throw new CompilerException.UnsupportedExpression( "Free form filters not supported yet." );
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
