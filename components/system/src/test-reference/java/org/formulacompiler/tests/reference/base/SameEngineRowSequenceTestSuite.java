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

public class SameEngineRowSequenceTestSuite extends AbstractEngineCompilingTestSuite
{
	private final boolean fullyBound;
	private int nextRowIndex;

	public SameEngineRowSequenceTestSuite( Context _cx, boolean _fullyBound )
	{
		super( "Compile row "
				+ (_cx.getRowIndex() + 1) + " with input columns " + _cx.getRowSetup().getInputIsBoundString(), _cx );
		this.fullyBound = _fullyBound;
	}

	@Override
	protected void addTests() throws Exception
	{
		addTestFor( cx().newChild() );
		if (this.fullyBound) {
			final Row[] rows = cx().getSheetRows();
			int iRow = cx().getRowIndex() + 1;
			while (iRow < rows.length) {
				final Context cx = cx().newChild();
				cx.setRow( iRow );
				if (!"...".equals( cx.getRowSetup().getName() )) break;
				addTestFor( cx );
				iRow++;
			}
			this.nextRowIndex = iRow;
		}
	}

	private void addTestFor( Context _cx )
	{
		_cx.getRowSetup().makeInput();
		addTest( new EngineRunningTestCase( "Run with input values from row " + (_cx.getRowIndex() + 1), _cx ).init() );
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
