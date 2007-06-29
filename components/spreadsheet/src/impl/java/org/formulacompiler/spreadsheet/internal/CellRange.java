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

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.formulacompiler.describable.DescriptionBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;


public final class CellRange extends Reference implements Spreadsheet.Range, Iterable<CellIndex>
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


	public static final CellRange getEntireWorkbook( SpreadsheetImpl _spreadsheet )
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


	public final boolean contains( Spreadsheet.Range _other )
	{
		CellRange other = (CellRange) _other;
		return contains( other.from ) && contains( other.to );
	}

	public final boolean contains( Spreadsheet.Cell _cell )
	{
		CellIndex cell = (CellIndex) _cell;
		return between( this.from.sheetIndex, this.to.sheetIndex, cell.sheetIndex )
				&& between( this.from.rowIndex, this.to.rowIndex, cell.rowIndex )
				&& between( this.from.columnIndex, this.to.columnIndex, cell.columnIndex );
	}

	private final boolean between( int _a, int _b, int _x )
	{
		return _a <= _x && _x <= _b;
	}


	// Result lengths for tilingAround:
	public static final int NO_INTERSECTION = 0;
	public static final int CONTAINED = 1;
	public static final int FLOW_TILES = 3;
	public static final int TILES = 9;

	// Result indices for tilingAround with length is FLOW_TILES:
	public static final int FLOW_BEFORE = 0;
	public static final int FLOW_INNER = 1;
	public static final int FLOW_AFTER = 2;

	// Result indices for tilingAround with length is TILES:
	public static final int TILE_TL = 0;
	public static final int TILE_T = 1;
	public static final int TILE_TR = 2;
	public static final int TILE_L = 3;
	public static final int TILE_I = 4;
	public static final int TILE_R = 5;
	public static final int TILE_BL = 6;
	public static final int TILE_B = 7;
	public static final int TILE_BR = 8;

	/**
	 * Returns either an empty array (no intersection), a 1-long array (containment), or a 9-long
	 * array (tiling) (see CellRangeTiler).
	 */
	public CellRange[] tilingAround( CellRange _inner )
	{
		if (_inner.contains( this )) {
			return new CellRange[] { this };
		}
		else if (_inner.overlaps( this, Orientation.VERTICAL ) && _inner.overlaps( this, Orientation.HORIZONTAL )) {
			return new Tiler( _inner, this ).tiling();
		}
		else {
			return new CellRange[ NO_INTERSECTION ];
		}
	}

	/**
	 * Returns either an empty array (no intersection), a 1-long array (containment), a 3-long array
	 * (flow tiling), or a 9-long array (full tiling) (see CellRangeTiler).
	 * <p>
	 * A flow tiiling contains either null, or the tile in the sequence B I A, where B is before, I
	 * is inner, A is after.
	 */
	public CellRange[] tilingAround( CellRange _inner, Orientation _flow )
	{
		if (_inner.contains( this )) {
			return new CellRange[] { _inner };
		}
		else if (_inner.overlaps( this, Orientation.VERTICAL ) && _inner.overlaps( this, Orientation.HORIZONTAL )) {
			final CellRange[] tiling = new Tiler( _inner, this ).tiling();
			return (_flow == Orientation.VERTICAL) ? detectFlowTilingIn( tiling, TILE_T, TILE_B ) : detectFlowTilingIn(
					tiling, TILE_L, TILE_R );
		}
		else {
			return new CellRange[ NO_INTERSECTION ];
		}
	}

	private CellRange[] detectFlowTilingIn( CellRange[] _tiling, int _tileBefore, int _tileAfter )
	{
		for (int iTile = 0; iTile < _tiling.length; iTile++) {
			if (iTile != TILE_I && iTile != _tileBefore && iTile != _tileAfter) {
				if (_tiling[ iTile ] != null) return _tiling;
			}
		}
		return new CellRange[] { _tiling[ _tileBefore ], _tiling[ TILE_I ], _tiling[ _tileAfter ] };
	}

	
	public Cell getTopLeft()
	{
		return this.from;
	}

	public Cell getBottomRight()
	{
		return this.to;
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		this.from.describeTo( _to );
		_to.append( ':' );
		this.to.describeTo( _to );
	}


	/**
	 * Computes a 3x3 tiling of a range "tiled" so that the inner tile contains its intersection with
	 * a range "inner". All the tiles are either null, or the respective part of "tiled". The
	 * resulting array contains the tiles, with the following figure read left-to-right, then
	 * top-to-bottom:
	 * 
	 * <pre>
	 *  TL T  TR
	 *  L  I  R
	 *  BL B  BR
	 * </pre>
	 * 
	 * where I is the inner intersection, and L, R, T, B are left, right, top, bottom.
	 */
	@SuppressWarnings("unqualified-field-access")
	final static class Tiler
	{
		private final CellIndex i_tl;
		private final CellIndex i_br;
		private final CellIndex t_tl;
		private final CellIndex t_br;
		private final SpreadsheetImpl ss;
		private final int si;
		private final CellRange[] results;

		/**
		 * The intersection of _inner and _tiled must not be empty.
		 */
		public Tiler(CellRange _inner, CellRange _tiled)
		{
			super();
			i_tl = _inner.getFrom();
			i_br = _inner.getTo();
			t_tl = _tiled.getFrom();
			t_br = _tiled.getTo();
			ss = i_tl.spreadsheet;
			si = i_tl.sheetIndex;
			results = new CellRange[ TILES ];
		}

		public CellRange[] tiling()
		{
			intersect();
			return results;
		}

		private void intersect()
		{
			int i_b;
			if (t_br.rowIndex > i_br.rowIndex) {
				intersectRow( TILE_BL, TILE_B, TILE_BR, i_br.rowIndex + 1, t_br.rowIndex );
				i_b = i_br.rowIndex;
			}
			else {
				i_b = t_br.rowIndex;
			}
			if (t_tl.rowIndex < i_tl.rowIndex) {
				intersectRow( TILE_TL, TILE_T, TILE_TR, t_tl.rowIndex, i_tl.rowIndex - 1 );
				intersectRow( TILE_L, TILE_I, TILE_R, i_tl.rowIndex, i_b );
			}
			else {
				intersectRow( TILE_L, TILE_I, TILE_R, t_tl.rowIndex, i_b );
			}
		}

		private void intersectRow( int _left, int _mid, int _right, int _top, int _bottom )
		{
			int i_r;
			if (t_br.columnIndex > i_br.columnIndex) {
				results[ _right ] = cr( i_br.columnIndex + 1, t_br.columnIndex, _top, _bottom );
				i_r = i_br.columnIndex;
			}
			else {
				i_r = t_br.columnIndex;
			}
			if (t_tl.columnIndex < i_tl.columnIndex) {
				results[ _left ] = cr( t_tl.columnIndex, i_tl.columnIndex - 1, _top, _bottom );
				results[ _mid ] = cr( i_tl.columnIndex, i_r, _top, _bottom );
			}
			else {
				results[ _mid ] = cr( t_tl.columnIndex, i_r, _top, _bottom );
			}
		}

		private CellRange cr( int l, int r, int t, int b )
		{
			return new CellRange( new CellIndex( ss, si, l, t ), new CellIndex( ss, si, r, b ) );
		}

	}


}
