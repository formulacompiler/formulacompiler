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

import org.formulacompiler.runtime.internal.Runtime_v2;
import org.formulacompiler.runtime.spreadsheet.CellAddress;

/**
 * @author Vladimir Korenev
 */
public class CellAddressImpl implements CellAddress
{

	private final String sheetName;
	private final int columnIndex;
	private final int rowIndex;

	public CellAddressImpl( String _sheetName, int _columnIndex, int _rowIndex )
	{
		if (null == _sheetName)
			throw new IllegalArgumentException();
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
		final StringBuilder result = new StringBuilder();
		Runtime_v2.appendQuotedSheetName( result, this.sheetName );
		result.append( "!" );
		Runtime_v2.appendNameA1ForCellIndex( result, this.columnIndex, false, this.rowIndex, false );
		return result.toString();
	}

}
