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

package org.formulacompiler.compiler.internal.model.interpreter;

import java.math.BigDecimal;
import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2;


abstract class InterpretedBigDecimalType extends InterpretedNumericType
{

	protected InterpretedBigDecimalType( NumericType _type, ComputationMode _mode, Environment _env )
	{
		super( _type, _mode, _env );
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
		else if (_value instanceof Boolean) result = ((Boolean) _value) ? BigDecimal.ONE : BigDecimal.ZERO;
		else if (_value instanceof LocalDate) result = BigDecimal.valueOf( ((LocalDate) _value).value() );
		else if (_value instanceof Duration) result = BigDecimal.valueOf( ((Duration) _value).value() );
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