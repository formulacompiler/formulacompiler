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
package sej.internal.spreadsheet.builder;

import java.util.Date;

import sej.Aggregator;
import sej.Function;
import sej.Operator;
import sej.Spreadsheet;
import sej.SpreadsheetBuilder;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForAggregator;
import sej.internal.expressions.ExpressionNodeForConstantValue;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.internal.expressions.ExpressionNodeForOperator;
import sej.internal.spreadsheet.CellIndex;
import sej.internal.spreadsheet.CellInstance;
import sej.internal.spreadsheet.CellRange;
import sej.internal.spreadsheet.CellWithConstant;
import sej.internal.spreadsheet.CellWithLazilyParsedExpression;
import sej.internal.spreadsheet.ExpressionNodeForCell;
import sej.internal.spreadsheet.ExpressionNodeForRange;
import sej.internal.spreadsheet.RowImpl;
import sej.internal.spreadsheet.SheetImpl;
import sej.internal.spreadsheet.SpreadsheetImpl;

public class SpreadsheetBuilderImpl implements SpreadsheetBuilder
{
	private final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
	private SheetImpl sheet;
	private RowImpl row;
	private CellInstance cell;


	public SpreadsheetBuilderImpl()
	{
		super();
		newSheet();
	}


	public Spreadsheet getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public SpreadsheetBuilder newSheet()
	{
		this.sheet = new SheetImpl( this.spreadsheet );
		newRow();
		return this;
	}


	public SpreadsheetBuilder newRow()
	{
		this.row = new RowImpl( this.sheet );
		this.cell = null;
		return this;
	}

	
	public SpreadsheetBuilder newCell( Constant _const )
	{
		this.cell = new CellWithConstant( this.row, valueOf( _const ) );
		return this;
	}

	public SpreadsheetBuilder newCell( ExprNode _expr )
	{
		this.cell = new CellWithLazilyParsedExpression( this.row, nodeOf( _expr ) );
		return this;
	}
	
	public SpreadsheetBuilder newCell()
	{
		this.cell = new CellWithConstant( this.row, null );
		return this;
	}


	public SpreadsheetBuilder nameCell( String _name )
	{
		this.spreadsheet.addToNameMap( _name, this.cell.getCellIndex() );
		return this;
	}


	public CellRef currentCell()
	{
		return new CellRefImpl( this.cell );
	}


	public RangeRef range( CellRef _oneCorner, CellRef _otherCorner )
	{
		final CellIndex a = cellOf( _oneCorner ).getCellIndex();
		final CellIndex b = cellOf( _otherCorner ).getCellIndex();
		
		int sheetMin = Math.min( a.sheetIndex, b.sheetIndex );
		int sheetMax = Math.max( a.sheetIndex, b.sheetIndex );
		int rowMin = Math.min( a.rowIndex, b.rowIndex );
		int rowMax = Math.max( a.rowIndex, b.rowIndex );
		int colMin = Math.min( a.columnIndex, b.columnIndex );
		int colMax = Math.max( a.columnIndex, b.columnIndex );
		
		final CellIndex min = new CellIndex( this.spreadsheet, sheetMin, colMin, rowMin );
		final CellIndex max = new CellIndex( this.spreadsheet, sheetMax, colMax, rowMax );
		
		return new RangeRefImpl( new CellRange( min, max ) );
	}

	public SpreadsheetBuilder nameRange( RangeRef _range, String _name )
	{
		this.spreadsheet.addToNameMap( _name, rangeOf( _range ) );
		return this;
	}


	public SpreadsheetBuilder styleRow( String _styleName )
	{
		this.row.setStyleName( _styleName );
		return this;
	}

	public SpreadsheetBuilder styleCell( String _styleName )
	{
		this.cell.setStyleName( _styleName );
		return this;
	}


	public Constant cst( String _const )
	{
		return new ConstantImpl( _const );
	}

	public Constant cst( Number _const )
	{
		return new ConstantImpl( _const );
	}

	public Constant cst( Date _const )
	{
		return new ConstantImpl( _const );
	}

	public Constant cst( boolean _const )
	{
		return new ConstantImpl( Boolean.valueOf( _const ) );
	}


	public ExprNode ref( Constant _const )
	{
		return new ExprNodeImpl( new ExpressionNodeForConstantValue( valueOf( _const ) ) );
	}

	public ExprNode ref( CellRef _cell )
	{
		return new ExprNodeImpl( new ExpressionNodeForCell( cellOf( _cell ) ) );
	}
	
	public ExprNode ref( RangeRef _rng )
	{
		return new ExprNodeImpl( new ExpressionNodeForRange( rangeOf( _rng ) ) );
	}
	

	public ExprNode op( Operator _op, ExprNode... _args )
	{
		return new ExprNodeImpl( new ExpressionNodeForOperator( _op, nodesOf( _args ) ) );
	}

	public ExprNode fun( Function _fun, ExprNode... _args )
	{
		return new ExprNodeImpl( new ExpressionNodeForFunction( _fun, nodesOf( _args ) ) );
	}

	public ExprNode agg( Aggregator _agg, ExprNode... _args )
	{
		return new ExprNodeImpl( new ExpressionNodeForAggregator( _agg, nodesOf( _args ) ) );
	}


	private Object valueOf( Constant _const )
	{
		return ((ConstantImpl) _const).value;
	}

	private static class ConstantImpl implements Constant
	{
		final Object value;

		ConstantImpl(Object _value)
		{
			super();
			this.value = _value;
		}

	}


	private ExpressionNode nodeOf( ExprNode _expr )
	{
		return ((ExprNodeImpl) _expr).node;
	}

	private ExpressionNode[] nodesOf( ExprNode... _expr )
	{
		if (_expr == null) return null;
		ExpressionNode[] result = new ExpressionNode[ _expr.length ];
		for (int i = 0; i < _expr.length; i++) {
			result[ i ] = nodeOf( _expr[ i ] );
		}
		return result;
	}

	private static class ExprNodeImpl implements ExprNode
	{
		final ExpressionNode node;

		ExprNodeImpl(ExpressionNode _node)
		{
			super();
			this.node = _node;
		}

	}


	private CellInstance cellOf( CellRef _expr )
	{
		return ((CellRefImpl) _expr).cell;
	}

	private static class CellRefImpl implements CellRef
	{
		final CellInstance cell;

		CellRefImpl(CellInstance _cell)
		{
			super();
			this.cell = _cell;
		}

	}


	private CellRange rangeOf( RangeRef _expr )
	{
		return ((RangeRefImpl) _expr).range;
	}

	private static class RangeRefImpl implements RangeRef
	{
		final CellRange range;

		RangeRefImpl(CellRange _range)
		{
			super();
			this.range = _range;
		}

	}

}
