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

import java.math.BigDecimal;
import java.util.Date;

import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.NotAvailableException;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;

public final class Inputs extends AbstractCellValues
{

	public Inputs( Context _cx, CellInstance... _cells )
	{
		super( _cx, _cells );
	}

	public Inputs( Context _cx, CellIndex... _cells )
	{
		super( _cx, cellIndexesToInstance( _cells ) );
	}

	private static CellInstance[] cellIndexesToInstance( CellIndex[] _cells )
	{
		CellInstance[] r = new CellInstance[ _cells.length ];
		for (int i = 0; i < _cells.length; i++)
			r[ i ] = _cells[ i ].getCell();
		return r;
	}

	public double dbl( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return 0.0;
		final Object val = getOrThrow( _inputIndex );
		return null == val ? 0.0 : ((Number) val).doubleValue();
	}

	public BigDecimal bdec( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		final Object val = getOrThrow( _inputIndex );
		return null == val ? null : (val instanceof BigDecimal) ? (BigDecimal) val : BigDecimal.valueOf( ((Number) val)
				.doubleValue() );
	}

	@ScaledLong( 6 )
	public long lng( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return 0;
		final Object val = getOrThrow( _inputIndex );
		return null == val ? 0 : (val instanceof Long) ? (Long) val : numericType().valueOf( (Number) val ).longValue();
	}

	public boolean bool( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return false;
		final Object val = getOrThrow( _inputIndex );
		return null == val ? false : (Boolean) val;
	}

	public Date date( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		return (Date) getOrThrow( _inputIndex );
	}

	public String str( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		return (String) getOrThrow( _inputIndex );
	}


	private Object getOrThrow( int _inputIndex )
	{
		final Object _val = this.vals[ _inputIndex ];
		if (_val == AbstractCellValues.ERR) throw new FormulaException();
		if (_val == AbstractCellValues.NA) throw new NotAvailableException();
		return _val;
	}

}
