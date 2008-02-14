/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler;


/**
 * Lists all the expression operators supported by AFC.
 * 
 * @author peo
 */
public enum Operator {


	CONCAT
	{
		@Override
		public String getSymbol()
		{
			return "&";
		}
	},


	PLUS
	{
		@Override
		public String getSymbol()
		{
			return "+";
		}
	},


	MINUS
	{
		@Override
		public String getSymbol()
		{
			return "-";
		}
	},


	TIMES
	{
		@Override
		public String getSymbol()
		{
			return "*";
		}
	},

	DIV
	{
		@Override
		public String getSymbol()
		{
			return "/";
		}
	},

	EXP
	{
		@Override
		public String getSymbol()
		{
			return "^";
		}
	},

	PERCENT
	{
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
	},

	EQUAL
	{
		@Override
		public String getSymbol()
		{
			return "=";
		}

		@Override
		public Operator inverse()
		{
			return NOTEQUAL;
		}
	},

	NOTEQUAL
	{
		@Override
		public String getSymbol()
		{
			return "<>";
		}

		@Override
		public Operator inverse()
		{
			return EQUAL;
		}
	},

	LESS
	{
		@Override
		public String getSymbol()
		{
			return "<";
		}

		@Override
		public Operator inverse()
		{
			return GREATEROREQUAL;
		}
	},

	LESSOREQUAL
	{
		@Override
		public String getSymbol()
		{
			return "<=";
		}

		@Override
		public Operator inverse()
		{
			return GREATER;
		}
	},

	GREATER
	{
		@Override
		public String getSymbol()
		{
			return ">";
		}

		@Override
		public Operator inverse()
		{
			return LESSOREQUAL;
		}
	},

	GREATEROREQUAL
	{
		@Override
		public String getSymbol()
		{
			return ">=";
		}

		@Override
		public Operator inverse()
		{
			return LESS;
		}
	},

	/**
	 * Internal - do not use!
	 */
	INTERNAL_MIN
	{
		@Override
		public String getName()
		{
			return "_min_";
		}

		@Override
		public String getSymbol()
		{
			return getName();
		}
	},

	/**
	 * Internal - do not use!
	 */
	INTERNAL_MAX
	{
		@Override
		public String getName()
		{
			return "_max_";
		}

		@Override
		public String getSymbol()
		{
			return getName();
		}
	};


	public String getName()
	{
		return toString();
	}

	public abstract String getSymbol();


	public boolean isPrefix()
	{
		return true;
	}

	public Operator inverse()
	{
		throw new IllegalStateException( "inverse() not supported for " + toString() );
	}

}
