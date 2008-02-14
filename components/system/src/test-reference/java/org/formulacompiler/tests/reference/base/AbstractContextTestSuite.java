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

package org.formulacompiler.tests.reference.base;


abstract class AbstractContextTestSuite extends AbstractInitializableTestSuite
{
	private final Context cx;

	public AbstractContextTestSuite( Context _cx )
	{
		super( null );
		this.cx = _cx;
	}

	public final Context cx()
	{
		return this.cx;
	}

	@Override
	public final String getName()
	{
		return getOwnName();
	}

	/**
	 * Accessed by Ant's test error formatter, so return the full name.
	 */
	@Override
	public String toString()
	{
		return getOwnName() + " in " + cx().getDescription();
	}

	protected abstract String getOwnName();

}
