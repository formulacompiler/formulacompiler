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
package org.formulacompiler.compiler;



/**
 * Lists all the expression operators supported by AFC.
 * 
 * @author peo
 */
public enum Operator {


	CONCAT {
		@Override
		public String getSymbol()
		{
			return "&";
		}
	},


	PLUS {
		@Override
		public String getSymbol()
		{
			return "+";
		}
	},


	MINUS {
		@Override
		public String getSymbol()
		{
			return "-";
		}
	},


	TIMES {
		@Override
		public String getSymbol()
		{
			return "*";
		}
	},

	DIV {
		@Override
		public String getSymbol()
		{
			return "/";
		}
	},

	EXP {
		@Override
		public String getSymbol()
		{
			return "^";
		}
	},

	PERCENT {
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

	EQUAL {
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

	NOTEQUAL {
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

	LESS {
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

	LESSOREQUAL {
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

	GREATER {
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

	GREATEROREQUAL {
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
	INTERNAL_MIN {
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
	INTERNAL_MAX {
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
