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
package sej.internal.model.interpreter;

import java.math.BigDecimal;

import sej.compiler.NumericType;
import sej.internal.runtime.RuntimeBigDecimal_v1;

abstract class InterpretedBigDecimalType_Base extends InterpretedNumericType
{
	private final int scale;
	private final int roundingMode;


	public InterpretedBigDecimalType_Base(NumericType _type)
	{
		super( _type );
		this.scale = _type.getScale();
		this.roundingMode = _type.getRoundingMode();
	}


	public BigDecimal adjustScale( BigDecimal _value )
	{
		if (NumericType.UNDEFINED_SCALE != this.scale) {
			return _value.setScale( this.scale, this.roundingMode );
		}
		else {
			return _value;
		}
	}


	@Override
	public Object adjustConstantValue( Object _value )
	{
		if (_value instanceof BigDecimal) {
			return adjustScale( (BigDecimal) _value );
		}
		else if (_value instanceof Double) {
			final Double value = (Double) _value;
			return adjustScale( BigDecimal.valueOf( value ) );

		}
		else if (_value instanceof Number) {
			final Number number = (Number) _value;
			return adjustScale( BigDecimal.valueOf( number.longValue() ) );
		}
		return _value;
	}


	@Override
	public Number fromString( String _s )
	{
		return new BigDecimal( _s );
	}


	@Override
	public Object toNumeric( Number _value )
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
		else result = _ifNull;
		return adjustScale( result );
	}

	public BigDecimal valueToBigDecimalOrZero( Object _value )
	{
		return valueToBigDecimal( _value, RuntimeBigDecimal_v1.ZERO );
	}


	// Conversions for generated code:

	protected final boolean needsValueAdjustment()
	{
		return (NumericType.UNDEFINED_SCALE != this.scale);
	}

	protected final BigDecimal adjustReturnedValue( BigDecimal _b )
	{
		return adjustScale( _b );
	}

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