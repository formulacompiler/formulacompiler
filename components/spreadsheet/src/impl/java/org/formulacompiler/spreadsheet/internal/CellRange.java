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

import org.formulacompiler.describable.AbstractDescribable;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetException;

public abstract class CellRange extends AbstractDescribable implements Spreadsheet.Range, Iterable<CellIndex>
{


	public static CellRange getCellRange( CellIndex _from, CellIndex _to )
	{
		if (_from.equals( _to )) {
			return _from;
		}
		else {
			return new MultiCellRange( _from, _to );
		}
	}

	public static CellRange getEntireWorkbook( SpreadsheetImpl _spreadsheet )
	{
		return getCellRange( CellIndex.getTopLeft( _spreadsheet ), CellIndex.getBottomRight( _spreadsheet ) );
	}


	public abstract CellIndex getFrom();


	public abstract CellIndex getTo();


	public boolean overlaps( CellRange _other, Orientation _orientation )
	{
		int l1 = this.getFrom().getIndex( _orientation );
		int r1 = this.getTo().getIndex( _orientation );
		int l2 = _other.getFrom().getIndex( _orientation );
		int r2 = _other.getTo().getIndex( _orientation );
		return !(l2 > r1 || l1 > r2);
	}


	/**
	 * Let's say range B2:B20 is called "Amount". Then, you can put the formula "=Amount*1.07" into, say, D2:D20.
	 * It will be automatically infered that "Amount" here means the respective cell on the same row from "Amount".
	 * So, when a cell formula references a range in a position where a single value is expected,
	 * it calls that range's <code>getCellIndexRelativeTo(this)</code>.
	 * This works for single-column and single-row ranges.
	 * <p/>
	 * If the range consists of only one cell, this cell is returned.
	 *
	 * @param _cell base cell.
	 * @return relative cell in the range.
	 * @throws SpreadsheetException if range is not unidimensional.
	 */
	public abstract CellIndex getCellIndexRelativeTo( CellIndex _cell ) throws SpreadsheetException;


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
		return new CellRange[]{ _tiling[ _tileBefore ], _tiling[ TILE_I ], _tiling[ _tileAfter ] };
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
	@SuppressWarnings( "unqualified-field-access" )
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
		public Tiler( CellRange _inner, CellRange _tiled )
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
			return getCellRange( new CellIndex( ss, si, l, t ), new CellIndex( ss, si, r, b ) );
		}

	}


}
