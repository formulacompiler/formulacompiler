/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.formulacompiler.tests.reference.base;

import org.formulacompiler.spreadsheet.Spreadsheet.Row;

public class SameNameRowSequenceTestSuite extends AbstractContextTestSuite
{
	private int nextRowIndex;

	public SameNameRowSequenceTestSuite( Context _cx )
	{
		super( _cx );
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
		final Row[] rows = cx().getSheetRows();
		while (iRow < rows.length) {
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
			addTest( SheetSuiteSetup.newSameEngineRowSequence( _cx, nextIndex ) );
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
