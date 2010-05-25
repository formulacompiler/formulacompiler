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

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.spreadsheet.internal.BaseSpreadsheet;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

/**
 * @author Vladimir Korenev
 */
public class SpreadsheetBuilder
{
	private final SpreadsheetImpl spreadsheet;

	public SpreadsheetBuilder( ComputationMode _computationMode )
	{
		this.spreadsheet = new SpreadsheetImpl( _computationMode );
	}

	private SheetBuilder sheetBuilder;

	public SheetBuilder beginSheet( String _name )
	{
		if (this.sheetBuilder != null) throw new IllegalStateException();

		this.sheetBuilder = new SheetBuilder( this, new SheetImpl( this.spreadsheet, _name ) );
		return this.sheetBuilder;
	}

	public SheetBuilder beginSheet()
	{
		if (this.sheetBuilder != null) throw new IllegalStateException();

		this.sheetBuilder = new SheetBuilder( this, new SheetImpl( this.spreadsheet ) );
		return this.sheetBuilder;
	}

	public SpreadsheetBuilder endSheet()
	{
		if (this.sheetBuilder == null) throw new IllegalStateException();

		this.sheetBuilder = null;

		return this;
	}

	public BaseSpreadsheet getSpreadsheet()
	{
		if (this.sheetBuilder != null) throw new IllegalStateException();

		return this.spreadsheet;
	}
}
