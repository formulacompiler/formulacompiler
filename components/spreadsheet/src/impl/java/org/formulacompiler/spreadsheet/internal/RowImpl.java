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


public final class RowImpl extends BaseRow
{
	private final List<CellInstance> cells;

	public RowImpl( SheetImpl _sheet )
	{
		super( _sheet, _sheet.getRowList().size() );
		_sheet.getRowList().add( this );
		this.cells = New.list();
	}

	public void copy()
	{
		final RowImpl row = new RowImpl( (SheetImpl) getSheet() );
		for (CellInstance cell : this.cells) {
			if (cell != null) cell.copyTo( row );
			else row.getCellList().add( null );
		}
	}

	@Override
	public List<CellInstance> getCellList()
	{
		return this.cells;
	}

	void trim()
	{
		boolean canRemove = true;
		for (int i = getCellList().size() - 1; i >= 0; i--) {
			CellInstance cell = getCellList().get( i );
			if (canRemove) {
				if (cell == null) {
					getCellList().remove( i );
				}
				else canRemove = false;
			}
		}
	}

}
