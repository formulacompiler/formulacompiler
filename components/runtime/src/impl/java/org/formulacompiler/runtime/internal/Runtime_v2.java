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

package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.NotAvailableException;


/**
 * Each fun_XXX method corresponds to XXX spreadsheet function.
 * If there are fun_XXX and fun_XXX_OOo methods, then they both correspond to XXX spreadsheet function,
 * but fun_XXX is used for Excel computation mode, and fun_XXX_OOo is used for Open Office Calc one.
 */
public abstract class Runtime_v2
{

	public static final int BROKEN_REF = -1;

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	static final long SECS_PER_HOUR = 60 * 60;
	static final long SECS_PER_DAY = 24 * SECS_PER_HOUR;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;
	static final int UTC_OFFSET_DAYS = 25569;
	static final TimeZone GMT_TIME_ZONE = TimeZone.getTimeZone( "GMT" );
	protected static Random generator = new Random();

	private static final int NON_LEAP_DAY = 61;
	private static final BigDecimal MAX_EXP_VALUE = BigDecimal.valueOf( 1, 4 ); // 1E-4


	public static double checkDouble( final double _value )
	{
		if (Double.isNaN( _value )) throw new FormulaException( "#NUM! (value is NaN)" );
		if (Double.isInfinite( _value )) throw new FormulaException( "#NUM! (value is infinite)" );
		return _value;
	}

	public static byte unboxByte( Byte _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static short unboxShort( Short _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static int unboxInteger( Integer _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static long unboxLong( Long _boxed )
	{
		return (_boxed == null) ? 0L : _boxed;
	}

	public static float unboxFloat( Float _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static double unboxDouble( Double _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
	}

	public static boolean unboxBoolean( Boolean _boxed )
	{
		return (_boxed == null) ? false : _boxed;
	}

	public static char unboxCharacter( Character _boxed )
	{
		return (_boxed == null) ? 0 : _boxed;
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


	static Number parseNumber( String _text, boolean _parseBigDecimal, Environment _environment, boolean _dateExcelCompatible )
	{
		final String text = _text.toUpperCase( _environment.locale() );

		{
			final NumberFormat numberFormat = getNumberFormat( _environment );
			final Number result = parseNumber( text, numberFormat, _parseBigDecimal );
			if (result != null) {
				return result;
			}
		}

		{
			final NumberFormat percentFormat = getPercentFormat( _environment );
			final Number result = parseNumber( text, percentFormat, _parseBigDecimal );
			if (result != null) {
				return result;
			}
		}

		{
			final NumberFormat percentFormat = getNumberFormat( _environment );
			if (percentFormat instanceof DecimalFormat) {
				final DecimalFormat decimalFormat = (DecimalFormat) percentFormat;
				decimalFormat.setPositiveSuffix( "%" );
				decimalFormat.setNegativeSuffix( "%" );
				decimalFormat.setMultiplier( 100 );
				final Number result = parseNumber( text, decimalFormat, _parseBigDecimal );
				if (result != null) {
					return result;
				}
			}
		}

		{
			final DecimalFormatSymbols formatSymbols = getDecimalFormatSymbols( _environment );
			final DecimalFormat scientificFormat = new DecimalFormat( "#0.###E0", formatSymbols );
			final Number result = parseNumber( text, scientificFormat, _parseBigDecimal );
			if (result != null) {
				return result;
			}
		}

		try {
			return parseDateAndOrTime( text, _environment, _dateExcelCompatible );
		}
		catch (ParseException e) {
			return null;
		}
	}

	private static Number parseNumber( String _text, NumberFormat _numberFormat, boolean _parseBigDecimal )
	{
		setParseBigDecimal( _numberFormat, _parseBigDecimal );
		final Number num = parseNumber( _text, _numberFormat );
		if (num == null && fixDecimalSeparator( _numberFormat )) {
			return parseNumber( _text, _numberFormat );
		}
		else {
			return num;
		}
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

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	static Date dateFromDouble( double _dateAsNum, TimeZone _timeZone, boolean _excelCompatible )
	{
		return new Date( msSinceUTC1970FromDouble( _dateAsNum, _timeZone, _excelCompatible ) );
	}

	static long msSinceUTC1970FromDouble( double _dateAsNum, TimeZone _timeZone, boolean _excelCompatible )
	{
		final long msSinceLocal1970 = msSinceLocal1970FromDate( _dateAsNum, _excelCompatible );
		final int timeZoneOffset = _timeZone.getOffset( msSinceLocal1970 - _timeZone.getRawOffset() );
		final long msSinceUTC1970 = msSinceLocal1970 - timeZoneOffset;
		return msSinceUTC1970;
	}

	public static long msFromDouble( double _date )
	{
		final long ms = Math.round( _date * SECS_PER_DAY ) * MS_PER_SEC;
		return ms;
	}

	static double dateToDouble( Date _date, TimeZone _timeZone, boolean _excelCompatible )
	{
		if (_date == null) {
			return 0;
		}
		else {
			final long msSinceLocal1970 = dateToMsSinceLocal1970( _date, _timeZone );
			final double excel = msSinceLocal1970ToDateNum( msSinceLocal1970, _excelCompatible );
			return excel;
		}
	}

	static double msSinceUTC1970ToDouble( long _msSinceUTC1970, TimeZone _timeZone, boolean _excelCompatible )
	{
		final int timeZoneOffset = _timeZone.getOffset( _msSinceUTC1970 );
		final long msSinceLocal1970 = _msSinceUTC1970 + timeZoneOffset;
		final double excel = msSinceLocal1970ToDateNum( msSinceLocal1970, _excelCompatible );
		return excel;
	}

	public static double msToDouble( long _ms )
	{
		final double excel = (double) _ms / (double) MS_PER_DAY;
		return excel;
	}

	private static long msSinceLocal1970FromDate( double _date, final boolean _excelCompatible )
	{
		final boolean time = (Math.abs( _date ) < 1);
		double numValue = checkDouble( _date );

		// Work round a bug in excel. Excel seems to think there is a date
		// called the 29th Feb, 1900 - but in actual fact this was not a leap year.
		// Therefore for values less than 61 in the 1900 date system,
		// add one to the numeric value
		if (_excelCompatible && !time && numValue < NON_LEAP_DAY) {
			numValue += 1;
		}

		// Convert this to the number of days since 01 Jan 1970
		final int offsetDays = UTC_OFFSET_DAYS;
		final double utcDays = numValue - offsetDays;

		// Convert this into utc by multiplying by the number of milliseconds
		// in a day. Use the round function prior to ms conversion due
		// to a rounding feature of Excel (contributed by Jurgen
		final long msSinceLocal1970 = Math.round( utcDays * SECS_PER_DAY ) * MS_PER_SEC;
		return msSinceLocal1970;
	}

	private static double msSinceLocal1970ToDateNum( final long _msSinceLocal1970, final boolean _excelCompatible )
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
		if (_excelCompatible && value < NON_LEAP_DAY) {
			value -= 1;
		}

		return value;
	}


	public static String stringFromObject( Object _obj )
	{
		return (_obj == null) ? "" : _obj.toString();
	}

	public static String stringFromString( String _str )
	{
		return (_str == null) ? "" : _str;
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
		final int maximumFractionDigits = fractionDigits > 0 ? Math.min( fractionDigits, _intDigitsLimitFrac - 1 ) : 0;
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
		return envSymbols != null ? envSymbols : new DecimalFormatSymbols( _environment.locale() );
	}


	public static String emptyString()
	{
		return "";
	}

	private static String notNull( String _s )
	{
		return (_s == null) ? "" : _s;
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
		final int pastEnd = (start + _len >= _s.length()) ? _s.length() : start + _len;
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

		final int len = (_len > max) ? max : _len;
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

	public static int fun_CODE( String _s, Environment _environment )
	{
		if (_s != null && _s.length() >= 1) {
			ByteBuffer bb = _environment.charset().encode( _s );
			if (bb.capacity() > 0) {
				int res = bb.get();
				if (res < 0) {
					res = 256 + res;
				}
				return res;
			}
			else {
				// return code of "?" symbol
				return 63;
			}
		}
		else {
			throw new FormulaException( "#VALUE! because no data in CODE" );
		}
	}

	public static String fun_CHAR( int num, Environment _environment )
	{
		if (num > 0 && num < 256) {
			byte[] oneByte = { (byte) num };
			ByteBuffer bb = ByteBuffer.wrap( oneByte );
			CharBuffer cb = _environment.charset().decode( bb );
			if (cb.capacity() > 0) {
				return String.valueOf( cb.get() );
			}
			else {
				throw new FormulaException( "#VALUE! because wrong symbol code in CHAR" );
			}
		}
		throw new FormulaException( "#VALUE! because illegal argument (num <= 0 or num >= 256) in CHAR" );
	}

	public static String fun_FIXED( Number _number, int _decimals, boolean _no_commas, Environment _environment )
	{
		final double multiplier = _decimals != 0 ? Math.pow( 10, _decimals ) : 1;
		final int decimals = (_decimals < 0) ? 0 : _decimals;
		double number = _number.doubleValue();
		number = Math.round( number * multiplier ) / multiplier;
		final NumberFormat numberFormat = getNumberFormat( _environment );
		fixDecimalSeparator( numberFormat );
		numberFormat.setMinimumFractionDigits( decimals );
		numberFormat.setGroupingUsed( !_no_commas );
		return numberFormat.format( number );
	}

	public static String fun_DOLLAR( Number _number, Environment _environment )
	{
		final Currency curr = Currency.getInstance( _environment.locale() );
		return fun_DOLLAR( _number, curr.getDefaultFractionDigits(), _environment );
	}

	public static String fun_DOLLAR( Number _number, int _decimals, Environment _environment )
	{
		final Locale loc = _environment.locale();
		final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance( loc );
		final int decimals = (_decimals < 0) ? 0 : _decimals;
		currencyFormat.setMinimumFractionDigits( decimals );
		fixDecimalSeparator( currencyFormat );

		final double number = RuntimeDouble_v2.round( _number.doubleValue(), _decimals );
		final String res = currencyFormat.format( number );
		return res;
	}

	private static boolean fixDecimalSeparator( final NumberFormat _numberFormat )
	{
		if (_numberFormat instanceof DecimalFormat) {
			final DecimalFormat decimalFormat = (DecimalFormat) _numberFormat;
			final DecimalFormatSymbols formatSymbols = decimalFormat.getDecimalFormatSymbols();
			final char c = formatSymbols.getGroupingSeparator();
			if (Character.isSpaceChar( c )) {
				formatSymbols.setGroupingSeparator( '\u0020' );
				decimalFormat.setDecimalFormatSymbols( formatSymbols );
				return true;
			}
		}
		return false;
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
				int index2 = (digit == 4) ? index - 1 : index - 2;
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
			final BigDecimal num = _num instanceof BigDecimal ? (BigDecimal) _num : BigDecimal
					.valueOf( _num.doubleValue() );
			return stringFromBigDecimal( num, _environment, 10, 11 );
		}
		throw new IllegalArgumentException( "TEXT() is not properly supported yet." );
	}

	public static String fun_ADDRESS( int _row, int _column, int _absRelType, boolean _a1Style, String _sheet, ComputationMode _mode )
	{
		int absRelType = _absRelType;
		if (_mode == ComputationMode.OPEN_OFFICE_CALC && _absRelType > 4) {
			absRelType -= 4;
		}
		if (absRelType < 1 || absRelType > 4) {
			fun_ERROR( "#VALUE! type of reference has incorrect value in ADDRESS" );
		}
		final boolean columnIndexAbsolute = (absRelType == 1) || (absRelType == 3);
		final boolean rowIndexAbsolute = (absRelType == 1) || (absRelType == 2);

		if ((_row < 1 && (rowIndexAbsolute || _a1Style)) || (_column < 1 && (columnIndexAbsolute || _a1Style))) {
			fun_ERROR( "#VALUE! incorrect column or row number in ADDRESS" );
		}

		final StringBuilder address = new StringBuilder();

		if (_sheet != null && !_sheet.equals( "" )) {
			appendQuotedSheetName( address, _sheet );
			if (_mode == ComputationMode.OPEN_OFFICE_CALC && _a1Style) {
				address.append( '.' );
			}
			else {
				address.append( '!' );
			}
		}

		if (_a1Style) {
			appendNameA1ForCellIndex( address, _column - 1, columnIndexAbsolute, _row - 1, rowIndexAbsolute );
		}
		else {
			address.append( 'R' );
			appendIndexR1C1( address, _row, rowIndexAbsolute );
			address.append( 'C' );
			appendIndexR1C1( address, _column, columnIndexAbsolute );
		}

		return address.toString();
	}

	private static void appendIndexR1C1( final StringBuilder _sb, final int _index, final boolean _indexAbsolute )
	{
		if (_indexAbsolute) {
			_sb.append( _index );
		}
		else {
			_sb.append( '[' ).append( _index ).append( ']' );
		}
	}

	public static void appendQuotedSheetName( StringBuilder _sb, String _sheetName )
	{
		final boolean quoted = _sheetName.contains( " " ) || _sheetName.contains( "'" ) || _sheetName.contains( "-" );
		if (quoted) {
			_sb.append( '\'' );
		}
		_sb.append( _sheetName.replace( "'", "''" ) );
		if (quoted) {
			_sb.append( '\'' );
		}
	}

	/**
	 * Creates A1-style cell index.
	 *
	 * @param _sb                  string builder to append to
	 * @param _columnIndex         column index (0-based)
	 * @param _columnIndexAbsolute column index is absolute
	 * @param _rowIndex            row index (0-based)
	 * @param _rowIndexAbsolute    row index is absolute
	 */
	public static void appendNameA1ForCellIndex( final StringBuilder _sb,
			final int _columnIndex, final boolean _columnIndexAbsolute,
			final int _rowIndex, final boolean _rowIndexAbsolute )
	{
		if (_columnIndexAbsolute) {
			_sb.append( '$' );
		}
		if (_columnIndex == BROKEN_REF) {
			_sb.append( "#REF!" );
		}
		else {
			appendColumn( _sb, _columnIndex );
		}
		if (_rowIndexAbsolute) {
			_sb.append( '$' );
		}
		if (_rowIndex == BROKEN_REF) {
			_sb.append( "#REF!" );
		}
		else {
			_sb.append( _rowIndex + 1 );
		}
	}

	private static void appendColumn( StringBuilder _result, int _columnIndex )
	{
		final int insPos = _result.length();
		int col = _columnIndex;
		while (col >= 0) {
			final int digit = col % 26;
			_result.insert( insPos, (char) ('A' + digit) );
			col = col / 26 - 1;
		}
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

	public static int fun_MATCH_Ascending( final String _x, String[] _xs, Environment _env )
	{
		final Collator c = _env.newCollator();
		return fun_MATCH_Sorted( _xs, c, new Comparable<String>()
		{

			public int compareTo( String _o2 )
			{
				return c.compare( _x, _o2 );
			}

		} );
	}

	public static int fun_MATCH_Descending( final String _x, String[] _xs, Environment _env )
	{
		final Collator c = _env.newCollator();
		return fun_MATCH_Sorted( _xs, c, new Comparable<String>()
		{

			public int compareTo( String _o2 )
			{
				return -c.compare( _x, _o2 );
			}

		} );
	}

	private static int fun_MATCH_Sorted( String[] _xs, Collator _collator, Comparable<String> _comp )
	{
		_collator.setDecomposition( Collator.FULL_DECOMPOSITION );
		_collator.setStrength( Collator.SECONDARY );
		return fun_MATCH_Sorted( _xs, _comp );
	}

	public static <T> int fun_MATCH_Sorted( T[] _xs, Comparable<T> _comp )
	{
		if (_comp.compareTo( _xs[ 0 ] ) < 0) throw new NotAvailableException();
		final int n = _xs.length;
		int res = 1; // Excel is 1-based
		for (int i = 1; i <= n - 1; i++) {
			final T xi = _xs[ i ];
			if (null == xi) continue;
			if (_comp.compareTo( xi ) < 0) return res;
			res++;
		}
		return n;
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


	/**
	 * Parses a string containing a date and/or time component. The reason for this routine is that
	 * the standard Java date parsers are quite picky about input strings having <em>all</em>
	 * components specified in the format. Handles century completion for 2-digit-years the way Excel
	 * does. Handles ISO standard format as well as locale-specific format.
	 *
	 * @param _s   string to parse
	 * @param _env environment
	 * @return date in numeric representation
	 * @throws java.text.ParseException if string cannot be parsed as date.
	 */
	public static double parseDateAndOrTime( String _s, final Environment _env, boolean _excelCompatible ) throws ParseException
	{
		if (isISODate( _s, '-' )) {
			final Date date = parseISODateAndOptionallyTime( _s, '-', _env );
			return dateToDouble( date, _env.timeZone(), _excelCompatible );
		}
		if (isISODate( _s, '/' )) {
			final Date date = parseISODateAndOptionallyTime( _s, '/', _env );
			return dateToDouble( date, _env.timeZone(), _excelCompatible );
		}

		final TimeZone tz = _env.timeZone();
		final Locale loc = _env.locale();

		final SimpleDateFormat dateFormat = (SimpleDateFormat) DateFormat.getDateInstance( DateFormat.SHORT, loc );
		final char dateSeparator = firstNonPatternCharIn( dateFormat.toPattern() );
		final boolean hasDate = _s.indexOf( dateSeparator ) >= 0;

		final SimpleDateFormat timeFormat = (SimpleDateFormat) DateFormat.getTimeInstance( DateFormat.SHORT, loc );
		final char timeSeparator = firstNonPatternCharIn( timeFormat.toPattern() );
		final int timeElements = countOccurrences( _s, timeSeparator ) + 1;
		final boolean hasTime = timeElements > 1;
		final String[] amPmStrings = timeFormat.getDateFormatSymbols().getAmPmStrings();
		final boolean hasAmPm;
		if (amPmStrings.length >= 2) {
			final String u = _s.toUpperCase( loc );
			hasAmPm = u.contains( amPmStrings[ 0 ].toUpperCase( loc ) )
					|| u.contains( amPmStrings[ 1 ].toUpperCase( loc ) );
		}
		else hasAmPm = false;

		final SimpleDateFormat format;
		if (hasDate) {
			if (hasTime) {
				format = (SimpleDateFormat) DateFormat.getDateTimeInstance( DateFormat.SHORT, DateFormat.SHORT, loc );
			}
			else format = dateFormat;
		}
		else format = timeFormat;

		format.setLenient( true );

		final String defaultPattern = format.toPattern();
		String effectivePattern = defaultPattern;

		/*
		 * DateInstance(SHORT) returns something like M/d/yy. If it should contain yyyy, we simply
		 * convert this to M/d/yy so we get proper century adjustment. We don't, however, use
		 * DateInstance(MEDIUM/LONG) because for en_US, for instance, it returns MMM d, yy, which is
		 * unusable.
		 */
		if (hasDate) {
			Calendar cal = Calendar.getInstance( tz, loc );
			cal.clear();
			cal.set( 1930, 0, 1 );
			format.set2DigitYearStart( cal.getTime() );
			if (defaultPattern.contains( "yyyy" )) {
				effectivePattern = effectivePattern.replace( "yyyy", "yy" );
			}
		}

		if (hasTime) {

			/*
			 * TimeInstance(SHORT) returns something like h:mm a. We simply convert this to h:mm:ss a.
			 * Again, we don't use MEDIUM/LONG to avoid problems with superfluous elements.
			 */
			if (timeElements > 2 && defaultPattern.indexOf( 's' ) < 0) {
				final int i = effectivePattern.indexOf( 'm' );
				if (i >= 0) {
					int j = i + 1;
					while (j < effectivePattern.length() && effectivePattern.charAt( j ) == 'm')
						j++;
					effectivePattern = effectivePattern.substring( 0, j )
							+ timeSeparator + "ss" + effectivePattern.substring( j );
				}
			}

			/*
			 * TimeInstance(SHORT) may return am/pm, but if the input string does not specify either am
			 * or pm, we parse as 24-hour time.
			 */
			if (!hasAmPm) {
				final int i = effectivePattern.indexOf( 'a' );
				if (i >= 0) {
					effectivePattern = effectivePattern.substring( 0, i ) + effectivePattern.substring( i + 1 );
					effectivePattern = effectivePattern.replace( 'h', 'H' );
				}
			}

		}

		if (!effectivePattern.equals( defaultPattern )) {
			format.applyPattern( effectivePattern.trim() );
		}

		if (hasDate) {
			format.setTimeZone( tz );
			final Date parsed = format.parse( _s );
			// Java will parse 6/3/7 as 6/3/0007, but Excel as 6/3/2007
			Calendar cal = Calendar.getInstance( tz, loc );
			cal.setTime( parsed );
			final int year = cal.get( Calendar.YEAR );
			if (year < 10 && !_s.contains( "000" )) {
				cal.set( Calendar.YEAR, year + 2000 );
				final Date fixed = cal.getTime();
				return dateToDouble( fixed, tz, _excelCompatible );
			}
			return dateToDouble( parsed, tz, _excelCompatible );
		}
		else {
			format.setTimeZone( GMT_TIME_ZONE );
			final Date parsed = format.parse( _s );
			return msToDouble( parsed.getTime() );
		}
	}

	private static boolean isISODate( String _s, char _dateSep )
	{
		return (_s.length() >= 5
				&& Character.isDigit( _s.charAt( 0 ) ) && Character.isDigit( _s.charAt( 1 ) )
				&& Character.isDigit( _s.charAt( 2 ) ) && Character.isDigit( _s.charAt( 3 ) ) && _s.charAt( 4 ) == _dateSep);
	}

	private static Date parseISODateAndOptionallyTime( String _s, char _dateSep, Environment _env ) throws ParseException
	{
		final int timeElements = countOccurrences( _s, ':' ) + 1;
		final boolean hasTime = timeElements > 1;

		String pattern = "yyyy-MM-dd";
		if (_dateSep != '-') {
			pattern = pattern.replace( '-', _dateSep );
		}
		if (hasTime) {
			pattern = pattern + ((timeElements > 2) ? " HH:mm:ss" : " HH:mm");
		}

		final SimpleDateFormat format = new SimpleDateFormat( pattern, _env.locale() );
		format.setTimeZone( _env.timeZone() );
		format.setLenient( true );
		final Date parsed = format.parse( _s );

		return parsed;
	}

	private static char firstNonPatternCharIn( String _pattern )
	{
		for (int i = 0; i < _pattern.length(); i++) {
			final char c = Character.toUpperCase( _pattern.charAt( i ) );
			switch (c) {
				case ' ':
				case 'D':
				case 'M':
				case 'Y':
				case 'H':
				case 'S':
				case 'A':
				case 'P':
				case 'N':
					break;
				default:
					return c;
			}
		}
		return 0;
	}

	private static int countOccurrences( String _s, char _c )
	{
		int r = 0;
		for (int i = 0; i < _s.length(); i++)
			if (_c == _s.charAt( i )) r++;
		return r;
	}
}
