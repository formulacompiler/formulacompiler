/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.model.util;

import java.math.BigDecimal;

import sej.internal.NumericTypeImpl;
import sej.internal.NumericTypeImpl.AbstractLongType;
import sej.internal.runtime.RuntimeLong_v1;

abstract class InterpretedScaledLongType_Base extends InterpretedNumericType
{
	private final NumericTypeImpl.AbstractLongType num;
	private final RuntimeLong_v1.Context runtimeCx;


	public InterpretedScaledLongType_Base(AbstractLongType _type)
	{
		super( _type );
		this.num = _type;
		this.runtimeCx = new RuntimeLong_v1.Context( _type.getScale() );
	}

	
	protected final RuntimeLong_v1.Context getContext()
	{
		return this.runtimeCx;		
	}

	private final int getScale()
	{
		return this.num.getScale();
	}

	private final long getScalingFactor()
	{
		return this.num.one();
	}

	private final long zeroL()
	{
		return this.num.zero();
	}

	private final long parse( String _value )
	{
		return this.num.parse( _value );
	}


	@Override
	public final Object adjustConstantValue( Object _value )
	{
		if (_value instanceof Number) {
			return numberToScaledLong( (Number) _value );
		}
		return _value;

	}
	
	
	@Override
	public Object toNumeric( Number _value )
	{
		return valueToScaledLongOrZero( _value );
	}


	private final long numberToScaledLong( Number _value )
	{
		String asString;
		if (_value instanceof BigDecimal) {
			final BigDecimal big = (BigDecimal) _value;
			asString = big.toPlainString();
		}
		else {
			asString = _value.toString();
		}
		return this.num.parse( asString );
	}


	private final long valueToScaledLongOrZero( Object _value )
	{
		if (null == _value) return zeroL();
		if (_value instanceof Long) return (Long) _value;
		if (_value instanceof Number) return numberToScaledLong( (Number) _value );
		if (_value instanceof String) return parse( (String) _value );
		return zeroL();
	}


	final double scaledLongToDouble( long _value )
	{
		if (getScale() == 0) return _value;
		final double unscaled = _value;
		final double divisor = getScalingFactor();
		return unscaled / divisor;
	}


	@Override
	protected final int compareNumerically( Object _a, Object _b )
	{
		long a = valueToScaledLongOrZero( _a );
		long b = valueToScaledLongOrZero( _b );
		return (a == b) ? 0 : (a < b) ? -1 : 1;
	}

	
	@Override
	protected final int valueToInt( Object _value, int _ifNull )
	{
		if (_value instanceof Long) {
			long value = (Long) _value;
			return (int) (value / this.num.one());
		}
		return super.valueToInt( _value, _ifNull );
	}

	
	// Conversions for generated code:
	
	protected final long to_long( Object _o )
	{
		return valueToScaledLongOrZero( _o );
	}

	protected final boolean isScaled()
	{
		return getScale() != 0;
	}

	
}