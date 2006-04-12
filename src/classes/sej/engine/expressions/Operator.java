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


public enum Operator {


	NOOP {
		@Override
		public String getName()
		{
			return "NOOP";
		}

		@Override
		public String getSymbol()
		{
			return "";
		}

		@Override
		public Object evaluate( Object... _args )
		{
			return _args[ 0 ];
		}
	},


	CONCAT {
		@Override
		public String getName()
		{
			return "CONCAT";
		}

		@Override
		public String getSymbol()
		{
			return "&";
		}

		@Override
		public Object evaluate( Object... _args )
		{
			StringBuilder result = new StringBuilder();
			for (Object a : _args) {
				result.append( Util.valueToString( a ) );
			}
			return result.toString();
		}
	},


	PLUS {
		@Override
		public String getName()
		{
			return "PLUS";
		}

		@Override
		public String getSymbol()
		{
			return "+";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Util.valueToDoubleOrZero( _a ) + Util.valueToDoubleOrZero( _b );
		}

		@Override
		public Object evaluate( Object... _args )
		{
			double result = 0;
			for (Object a : _args) {
				result += Util.valueToDoubleOrZero( a );
			}
			return result;
		}
	},


	MINUS {
		@Override
		public String getName()
		{
			return "MINUS";
		}

		@Override
		public String getSymbol()
		{
			return "-";
		}

		@Override
		public Object evaluate( Object _a )
		{
			return -Util.valueToDoubleOrZero( _a );
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Util.valueToDoubleOrZero( _a ) - Util.valueToDoubleOrZero( _b );
		}
	},


	TIMES {
		@Override
		public String getName()
		{
			return "TIMES";
		}

		@Override
		public String getSymbol()
		{
			return "*";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Util.valueToDoubleOrZero( _a ) * Util.valueToDoubleOrZero( _b );
		}

		@Override
		public Object evaluate( Object... _args )
		{
			double result = 1;
			for (Object a : _args) {
				if (null != a) {
					result *= Util.valueToDouble( a, 1.0 );
				}
			}
			return result;
		}
	},

	DIV {
		@Override
		public String getName()
		{
			return "DIV";
		}

		@Override
		public String getSymbol()
		{
			return "/";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Util.valueToDoubleOrZero( _a ) / Util.valueToDoubleOrZero( _b );
		}
	},

	EXP {
		@Override
		public String getName()
		{
			return "EXP";
		}

		@Override
		public String getSymbol()
		{
			return "^";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Math.pow( Util.valueToDoubleOrZero( _a ), Util.valueToDoubleOrZero( _b ) );
		}
	},

	PERCENT {
		@Override
		public String getName()
		{
			return "PERCENT";
		}

		@Override
		public String getSymbol()
		{
			return "%";
		}

		@Override
		public boolean isPrefix()
		{
			return false;
		}

		@Override
		public Object evaluate( Object _a )
		{
			return Util.valueToDoubleOrZero( _a ) / 100;
		}
	},

	EQUAL {
		@Override
		public String getName()
		{
			return "EQUAL";
		}

		@Override
		public String getSymbol()
		{
			return "=";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return _a.equals( _b );
		}
	},

	NOTEQUAL {
		@Override
		public String getName()
		{
			return "NOTEQUAL";
		}

		@Override
		public String getSymbol()
		{
			return "<>";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return !_a.equals( _b );
		}
	},

	LESS {
		@Override
		public String getName()
		{
			return "LESS";
		}

		@Override
		public String getSymbol()
		{
			return "<";
		}

		@Override
		@SuppressWarnings("unchecked")
		public Object evaluate( Object _a, Object _b )
		{
			return compare( _a, _b ) < 0;
		}
	},

	LESSOREQUAL {
		@Override
		public String getName()
		{
			return "LESSOREQUAL";
		}

		@Override
		public String getSymbol()
		{
			return "<=";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return compare( _a, _b ) <= 0;
		}
	},

	GREATER {
		@Override
		public String getName()
		{
			return "GREATER";
		}

		@Override
		public String getSymbol()
		{
			return ">";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return compare( _a, _b ) > 0;
		}
	},

	GREATEROREQUAL {
		@Override
		public String getName()
		{
			return "GREATEROREQUAL";
		}

		@Override
		public String getSymbol()
		{
			return ">=";
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return compare( _a, _b ) >= 0;
		}
	},

	MIN {
		@Override
		public String getName()
		{
			return "$MIN";
		}

		@Override
		public String getSymbol()
		{
			return getName();
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return compare( _a, _b ) <= 0 ? _a : _b;
		}
	},

	MAX {
		@Override
		public String getName()
		{
			return "$MAX";
		}

		@Override
		public String getSymbol()
		{
			return getName();
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return compare( _a, _b ) >= 0 ? _a : _b;
		}
	},

	AND {
		@Override
		public String getName()
		{
			return "$AND";
		}

		@Override
		public String getSymbol()
		{
			return getName();
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Util.valueToBoolean( _a, false ) && Util.valueToBoolean( _b, false );
		}
	},

	OR {
		@Override
		public String getName()
		{
			return "$OR";
		}

		@Override
		public String getSymbol()
		{
			return getName();
		}

		@Override
		public Object evaluate( Object _a, Object _b )
		{
			return Util.valueToBoolean( _a, false ) || Util.valueToBoolean( _b, false );
		}
	};


	public static final String RUNTIME = "sej/engine/Runtime_v1";

	public abstract String getName();

	public abstract String getSymbol();


	public boolean isPrefix()
	{
		return true;
	}


	@SuppressWarnings("unchecked")
	protected int compare( Object _a, Object _b )
	{
		if ((_a instanceof Comparable) && (_b instanceof Comparable)) {
			Comparable a = (Comparable) _a;
			Comparable b = (Comparable) _b;
			return a.compareTo( b );
		}
		return 0;
	}


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

}
