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

package org.formulacompiler.runtime.internal.spreadsheet;

import org.formulacompiler.runtime.spreadsheet.CellAddress;

/**
 * @author Vladimir Korenev
 */
public class CellAddressImpl implements CellAddress
{
	public static final int BROKEN_REF = -1;

	private final String sheetName;
	private final int columnIndex;
	private final int rowIndex;

	public CellAddressImpl( String _sheetName, int _columnIndex, int _rowIndex )
	{
		this.sheetName = _sheetName;
		this.columnIndex = _columnIndex;
		this.rowIndex = _rowIndex;
	}

	public String getSheetName()
	{
		return this.sheetName;
	}

	public int getColumnIndex()
	{
		return this.columnIndex;
	}

	public int getRowIndex()
	{
		return this.rowIndex;
	}

	@Override
	public boolean equals( final Object o )
	{
		if (this == o) return true;
		if (!(o instanceof CellAddress)) return false;

		final CellAddress cellAddress = (CellAddress) o;

		return this.columnIndex == cellAddress.getColumnIndex()
				&& this.rowIndex == cellAddress.getRowIndex()
				&& this.sheetName.equals( cellAddress.getSheetName() );
	}

	@Override
	public int hashCode()
	{
		int result;
		result = this.sheetName.hashCode();
		result = 31 * result + this.columnIndex;
		result = 31 * result + this.rowIndex;
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		appendQuotedSheetName( result, this.sheetName );
		appendNameA1ForCellIndex( result, this.columnIndex, false, this.rowIndex, false );
		return result.toString();
	}

	public static void appendQuotedSheetName( StringBuilder _result, String _sheetName )
	{
		final boolean quoted = _sheetName.contains( " " ) || _sheetName.contains( "'" ) || _sheetName.contains( "-" );
		if (quoted) {
			_result.append( '\'' );
		}
		_result.append( _sheetName.replace( "'", "''" ) );
		if (quoted) {
			_result.append( '\'' );
		}
		_result.append( "!" );
	}

	public static void appendNameA1ForCellIndex( final StringBuilder _result, final int _columnIndex, final boolean _columnIndexAbsolute, final int _rowIndex, final boolean _rowIndexAbsolute )
	{
		if (_columnIndexAbsolute) {
			_result.append( '$' );
		}
		if (_columnIndex == BROKEN_REF) {
			_result.append( "#REF!" );
		}
		else {
			appendColumn( _result, _columnIndex );
		}
		if (_rowIndexAbsolute) {
			_result.append( '$' );
		}
		if (_rowIndex == BROKEN_REF) {
			_result.append( "#REF!" );
		}
		else {
			_result.append( _rowIndex + 1 );
		}
	}

	private static void appendColumn( StringBuilder _result, int _columnIndex )
	{
		int insPos = _result.length();
		int col = _columnIndex;
		while (col >= 0) {
			int digit = col % 26;
			_result.insert( insPos, (char) ('A' + digit) );
			col = col / 26 - 1;
		}
	}

}
