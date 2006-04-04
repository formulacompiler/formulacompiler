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
package sej.model;

import sej.Orientation;
import sej.Spreadsheet;
import sej.describable.DescriptionBuilder;

public class CellIndex extends Reference implements Spreadsheet.Cell
{
	public static final CellIndex TOP_LEFT = new CellIndex( 0, 0, 0 );
	public static final CellIndex BOTTOM_RIGHT = new CellIndex( Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE );

	public final int sheetIndex;
	public final int columnIndex;
	public final int rowIndex;
	public final int precomputedHashCode;
	public final boolean isColumnIndexAbsolute;
	public final boolean isRowIndexAbsolute;


	public CellIndex(int _sheetIndex, int _columnIndex, boolean _columnIndexAbsolute, int _rowIndex,
			boolean _rowIndexAbsolute)
	{
		this.sheetIndex = _sheetIndex;
		this.columnIndex = _columnIndex;
		this.rowIndex = _rowIndex;
		this.isColumnIndexAbsolute = _columnIndexAbsolute;
		this.isRowIndexAbsolute = _rowIndexAbsolute;
		this.precomputedHashCode = computeHashCode();
	}


	public CellIndex(int _sheetIndex, int _columnIndex, int _rowIndex)
	{
		this( _sheetIndex, _columnIndex, false, _rowIndex, false );
	}


	private int computeHashCode()
	{
		return (this.sheetIndex * 16384) ^ this.rowIndex ^ (this.columnIndex * 512);
	}


	public boolean equals( CellIndex _other )
	{
		return this.sheetIndex == _other.sheetIndex
				&& this.rowIndex == _other.rowIndex && this.columnIndex == _other.columnIndex;
	}


	@Override
	public boolean equals( Object _obj )
	{
		if (_obj instanceof CellIndex) {
			CellIndex other = (CellIndex) _obj;
			return equals( other );
		}
		return super.equals( _obj );
	}


	@Override
	public int hashCode()
	{
		return this.precomputedHashCode;
	}


	public Sheet getSheet( Workbook _workbook )
	{
		return _workbook.getSheets().get( this.sheetIndex );
	}


	public Row getRow( Workbook _workbook )
	{
		Sheet sheet = getSheet( _workbook );
		if (this.rowIndex < sheet.getRows().size()) {
			return sheet.getRows().get( this.rowIndex );
		}
		else {
			return null;
		}
	}


	public CellInstance getCell( Workbook _workbook )
	{
		Row row = getRow( _workbook );
		if (null != row && this.columnIndex < row.getCells().size()) {
			return row.getCells().get( this.columnIndex );
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
			return new CellIndex( this.sheetIndex, _index, this.rowIndex );
		case VERTICAL:
			return new CellIndex( this.sheetIndex, this.columnIndex, _index );
		}
		assert false;
		return null;
	}


	public CellIndex getAbsoluteIndex( boolean _columnAbsolute, boolean _rowAbsolute )
	{
		return new CellIndex( this.sheetIndex, this.columnIndex, _columnAbsolute, this.rowIndex, _rowAbsolute );
	}


	@Override
	public String toString()
	{
		return Sheet.getCanonicalNameForCellIndex( this.columnIndex, this.rowIndex );
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		_to.append( toString() );
	}


}