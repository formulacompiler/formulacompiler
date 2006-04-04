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
package sej.engine.expressions;

import sej.engine.Runtime_v1;


public enum Function {


	IF {
		@Override
		public String getName()
		{
			return "IF";
		}

		@Override
		public Object evaluate( Object _a, Object _b, Object _c )
		{
			return ((Boolean) _a) ? _b : _c;
		}
	},

	NOT {

		@Override
		public String getName()
		{
			return "NOT";
		}

		@Override
		public Object evaluate( Object _arg )
		{
			if (null == _arg) return _arg;
			return !Util.valueToBoolean( _arg, false );
		}
	},

	ROUND {
		@Override
		public String getName()
		{
			return "ROUND";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			if (_a instanceof Double) {
				double val = (Double) _a;
				long maxFrac = Math.round( (Double) _b );
				return Runtime_v1.round( val, (int) maxFrac );
			}
			else return _a;
		}
	},

	MATCH {
		@Override
		public String getName()
		{
			return "MATCH";
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate( Object _lookupValue, Object _lookupArray )
		{
			return (double) match( _lookupValue, _lookupArray, 1 ) + 1;
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate( Object _lookupValue, Object _lookupArray, Object _matchType )
		{
			return (double) match( _lookupValue, _lookupArray, Util.valueToIntOrOne( _matchType ) ) + 1;
		}
	},

	INDEX {
		@Override
		public String getName()
		{
			return "INDEX";
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate( Object _array, Object _rowNum )
		{
			if (null == _array) {
				return null;
			}
			else {
				RangeValue array = (RangeValue) _array;
				int index = Util.valueToIntOrZero( _rowNum );
				return array.get( index - 1 );
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate( Object _array, Object _rowNum, Object _colNum )
		{
			if (null == _array) {
				return null;
			}
			else {
				RangeValue array = (RangeValue) _array;
				int iRow = Util.valueToIntOrOne( _rowNum ) - 1;
				int iCol = Util.valueToIntOrOne( _colNum ) - 1;
				int iValue;
				if (iRow < 0) iRow = 0;
				if (iCol < 0) iCol = 0;
				if (null != _rowNum && null != _colNum) {
					iValue = iRow * array.getNumberOfColumns() + iCol;
				}
				else {
					iValue = iRow + iCol;
				}
				return array.get( iValue );
			}
		}
	};


	public abstract String getName();


	public Object evaluate()
	{
		Object[] args = null;
		return evaluate( args );
	}


	public Object evaluate( Object _a )
	{
		return evaluate( new Object[] { _a } );
	}


	public Object evaluate( Object _a, Object _b )
	{
		return evaluate( new Object[] { _a, _b } );
	}


	public Object evaluate( Object _a, Object _b, Object _c )
	{
		return evaluate( new Object[] { _a, _b, _c } );
	}


	public Object evaluate( Object... _args )
	{
		return null;
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
