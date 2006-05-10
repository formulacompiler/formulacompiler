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

import sej.engine.RuntimeDouble_v1;
import sej.expressions.Function;
import sej.expressions.Operator;

final class DoubleType extends InterpretedNumericType
{

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
					result += Util.valueToDoubleOrZero( arg );
				}
				return result;
			}

			case MINUS: {
				switch (_args.length) {
					case 1:
						return -Util.valueToDoubleOrZero( _args[ 0 ] );
					case 2:
						return Util.valueToDoubleOrZero( _args[ 0 ] ) - Util.valueToDoubleOrZero( _args[ 1 ] );
				}
			}

			case TIMES: {
				double result = 1.0;
				for (Object arg : _args) {
					result *= Util.valueToDoubleOrZero( arg );
				}
				return result;
			}

			case DIV: {
				switch (_args.length) {
					case 2:
						return Util.valueToDoubleOrZero( _args[ 0 ] ) / Util.valueToDoubleOrZero( _args[ 1 ] );
				}
			}

			case EXP: {
				switch (_args.length) {
					case 2:
						return Math.pow( Util.valueToDoubleOrZero( _args[ 0 ] ), Util.valueToDoubleOrZero( _args[ 1 ] ) );
				}
			}

			case PERCENT: {
				switch (_args.length) {
					case 1:
						return Util.valueToDoubleOrZero( _args[ 0 ] ) / 100;
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
						double val = Util.valueToDoubleOrZero( _args[ 0 ] );
						int maxFrac = Util.valueToIntOrZero( _args[ 1 ] );
						return RuntimeDouble_v1.round( val, maxFrac );
				}
			}

			case MATCH: {
				switch (cardinality) {
					case 2:
						return (double) InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], 1 ) + 1;
					case 3:
						return (double) InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], Util.valueToIntOrOne( _args[ 2 ] ) ) + 1;
				}
			}

		}
		return super.compute( _function, _args );
	}

	@Override
	protected int compare( Object _a, Object _b )
	{
		double a = Util.valueToDoubleOrZero( _a );
		double b = Util.valueToDoubleOrZero( _b );
		return Double.compare( a, b );
	}

}