/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.model.interpreter;

import java.text.ParseException;
import java.util.List;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.internal.NumericTypeImpl;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.runtime.internal.Environment;


abstract class InterpretedNumericType_Base
{
	private final NumericTypeImpl num;


	InterpretedNumericType_Base( NumericType _type, Environment _env )
	{
		super();
		this.num = (NumericTypeImpl) _type;
	}


	public abstract Object adjustConstantValue( Object _value );


	protected final int compare( Object _a, Object _b ) throws InterpreterException
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
				return ((String) _b).length() == 0? 0 : -1;
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
	
	public final Number minValue()
	{ 
		return this.num.getMinValue();
	}

	public final Number maxValue()
	{ 
		return this.num.getMaxValue();
	}


	public final Number fromString( String _s, Environment _env ) throws ParseException
	{
		return this.num.valueOf( _s, _env );
	}


	public boolean toBoolean( Object _value )
	{
		return valueToBoolean( _value, false );
	}


	public String toString( Object _value ) throws InterpreterException
	{
		if (_value == null) {
			return "";
		}
		else if (_value instanceof String) {
			return (String) _value;
		}
		else if (_value instanceof Number) {
			throw new InterpreterException.IsRuntimeEnvironmentDependent(); // Needs locale.
		}
		else if (_value instanceof Boolean) {
			return ((Boolean) _value)? "1" : "0";
		}
		return _value.toString();
	}


	public int toInt( Object _value, int _ifNull )
	{
		if (_value == null) return _ifNull;
		return valueToInt( _value, _ifNull );
	}


	public abstract Number toNumeric( Number _value );


	public Object compute( Operator _operator, Object... _args ) throws InterpreterException
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

	@SuppressWarnings( "unused" )
	public Object compute( Function _function, Object... _args ) throws InterpreterException
	{
		final int cardinality = _args.length;
		switch (_function) {

			case IF: { // short-circuit eval
				switch (cardinality) {
					case 2:
						return toBoolean( _args[ 0 ] )? _args[ 1 ] : false;
					case 3:
						return toBoolean( _args[ 0 ] )? _args[ 1 ] : _args[ 2 ];
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
						return toNumeric( match( _args[ 0 ], _args[ 1 ], 1 ) + 1 );
					case 3:
						return toNumeric( match( _args[ 0 ], _args[ 1 ], valueToIntOrOne( _args[ 2 ] ) ) + 1 );
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
		if (iRow < 0 || iRow >= desc.numberOfRows()) return null;
		if (iCol < 0 || iCol >= desc.numberOfColumns()) return null;
		if (null != _rowIndex && null != _colIndex) {
			iValue = iRow * desc.numberOfColumns() + iCol;
		}
		else {
			iValue = iRow + iCol;
		}
		return ((ExpressionNodeForConstantValue) _range.argument( iValue )).value();
	}


	@SuppressWarnings( "unchecked" )
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
				final int isToRightWhenComparesAs = (_type > 0)? 1 : -1;
				final List<ExpressionNode> args = range.arguments();
				final int iLast = args.size() - 1;
				int iLeft = 0;
				int iRight = iLast;
				while (iLeft < iRight) {
					final int iMid = iLeft + ((iRight - iLeft) >> 1);
					final Object arg = args.get( iMid );
					final Object elt = ((ExpressionNodeForConstantValue) arg).value();
					final int compRes = comp.compareTo( elt );
					if (compRes == isToRightWhenComparesAs) iLeft = iMid + 1;
					else iRight = iMid;
				}
				if (iLeft <= iLast) {
					final Object arg = args.get( iLeft );
					final Object elt = ((ExpressionNodeForConstantValue) arg).value();
					final int compRes = comp.compareTo( elt );
					if (compRes == 0 || compRes == isToRightWhenComparesAs) return iLeft;
				}
				return iLeft - 1;
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

	protected final String to_String( Object _o ) throws InterpreterException
	{
		return toString( _o );
	}

	protected final Number to_Number( Object _o )
	{
		return (Number) _o;
	}


	protected final Object[] asArrayOfConsts( Object _value )
	{
		if (_value instanceof ExpressionNodeForArrayReference) {
			final ExpressionNodeForArrayReference array = (ExpressionNodeForArrayReference) _value;
			final Object[] r = new Object[ array.arrayDescriptor().numberOfElements() ];
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
