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
 * Lists all the functions supported by AFC.
 * 
 * @author peo
 */
public enum Function {

	// Logic

	IF, NOT,

	// Math

	ABS, ACOS, ASIN, ATAN, ATAN2, COS, SIN, TAN, DEGREES, RADIANS, PI,
	CEILING, FLOOR, ROUND, ROUNDDOWN, ROUNDUP, TRUNC, EVEN, ODD, INT,
	EXP, POWER, LN, LOG, LOG10, MOD, SQRT,

	// Combinatorics

	FACT, COMBIN,

	// Financials

	NPV, MIRR, IRR, DB, DDB, SLN, SYD, FV, NPER, PMT, PV, RATE,

	// Dates

	DATE, TIME, SECOND, MINUTE, HOUR, WEEKDAY, DAY, MONTH, YEAR,
	NOW {
		@Override
		public boolean isVolatile()
		{
			return true;
		}
	},
	TODAY {
		@Override
		public boolean isVolatile()
		{
			return true;
		}
	},

	// Lookup

	MATCH, INDEX,

	// String

	CONCATENATE, LEN, LENB, MID, LEFT, RIGHT, SUBSTITUTE, REPLACE, SEARCH, FIND, EXACT, LOWER, UPPER, PROPER, REPT, TRIM,
	
	// Conversions

	N, T,
	VALUE {
		@Override
		public boolean isVolatile()
		{
			return true;
		}
	},
	TEXT
	{
		@Override
		public boolean isVolatile()
		{
			return true;
		}
	},

	//Types

	ISBLANK, ISERR, ISERROR, ISLOGICAL, ISNA, ISNONTEXT, ISNUMBER, ISTEXT,

	// Aggregators
	// Don't forget to update AGGREGATORS below!

	SUM, PRODUCT, MIN, MAX, COUNT, AVERAGE, VAR, VARP, AND, OR, KURT, SKEW, STDEV, STDEVP,

	// Database aggregators
	DSUM, DPRODUCT, DCOUNT, DMIN, DMAX;


	private static final Function[] AGGREGATORS = { SUM, PRODUCT, MIN, MAX, COUNT, AVERAGE, VAR, VARP, AND, OR, KURT, SKEW, STDEV, STDEVP };


	public String getName()
	{
		return toString();
	}

	public boolean isVolatile()
	{
		return false;
	}

	public static Function[] aggregators()
	{
		return AGGREGATORS;
	}

}
