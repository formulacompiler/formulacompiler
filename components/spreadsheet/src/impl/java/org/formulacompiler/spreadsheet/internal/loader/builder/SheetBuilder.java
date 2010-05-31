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

package org.formulacompiler.spreadsheet.internal.loader.builder;

import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;

/**
 * @author Vladimir Korenev
 */
public class SheetBuilder
{
	private final SpreadsheetBuilder spreadsheetBuilder;
	private final SheetImpl sheet;

	private int emptyRows = 0;
	private RowBuilder rowBuilder;

	public SheetBuilder( final SpreadsheetBuilder _spreadsheetBuilder, final SheetImpl _sheet )
	{
		this.spreadsheetBuilder = _spreadsheetBuilder;
		this.sheet = _sheet;
	}

	public RowBuilder beginRow()
	{
		if (this.rowBuilder != null) throw new IllegalStateException();

		this.rowBuilder = new RowBuilder( this );
		return this.rowBuilder;
	}

	public SheetBuilder endRow()
	{
		if (this.rowBuilder == null) throw new IllegalStateException();

		if (this.rowBuilder.isEmpty()) {
			this.emptyRows++;
		}

		this.rowBuilder = null;

		return this;
	}

	public SheetBuilder endRow( int numberRowsRepeated )
	{
		if (this.rowBuilder == null) throw new IllegalStateException();

		if (this.rowBuilder.isEmpty()) {
			this.emptyRows += numberRowsRepeated;
		}
		else {
			for (int i = 1; i < numberRowsRepeated; i++) this.rowBuilder.copy();
		}

		this.rowBuilder = null;

		return this;
	}

	public SpreadsheetBuilder endSheet()
	{
		this.spreadsheetBuilder.endSheet();
		return this.spreadsheetBuilder;
	}

	SheetImpl getSheet()
	{
		for (int i = 0; i < this.emptyRows; i++) {
			new RowImpl( this.sheet );
		}
		this.emptyRows = 0;

		return this.sheet;
	}
}
