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

package org.formulacompiler.spreadsheet.internal.binding;

import org.formulacompiler.compiler.internal.DescriptionBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellRange;

public class RootSectionBinding extends SectionBinding
{
	/**
	 * Constructs the root binding of a workbook, which encompasses the entire workbook, but does not
	 * constitute a repeating section. Nevertheless, it has a default orientation, vertical, which
	 * determines the sort order of its subsections.
	 */
	public RootSectionBinding( WorkbookBinding _workbook, Class _inputClass, Class _outputClass )
	{
		super( _workbook, _inputClass, _outputClass, Orientation.VERTICAL );
	}


	@Override
	void checkChildInSection( ElementBinding _child, CellRange _childRange ) throws SpreadsheetException.NotInSection
	{
		// Always in root section.
	}


	@Override
	public boolean contains( CellIndex _cellIndex )
	{
		return true;
	}


	@Override
	public CellRange[] tiling( CellRange _range )
	{
		return new CellRange[]{ _range };
	}


	@Override
	public void describeTo( final DescriptionBuilder _to )
	{
		_to.append( "Spreadsheet" );
	}
}
