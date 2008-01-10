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

	ABS, ACOS, ASIN, ATAN, ATAN2, ACOSH, ASINH, ATANH, COS, COSH, GEOMEAN, SIN, TAN, DEGREES, RADIANS, PI, CEILING, FLOOR, ROUND, ROUNDDOWN, ROUNDUP, TRUNC, EVEN, ODD, INT, EXP, POWER, LN, LOG, LOG10, MOD, SQRT, HARMEAN, PERMUT, SINH, TANH, SIGN,

	RAND( true ),

	// Combinatorics

	FACT, COMBIN,

	// Financials

	NPV, MIRR, IRR, DB, DDB, SLN, SYD, FV, NPER, PMT, PV, RATE, VDB,

	// Statistical

	RANK, VAR, VARP, STDEV, STDEVP, AVEDEV, DEVSQ, COVAR, SKEW, KURT, CONFIDENCE,
	ERF, ERFC, NORMDIST, NORMSDIST, NORMINV, NORMSINV, LOGNORMDIST, LOGINV,
	BETADIST, BETAINV, BINOMDIST, CHIDIST, CHIINV, CHITEST, EXPONDIST, FDIST, FINV, FTEST, 
	FISHER, FISHERINV, GAMMADIST, GAMMAINV, GAMMALN, HYPGEOMDIST, NEGBINOMDIST, PEARSON,
	POISSON, RSQ, STANDARDIZE, TDIST, TINV, TTEST, WEIBULL, ZTEST,
	STDEVPA, SUMX2MY2, SUMX2PY2, SUMXMY2, VARA, STEYX, CORREL, INTERCEPT, SLOPE, FORECAST, PROB,
	CRITBINOM, LARGE, MEDIAN, MODE, PERCENTILE, PERCENTRANK, QUARTILE, SMALL, TRIMMEAN,

	// Dates

	DATE, TIME, SECOND, MINUTE, HOUR, WEEKDAY, DAY, MONTH, YEAR, DAYS360,

	NOW( true ), TODAY( true ),

	// Lookup

	CHOOSE, MATCH, INDEX, LOOKUP, HLOOKUP, VLOOKUP,

	INTERNAL_MATCH_INT( false, true ),

	// String

	CONCATENATE, CLEAN, LEN, LENB, MID, LEFT, RIGHT, SUBSTITUTE, REPLACE, SEARCH, FIND, EXACT, LOWER, UPPER, PROPER, REPT, TRIM,

	// Conversions

	FIXED( true ), DOLLAR( true ),

	ROMAN, N, T, VALUE, CHAR, CODE, DATEVALUE, TIMEVALUE,

	/**
	 * {@code TEXT} is volatile because all to-text conversions are considered dependent on the
	 * runtime locale/time-zone configuration.
	 */
	TEXT( true ),

	// Types

	ISBLANK, ISERR, ISERROR, ISLOGICAL, ISNA, ISNONTEXT, ISNUMBER, ISTEXT,

	ERRORTYPE,

	// Aggregators
	// Don't forget to update AGGREGATORS below!

	SUM, PRODUCT, MIN, MAX, COUNT, COUNTA, AVERAGE, AND, OR, SUMSQ,

	// Database aggregators
	DSUM, DPRODUCT, DCOUNT, DCOUNTA, DMIN, DMAX, DAVERAGE, DVARP, DVAR, DSTDEVP, DSTDEV, DGET, 
	SUMIF, COUNTIF,

	/**
	 * {@code ERROR()} is volatile because it throws exceptions.
	 */
	ERROR( true ),

	/**
	 * {@code NA()} is volatile because it throws exceptions.
	 */
	NA( true );


	private static final Function[] AGGREGATORS = { SUM, PRODUCT, MIN, MAX, COUNT, COUNTA, AVERAGE, VAR, VARP, AND, OR,
			KURT, SKEW, STDEV, STDEVP, AVEDEV, DEVSQ, SUMSQ, GEOMEAN, HARMEAN };


	private final boolean isVolatile;
	private final boolean returnsInt;

	private Function()
	{
		this( false, false );
	}

	private Function( boolean _isVolatile )
	{
		this( _isVolatile, false );
	}

	private Function( boolean _isVolatile, boolean _returnsInt )
	{
		this.isVolatile = _isVolatile;
		this.returnsInt = _returnsInt;
	}

	public String getName()
	{
		return toString();
	}

	public boolean isVolatile()
	{
		return this.isVolatile;
	}

	public boolean returnsInt()
	{
		return this.returnsInt;
	}

	public static Function[] aggregators()
	{
		return AGGREGATORS;
	}

}
