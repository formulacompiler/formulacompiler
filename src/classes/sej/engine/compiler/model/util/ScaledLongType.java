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
package sej.engine.compiler.model.util;

import java.math.BigDecimal;

import sej.NumericType;
import sej.NumericType.AbstractLongType;
import sej.engine.RuntimeLong_v1;
import sej.expressions.Function;
import sej.expressions.Operator;

final class ScaledLongType extends InterpretedNumericType
{
	private final NumericType.AbstractLongType num;
	private final RuntimeLong_v1 runtime;


	public ScaledLongType(AbstractLongType _type)
	{
		super( _type );
		this.num = _type;
		this.runtime = new RuntimeLong_v1( _type.getScale() );
	}


	private int getScale()
	{
		return this.num.getScale();
	}

	private long getScalingFactor()
	{
		return this.num.one();
	}

	private long zero()
	{
		return this.num.zero();
	}

	private long one()
	{
		return this.num.one();
	}

	private long parse( String _value )
	{
		return this.num.parse( _value );
	}


	@Override
	public Object adjustConstantValue( Object _value )
	{
		if (_value instanceof Number) {
			return numberToScaledLong( (Number) _value );
		}
		return _value;

	}


	private long numberToScaledLong( Number _value )
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


	private long valueToScaledLongOrZero( Object _value )
	{
		if (null == _value) return zero();
		if (_value instanceof Long) return (Long) _value;
		if (_value instanceof Number) return numberToScaledLong( (Number) _value );
		if (_value instanceof String) return parse( (String) _value );
		return zero();
	}


	double scaledLongToDouble( long _value )
	{
		if (getScale() == 0) return _value;
		final double unscaled = _value;
		final double divisor = getScalingFactor();
		return unscaled / divisor;
	}


	@Override
	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case PLUS: {
				switch (_args.length) {
					case 2:
						return valueToScaledLongOrZero( _args[ 0 ] ) + valueToScaledLongOrZero( _args[ 1 ] );
				}
			}

			case MINUS: {
				switch (_args.length) {
					case 1:
						return -valueToScaledLongOrZero( _args[ 0 ] );
					case 2:
						return valueToScaledLongOrZero( _args[ 0 ] ) - valueToScaledLongOrZero( _args[ 1 ] );
				}
			}

			case TIMES: {
				switch (_args.length) {
					case 2:
						return valueToScaledLongOrZero( _args[ 0 ] )
								* valueToScaledLongOrZero( _args[ 1 ] ) / getScalingFactor();
				}
			}

			case DIV: {
				switch (_args.length) {
					case 2:
						return valueToScaledLongOrZero( _args[ 0 ] )
								* getScalingFactor() / valueToScaledLongOrZero( _args[ 1 ] );
				}
			}

			case EXP: {
				switch (_args.length) {
					case 2:
						return pow( valueToScaledLongOrZero( _args[ 0 ] ), valueToScaledLongOrZero( _args[ 1 ] ) );
				}
			}

			case PERCENT: {
				switch (_args.length) {
					case 1:
						return valueToScaledLongOrZero( _args[ 0 ] ) / 100L;
				}
			}

		}

		return super.compute( _operator, _args );
	}


	private long pow( long _x, long _exp )
	{
		if (_exp == zero()) return one();
		if (_exp == one()) return _x;
		return numberToScaledLong( Math.pow( scaledLongToDouble( _x ), scaledLongToDouble( _exp ) ) );
	}


	@Override
	public Object compute( Function _function, Object... _args )
	{
		final int cardinality = _args.length;
		switch (_function) {

			case ROUND: {
				switch (cardinality) {
					case 2:
						long val = valueToScaledLongOrZero( _args[ 0 ] );
						int maxFrac = (int) (valueToScaledLongOrZero( _args[ 1 ] ) / this.num.one());
						return this.runtime.round( val, maxFrac );
				}
			}

			case MATCH: {
				switch (cardinality) {
					case 2:
						return numberToScaledLong( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], 1 ) + 1 );
					case 3:
						final int matchType = valueToIntOrOne( _args[ 2 ] );
						return numberToScaledLong( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], matchType ) + 1 );
				}
			}

		}
		return super.compute( _function, _args );
	}


	@Override
	protected int compare( Object _a, Object _b )
	{
		long a = valueToScaledLongOrZero( _a );
		long b = valueToScaledLongOrZero( _b );
		return (a == b) ? 0 : (a < b) ? -1 : 1;
	}

	
	@Override
	protected int valueToInt( Object _value, int _ifNull )
	{
		if (_value instanceof Long) {
			long value = (Long) _value;
			return (int) (value / this.num.one());
		}
		return super.valueToInt( _value, _ifNull );
	}
	
}