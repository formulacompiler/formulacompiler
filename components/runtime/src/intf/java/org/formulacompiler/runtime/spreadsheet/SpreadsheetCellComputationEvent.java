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

package org.formulacompiler.runtime.spreadsheet;

import org.formulacompiler.runtime.event.CellComputationEvent;


/**
 * This event is emitted after a cell has been computed.
 *
 * @author Vladimir Korenev
 * @see org.formulacompiler.runtime.event.CellComputationListener
 * @see org.formulacompiler.runtime.Computation.Config#cellComputationListener
 */
public class SpreadsheetCellComputationEvent extends CellComputationEvent
{
	private final SectionInfo sectionInfo;

	/**
	 * Creates a new event.
	 *
	 * @param _source  the cell which contains the computation.
	 * @param _section the section which contains the cell.
	 * @param _value   the computed value.
	 */
	public SpreadsheetCellComputationEvent( CellInfo _source, SectionInfo _section, Object _value )
	{
		super( _source, _value );
		this.sectionInfo = _section;
	}

	/**
	 * Returns information about the cell.
	 *
	 * @return cell info.
	 */
	public CellInfo getCellInfo()
	{
		return (CellInfo) getSource();
	}

	/**
	 * Returns information about the section.
	 *
	 * @return section info.
	 */
	public SectionInfo getSectionInfo()
	{
		return this.sectionInfo;
	}


	public String toString()
	{
		return getClass().getName() + "[cell=" + this.getCellInfo() + ",section=" + this.getSectionInfo() + ",value=" + this.getValue() + "]";
	}

}
