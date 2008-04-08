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

import java.util.List;

import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;


public final class SheetImpl extends AbstractStyledElement implements Spreadsheet.Sheet
{
	private final SpreadsheetImpl spreadsheet;
	private final int sheetIndex;
	private final String name;
	private final List<RowImpl> rows = New.list();


	public SheetImpl( SpreadsheetImpl _spreadsheet )
	{
		this( _spreadsheet, "Sheet" + (_spreadsheet.getSheetList().size() + 1) );
	}


	public SheetImpl( SpreadsheetImpl _spreadsheet, String _name )
	{
		this.spreadsheet = _spreadsheet;
		this.sheetIndex = _spreadsheet.getSheetList().size();
		this.name = _name;
		_spreadsheet.getSheetList().add( this );
	}


	public SpreadsheetImpl getSpreadsheet()
	{
		return this.spreadsheet;
	}


	public final String getName()
	{
		return this.name;
	}


	public Row[] getRows()
	{
		return this.rows.toArray( new Row[this.rows.size()] );
	}


	public int getSheetIndex()
	{
		return this.sheetIndex;
	}


	public List<RowImpl> getRowList()
	{
		return this.rows;
	}


	public int getMaxColumnCount()
	{
		int result = 0;
		for (RowImpl row : getRowList()) {
			final int colCount = row.getCellList().size();
			if (colCount > result) result = colCount;
		}
		return result;
	}


	public static String getNameA1ForCellIndex( int _columnIndex, boolean _columnIndexAbsolute,
			int _rowIndex, boolean _rowIndexAbsolute )
	{
		StringBuilder result = new StringBuilder();
		appendNameA1ForCellIndex( result, _columnIndex, _columnIndexAbsolute, _rowIndex, _rowIndexAbsolute );
		return result.toString();
	}

	public static void appendNameA1ForCellIndex( final StringBuilder _result, final int _columnIndex, final boolean _columnIndexAbsolute, final int _rowIndex, final boolean _rowIndexAbsolute )
	{
		if (_columnIndexAbsolute) {
			_result.append( '$' );
		}
		if (_columnIndex <= 25) {
			_result.append( (char) ('A' + _columnIndex) );
		}
		else {
			int firstLetterIndex = _columnIndex / 26 - 1;
			int secondLetterIndex = _columnIndex % 26;
			_result.append( (char) ('A' + firstLetterIndex) );
			_result.append( (char) ('A' + secondLetterIndex) );
		}
		if (_rowIndexAbsolute) {
			_result.append( '$' );
		}
		_result.append( _rowIndex + 1 );
	}


	public void trim()
	{
		boolean canRemove = true;
		for (int i = getRowList().size() - 1; i >= 0; i--) {
			RowImpl row = getRowList().get( i );
			row.trim();
			if (canRemove) {
				if (row.getCellList().size() == 0) {
					getRowList().remove( i );
				}
				else canRemove = false;
			}
		}
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.vn( "name" ).v( getName() ).lf();
		_to.ln( "rows" ).l( getRowList() );
	}


}
