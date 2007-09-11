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
package org.formulacompiler.compiler.internal.model.interpreter;

import java.math.BigDecimal;
import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.LocalExcelDate;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2;


abstract class InterpretedBigDecimalType extends InterpretedNumericType
{

	protected InterpretedBigDecimalType(NumericType _type, Environment _env)
	{
		super( _type, _env );
	}


	protected abstract BigDecimal adjustConvertedValue( BigDecimal _value );


	@Override
	public Object adjustConstantValue( Object _value )
	{
		if (_value instanceof BigDecimal) {
			return adjustConvertedValue( (BigDecimal) _value );
		}
		else if (_value instanceof Double) {
			final Double value = (Double) _value;
			return adjustConvertedValue( BigDecimal.valueOf( value ) );

		}
		else if (_value instanceof Number) {
			final Number number = (Number) _value;
			return adjustConvertedValue( BigDecimal.valueOf( number.longValue() ) );
		}
		return _value;
	}


	@Override
	public Number toNumeric( Number _value )
	{
		return valueToBigDecimalOrZero( _value );
	}


	@Override
	protected int compareNumerically( Object _a, Object _b )
	{
		BigDecimal a = valueToBigDecimalOrZero( _a );
		BigDecimal b = valueToBigDecimalOrZero( _b );
		return a.compareTo( b );
	}


	private BigDecimal valueToBigDecimal( Object _value, BigDecimal _ifNull )
	{
		BigDecimal result;
		if (_value instanceof BigDecimal) result = (BigDecimal) _value;
		else if (_value instanceof Double) result = BigDecimal.valueOf( (Double) _value );
		else if (_value instanceof Integer) result = BigDecimal.valueOf( (Integer) _value );
		else if (_value instanceof Long) result = BigDecimal.valueOf( (Long) _value );
		else if (_value instanceof String) result = new BigDecimal( (String) _value );
		else if (_value instanceof LocalExcelDate) result = BigDecimal.valueOf( ((LocalExcelDate) _value).value() );
		else if (_value instanceof Date) {
			throw new IllegalArgumentException( "Cannot interpret java.util.Date - it is runtime time-zone specific." );
		}
		else result = _ifNull;
		return adjustConvertedValue( result );
	}

	public BigDecimal valueToBigDecimalOrZero( Object _value )
	{
		return valueToBigDecimal( _value, RuntimeScaledBigDecimal_v2.ZERO );
	}


	// Conversions for generated code:

	protected final BigDecimal to_BigDecimal( Object _o )
	{
		return valueToBigDecimalOrZero( _o );
	}

	protected final BigDecimal[] to_array( Object _value )
	{
		final Object[] consts = asArrayOfConsts( _value );
		final BigDecimal[] r = new BigDecimal[ consts.length ];
		int i = 0;
		for (Object cst : consts) {
			r[ i++ ] = to_BigDecimal( cst );
		}
		return r;
	}


}