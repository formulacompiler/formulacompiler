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

package org.formulacompiler.spreadsheet.internal.odf.saver;

public class Style implements Comparable<Style>
{
	private final String name;
	private final String family;

	public Style( final String _name, final String _family )
	{
		this.name = _name;
		this.family = _family;
	}

	public String getName()
	{
		return this.name;
	}

	public String getFamily()
	{
		return this.family;
	}

	@Override
	public boolean equals( final Object o )
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Style style = (Style) o;

		return this.family.equals( style.family ) &&
				this.name.equals( style.name );
	}

	@Override
	public int hashCode()
	{
		return this.name.hashCode() ^ this.family.hashCode();
	}

	@Override
	public String toString()
	{
		return "style[ name='" + this.name + "'; family='" + this.family + "' ]";
	}

	public int compareTo( final Style _style )
	{
		final int fc = this.family.compareTo( _style.family );
		return fc != 0 ? fc : this.name.compareTo( _style.name );
	}
}
