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

package org.formulacompiler.spreadsheet.internal.builder;

import java.util.Date;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForFunction;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForCell;
import org.formulacompiler.spreadsheet.internal.ExpressionNodeForRange;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

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

	public static final class Factory implements SpreadsheetBuilder.Factory
	{
		public SpreadsheetBuilder newInstance()
		{
			return new SpreadsheetBuilderImpl();
		}
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
		this.cell = new CellWithExpression( this.row, nodeOf( _expr ) );
		return this;
	}

	public SpreadsheetBuilder newCell()
	{
		this.cell = new CellWithConstant( this.row, null );
		return this;
	}


	public SpreadsheetBuilder nameCell( String _name )
	{
		this.spreadsheet.defineModelRangeName( _name, this.cell.getCellIndex() );
		return this;
	}


	public CellRef currentCell()
	{
		return new CellRefImpl( this.cell.getCellIndex() );
	}


	public RangeRef range( CellRef _oneCorner, CellRef _otherCorner )
	{
		final CellIndex a = cellOf( _oneCorner );
		final CellIndex b = cellOf( _otherCorner );

		int sheetMin = Math.min( a.getSheetIndex(), b.getSheetIndex() );
		int sheetMax = Math.max( a.getSheetIndex(), b.getSheetIndex() );
		int rowMin = Math.min( a.getRowIndex(), b.getRowIndex() );
		int rowMax = Math.max( a.getRowIndex(), b.getRowIndex() );
		int colMin = Math.min( a.getColumnIndex(), b.getColumnIndex() );
		int colMax = Math.max( a.getColumnIndex(), b.getColumnIndex() );

		final CellIndex min = new CellIndex( this.spreadsheet, sheetMin, colMin, rowMin );
		final CellIndex max = new CellIndex( this.spreadsheet, sheetMax, colMax, rowMax );

		return new RangeRefImpl( CellRange.getCellRange( min, max ) );
	}

	public SpreadsheetBuilder nameRange( RangeRef _range, String _name )
	{
		this.spreadsheet.defineModelRangeName( _name, rangeOf( _range ) );
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


	private Object valueOf( Constant _const )
	{
		return ((ConstantImpl) _const).value;
	}

	private static class ConstantImpl implements Constant
	{
		final Object value;

		ConstantImpl( Object _value )
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

		ExprNodeImpl( ExpressionNode _node )
		{
			super();
			this.node = _node;
		}

	}


	private CellIndex cellOf( CellRef _expr )
	{
		return ((CellRefImpl) _expr).cell;
	}

	private static class CellRefImpl implements CellRef
	{
		final CellIndex cell;

		CellRefImpl( CellIndex _cell )
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

		RangeRefImpl( CellRange _range )
		{
			super();
			this.range = _range;
		}

	}

}
