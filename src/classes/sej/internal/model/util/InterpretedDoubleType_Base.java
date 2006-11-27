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

import sej.Function;
import sej.NumericType;
import sej.Operator;

abstract class InterpretedDoubleType_Base extends InterpretedNumericType
{

	public InterpretedDoubleType_Base(NumericType _type)
	{
		super( _type );
	}


	@Override
	public Object adjustConstantValue( Object _value )
	{
		return _value;
	}

	@Override
	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case PLUS: {
				double result = 0.0;
				for (Object arg : _args) {
					result += valueToDoubleOrZero( arg );
				}
				return result;
			}

			case MINUS: {
				switch (_args.length) {
					case 1:
						return -valueToDoubleOrZero( _args[ 0 ] );
					case 2:
						return valueToDoubleOrZero( _args[ 0 ] ) - valueToDoubleOrZero( _args[ 1 ] );
				}
				break;
			}

			case TIMES: {
				double result = 1.0;
				for (Object arg : _args) {
					result *= valueToDoubleOrZero( arg );
				}
				return result;
			}

			case DIV: {
				switch (_args.length) {
					case 2:
						return valueToDoubleOrZero( _args[ 0 ] ) / valueToDoubleOrZero( _args[ 1 ] );
				}
				break;
			}

			case EXP: {
				switch (_args.length) {
					case 2:
						return Math.pow( valueToDoubleOrZero( _args[ 0 ] ), valueToDoubleOrZero( _args[ 1 ] ) );
				}
				break;
			}

			case PERCENT: {
				switch (_args.length) {
					case 1:
						return valueToDoubleOrZero( _args[ 0 ] ) / 100;
				}
				break;
			}

		}
		return super.compute( _operator, _args );
	}

	@Override
	public Object compute( Function _function, Object... _args )
	{
		final int cardinality = _args.length;
		switch (_function) {

			case MATCH: {
				switch (cardinality) {
					case 2:
						return (double) InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], 1 ) + 1;
					case 3:
						return (double) InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], valueToIntOrOne( _args[ 2 ] ) ) + 1;
				}
				break;
			}
			
		}
		return super.compute( _function, _args );
	}

	@Override
	protected int compareNumerically( Object _a, Object _b )
	{
		double a = valueToDoubleOrZero( _a );
		double b = valueToDoubleOrZero( _b );
		return Double.compare( a, b );
	}


	private final double valueToDouble( Object _value, double _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).doubleValue();
		if (_value instanceof String) return Double.valueOf( (String) _value );
		return _ifNull;
	}

	private final double valueToDoubleOrZero( Object _value )
	{
		return valueToDouble( _value, 0.0 );
	}
	

	// Conversions for generated code:
	
	protected final double to_double( Object _value )
	{
		return valueToDoubleOrZero( _value );
	}

}