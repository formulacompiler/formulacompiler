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
		return null == val? 0.0 : ((Number) val).doubleValue();
	}

	public BigDecimal bdec( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		final Object val = getOrThrow( _inputIndex );
		return null == val? null : (val instanceof BigDecimal)? (BigDecimal) val : BigDecimal.valueOf( ((Number) val)
				.doubleValue() );
	}

	@ScaledLong( 6 )
	public long lng( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return 0;
		final Object val = getOrThrow( _inputIndex );
		return null == val? 0 : (val instanceof Long)? (Long) val : numericType().valueOf( (Number) val ).longValue();
	}

	public boolean bool( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return false;
		final Object val = getOrThrow( _inputIndex );
		return null == val? false : (Boolean) val;
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
