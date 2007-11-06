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

import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;

public final class Inputs extends AbstractCellValues
{

	public Inputs( Context _cx, Cell... _cells )
	{
		super( _cx, _cells );
	}


	public double dbl( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return 0.0;
		final Object val = this.vals[ _inputIndex ];
		return null == val? 0.0 : ((Number) val).doubleValue();
	}

	public BigDecimal bdec( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		final Object val = this.vals[ _inputIndex ];
		return null == val? null : (val instanceof BigDecimal)? (BigDecimal) val : BigDecimal.valueOf( ((Number) val).doubleValue() );
	}

	@ScaledLong( 6 )
	public long lng( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return 0;
		final Object val = this.vals[ _inputIndex ];
		return null == val? 0 : (val instanceof Long)? (Long) val : numericType().valueOf( (Number) val ).longValue();
	}

	public boolean bool( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return false;
		final Object val = this.vals[ _inputIndex ];
		return null == val? false : (Boolean) val;
	}

	public Date date( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		return (Date) this.vals[ _inputIndex ];
	}

	public String str( int _inputIndex )
	{
		if (_inputIndex < 0 || _inputIndex >= this.vals.length) return null;
		return (String) this.vals[ _inputIndex ];
	}


}
