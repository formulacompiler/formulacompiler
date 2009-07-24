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

package org.formulacompiler.compiler.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.ParseException;

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;

public abstract class AbstractBigDecimalType extends NumericTypeImpl
{

	protected AbstractBigDecimalType( int _scale, int _roundingMode )
	{
		super( BigDecimal.class, _scale, _roundingMode );
	}

	protected AbstractBigDecimalType( MathContext _mathContext )
	{
		super( BigDecimal.class, _mathContext );
	}

	@Override
	public final Number getMinValue()
	{
		return RuntimeBigDecimal_v2.EXTREMUM;
	}

	@Override
	public final Number getMaxValue()
	{
		return RuntimeBigDecimal_v2.EXTREMUM;
	}

	@Override
	protected BigDecimal assertProperNumberType( Number _value )
	{
		return (BigDecimal) _value;
	}

	@Override
	protected final BigDecimal convertFromAnyNumber( Number _value )
	{
		BigDecimal v;
		if (_value instanceof BigDecimal) {
			v = (BigDecimal) _value;
		}
		else if (_value instanceof Long) {
			v = BigDecimal.valueOf( _value.longValue() );
		}
		else if (_value instanceof Integer) {
			v = BigDecimal.valueOf( _value.longValue() );
		}
		else if (_value instanceof Byte) {
			v = BigDecimal.valueOf( _value.longValue() );
		}
		else {
			v = BigDecimal.valueOf( _value.doubleValue() );
		}
		return adjustConvertedValue( v );
	}

	@Override
	protected final Number convertFromString( String _value, Environment _env ) throws ParseException
	{
		_env.decimalFormat().setParseBigDecimal( true );
		return adjustConvertedValue( (BigDecimal) super.convertFromString( _value, _env ) );
	}

	@Override
	protected final String convertToConciseString( Number _value, Environment _env )
	{
		return RuntimeBigDecimal_v2.toExcelString( (BigDecimal) _value, _env );
	}

	protected abstract BigDecimal adjustConvertedValue( BigDecimal _value );

}