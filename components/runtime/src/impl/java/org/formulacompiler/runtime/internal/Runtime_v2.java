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
package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.Collator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.NotAvailableException;


public abstract class Runtime_v2
{

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	static final long SECS_PER_HOUR = 60 * 60;
	static final long SECS_PER_DAY = 24 * SECS_PER_HOUR;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;
	static final int NON_LEAP_DAY = 61; // LATER Do not use it for OpenDocument
	static final int UTC_OFFSET_DAYS = 25569;
	static final int UTC_OFFSET_DAYS_1904 = 24107;
	static final boolean BASED_ON_1904 = false;
	protected static Random generator = new Random();

	private static final BigDecimal MAX_EXP_VALUE = BigDecimal.valueOf( 1, 4 ); // 1E-4


	public static double checkDouble( final double _value )
	{
		if (Double.isNaN( _value )) throw new FormulaException( "#NUM! (value is NaN)" );
		if (Double.isInfinite( _value )) throw new FormulaException( "#NUM! (value is infinite)" );
		return _value;
	}

	public static byte unboxByte( Byte _boxed )
	{
		return (_boxed == null)? 0 : _boxed;
	}

	public static short unboxShort( Short _boxed )
	{
		return (_boxed == null)? 0 : _boxed;
	}

	public static int unboxInteger( Integer _boxed )
	{
		return (_boxed == null)? 0 : _boxed;
	}

	public static long unboxLong( Long _boxed )
	{
		return (_boxed == null)? 0L : _boxed;
	}

	public static float unboxFloat( Float _boxed )
	{
		return (_boxed == null)? 0 : _boxed;
	}

	public static double unboxDouble( Double _boxed )
	{
		return (_boxed == null)? 0 : _boxed;
	}

	public static boolean unboxBoolean( Boolean _boxed )
	{
		return (_boxed == null)? false : _boxed;
	}

	public static char unboxCharacter( Character _boxed )
	{
		return (_boxed == null)? 0 : _boxed;
	}


	static Date now( final ComputationTime _computationTime )
	{
		return new Date( _computationTime.getNowMillis() );
	}

	static Date today( TimeZone _timeZone, final ComputationTime _computationTime )
	{
		final Calendar calendar = Calendar.getInstance( _timeZone );
		calendar.setTimeInMillis( _computationTime.getNowMillis() );
		calendar.set( Calendar.HOUR_OF_DAY, 0 );
		calendar.set( Calendar.MINUTE, 0 );
		calendar.set( Calendar.SECOND, 0 );
		calendar.set( Calendar.MILLISECOND, 0 );
		return calendar.getTime();
	}

	public static long dateToMsSinceLocal1970( Date _date, TimeZone _timeZone )
	{
		final long msSinceUTC1970 = _date.getTime();
		final int timeZoneOffset = _timeZone.getOffset( msSinceUTC1970 );
		final long msSinceLocal1970 = msSinceUTC1970 + timeZoneOffset;
		return msSinceLocal1970;
	}


	static Number parseNumber( String _text, boolean _parseBigDecimal, Environment _environment )
	{
		final String text = _text.toUpperCase( _environment.locale() );

		final NumberFormat numberFormat = getNumberFormat( _environment );
		setParseBigDecimal( numberFormat, _parseBigDecimal );
		Number result = parseNumber( text, numberFormat );
		if (result != null) {
			return result;
		}

		if (numberFormat instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			final DecimalFormatSymbols formatSymbols = decimalFormat.getDecimalFormatSymbols();
			final char c = formatSymbols.getGroupingSeparator();
			if (Character.isSpaceChar( c )) {
				formatSymbols.setGroupingSeparator( '\u0020' );
				decimalFormat.setDecimalFormatSymbols( formatSymbols );
				result = parseNumber( text, decimalFormat );
				if (result != null) {
					return result;
				}
			}
		}

		final NumberFormat percentFormat = getPercentFormat( _environment );
		setParseBigDecimal( percentFormat, _parseBigDecimal );
		result = parseNumber( text, percentFormat );
		if (result != null) {
			return result;
		}

		final DecimalFormatSymbols formatSymbols = getDecimalFormatSymbols( _environment );
		final DecimalFormat scientificFormat = new DecimalFormat( "#0.###E0", formatSymbols );
		scientificFormat.setParseBigDecimal( _parseBigDecimal );
		result = parseNumber( text, numberFormat );
		if (result != null) {
			return result;
		}

		return parseDateAndOrTime( text, _environment );
	}

	private static Number parseNumber( String _text, NumberFormat _numberFormat )
	{
		final ParsePosition parsePosition = new ParsePosition( 0 );
		final Number number = _numberFormat.parse( _text, parsePosition );
		final int index = parsePosition.getIndex();
		if (_text.length() == index) {
			return number;
		}
		else {
			return null;
		}
	}

	private static NumberFormat getPercentFormat( final Environment _environment )
	{
		final NumberFormat format = NumberFormat.getPercentInstance( _environment.locale() );
		setDecimalFormatSymbols( format, _environment );
		return format;
	}

	private static NumberFormat getNumberFormat( final Environment _environment )
	{
		final NumberFormat format = NumberFormat.getInstance( _environment.locale() );
		setDecimalFormatSymbols( format, _environment );
		return format;
	}

	private static void setDecimalFormatSymbols( final NumberFormat _format, final Environment _environment )
	{
		final DecimalFormatSymbols envSymbols = _environment.decimalFormatSymbols();
		if (envSymbols != null && _format instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) _format;
			decimalFormat.setDecimalFormatSymbols( envSymbols );
		}
	}

	private static void setParseBigDecimal( final NumberFormat _format, final boolean _parseBigDecimal )
	{
		if (_format instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) _format;
			decimalFormat.setParseBigDecimal( _parseBigDecimal );
		}
	}

	private static Number parseDateAndOrTime( String _text, Environment _environment )
	{
		try {
			final Date date = _environment.parseDateAndOrTime( _text );
			return dateToDouble( date, _environment.timeZone() );
		}
		catch (ParseException e) {
			return null;
		}
	}


	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	public static Date dateFromDouble( double _excel, TimeZone _timeZone )
	{
		return new Date( msSinceUTC1970FromDouble( _excel, _timeZone ) );
	}

	public static long msSinceUTC1970FromDouble( double _excel, TimeZone _timeZone )
	{
		final long msSinceLocal1970 = msSinceLocal1970FromExcelDate( _excel );
		final int timeZoneOffset = _timeZone.getOffset( msSinceLocal1970 - _timeZone.getRawOffset() );
		final long msSinceUTC1970 = msSinceLocal1970 - timeZoneOffset;
		return msSinceUTC1970;
	}

	public static long msFromDouble( double _excel )
	{
		final long ms = Math.round( _excel * SECS_PER_DAY ) * MS_PER_SEC;
		return ms;
	}

	public static double dateToDouble( Date _date, TimeZone _timeZone )
	{
		if (_date == null) {
			return 0;
		}
		else {
			final long msSinceLocal1970 = dateToMsSinceLocal1970( _date, _timeZone );
			final double excel = msSinceLocal1970ToExcelDate( msSinceLocal1970 );
			return excel;
		}
	}

	public static double msSinceUTC1970ToDouble( long _msSinceUTC1970, TimeZone _timeZone )
	{
		final int timeZoneOffset = _timeZone.getOffset( _msSinceUTC1970 );
		final long msSinceLocal1970 = _msSinceUTC1970 + timeZoneOffset;
		final double excel = msSinceLocal1970ToExcelDate( msSinceLocal1970 );
		return excel;
	}

	public static double msToDouble( long _ms )
	{
		final double excel = (double) _ms / (double) MS_PER_DAY;
		return excel;
	}

	private static long msSinceLocal1970FromExcelDate( double _excelDate )
	{
		final boolean time = (Math.abs( _excelDate ) < 1);
		double numValue = checkDouble( _excelDate );

		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but in actual fact this was not a leap year.
		// Therefore for values less than 61 in the 1900 date system,
		// add one to the numeric value
		if (!BASED_ON_1904 && !time && numValue < NON_LEAP_DAY) {
			numValue += 1;
		}

		// Convert this to the number of days since 01 Jan 1970
		final int offsetDays = BASED_ON_1904? UTC_OFFSET_DAYS_1904 : UTC_OFFSET_DAYS;
		final double utcDays = numValue - offsetDays;

		// Convert this into utc by multiplying by the number of milliseconds
		// in a day. Use the round function prior to ms conversion due
		// to a rounding feature of Excel (contributed by Jurgen
		final long msSinceLocal1970 = Math.round( utcDays * SECS_PER_DAY ) * MS_PER_SEC;
		return msSinceLocal1970;
	}

	private static double msSinceLocal1970ToExcelDate( final long _msSinceLocal1970 )
	{
		// Convert this to the number of days, plus fractions of a day since
		// 01 Jan 1970
		final double utcDays = (double) _msSinceLocal1970 / (double) MS_PER_DAY;

		// Add in the offset to get the number of days since 01 Jan 1900
		double value = utcDays + UTC_OFFSET_DAYS;

		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but this was not a leap year.
		// Therefore for values less than 61, we must subtract 1. Only do
		// this for full dates, not times
		if (value < NON_LEAP_DAY) {
			value -= 1;
		}

		return value;
	}


	public static String stringFromObject( Object _obj )
	{
		return (_obj == null)? "" : _obj.toString();
	}

	public static String stringFromString( String _str )
	{
		return (_str == null)? "" : _str;
	}

	static String stringFromBigDecimal( BigDecimal _value, Environment _environment )
	{
		if (_value.compareTo( BigDecimal.ZERO ) == 0) return "0"; // avoid "0.0"
		final BigDecimal stripped = _value.stripTrailingZeros();
		final int scale = stripped.scale();
		final int prec = stripped.precision();
		final int ints = prec - scale;
		if (ints > 20) {
			final DecimalFormatSymbols syms = getDecimalFormatSymbols( _environment );
			// Note: '.' is hard-coded in BigDecimal.toString().
			return stripped.toString().replace( '.', syms.getDecimalSeparator() );
		}
		else {
			final NumberFormat numberFormat = getNumberFormat( _environment );
			numberFormat.setGroupingUsed( false );
			numberFormat.setMaximumFractionDigits( scale );
			return numberFormat.format( stripped );
		}
	}

	private static String stringFromBigDecimal( BigDecimal _value, Environment _environment, int _intDigitsLimitFrac,
			int _intDigitsLimitInt )
	{
		if (_value.compareTo( BigDecimal.ZERO ) == 0) {
			return "0"; // avoid "0.0"
		}

		final BigDecimal stripped = _value.stripTrailingZeros();
		final int scale = stripped.scale();
		final int precision = stripped.precision();
		final int integerDigits = precision - scale;
		if (integerDigits > _intDigitsLimitInt) {
			return formatExp( stripped, _environment );
		}
		if (scale > 9 && integerDigits <= 0 && _value.abs().compareTo( MAX_EXP_VALUE ) < 0) {
			return formatExp( stripped, _environment );
		}
		final int fractionDigits = _intDigitsLimitFrac - integerDigits;
		final int maximumFractionDigits = fractionDigits > 0? Math.min( fractionDigits, _intDigitsLimitFrac - 1 ) : 0;
		if (scale > maximumFractionDigits) {
			final BigDecimal scaled = stripped.setScale( maximumFractionDigits, RoundingMode.HALF_UP );
			return stringFromBigDecimal( scaled, _environment, _intDigitsLimitFrac, _intDigitsLimitInt );
		}

		final NumberFormat numberFormat = getNumberFormat( _environment );
		numberFormat.setGroupingUsed( false );
		numberFormat.setMaximumFractionDigits( maximumFractionDigits );
		return numberFormat.format( stripped );
	}

	private static String formatExp( BigDecimal _value, Environment _environment )
	{
		final DecimalFormatSymbols formatSymbols = getDecimalFormatSymbols( _environment );
		final DecimalFormat scientificFormat = new DecimalFormat( "0.#####E00", formatSymbols );
		final String numStr = scientificFormat.format( _value );
		final int expIndex = numStr.indexOf( 'E' );
		if (expIndex != -1 && numStr.charAt( expIndex + 1 ) != formatSymbols.getMinusSign()) {
			final StringBuilder stringBuilder = new StringBuilder();
			stringBuilder.append( numStr.substring( 0, expIndex + 1 ) );
			stringBuilder.append( '+' );
			stringBuilder.append( numStr.substring( expIndex + 1 ) );
			return stringBuilder.toString();
		}
		else {
			return numStr;
		}
	}

	private static DecimalFormatSymbols getDecimalFormatSymbols( Environment _environment )
	{
		final DecimalFormatSymbols envSymbols = _environment.decimalFormatSymbols();
		return envSymbols != null? envSymbols : new DecimalFormatSymbols( _environment.locale() );
	}


	public static String emptyString()
	{
		return "";
	}

	private static String notNull( String _s )
	{
		return (_s == null)? "" : _s;
	}


	protected static void err_CEILING()
	{
		fun_ERROR( "#NUM! because signum of args not equal in CEILING" );
	}

	protected static void err_FLOOR()
	{
		fun_ERROR( "#NUM! because signum of args not equal in FLOOR" );
	}

	protected static void err_FACT()
	{
		fun_ERROR( "#NUM! because n < 0 in FACT" );
	}


	public static String fun_MID( String _s, int _start, int _len )
	{
		final int start = _start - 1;
		if (start < 0) fun_ERROR( "#VALUE! because start < 0 in MID" );
		if (start >= _s.length()) return "";
		if (_len < 0) fun_ERROR( "#VALUE! because len < 0 in MID" );
		final int pastEnd = (start + _len >= _s.length())? _s.length() : start + _len;
		return _s.substring( start, pastEnd );
	}

	public static String fun_LEFT( String _s, int _len )
	{
		if (_len < 0) fun_ERROR( "#VALUE! because len < 0 in LEFT" );
		if (_len == 0) return "";
		if (_len >= _s.length()) return _s;
		return _s.substring( 0, _len );
	}

	public static String fun_RIGHT( String _s, int _len )
	{
		if (_len < 0) fun_ERROR( "#VALUE! because len < 0 in RIGHT" );
		if (_len == 0) return "";
		if (_len >= _s.length()) return _s;
		final int max = _s.length();

		final int len = (_len > max)? max : _len;
		return _s.substring( max - len );
	}

	public static String fun_SUBSTITUTE( String _s, String _src, String _tgt )
	{
		if (_s == null || _s.equals( "" )) return _s;
		if (_src == null || _src.equals( "" ) || _src.equals( _tgt )) return _s;
		return _s.replace( notNull( _src ), notNull( _tgt ) );
	}

	public static String fun_SUBSTITUTE( String _s, String _src, String _tgt, int _occurrence )
	{
		if (_occurrence <= 0) fun_ERROR( "#VALUE! because occurrence <= 0 in SUBSTITUTE" );
		if (_s == null || _s.equals( "" )) return _s;
		if (_src == null || _src.equals( "" ) || _src.equals( _tgt )) return _s;
		int at = 0;
		int seen = 0;
		while (at < _s.length()) {
			final int p = _s.indexOf( _src, at );
			if (p < 0) break;
			if (++seen == _occurrence) {
				return _s.substring( 0, p ) + _tgt + _s.substring( p + _src.length() );
			}
			at = p + _src.length();
		}
		return _s;
	}

	public static String fun_REPLACE( String _s, int _at, int _len, String _repl )
	{
		if (_at < 1) fun_ERROR( "#VALUE! because at <= 0 in REPLACE" );
		if (_len < 0) fun_ERROR( "#VALUE! because len < 0 in REPLACE" );
		if (_s == null || _s.equals( "" )) return _repl;
		final int at = _at - 1;
		if (at >= _s.length()) return _s + _repl;
		if (at + _len >= _s.length()) return _s.substring( 0, at ) + _repl;
		return _s.substring( 0, at ) + _repl + _s.substring( at + _len );
	}

	public static boolean fun_EXACT( String _a, String _b )
	{
		return _a.equals( _b );
	}

	public static int fun_FIND( String _what, String _within, int _startingAt )
	{
		if (_what == null || _what.equals( "" )) return 1;
		if (_within == null || _within.equals( "" )) fun_ERROR( "#VALUE! because no result in FIND" );
		if (_startingAt > _within.length()) fun_ERROR( "#VALUE! because start is past end in SEARCH" );
		final int ix = _within.indexOf( _what, _startingAt - 1 );
		if (ix < 0) fun_ERROR( "#VALUE! because no result in FIND" );
		return ix + 1;
	}

	public static int fun_SEARCH( String _what, String _within, int _startingAt )
	{
		if (_within == null || _within.equals( "" )) fun_ERROR( "#VALUE! because no result in SEARCH" );
		if (_what == null || _what.equals( "" )) return 1;
		if (_startingAt > _within.length()) fun_ERROR( "#VALUE! because start is past end in SEARCH" );

		final Pattern pattern = patternFor( _what.toLowerCase() );
		final Matcher matcher = pattern.matcher( _within.toLowerCase() );
		if (matcher.find( _startingAt - 1 )) {
			return matcher.start() + 1;
		}
		else {
			throw new FormulaException( "#VALUE! because no result in FIND" );
		}
	}

	private static final Pattern patternFor( String _stringWithWildcards )
	{
		final StringBuilder src = new StringBuilder(); // using "(?i)" has trouble with umlauts
		int i = 0;
		while (i < _stringWithWildcards.length()) {
			char c = _stringWithWildcards.charAt( i++ );
			switch (c) {
				case '*':
					src.append( ".*" );
					break;
				case '?':
					src.append( "." );
					break;
				case '~':
					if (i < _stringWithWildcards.length()) {
						final char cc = _stringWithWildcards.charAt( i++ );
						appendLiteral( src, cc );
					}
					else {
						appendLiteral( src, c );
					}
					break;
				default:
					appendLiteral( src, c );
			}
		}
		return Pattern.compile( src.toString() );
	}

	private static final void appendLiteral( final StringBuilder _src, char _char )
	{
		_src.append( "\\u" );
		_src.append( Integer.toHexString( 0x10000 | _char ).substring( 1 ) );
	}

	private static boolean isSymbolVisible( int codePoint )
	{
		return (codePoint >= 32 && codePoint != 127);
	}

	public static String fun_CLEAN( String _s )
	{
		final StringBuilder sb = new StringBuilder();
		int i = 0;
		while (i < _s.length()) {
			int codePoint = _s.codePointAt( i );
			if (Character.isSupplementaryCodePoint( codePoint )) {
				i += 2;
			}
			else {
				i++;
			}
			if (isSymbolVisible( codePoint )) {
				sb.append( Character.toChars( codePoint ) );
			}
		}
		return sb.toString();
	}

	public static String fun_FIXED( Number _number, int _decimals, boolean _no_commas, Environment _environment )
	{
		final double multiplier = _decimals != 0? Math.pow( 10, _decimals ) : 1;
		final int decimals = (_decimals < 0)? 0 : _decimals;
		double number = _number.doubleValue();
		number = Math.round( number * multiplier ) / multiplier;
		final NumberFormat numberFormat = getNumberFormat( _environment );
		if (numberFormat instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) numberFormat;
			final DecimalFormatSymbols formatSymbols = decimalFormat.getDecimalFormatSymbols();
			final char c = formatSymbols.getGroupingSeparator();
			if (Character.isSpaceChar( c )) {
				formatSymbols.setGroupingSeparator( '\u0020' );
				decimalFormat.setDecimalFormatSymbols( formatSymbols );
			}
			numberFormat.setMinimumFractionDigits( decimals );
			numberFormat.setGroupingUsed( !_no_commas );
			return numberFormat.format( number );
		}
		else {
			return "0";
		}
	}

	public static String fun_LOWER( String _s )
	{
		return _s.toLowerCase();
	}

	public static String fun_UPPER( String _s )
	{
		return _s.toUpperCase();
	}


	public static String fun_PROPER( String _s )
	{
		final StringBuilder sb = new StringBuilder();
		final String str = _s.toLowerCase();
		boolean wordMiddle = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt( i );
			if (Character.isLetter( c )) {
				if (wordMiddle) {
					sb.append( c );
				}
				else {
					sb.append( Character.toUpperCase( c ) );
					wordMiddle = true;
				}
			}
			else {
				sb.append( c );
				wordMiddle = false;
			}
		}
		return sb.toString();
	}

	public static String fun_REPT( String _text, int _num )
	{
		final StringBuilder sb = new StringBuilder();
		for (int i = 0; i < _num; i++) {
			sb.append( _text );
		}
		return sb.toString();
	}

	public static String fun_ROMAN( int _val, int _mode )
	{
		if (_mode < 0 || _mode > 4) {
			fun_ERROR( "#VALUE! because mode out of range in ROMAN" );
		}
		if (_val < 0 || _val >= 4000) {
			fun_ERROR( "#VALUE! because value out of range in ROMAN" );
		}
		final StringBuilder result = new StringBuilder();
		final int[] values = { 1000, 500, 100, 50, 10, 5, 1 };
		final String[] roman = { "M", "D", "C", "L", "X", "V", "I" };
		int maxIndex = values.length - 1;
		int val = _val;
		for (int i = 0; i <= maxIndex / 2; i++) {
			int index = i * 2;
			int digit = val / values[ index ];
			if ((digit % 5) == 4) {
				int index2 = (digit == 4)? index - 1 : index - 2;
				int step = 0;
				while ((step < _mode) & (index < maxIndex)) {
					step++;
					if (values[ index2 ] - values[ index + 1 ] <= val) index++;
					else step = _mode;
				}
				result.append( roman[ index ] );
				result.append( roman[ index2 ] );
				val += values[ index ];
				val -= values[ index2 ];
			}
			else {
				if (digit > 4) {
					result.append( roman[ index - 1 ] );
				}
				if (digit > 0) {
					for (int j = 0; j < digit % 5; j++) {
						result.append( roman[ index ] );
					}
				}
				val %= values[ index ];
			}
		}
		return result.toString();
	}

	/**
	 * Strips leading and trailing blanks, and collapses runs of multiple blanks to just one.
	 */
	public static String fun_TRIM( String _text )
	{
		final StringBuilder sb = new StringBuilder();
		boolean whiteSpaceMet = false;
		boolean nonWhiteSpaceMet = false;
		for (int i = 0; i < _text.length(); i++) {
			char c = _text.charAt( i );
			if (c == ' ') {
				if (nonWhiteSpaceMet) {
					whiteSpaceMet = true;
				}
			}
			else {
				nonWhiteSpaceMet = true;
				if (whiteSpaceMet) {
					sb.append( ' ' );
					whiteSpaceMet = false;
				}
				sb.append( c );
			}
		}
		return sb.toString();
	}

	public static String fun_TEXT( Number _num, String _format, Environment _environment )
	{
		if ("@".equals( _format )) {
			final BigDecimal num = _num instanceof BigDecimal? (BigDecimal) _num : BigDecimal.valueOf( _num.doubleValue() );
			return stringFromBigDecimal( num, _environment, 10, 11 );
		}
		throw new IllegalArgumentException( "TEXT() is not properly supported yet." );
	}


	public static int fun_MATCH_Exact( String _x, String[] _xs )
	{
		if (_x.indexOf( '*' ) >= 0 || _x.indexOf( '?' ) >= 0) {
			final Pattern pattern = patternFor( _x.toLowerCase() );
			for (int i = 0; i < _xs.length; i++) {
				final Matcher matcher = pattern.matcher( _xs[ i ].toLowerCase() );
				if (matcher.find()) {
					return i + 1; // Excel is 1-based
				}
			}
		}
		else {
			for (int i = 0; i < _xs.length; i++) {
				if (0 == _x.compareToIgnoreCase( _xs[ i ] )) return i + 1; // Excel is 1-based
			}
		}
		throw new NotAvailableException();
	}

	public static int fun_MATCH_Ascending( String _x, String[] _xs, Environment _env )
	{
		final Collator c = _env.newCollator();
		return fun_MATCH_Sorted( _x, _xs, c, new Comparator<String>()
		{

			public int compare( String _o1, String _o2 )
			{
				return c.compare( _o1, _o2 );
			}

		} );
	}

	public static int fun_MATCH_Descending( String _x, String[] _xs, Environment _env )
	{
		final Collator c = _env.newCollator();
		return fun_MATCH_Sorted( _x, _xs, c, new Comparator<String>()
		{

			public int compare( String _o1, String _o2 )
			{
				return -c.compare( _o1, _o2 );
			}

		} );
	}

	private static int fun_MATCH_Sorted( String _x, String[] _xs, Collator _collator, Comparator<String> _comp )
	{
		_collator.setDecomposition( Collator.FULL_DECOMPOSITION );
		_collator.setStrength( Collator.SECONDARY );
		final int iLast = _xs.length - 1;
		int iLeft = 0;
		int iRight = iLast;
		while (iLeft < iRight) {
			final int iMid = iLeft + ((iRight - iLeft) >> 1);
			if (_comp.compare( _x, _xs[ iMid ] ) > 0) iLeft = iMid + 1;
			else iRight = iMid;
		}
		if (iLeft > iLast || _comp.compare( _x, _xs[ iLeft ] ) < 0) iLeft--;
		if (iLeft < 0) fun_NA();
		return iLeft + 1; // Excel is 1-based
	}


	static final long[] FACTORIALS = { 1, 1, 2, 6, 24, 120, 720, 5040, 40320, 362880, 3628800, 39916800, 479001600 };


	public static void fun_ERROR( String _message )
	{
		throw new FormulaException( _message );
	}

	public static void fun_NA()
	{
		throw new NotAvailableException();
	}


}
