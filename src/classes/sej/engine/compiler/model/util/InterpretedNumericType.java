package sej.engine.compiler.model.util;

import java.math.BigDecimal;

import sej.NumericType;
import sej.engine.compiler.model.RangeValue;
import sej.expressions.Function;
import sej.expressions.Operator;


public abstract class InterpretedNumericType
{

	InterpretedNumericType()
	{
		super();
	}

	public static InterpretedNumericType typeFor( NumericType _type )
	{
		if (Double.TYPE == _type.getValueType()) {
			return new DoubleType();
		}
		else if (BigDecimal.class == _type.getValueType()) {
			return new BigDecimalType( _type.getScale() );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for run-time interpretation." );
		}
	}


	public abstract Object adjustConstantValue( Object _value );
	protected abstract int compare( Object _a, Object _b );


	public boolean toBoolean( Object _value )
	{
		return Util.valueToBoolean( _value, false );
	}


	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case NOOP: {
				switch (_args.length) {
					case 1:
						return _args[ 0 ];
				}
			}

			case CONCAT: {
				StringBuilder result = new StringBuilder();
				for (Object arg : _args) {
					result.append( Util.valueToString( arg ) );
				}
				return result.toString();
			}

			case MIN: {
				switch (_args.length) {
					case 2:
						Object a = _args[ 0 ];
						Object b = _args[ 1 ];
						if (null == a) return b;
						if (null == b) return a;
						return compare( a, b ) <= 0 ? a : b;
				}
			}

			case MAX: {
				switch (_args.length) {
					case 2:
						Object a = _args[ 0 ];
						Object b = _args[ 1 ];
						if (null == a) return b;
						if (null == b) return a;
						return compare( a, b ) >= 0 ? a : b;
				}
			}

			case EQUAL: {
				switch (_args.length) {
					case 2:
						Object a = _args[ 0 ];
						Object b = _args[ 1 ];
						if (null == a) return (0 == Util.valueToIntOrZero( b ));
						if (null == b) return (0 == Util.valueToIntOrZero( a ));
						return a.equals( b );
				}
			}

			case NOTEQUAL: {
				switch (_args.length) {
					case 2:
						Object a = _args[ 0 ];
						Object b = _args[ 1 ];
						if (null == a) return (0 != Util.valueToIntOrZero( b ));
						if (null == b) return (0 != Util.valueToIntOrZero( a ));
						return !a.equals( b );
				}
			}

			case GREATER: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) > 0;
				}
			}

			case GREATEROREQUAL: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) >= 0;
				}
			}

			case LESS: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) < 0;
				}
			}

			case LESSOREQUAL: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) <= 0;
				}
			}

			case AND: {
				switch (_args.length) {
					case 2:
						return toBoolean( _args[ 0 ] ) && toBoolean( _args[ 1 ] );
				}
			}

			case OR: {
				switch (_args.length) {
					case 2:
						return toBoolean( _args[ 0 ] ) || toBoolean( _args[ 1 ] );
				}
			}

		}

		throw new IllegalArgumentException( "Cannot interpret operator "
				+ _operator.getSymbol() + " with " + _args.length + " arguments" );
	}


	public Object compute( Function _function, Object... _args )
	{
		final int cardinality = _args.length;
		switch (_function) {

			case IF: { // short-circuit eval
				switch (cardinality) {
					case 2:
						return toBoolean( _args[ 0 ] ) ? _args[ 1 ] : false;
					case 3:
						return toBoolean( _args[ 0 ] ) ? _args[ 1 ] : _args[ 2 ];
				}
			}

			case NOT: {
				switch (cardinality) {
					case 1:
						return !toBoolean( _args[ 0 ] );
				}
			}

			case INDEX: {
				switch (cardinality) {
					case 2:
						return evalIndex( (RangeValue) _args[ 0 ], _args[ 1 ] );
					case 3:
						return evalIndex( (RangeValue) _args[ 0 ], _args[ 1 ], _args[ 2 ] );
				}
			}

		}

		throw new IllegalArgumentException( "Cannot interpret function "
				+ _function.getName() + " with " + _args.length + " arguments" );
	}


	private Object evalIndex( RangeValue _range, Object _index )
	{
		int index = Util.valueToIntOrZero( _index );
		return _range.get( index - 1 );
	}


	private Object evalIndex( RangeValue _range, Object _rowIndex, Object _colIndex )
	{
		int iRow = Util.valueToIntOrOne( _rowIndex ) - 1;
		int iCol = Util.valueToIntOrOne( _colIndex ) - 1;
		int iValue;
		if (iRow < 0) iRow = 0;
		if (iCol < 0) iCol = 0;
		if (null != _rowIndex && null != _colIndex) {
			iValue = iRow * _range.getNumberOfColumns() + iCol;
		}
		else {
			iValue = iRow + iCol;
		}
		return _range.get( iValue );
	}


	@SuppressWarnings("unchecked")
	public static int match( Object _lookup, Object _in, int _type )
	{
		if (null == _in) {
			return -1;
		}
		else {
			RangeValue range = (RangeValue) _in;
			if (0 == _type) {
				int iObj = 0;
				for (Object elt : range) {
					if (_lookup.equals( elt )) return iObj;
					iObj++;
				}
				return -1;
			}
			else {
				Comparable comp = (Comparable) _lookup;
				int iObj = 0;
				for (Object elt : range) {
					int compRes = comp.compareTo( elt );
					if (-compRes == _type) return iObj - 1;
					iObj++;
				}
				return range.size() - 1;
			}
		}
	}


}
