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

package org.formulacompiler.spreadsheet.internal;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Set;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.YamlBuilder;


public abstract class CellInstance extends AbstractStyledElement
{
	private final RowImpl row;
	private final int columnIndex;
	private int maxFractionalDigits = NumericType.UNLIMITED_FRACTIONAL_DIGITS;
	private Object value;


	public CellInstance( RowImpl _row )
	{
		this.row = _row;
		this.columnIndex = _row.getCellList().size();
		_row.getCellList().add( this );
	}


	public RowImpl getRow()
	{
		return this.row;
	}


	public int getColumnIndex()
	{
		return this.columnIndex;
	}


	public int getMaxFractionalDigits()
	{
		return this.maxFractionalDigits;
	}

	public void setMaxFractionalDigits( int _maxFractionalDigits )
	{
		this.maxFractionalDigits = _maxFractionalDigits;
	}


	public void applyNumberFormat( NumberFormat _numberFormat )
	{
		setMaxFractionalDigits( getMaxFractionalDigitsFor( _numberFormat ) );
	}

	private int getMaxFractionalDigitsFor( NumberFormat _numberFormat )
	{
		if (null == _numberFormat) {
			return NumericType.UNLIMITED_FRACTIONAL_DIGITS;
		}
		else {
			int maxFrac = _numberFormat.getMaximumFractionDigits();
			if (_numberFormat instanceof DecimalFormat) {
				DecimalFormat decFormat = (DecimalFormat) _numberFormat;
				int decMult = decFormat.getMultiplier();
				switch (decMult) {
					case 10:
						maxFrac += 1;
						break;
					case 100:
						maxFrac += 2;
						break;
				}
			}
			return maxFrac;
		}
	}


	public Object getValue()
	{
		return this.value;
	}

	public void setValue( Object _value )
	{
		this.value = _value;
	}


	public String getCanonicalName()
	{
		return getCellIndex().getNameA1ForCellIndex( false, false, null );
	}


	public CellIndex getCellIndex()
	{
		int iCol = getColumnIndex();
		int iRow = this.row.getRowIndex();
		SheetImpl sheet = this.row.getSheet();
		int iSheet = sheet.getSheetIndex();
		return new CellIndex( sheet.getSpreadsheet(), iSheet, iCol, iRow );
	}


	public abstract void copyTo( RowImpl _row );


	@Override
	public void yamlTo( YamlBuilder _to )
	{
		final Set<String> names = getRow().getSheet().getSpreadsheet().getModelNamesFor( getCellIndex() );
		if (names != null && !names.isEmpty()) {
			_to.vn( "names" ).lOneLine( names ).lf();
		}
	}

}
