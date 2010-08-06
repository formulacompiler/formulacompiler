/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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
	
	// Matrix operations
	
	MDETERM,

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

	// Lookup and reference

	CHOOSE, MATCH, INDEX, LOOKUP, HLOOKUP, VLOOKUP, ROW, COLUMN,

	INTERNAL_MATCH_INT( false, true ),

	// String

	CONCATENATE, CLEAN, LEN, LENB, MID, LEFT, RIGHT, SUBSTITUTE, REPLACE, SEARCH, FIND, EXACT, LOWER, UPPER, PROPER, REPT, TRIM,

	// Conversions

	FIXED( true ), DOLLAR( true ),

	ROMAN, N, T, VALUE, CHAR, CODE, DATEVALUE, TIMEVALUE, ASC,

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
