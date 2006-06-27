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
package sej.internal.spreadsheet;

import java.util.Iterator;
import java.util.NoSuchElementException;

import sej.Orientation;
import sej.Spreadsheet;
import sej.SpreadsheetException;
import sej.describable.DescriptionBuilder;


public class CellRange extends Reference implements Spreadsheet.Range, Iterable<CellIndex>
{
	private CellIndex from;
	private CellIndex to;


	public CellRange(CellIndex _from, CellIndex _to)
	{
		super();
		if (_from.spreadsheet != _to.spreadsheet)
			throw new IllegalArgumentException( "From and to not from same spreadsheet for range" );
		setFromTo( _from, _to );
	}

	public CellRange(SheetImpl _sheet, String _fromCellNameOrCanonicalName, String _toCellNameOrCanonicalName,
			CellIndex _relativeTo)
	{
		this( _sheet.getCellIndexForCanonicalName( _fromCellNameOrCanonicalName, _relativeTo ), _sheet
				.getCellIndexForCanonicalName( _toCellNameOrCanonicalName, _relativeTo ) );
	}


	public static final CellRange getEntireSheet( SpreadsheetImpl _spreadsheet )
	{
		return new CellRange( CellIndex.getTopLeft( _spreadsheet ), CellIndex.getBottomRight( _spreadsheet ) );
	}


	public CellIndex getFrom()
	{
		return this.from;
	}


	public void setFrom( CellIndex _from )
	{
		setFromTo( _from, getTo() );
	}


	public CellIndex getTo()
	{
		return this.to;
	}


	public void setTo( CellIndex _to )
	{
		setFromTo( getFrom(), _to );
	}


	public void setFromTo( CellIndex _from, CellIndex _to )
	{
		assert _from.sheetIndex <= _to.sheetIndex;
		assert _from.rowIndex <= _to.rowIndex;
		assert _from.columnIndex <= _to.columnIndex;
		this.from = _from;
		this.to = _to;
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
			int firstSheet = getFrom().sheetIndex;
			this.lastSheet = getTo().sheetIndex;
			this.firstRow = getFrom().rowIndex;
			this.lastRow = getTo().rowIndex;
			this.firstColumn = getFrom().columnIndex;
			this.lastColumn = getTo().columnIndex;

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
				return new CellIndex( CellRange.this.from.spreadsheet, this.iSheet, this.iColumn, this.iRow );
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


	public boolean overlaps( CellRange _other, Orientation _orientation )
	{
		int l1 = this.from.getIndex( _orientation );
		int r1 = this.to.getIndex( _orientation );
		int l2 = _other.from.getIndex( _orientation );
		int r2 = _other.to.getIndex( _orientation );
		return !(l2 > r1 || l1 > r2);
	}


	public CellIndex getCellIndexRelativeTo( CellIndex _cell ) throws SpreadsheetException
	{
		if (this.from.columnIndex == this.to.columnIndex) {
			return new CellIndex( this.from.spreadsheet, _cell.sheetIndex, this.from.columnIndex, _cell.rowIndex );
		}
		else if (this.from.rowIndex == this.to.rowIndex) {
			return new CellIndex( this.from.spreadsheet, _cell.sheetIndex, _cell.columnIndex, this.from.rowIndex );
		}
		throw new SpreadsheetException.CellRangeNotUniDimensional( "Range "
				+ this + " cannot be used to specify a relative cell for " + _cell );
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		this.from.describeTo( _to );
		_to.append( ':' );
		this.to.describeTo( _to );
	}


}
