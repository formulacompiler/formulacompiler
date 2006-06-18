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
package sej;


/**
 * Lists all the expression operators supported by SEJ.
 * 
 * @author peo
 * 
 * @see SpreadsheetBuilder#op(Operator, sej.SpreadsheetBuilder.ExprNode[])
 */
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
	};


	public abstract String getName();

	public abstract String getSymbol();


	public boolean isPrefix()
	{
		return true;
	}

}
