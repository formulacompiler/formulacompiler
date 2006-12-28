/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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

import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.expressions.ArrayDescriptor;
import sej.internal.expressions.ExpressionNode;
import sej.internal.expressions.ExpressionNodeForArrayReference;
import sej.internal.expressions.ExpressionNodeForConstantValue;

abstract class InterpretedNumericType_Base
{
	private final NumericType num;


	InterpretedNumericType_Base(NumericType _type)
	{
		super();
		this.num = _type;
	}


	public abstract Object adjustConstantValue( Object _value );


	protected final int compare( Object _a, Object _b )
	{
		if (_a instanceof String) {
			if (_b instanceof String || null == _b) {
				return toString( _a ).compareToIgnoreCase( toString( _b ) );
			}
			else {
				return +1; // String always greater than number in Excel.
			}
		}
		else if (_b instanceof String) {
			if (null == _a) {
				return ((String) _b).length() == 0 ? 0 : -1;
			}
			else {
				return -1; // Number always less than string in Excel.
			}
		}
		else {
			return compareNumerically( _a, _b );
		}
	}

	protected abstract int compareNumerically( Object _a, Object _b );


	public final Number zero()
	{
		return this.num.getZero();
	}


	public abstract Number fromString( String _s );


	public boolean toBoolean( Object _value )
	{
		return valueToBoolean( _value, false );
	}


	public String toString( Object _value )
	{
		if (_value == null) {
			return "";
		}
		else if (_value instanceof String) {
			return (String) _value;
		}
		else if (_value instanceof Number) {
			Number number = (Number) _value;
			return this.num.valueToConciseString( number );
		}
		else if (_value instanceof Boolean) {
			Boolean bool = (Boolean) _value;
			return bool ? "1" : "0";
		}
		return _value.toString();
	}


	public int toInt( Object _value, int _ifNull )
	{
		if (_value == null) return _ifNull;
		return valueToInt( _value, _ifNull );
	}


	public abstract Object toNumeric( Number _value );


	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case CONCAT: {
				StringBuilder result = new StringBuilder();
				for (Object arg : _args) {
					result.append( toString( arg ) );
				}
				return result.toString();
			}

			case EQUAL: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) == 0;
				}
				break;
			}

			case NOTEQUAL: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) != 0;
				}
				break;
			}

			case GREATER: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) > 0;
				}
				break;
			}

			case GREATEROREQUAL: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) >= 0;
				}
				break;
			}

			case LESS: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) < 0;
				}
				break;
			}

			case LESSOREQUAL: {
				switch (_args.length) {
					case 2:
						return compare( _args[ 0 ], _args[ 1 ] ) <= 0;
				}
				break;
			}

		}

		throw new EvalNotPossibleException();
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
				break;
			}

			case NOT: {
				switch (cardinality) {
					case 1:
						return !toBoolean( _args[ 0 ] );
				}
				break;
			}

			case INDEX: {
				switch (cardinality) {
					case 2:
						return evalIndex( (ExpressionNodeForArrayReference) _args[ 0 ], _args[ 1 ] );
					case 3:
						return evalIndex( (ExpressionNodeForArrayReference) _args[ 0 ], _args[ 1 ], _args[ 2 ] );
				}
				break;
			}

			case MATCH: {
				switch (cardinality) {
					case 2:
						return toNumeric( InterpretedNumericType.match( _args[ 0 ], _args[ 1 ], 1 ) + 1 );
					case 3:
						return toNumeric( InterpretedNumericType
								.match( _args[ 0 ], _args[ 1 ], valueToIntOrOne( _args[ 2 ] ) ) + 1 );
				}
				break;
			}

			case COUNT: {
				return _args.length;
			}

		}

		throw new EvalNotPossibleException();
	}


	private Object evalIndex( ExpressionNodeForArrayReference _range, Object _index )
	{
		int index = valueToIntOrZero( _index );
		return ((ExpressionNodeForConstantValue) _range.argument( index - 1 )).value();
	}


	private Object evalIndex( ExpressionNodeForArrayReference _range, Object _rowIndex, Object _colIndex )
	{
		final ArrayDescriptor desc = _range.arrayDescriptor();
		final int iRow = valueToIntOrOne( _rowIndex ) - 1;
		final int iCol = valueToIntOrOne( _colIndex ) - 1;
		int iValue;
		if (iRow < 0 || iRow >= desc.getNumberOfRows()) return null;
		if (iCol < 0 || iCol >= desc.getNumberOfColumns()) return null;
		if (null != _rowIndex && null != _colIndex) {
			iValue = iRow * desc.getNumberOfColumns() + iCol;
		}
		else {
			iValue = iRow + iCol;
		}
		return ((ExpressionNodeForConstantValue) _range.argument( iValue )).value();
	}


	@SuppressWarnings("unchecked")
	public static int match( Object _lookup, Object _in, int _type )
	{
		if (null == _in) {
			return -1;
		}
		else {
			final ExpressionNodeForArrayReference range = (ExpressionNodeForArrayReference) _in;
			if (0 == _type) {
				int iObj = 0;
				for (Object arg : range.arguments()) {
					final Object elt = ((ExpressionNodeForConstantValue) arg).value();
					if (_lookup.equals( elt )) return iObj;
					iObj++;
				}
				return -1;
			}
			else {
				final Comparable comp = (Comparable) _lookup;
				final int compResIndicatingMatch = (_type < 0) ? 1 : -1;
				int iObj = 0;
				for (Object arg : range.arguments()) {
					final Object elt = ((ExpressionNodeForConstantValue) arg).value();
					final int compRes = comp.compareTo( elt );
					if (compRes == compResIndicatingMatch) return iObj - 1;
					iObj++;
				}
				return range.arguments().size() - 1;
			}
		}
	}


	protected int valueToInt( Object _value, int _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).intValue();
		if (_value instanceof String) return Integer.valueOf( (String) _value );
		return _ifNull;
	}

	protected final int valueToIntOrZero( Object _value )
	{
		return valueToInt( _value, 0 );
	}

	protected final int valueToIntOrOne( Object _value )
	{
		return valueToInt( _value, 1 );
	}

	protected boolean valueToBoolean( Object _value, boolean _ifNull )
	{
		if (_value instanceof Boolean) return (Boolean) _value;
		if (_value instanceof Number) return (0 != ((Number) _value).intValue());
		if (_value instanceof String) return Boolean.parseBoolean( (String) _value );
		return _ifNull;
	}


	// Conversions for generated code:

	protected final int to_int( Object _o )
	{
		return valueToIntOrOne( _o );
	}

	protected final String to_String( Object _o )
	{
		return toString( _o );
	}


	protected final Object[] asArrayOfConsts( Object _value )
	{
		if (_value instanceof ExpressionNodeForArrayReference) {
			final ExpressionNodeForArrayReference array = (ExpressionNodeForArrayReference) _value;
			final Object[] r = new BigDecimal[ array.arrayDescriptor().getNumberOfElements() ];
			int i = 0;
			for (ExpressionNode cst : array.arguments()) {
				r[ i++ ] = ((ExpressionNodeForConstantValue) cst).value();
			}
			return r;
		}
		else {
			throw new IllegalArgumentException();
		}

	}

}
