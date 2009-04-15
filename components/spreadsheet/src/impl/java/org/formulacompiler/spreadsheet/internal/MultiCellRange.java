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

package org.formulacompiler.spreadsheet.internal;

import static org.formulacompiler.runtime.internal.spreadsheet.CellAddressImpl.BROKEN_REF;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;


public final class MultiCellRange extends CellRange
{
	private final CellIndex from;
	private final CellIndex to;


	MultiCellRange( CellIndex _from, CellIndex _to )
	{
		super();
		if (_from.spreadsheet != _to.spreadsheet)
			throw new IllegalArgumentException( "From and to not from same spreadsheet for range" );
		assert _from.sheetIndex == BROKEN_REF || _to.sheetIndex == BROKEN_REF || _from.sheetIndex <= _to.sheetIndex;
		assert _from.rowIndex == BROKEN_REF || _to.rowIndex == BROKEN_REF || _from.rowIndex <= _to.rowIndex;
		assert _from.columnIndex == BROKEN_REF || _to.columnIndex == BROKEN_REF || _from.columnIndex <= _to.columnIndex;
		this.from = _from;
		this.to = _to;
	}


	@Override
	public CellIndex getFrom()
	{
		return this.from;
	}


	@Override
	public CellIndex getTo()
	{
		return this.to;
	}


	public Iterator<CellIndex> iterator()
	{
		return new CellIndexRangeIterator();
	}


	private class CellIndexRangeIterator implements Iterator<CellIndex>
	{
		private int iSheet, lastSheet;
		private int firstRow, iRow, lastRow;
		private int firstColumn, iColumn, lastColumn;


		CellIndexRangeIterator()
		{
			int firstSheet = getFrom().getSheetIndex();
			this.lastSheet = getTo().getSheetIndex();
			this.firstRow = getFrom().getRowIndex();
			this.lastRow = getTo().getRowIndex();
			this.firstColumn = getFrom().getColumnIndex();
			this.lastColumn = getTo().getColumnIndex();

			this.iSheet = firstSheet - 1;
			this.iRow = this.lastRow;
			this.iColumn = this.lastColumn;
		}


		public boolean hasNext()
		{
			return (this.iSheet < this.lastSheet) || (this.iRow < this.lastRow) || (this.iColumn < this.lastColumn);
		}


		public CellIndex next()
		{
			this.iColumn++;
			if (this.iColumn > this.lastColumn) {
				this.iColumn = this.firstColumn;
				this.iRow++;
				if (this.iRow > this.lastRow) {
					this.iRow = this.firstRow;
					this.iSheet++;
				}
			}
			if ((this.iColumn <= this.lastColumn) && (this.iRow <= this.lastRow) && (this.iSheet <= this.lastSheet)) {
				return new CellIndex( MultiCellRange.this.from.spreadsheet, this.iSheet, this.iColumn, this.iRow );
			}
			else {
				throw new NoSuchElementException();
			}
		}


		public void remove()
		{
			assert false;
		}
	}


	@Override
	public CellIndex getCellIndexRelativeTo( CellIndex _cell ) throws SpreadsheetException
	{
		if (this.from.getColumnIndex() == this.to.getColumnIndex()) {
			return new CellIndex( this.from.spreadsheet, _cell.getSheetIndex(), this.from.getColumnIndex(), _cell.getRowIndex() );
		}
		else if (this.from.getRowIndex() == this.to.getRowIndex()) {
			return new CellIndex( this.from.spreadsheet, _cell.getSheetIndex(), _cell.getColumnIndex(), this.from.getRowIndex() );
		}
		throw new SpreadsheetException.CellRangeNotUniDimensional( "Range "
				+ this + " cannot be used to specify a relative cell for " + _cell );
	}


	public final boolean contains( Spreadsheet.Range _other )
	{
		final CellRange range = (CellRange) _other;
		return this.from.getSheetIndex() <= range.getFrom().getSheetIndex()
				&& this.from.getRowIndex() <= range.getFrom().getRowIndex() && this.from.getColumnIndex() <= range.getFrom().getColumnIndex()
				&& this.to.getSheetIndex() >= range.getTo().getSheetIndex() && this.to.getRowIndex() >= range.getTo().getRowIndex()
				&& this.to.getColumnIndex() >= range.getTo().getColumnIndex();
	}


	public Spreadsheet.Cell getTopLeft()
	{
		return this.from;
	}

	public Spreadsheet.Cell getBottomRight()
	{
		return this.to;
	}

	public Iterable<Spreadsheet.Cell> cells()
	{
		final Iterator<CellIndex> baseIterator = iterator();
		return new Iterable<Spreadsheet.Cell>()
		{

			public Iterator<Spreadsheet.Cell> iterator()
			{
				return new Iterator<Spreadsheet.Cell>()
				{

					public boolean hasNext()
					{
						return baseIterator.hasNext();
					}

					public Spreadsheet.Cell next()
					{
						return baseIterator.next();
					}

					public void remove()
					{
						baseIterator.remove();
					}

				};
			}
		};
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		final CellIndex from = getFrom();
		_to.append( from );
		_to.pushContext( from.getSheet() );
		try {
			_to.append( ":" ).append( getTo() );
		} finally {
			_to.popContext();
		}
	}

	@Override
	public CellRange clone( int _colOffset, int _rowOffset )
	{
		return new MultiCellRange( (CellIndex) this.from.clone( _colOffset, _rowOffset ),
				(CellIndex) this.to.clone( _colOffset, _rowOffset ));
	}

}
