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

import org.formulacompiler.runtime.New;


public final class SheetImpl extends BaseSheet
{
	private final List<RowImpl> rows = New.list();

	public SheetImpl( SpreadsheetImpl _spreadsheet )
	{
		this( _spreadsheet, "Sheet" + (_spreadsheet.getSheetList().size() + 1) );
	}

	public SheetImpl( SpreadsheetImpl _spreadsheet, String _name )
	{
		super( _spreadsheet, _name, _spreadsheet.getSheetList().size() );
		_spreadsheet.getSheetList().add( this );
	}

	@Override
	public List<RowImpl> getRowList()
	{
		return this.rows;
	}

	void trim()
	{
		boolean canRemove = true;
		for (int i = this.rows.size() - 1; i >= 0; i--) {
			RowImpl row = this.rows.get( i );
			row.trim();
			if (canRemove) {
				if (row.getCellList().size() == 0) {
					this.rows.remove( i );
				}
				else canRemove = false;
			}
		}
	}
}
