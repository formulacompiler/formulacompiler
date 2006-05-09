/**
 * 
 */
package sej.engine.compiler.model.util;

import java.math.BigDecimal;

import sej.engine.RuntimeBigDecimal_v1;
import sej.expressions.Function;
import sej.expressions.Operator;

final class BigDecimalType extends InterpretedNumericType
{

	public BigDecimalType(int _scale)
	{
		super();
	}

	@Override
	public Object adjustConstantValue( Object _value )
	{
		if (_value instanceof Double) {
			Double value = (Double) _value;
			return BigDecimal.valueOf( value );
		}
		if (_value instanceof Number) {
			Number number = (Number) _value;
			return BigDecimal.valueOf( number.longValue() );
		}
		return _value;
	}

	@Override
	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case PLUS: {
				BigDecimal result = BigDecimal.ZERO;
				for (Object arg : _args) {
					result = result.add( Util.valueToBigDecimalOrZero( arg ) );
				}
				return result;
			}

			case MINUS: {
				switch (_args.length) {
					case 1:
						return Util.valueToBigDecimalOrZero( _args[ 0 ] ).negate();
					case 2:
						return Util.valueToBigDecimalOrZero( _args[ 0 ] )
								.subtract( Util.valueToBigDecimalOrZero( _args[ 1 ] ) );
				}
			}

			case TIMES: {
				BigDecimal result = BigDecimal.ONE;
				for (Object arg : _args) {
					result = result.multiply( Util.valueToBigDecimalOrZero( arg ) );
				}
				return result;
			}

			case DIV: {
				switch (_args.length) {
					case 2:
						return Util.valueToBigDecimalOrZero( _args[ 0 ] ).divide( Util.valueToBigDecimalOrZero( _args[ 1 ] ) );
				}
			}

			case EXP: {
				switch (_args.length) {
					case 2:
						return Util.valueToBigDecimalOrZero( _args[ 0 ] ).pow( Util.valueToIntOrZero( _args[ 1 ] ) );
				}
			}

			case PERCENT: {
				switch (_args.length) {
					case 1:
						return Util.valueToBigDecimalOrZero( _args[ 0 ] ).movePointLeft( 2 );
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
						BigDecimal val = Util.valueToBigDecimalOrZero( _args[ 0 ] );
						int maxFrac = Util.valueToIntOrZero( _args[ 1 ] );
						return RuntimeBigDecimal_v1.round( val, maxFrac );
				}
			}

			case MATCH: {
				switch (cardinality) {
					case 2:
						return new BigDecimal( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], 1 ) )
								.add( BigDecimal.ONE );
					case 3:
						return new BigDecimal( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], Util
								.valueToIntOrOne( _args[ 2 ] ) ) ).add( BigDecimal.ONE );
				}
			}

		}
		return super.compute( _function, _args );
	}

	@Override
	protected int compare( Object _a, Object _b )
	{
		BigDecimal a = Util.valueToBigDecimalOrZero( _a );
		BigDecimal b = Util.valueToBigDecimalOrZero( _b );
		return a.compareTo( b );
	}

}