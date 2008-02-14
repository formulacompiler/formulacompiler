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

package org.formulacompiler.spreadsheet.internal.binding;

import org.formulacompiler.compiler.internal.AbstractDescribable;
import org.formulacompiler.spreadsheet.SpreadsheetException;
import org.formulacompiler.spreadsheet.internal.CellRange;

public abstract class ElementBinding extends AbstractDescribable
{
	private final SectionBinding section;


	public ElementBinding( final SectionBinding _section )
	{
		super();
		this.section = _section;
	}


	public SectionBinding getSection()
	{
		return this.section;
	}


	protected void notInSection( String _name, CellRange _ref ) throws SpreadsheetException.NotInSection
	{
		throw new SpreadsheetException.NotInSection( _name, _ref.toString(), getSection().toString(), getSection()
				.getRange().toString() );
	}


}