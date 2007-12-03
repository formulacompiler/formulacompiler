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

import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;

public abstract class RowSetup
{

	protected abstract static class Builder
	{
		abstract RowSetup newInstance( Context _cx );
	}

	private final Context cx;

	protected RowSetup( Context _cx )
	{
		this.cx = _cx;
	}

	public Context cx()
	{
		return this.cx;
	}
	

	// DO NOT REFORMAT BELOW THIS LINE
	protected abstract int startingRow();
	protected abstract int expectedCol();
	protected abstract int actualCol();
	protected abstract int nameCol();
	protected abstract int highlightCol();
	protected abstract int excelSaysCol();
	protected abstract int skipIfCol();
	protected abstract int documentedColCount();
	// DO NOT REFORMAT ABOVE THIS LINE


	public boolean isTestRow() throws Exception
	{
		final CellInstance outputCell = cx().getRowCell( actualCol() );
		if (null == outputCell) return false;
		if (outputCell instanceof CellWithLazilyParsedExpression) return true;
		if (null == outputCell.getValue()) return false;
		return true;
	}

	public boolean isTestActive() throws Exception
	{
		if (!isTestRow()) return false;

		final CellInstance skipIfCell = cx().getRowCell( skipIfCol() );
		if (null != skipIfCell) {
			final String skipIf = (String) skipIfCell.getValue();
			if (null != skipIf) {
				final BindingType type = cx().getNumberBindingType();
				// "big" is legacy support:
				if (skipIf.contains( SKIP_INDICATORS[ type.ordinal() ] )
						|| (type == BindingType.BIGDEC_SCALE && skipIf.contains( "big" ))) {
					return false;
				}
			}
		}

		return true;
	}

	private static final String[] SKIP_INDICATORS = { "double", "bprec", "bscale", "long" };


	public String getName()
	{
		final CellInstance nameCell = cx().getRowCell( nameCol() );
		return nameCell != null? (String) nameCell.getValue() : "";
	}


	public RowSetup makeOutput()
	{
		final CellInstance outputCell = cx().getRowCell( actualCol() );
		cx().setOutputCell( outputCell.getCellIndex() );
		return this;
	}

	public abstract RowSetup makeInput();

	protected RowSetup makeExpected()
	{
		final Context cx = cx();
		final CellInstance expectedCell = cx.getRowCell( expectedCol() );
		cx.setExpectedCell( expectedCell.getCellIndex() );
		return this;
	}
	
	
	public RowSetup setupValues()
	{
		final Context cx = cx();
		makeExpected();
		cx.setExpected( new Inputs( cx, cx.getExpectedCell() ) );
		cx.setInputs( new Inputs( cx, cx.getInputCells() ) );
		return this;
	}

}
