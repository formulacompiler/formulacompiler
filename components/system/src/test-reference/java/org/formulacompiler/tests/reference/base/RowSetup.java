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

import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;

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


	public int checkingCol()
	{
		return -1;
	}


	public boolean isTestRow() throws Exception
	{
		final CellInstance outputCell = cx().getRowCell( actualCol() );
		if (null == outputCell) return false;
		if (outputCell instanceof CellWithExpression) return true;
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
		return nameCell != null ? (String) nameCell.getValue() : "";
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
