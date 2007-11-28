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

import java.util.List;

import org.formulacompiler.spreadsheet.internal.RowImpl;

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
		addTestFor( cx(), false );
		if (this.fullyBound) {
			final List<RowImpl> rows = cx().getSheetRows();
			int iRow = cx().getRowIndex() + 1;
			while (iRow < rows.size()) {
				final Context cx = new Context( cx() );
				cx.setRow( iRow );
				if (!"...".equals( cx.getRowSetup().getName() )) break;
				addTestFor( cx, true );
				iRow++;
			}
			this.nextRowIndex = iRow;
		}
	}

	private void addTestFor( Context _cx, boolean _differingInputs )
	{
		addTest( new EngineRunningTestCase( _cx, _differingInputs ).init() );
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
