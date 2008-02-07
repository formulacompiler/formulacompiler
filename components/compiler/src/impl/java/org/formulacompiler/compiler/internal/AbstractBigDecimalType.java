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