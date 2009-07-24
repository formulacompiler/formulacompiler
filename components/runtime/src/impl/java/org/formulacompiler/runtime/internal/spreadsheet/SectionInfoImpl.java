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

import org.formulacompiler.runtime.spreadsheet.RangeAddress;
import org.formulacompiler.runtime.spreadsheet.SectionInfo;

public class SectionInfoImpl implements SectionInfo
{
	private final String name;
	private final RangeAddress range;
	private final int index;

	public SectionInfoImpl( final String _name, final RangeAddress _range, final int _index )
	{
		this.name = _name;
		this.range = _range;
		this.index = _index;
	}

	public String getName()
	{
		return this.name;
	}

	public RangeAddress getRange()
	{
		return this.range;
	}

	public int getIndex()
	{
		return this.index;
	}

	@Override
	public String toString()
	{
		final StringBuilder sb = new StringBuilder();
		if (this.range != null) sb.append( this.range );
		else sb.append( "ROOT" );
		if (this.name != null) sb.append( '(' ).append( this.name ).append( ')' );
		if (this.index >= 0) sb.append( '[' ).append( this.index ).append( ']' );
		return sb.toString();
	}
}
