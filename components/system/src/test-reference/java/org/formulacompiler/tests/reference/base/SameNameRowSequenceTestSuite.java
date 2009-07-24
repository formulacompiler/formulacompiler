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

package org.formulacompiler.tests.reference.base;

import java.util.List;

import org.formulacompiler.spreadsheet.internal.RowImpl;

public class SameNameRowSequenceTestSuite extends AbstractContextTestSuite
{
	private final RowTestSetup rowTestSetup;
	private int nextRowIndex;

	public SameNameRowSequenceTestSuite( Context _cx, final RowTestSetup _setup )
	{
		super( _cx );
		this.rowTestSetup = _setup;
	}

	@Override
	protected String getOwnName()
	{
		return cx().getRowSetup().getName().replace( '(', '[' ).replace( ')', ']' );
	}

	@Override
	protected void addTests() throws Exception
	{
		int iRow = addTestFor( new Context( cx() ) );
		final List<RowImpl> rows = cx().getSheetRows();
		while (iRow < rows.size()) {
			final Context childRowCx = new Context( cx() );
			childRowCx.setRow( iRow );
			final RowSetup rowSetup = childRowCx.getRowSetup();
			if (null == rowSetup.getName() || "".equals( rowSetup.getName() )) {
				iRow = addTestFor( childRowCx );
			}
			else {
				break;
			}
		}
		this.nextRowIndex = iRow;
	}

	private int addTestFor( Context _cx ) throws Exception
	{
		if (_cx.getRowSetup().isTestActive()) {
			int[] nextIndex = new int[ 1 ];
			addTest( SheetSuiteSetup.newSameEngineRowSequence( _cx, this.rowTestSetup, nextIndex ) );
			return nextIndex[ 0 ];
		}
		else return _cx.getRowIndex() + 1;
	}

	public int getNextRowIndex()
	{
		return this.nextRowIndex;
	}


	@Override
	protected void setUp() throws Throwable
	{
		super.setUp();
		cx().getDocumenter().beginNamedSection( cx() );
	}

	@Override
	protected void tearDown() throws Throwable
	{
		cx().getDocumenter().endNamedSection();
		super.tearDown();
	}

}
