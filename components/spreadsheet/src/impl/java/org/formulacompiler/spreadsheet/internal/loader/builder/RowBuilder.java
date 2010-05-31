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

import java.text.NumberFormat;
import java.util.List;

import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.CellWithError;
import org.formulacompiler.spreadsheet.internal.CellWithLazilyParsedExpression;
import org.formulacompiler.spreadsheet.internal.LazyExpressionParser;
import org.formulacompiler.spreadsheet.internal.RowImpl;

/**
 * @author Vladimir Korenev
 */
public class RowBuilder
{
	private final SheetBuilder sheetBuilder;

	private int emptyCells = 0;
	private RowImpl row = null;
	private CellInstance lastAddedCell = null;

	public RowBuilder( final SheetBuilder _sheetBuilder )
	{
		this.sheetBuilder = _sheetBuilder;
	}

	public RowBuilder addCellWithConstant( Object _value )
	{
		this.lastAddedCell = new CellWithConstant( getRow(), _value );
		return this;
	}

	public RowBuilder addCellWithExpression( LazyExpressionParser _expressionParser )
	{
		this.lastAddedCell = new CellWithLazilyParsedExpression( getRow(), _expressionParser );
		return this;
	}

	public RowBuilder addCellWithError( String _text )
	{
		this.lastAddedCell = new CellWithError( getRow(), _text );
		return this;
	}

	public RowBuilder addEmptyCell()
	{
		this.lastAddedCell = null;
		this.emptyCells++;
		return this;
	}

	public RowBuilder applyNumberFormat( NumberFormat _numberFormat )
	{
		if (this.lastAddedCell != null) this.lastAddedCell.applyNumberFormat( _numberFormat );
		return this;
	}

	public RowBuilder setValue( Object _value )
	{
		if (this.lastAddedCell != null) this.lastAddedCell.setValue( _value );
		return this;
	}

	public SheetBuilder endRow()
	{
		this.sheetBuilder.endRow();
		return this.sheetBuilder;
	}

	boolean isEmpty()
	{
		return this.row == null;
	}

	void copy()
	{
		this.row.copy();
	}

	private RowImpl getRow()
	{
		if (this.row == null) this.row = new RowImpl( this.sheetBuilder.getSheet() );

		final List<CellInstance> cellList = this.row.getCellList();
		for (int i = 0; i < this.emptyCells; i++) {
			cellList.add( null );
		}
		this.emptyCells = 0;

		return this.row;
	}
}
