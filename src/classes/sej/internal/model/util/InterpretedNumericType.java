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

import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.NumericTypeImpl;
import sej.internal.model.RangeValue;
import sej.internal.runtime.Runtime_v1;


public abstract class InterpretedNumericType
{
	private final NumericType num;


	InterpretedNumericType(NumericType _type)
	{
		super();
		this.num = _type;
	}

	public static InterpretedNumericType typeFor( NumericType _type )
	{
		if (Double.TYPE == _type.getValueType()) {
			return new InterpretedDoubleType( _type );
		}
		else if (BigDecimal.class == _type.getValueType()) {
			return new InterpretedBigDecimalType( _type );
		}
		else if (Long.TYPE == _type.getValueType()) {
			return new InterpretedScaledLongType( (NumericTypeImpl.AbstractLongType) _type );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for run-time interpretation." );
		}
	}


	public abstract Object adjustConstantValue( Object _value );
	protected abstract int compare( Object _a, Object _b );


	public final Number zero()
	{
		return this.num.getZero();
	}


	public boolean toBoolean( Object _value )
	{
		return valueToBoolean( _value, false );
	}


	public String toString( Object _value )
	{
		if (_value == null) return "";
		if (_value instanceof Number) {
			Number number = (Number) _value;
			return this.num.valueToConciseString( number );
		}
		if (_value instanceof Boolean) {
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


	public Object compute( Operator _operator, Object... _args )
	{
		switch (_operator) {

			case NOOP: {
				switch (_args.length) {
					case 1:
						return _args[ 0 ];
				}
				break;
			}

			case CONCAT: {
				StringBuilder result = new StringBuilder();
				for (Object arg : _args) {
					result.append( toString( arg ) );
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
				break;
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
				break;
			}

			case EQUAL: {
				switch (_args.length) {
					case 2:
						Object a = _args[ 0 ];
						Object b = _args[ 1 ];
						if (null == a) return (0 == valueToIntOrZero( b ));
						if (null == b) return (0 == valueToIntOrZero( a ));
						return a.equals( b );
				}
				break;
			}

			case NOTEQUAL: {
				switch (_args.length) {
					case 2:
						Object a = _args[ 0 ];
						Object b = _args[ 1 ];
						if (null == a) return (0 != valueToIntOrZero( b ));
						if (null == b) return (0 != valueToIntOrZero( a ));
						return !a.equals( b );
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

			case AND: {
				switch (_args.length) {
					case 2:
						return toBoolean( _args[ 0 ] ) && toBoolean( _args[ 1 ] );
				}
				break;
			}

			case OR: {
				switch (_args.length) {
					case 2:
						return toBoolean( _args[ 0 ] ) || toBoolean( _args[ 1 ] );
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
						return evalIndex( (RangeValue) _args[ 0 ], _args[ 1 ] );
					case 3:
						return evalIndex( (RangeValue) _args[ 0 ], _args[ 1 ], _args[ 2 ] );
				}
				break;
			}
			
			case COUNT: {
				return _args.length;
			}

			case LEN: {
				switch (cardinality) {
					case 1:
						return toString( _args[ 0 ] ).length();
				}
				break;
			}

			case MID: {
				switch (cardinality) {
					case 3:
						return Runtime_v1.stdMID( toString( _args[ 0 ] ), toInt( _args[ 1 ], 1 ), toInt( _args[ 2 ], 0 ) );
				}
				break;
			}

			case LEFT:
				switch (cardinality) {
					case 1:
						return Runtime_v1.stdLEFT( toString( _args[ 0 ] ), 1 );
					case 2:
						return Runtime_v1.stdLEFT( toString( _args[ 0 ] ), toInt( _args[ 1 ], 0 ) );
				}
				break;

			case RIGHT:
				switch (cardinality) {
					case 1:
						return Runtime_v1.stdRIGHT( toString( _args[ 0 ] ), 1 );
					case 2:
						return Runtime_v1.stdRIGHT( toString( _args[ 0 ] ), toInt( _args[ 1 ], 0 ) );
				}
				break;

			case SUBSTITUTE:
				switch (cardinality) {
					case 3:
						return Runtime_v1.stdSUBSTITUTE( toString( _args[ 0 ] ), toString( _args[ 1 ] ),
								toString( _args[ 2 ] ) );
					case 4:
						return Runtime_v1.stdSUBSTITUTE( toString( _args[ 0 ] ), toString( _args[ 1 ] ),
								toString( _args[ 2 ] ), toInt( _args[ 3 ], 0 ) );
				}
				break;

			case REPLACE:
				switch (cardinality) {
					case 4:
						return Runtime_v1.stdREPLACE( toString( _args[ 0 ] ), toInt( _args[ 1 ], 0 ), toInt( _args[ 2 ], 0 ),
								toString( _args[ 3 ] ) );
				}
				break;

			case EXACT:
				switch (cardinality) {
					case 2:
						return Runtime_v1.stdEXACT( toString( _args[ 0 ] ), toString( _args[ 1 ] ) );
				}
				break;

			case FIND:
				switch (cardinality) {
					case 2:
						return Runtime_v1.stdFIND( toString( _args[ 0 ] ), toString( _args[ 1 ] ), 1 );
					case 3:
						return Runtime_v1.stdFIND( toString( _args[ 0 ] ), toString( _args[ 1 ] ), toInt( _args[ 2 ], 1 ) );
				}
				break;

			case SEARCH:
				switch (cardinality) {
					case 2:
						return Runtime_v1.stdSEARCH( toString( _args[ 0 ] ), toString( _args[ 1 ] ), 1 );
					case 3:
						return Runtime_v1.stdSEARCH( toString( _args[ 0 ] ), toString( _args[ 1 ] ), toInt( _args[ 2 ], 1 ) );
				}
				break;

			case LOWER:
				switch (cardinality) {
					case 1:
						return Runtime_v1.stdLOWER( toString( _args[ 0 ] ) );
				}
				break;

			case UPPER:
				switch (cardinality) {
					case 1:
						return Runtime_v1.stdUPPER( toString( _args[ 0 ] ) );
				}
				break;

			// LATER case PROPER:
			/*
			 * switch (cardinality) { case 1: return Runtime_v1.stdPROPER( toString( _args[ 0 ] ) ); }
			 * break;
			 */

		}

		throw new EvalNotPossibleException();
	}


	private Object evalIndex( RangeValue _range, Object _index )
	{
		int index = valueToIntOrZero( _index );
		return _range.get( index - 1 );
	}


	private Object evalIndex( RangeValue _range, Object _rowIndex, Object _colIndex )
	{
		int iRow = valueToIntOrOne( _rowIndex ) - 1;
		int iCol = valueToIntOrOne( _colIndex ) - 1;
		int iValue;
		if (iRow < 0 || iRow >= _range.getNumberOfRows()) return null;
		if (iCol < 0 || iCol >= _range.getNumberOfColumns()) return null;
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
			final RangeValue range = (RangeValue) _in;
			if (0 == _type) {
				int iObj = 0;
				for (Object elt : range) {
					if (_lookup.equals( elt )) return iObj;
					iObj++;
				}
				return -1;
			}
			else {
				final Comparable comp = (Comparable) _lookup;
				final int compResIndicatingMatch = (_type < 0)? 1 : -1;
				int iObj = 0;
				for (Object elt : range) {
					final int compRes = comp.compareTo( elt );
					if (compRes == compResIndicatingMatch) return iObj - 1;
					iObj++;
				}
				return range.size() - 1;
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

}
