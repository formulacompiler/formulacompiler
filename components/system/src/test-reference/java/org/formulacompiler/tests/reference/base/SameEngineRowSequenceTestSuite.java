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


public class SameEngineRowSequenceTestSuite extends AbstractEngineCompilingTestSuite
{
	private final boolean fullyBound;
	private int nextRowIndex;

	public SameEngineRowSequenceTestSuite( Context _cx, boolean _fullyBound )
	{
		super( _cx );
		this.fullyBound = _fullyBound;
	}

	@Override
	protected String getOwnName()
	{
		final int bits = cx().getInputBindingBits();
		if (bits < 0) return "Compile";
		return "Compile; bind only " + Integer.toBinaryString( bits );
	}

	@Override
	protected void addTests() throws Exception
	{
		addTest( new EngineRunningTestCase( cx(), false ).init() );
		if (this.fullyBound) {
			this.nextRowIndex = cx().getRowIndex() + 1;
		}
	}

	public int getNextRowIndex()
	{
		assert this.fullyBound;
		return this.nextRowIndex;
	}

	@Override
	protected void setUp() throws Throwable
	{
		super.setUp();
		cx().getDocumenter().newEngineRow( cx() );
	}

}
