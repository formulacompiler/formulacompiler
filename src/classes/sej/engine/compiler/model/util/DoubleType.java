/**
 * 
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