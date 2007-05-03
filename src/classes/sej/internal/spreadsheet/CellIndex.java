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

import sej.describable.DescriptionBuilder;
import sej.spreadsheet.Orientation;
import sej.spreadsheet.SpreadsheetException;
import sej.spreadsheet.Spreadsheet.Cell;

public final class CellIndex extends Reference implements Cell
{
	public final SpreadsheetImpl spreadsheet;
	public final int sheetIndex;
	public final int columnIndex;
	public final int rowIndex;
	public final int precomputedHashCode;
	public final boolean isColumnIndexAbsolute;
	public final boolean isRowIndexAbsolute;


	public CellIndex(SpreadsheetImpl _spreadsheet, int _sheetIndex, int _columnIndex, boolean _columnIndexAbsolute,
			int _rowIndex, boolean _rowIndexAbsolute)
	{
		this.spreadsheet = _spreadsheet;
		this.sheetIndex = _sheetIndex;
		this.columnIndex = _columnIndex;
		this.rowIndex = _rowIndex;
		this.isColumnIndexAbsolute = _columnIndexAbsolute;
		this.isRowIndexAbsolute = _rowIndexAbsolute;
		this.precomputedHashCode = computeHashCode();
	}


	public CellIndex(SpreadsheetImpl _spreadsheet, int _sheetIndex, int _columnIndex, int _rowIndex)
	{
		this( _spreadsheet, _sheetIndex, _columnIndex, false, _rowIndex, false );
	}


	public static final CellIndex getTopLeft( SpreadsheetImpl _spreadsheet )
	{
		return new CellIndex( _spreadsheet, 0, 0, 0 );
	}

	public static final CellIndex getBottomRight( SpreadsheetImpl _spreadsheet )
	{
		return new CellIndex( _spreadsheet, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE );
	}


	private int computeHashCode()
	{
		return (this.sheetIndex * 16384) ^ this.rowIndex ^ (this.columnIndex * 512);
	}


	public boolean equals( CellIndex _other )
	{
		return this.spreadsheet == _other.spreadsheet
				&& this.sheetIndex == _other.sheetIndex && this.rowIndex == _other.rowIndex
				&& this.columnIndex == _other.columnIndex;
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


	public SheetImpl getSheet()
	{
		return this.spreadsheet.getSheetList().get( this.sheetIndex );
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
			return ((CellWithConstant) cell).getValue();
		}
		else {
			return null;
		}
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
			return "'" + getSheet().getName() + "'!" + SheetImpl.getCanonicalNameForCellIndex( this.columnIndex, this.rowIndex );
		}
		else {
			return SheetImpl.getCanonicalNameForCellIndex( this.columnIndex, this.rowIndex );
		}
	}


	@Override
	public void describeTo( DescriptionBuilder _to )
	{
		_to.append( toString() );
	}


}