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

package org.formulacompiler.spreadsheet.internal;

import java.util.List;

import org.formulacompiler.compiler.internal.YamlBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Row;


public abstract class BaseSheet extends AbstractStyledElement implements Spreadsheet.Sheet
{
	private final BaseSpreadsheet spreadsheet;
	private final int sheetIndex;
	private final String name;

	public BaseSheet( BaseSpreadsheet _spreadsheet, String _name, int _sheetIndex )
	{
		this.spreadsheet = _spreadsheet;
		this.name = _name;
		this.sheetIndex = _sheetIndex;
	}

	public BaseSpreadsheet getSpreadsheet()
	{
		return this.spreadsheet;
	}

	public final String getName()
	{
		return this.name;
	}

	public Row[] getRows()
	{
		final List<? extends BaseRow> rowList = getRowList();
		return rowList.toArray( new Row[rowList.size()] );
	}

	public int getSheetIndex()
	{
		return this.sheetIndex;
	}

	public abstract List<? extends BaseRow> getRowList();

	public int getMaxColumnCount()
	{
		int result = 0;
		for (BaseRow row : getRowList()) {
			final int colCount = row.getCellList().size();
			if (colCount > result) result = colCount;
		}
		return result;
	}


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		_to.vn( "name" ).v( getName() ).lf();
		_to.ln( "rows" ).l( getRowList() );
	}


}
