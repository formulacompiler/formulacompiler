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
package org.formulacompiler.spreadsheet.internal;

import java.util.Collections;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;

public final class CellIndex extends CellRange implements Cell
{
	public final SpreadsheetImpl spreadsheet;
	public final int sheetIndex;
	public final int columnIndex;
	public final int rowIndex;
	public final boolean isColumnIndexAbsolute;
	public final boolean isRowIndexAbsolute;


	public CellIndex( SpreadsheetImpl _spreadsheet, int _sheetIndex, int _columnIndex, boolean _columnIndexAbsolute,
			int _rowIndex, boolean _rowIndexAbsolute )
	{
		this.spreadsheet = _spreadsheet;
		this.sheetIndex = _sheetIndex;
		this.columnIndex = _columnIndex;
		this.rowIndex = _rowIndex;
		this.isColumnIndexAbsolute = _columnIndexAbsolute;
		this.isRowIndexAbsolute = _rowIndexAbsolute;
	}


	public CellIndex( SpreadsheetImpl _spreadsheet, int _sheetIndex, int _columnIndex, int _rowIndex )
	{
		this( _spreadsheet, _sheetIndex, _columnIndex, false, _rowIndex, false );
	}


	public int getColumnIndex()
	{
		return this.columnIndex;
	}


	public static final CellIndex getTopLeft( SpreadsheetImpl _spreadsheet )
	{
		return new CellIndex( _spreadsheet, 0, 0, 0 );
	}

	public static final CellIndex getBottomRight( SpreadsheetImpl _spreadsheet )
	{
		return new CellIndex( _spreadsheet, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE );
	}


	public boolean equals( CellIndex _other )
	{
		if (this == _other) return true;
		return this.spreadsheet == _other.spreadsheet
				&& this.sheetIndex == _other.sheetIndex && this.rowIndex == _other.rowIndex
				&& this.columnIndex == _other.columnIndex;
	}


	@Override
	public boolean equals( Object _obj )
	{
		if (this == _obj) return true;
		if (_obj instanceof CellIndex) {
			return equals( (CellIndex) _obj );
		}
		return false;
	}


	@Override
	public int hashCode()
	{
		// Final, immutable class, so this should be SMP safe.
		if (this.hashCode == 0) {
			this.hashCode = ((17 * 59 + this.sheetIndex) * 59 + this.rowIndex) * 59 + this.columnIndex;
		}
		return this.hashCode;
	}
	
	private transient int hashCode;


	public SheetImpl getSheet()
	{
		return this.spreadsheet.getSheetList().get( this.sheetIndex );
	}

	public int getSheetIndex()
	{
		return this.sheetIndex;
	}

	public RowImpl getRow()
	{
		SheetImpl sheet = getSheet();
		if (this.rowIndex < sheet.getRowList().size()) {
			return sheet.getRowList().get( this.rowIndex );
		}
		else {
			return null;
		}
	}


	public CellInstance getCell()
	{
		RowImpl row = getRow();
		if (null != row && this.columnIndex < row.getCellList().size()) {
			return row.getCellList().get( this.columnIndex );
		}
		else {
			return null;
		}
	}


	public Object getConstantValue()
	{
		final CellInstance cell = getCell();
		if (cell instanceof CellWithConstant) {
			return cell.getValue();
		}
		else {
			return null;
		}
	}

	public String getErrorText()
	{
		final CellInstance cell = getCell();
		if (cell instanceof CellWithError) {
			return (String) cell.getValue();
		}
		else {
			return null;
		}
	}


	public Object getValue()
	{
		final CellInstance cell = getCell();
		return (cell == null) ? null : cell.getValue();
	}


	public String getExpressionText() throws SpreadsheetException
	{
		final CellInstance cell = getCell();
		if (cell instanceof CellWithLazilyParsedExpression) {
			return ((CellWithLazilyParsedExpression) cell).getExpression().toString();
		}
		else {
			return null;
		}
	}


	public int getIndex( Orientation _orientation )
	{
		switch (_orientation) {
			case HORIZONTAL:
				return this.columnIndex;
			case VERTICAL:
				return this.rowIndex;
		}
		assert false;
		return -1;
	}


	public CellIndex setIndex( Orientation _orientation, int _index )
	{
		switch (_orientation) {
			case HORIZONTAL:
				return new CellIndex( this.spreadsheet, this.sheetIndex, _index, this.rowIndex );
			case VERTICAL:
				return new CellIndex( this.spreadsheet, this.sheetIndex, this.columnIndex, _index );
		}
		assert false;
		return null;
	}


	public CellIndex getAbsoluteIndex( boolean _columnAbsolute, boolean _rowAbsolute )
	{
		return new CellIndex( this.spreadsheet, this.sheetIndex, this.columnIndex, _columnAbsolute, this.rowIndex,
				_rowAbsolute );
	}


	@Override
	public String toString()
	{
		if (this.sheetIndex > 0) {
			return "'"
					+ getSheet().getName() + "'!" + SheetImpl.getNameA1ForCellIndex( this.columnIndex, this.rowIndex );
		}
		else {
			return SheetImpl.getNameA1ForCellIndex( this.columnIndex, this.rowIndex );
		}
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		if (_to instanceof SpreadsheetDescriptionBuilder) {
			final String name = getSheet().getSpreadsheet().getNameFor( this );
			if (null != name) {
				_to.append( name );
			}
			else {
				final SpreadsheetDescriptionBuilder b = (SpreadsheetDescriptionBuilder) _to;
				final CellIndex relativeTo = b.getRelativeTo();
				if (this.sheetIndex != ((relativeTo != null) ? relativeTo.sheetIndex : 0)) {
					_to.append( '\'' ).append( getSheet().getName() ).append( "'!" );
				}
				if (null == relativeTo) {
					_to.append( 'R' ).append( this.rowIndex + 1 ).append( 'C' ).append( this.columnIndex + 1 );
				}
				else {
					if (this.isRowIndexAbsolute) _to.append( 'R' ).append( this.rowIndex + 1 );
					else describeOffsetTo( _to, 'R', this.rowIndex - relativeTo.rowIndex );
					if (this.isColumnIndexAbsolute) _to.append( 'C' ).append( this.columnIndex + 1 );
					else describeOffsetTo( _to, 'C', this.columnIndex - relativeTo.columnIndex );
				}
			}
		}
		else {
			_to.append( toString() );
		}
	}

	private void describeOffsetTo( DescriptionBuilder _to, char _prefix, int _offset )
	{
		_to.append( _prefix );
		if (_offset != 0) {
			_to.append( '[' ).append( _offset ).append( ']' );
		}
	}

	@Override
	public CellIndex getFrom()
	{
		return this;
	}

	@Override
	public CellIndex getTo()
	{
		return this;
	}

	@Override
	public CellIndex getCellIndexRelativeTo( final CellIndex _cell ) throws SpreadsheetException
	{
		return this;
	}

	public boolean contains( final Range _other )
	{
		return equals( _other );
	}

	public Cell getTopLeft()
	{
		return this;
	}

	public Cell getBottomRight()
	{
		return this;
	}

	public Iterable<Cell> cells()
	{
		return Collections.singleton( (Cell) this );
	}

	public Iterator<CellIndex> iterator()
	{
		return new Iterator<CellIndex>()
		{
			private boolean hasNext = true;

			public boolean hasNext()
			{
				return this.hasNext;
			}

			public CellIndex next()
			{
				if (this.hasNext) {
					this.hasNext = false;
					return CellIndex.this;
				}
				throw new NoSuchElementException();
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
	}


}