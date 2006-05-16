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
import sej.engine.RuntimeBigDecimal_v1;
import sej.expressions.Function;
import sej.expressions.Operator;

final class BigDecimalType extends InterpretedNumericType
{
	private final int scale;
	private final int roundingMode;


	public BigDecimalType(NumericType _type)
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
	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case PLUS: {
				BigDecimal result = RuntimeBigDecimal_v1.ZERO;
				for (Object arg : _args) {
					result = result.add( valueToBigDecimalOrZero( arg ) );
				}
				return result;
			}

			case MINUS: {
				switch (_args.length) {
					case 1:
						return valueToBigDecimalOrZero( _args[ 0 ] ).negate();
					case 2:
						return valueToBigDecimalOrZero( _args[ 0 ] ).subtract( valueToBigDecimalOrZero( _args[ 1 ] ) );
				}
			}

			case TIMES: {
				BigDecimal result = RuntimeBigDecimal_v1.ONE;
				for (Object arg : _args) {
					result = adjustScale( result.multiply( valueToBigDecimalOrZero( arg ) ) );
				}
				return result;
			}

			case DIV: {
				switch (_args.length) {
					case 2:
						return valueToBigDecimalOrZero( _args[ 0 ] ).divide( valueToBigDecimalOrZero( _args[ 1 ] ),
								this.scale, this.roundingMode );
				}
			}

			case EXP: {
				switch (_args.length) {
					case 2:
						return adjustScale( valueToBigDecimalOrZero( _args[ 0 ] ).pow( valueToIntOrZero( _args[ 1 ] ) ) );
				}
			}

			case PERCENT: {
				switch (_args.length) {
					case 1:
						return adjustScale( valueToBigDecimalOrZero( _args[ 0 ] ).movePointLeft( 2 ) );
				}
			}

		}

		return super.compute( _operator, _args );
	}


	@Override
	public Object compute( Function _function, Object... _args )
	{
		final int cardinality = _args.length;
		switch (_function) {

			case ROUND: {
				switch (cardinality) {
					case 2:
						BigDecimal val = valueToBigDecimalOrZero( _args[ 0 ] );
						int maxFrac = valueToIntOrZero( _args[ 1 ] );
						return adjustScale( RuntimeBigDecimal_v1.round( val, maxFrac ) );
				}
			}

			case MATCH: {
				switch (cardinality) {
					case 2:
						return adjustScale( BigDecimal
								.valueOf( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], 1 ) + 1 ) );
					case 3:
						return adjustScale( BigDecimal.valueOf( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ],
								valueToIntOrOne( _args[ 2 ] ) ) + 1 ) );
				}
			}

		}
		return super.compute( _function, _args );
	}


	@Override
	protected int compare( Object _a, Object _b )
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
		else if (_value instanceof String) result = new BigDecimal( (String) _value );
		else result = _ifNull;
		return adjustScale( result );
	}

	public BigDecimal valueToBigDecimalOrZero( Object _value )
	{
		return valueToBigDecimal( _value, RuntimeBigDecimal_v1.ZERO );
	}

}