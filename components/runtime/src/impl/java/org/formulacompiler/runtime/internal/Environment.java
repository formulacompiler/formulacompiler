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

package org.formulacompiler.runtime.internal;

import java.nio.charset.Charset;
import java.text.Collator;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.New;

public final class Environment
{
	public static final Environment DEFAULT = new Environment( new Computation.Config() );
	private static final Map<String, Integer> LANG_CODEPAGE_MAP = New.map();
	private static final int DEFAULT_CODEPAGE = 1252;

	private final Locale locale;
	private final DecimalFormatSymbols decimalFormatSymbols;
	private final TimeZone timeZone;
	private final Charset charset;

	static {
		// different codepages for languages: "az", "uz" and "sr" now realized only for Cyrillic, not for Latin
		// for languages which have the same codepage for any countries
		LANG_CODEPAGE_MAP.put( "cs", 1250 );
		LANG_CODEPAGE_MAP.put( "hr", 1250 );
		LANG_CODEPAGE_MAP.put( "hu", 1250 );
		LANG_CODEPAGE_MAP.put( "pl", 1250 );
		LANG_CODEPAGE_MAP.put( "ro", 1250 );
		LANG_CODEPAGE_MAP.put( "sk", 1250 );
		LANG_CODEPAGE_MAP.put( "sl", 1250 );
		LANG_CODEPAGE_MAP.put( "sq", 1250 );
		LANG_CODEPAGE_MAP.put( "be", 1251 );
		LANG_CODEPAGE_MAP.put( "bg", 1251 );
		LANG_CODEPAGE_MAP.put( "kk", 1251 );
		LANG_CODEPAGE_MAP.put( "ky", 1251 );
		LANG_CODEPAGE_MAP.put( "mk", 1251 );
		LANG_CODEPAGE_MAP.put( "mn", 1251 );
		LANG_CODEPAGE_MAP.put( "ru", 1251 );
		LANG_CODEPAGE_MAP.put( "az", 1251 ); // or 1254 for latin
		LANG_CODEPAGE_MAP.put( "uz", 1251 ); // or 1254 for latin
		LANG_CODEPAGE_MAP.put( "sr", 1251 ); // or 1250 for latin
		LANG_CODEPAGE_MAP.put( "tt", 1251 );
		LANG_CODEPAGE_MAP.put( "ua", 1251 );
		LANG_CODEPAGE_MAP.put( "af", 1252 );
		LANG_CODEPAGE_MAP.put( "br", 1252 );
		LANG_CODEPAGE_MAP.put( "ca", 1252 );
		LANG_CODEPAGE_MAP.put( "da", 1252 );
		LANG_CODEPAGE_MAP.put( "de", 1252 );
		LANG_CODEPAGE_MAP.put( "en", 1252 );
		LANG_CODEPAGE_MAP.put( "es", 1252 );
		LANG_CODEPAGE_MAP.put( "eu", 1252 );
		LANG_CODEPAGE_MAP.put( "fi", 1252 );
		LANG_CODEPAGE_MAP.put( "fo", 1252 );
		LANG_CODEPAGE_MAP.put( "fr", 1252 );
		LANG_CODEPAGE_MAP.put( "id", 1252 );
		LANG_CODEPAGE_MAP.put( "is", 1252 );
		LANG_CODEPAGE_MAP.put( "it", 1252 );
		LANG_CODEPAGE_MAP.put( "ms", 1252 );
		LANG_CODEPAGE_MAP.put( "nl", 1252 );
		LANG_CODEPAGE_MAP.put( "no", 1252 );
		LANG_CODEPAGE_MAP.put( "pt", 1252 );
		LANG_CODEPAGE_MAP.put( "sv", 1252 );
		LANG_CODEPAGE_MAP.put( "sw", 1252 );
		LANG_CODEPAGE_MAP.put( "el", 1253 );
		LANG_CODEPAGE_MAP.put( "tr", 1254 );
		LANG_CODEPAGE_MAP.put( "he", 1255 );
		LANG_CODEPAGE_MAP.put( "ar", 1256 );
		LANG_CODEPAGE_MAP.put( "fa", 1256 );
		LANG_CODEPAGE_MAP.put( "ur", 1256 );
		LANG_CODEPAGE_MAP.put( "et", 1257 );
		LANG_CODEPAGE_MAP.put( "lt", 1257 );
		LANG_CODEPAGE_MAP.put( "lv", 1257 );
		LANG_CODEPAGE_MAP.put( "vi", 1258 );
		LANG_CODEPAGE_MAP.put( "th", 874 );

		// double byte character set codepages for languages Japanese and Korean are not supported yet
		// LANG_CODEPAGE_MAP.put( "ja", 932 );  // Cp932 does not supported by jdk 1.5.0
		//LANG_CODEPAGE_MAP.put( "ko", 949 );

		// double byte character set codepages for language Chinese are not supported yet
		// for languages which have different codepages for different countries
		// LANG_CODEPAGE_MAP.put( "zh_CN", 936 );
		// LANG_CODEPAGE_MAP.put( "zh_SG", 936 );
		// LANG_CODEPAGE_MAP.put( "zh_MC", 950 );
		// LANG_CODEPAGE_MAP.put( "zh_HK", 950 );
		// LANG_CODEPAGE_MAP.put( "zh_TW", 950 );

	}

	public static Environment getInstance( Computation.Config _cfg )
	{
		if (_cfg == null) {
			return DEFAULT;
		}
		return new Environment( _cfg );
	}

	private Environment( Computation.Config _cfg )
	{
		super();
		this.locale = _cfg.locale;
		// Defensive copies of mutable structures:
		this.decimalFormatSymbols = (null == _cfg.decimalFormatSymbols) ? null
				: (DecimalFormatSymbols) _cfg.decimalFormatSymbols.clone();
		this.timeZone = (null == _cfg.timeZone) ? null : (TimeZone) _cfg.timeZone.clone();
		this.charset = _cfg.charset;
	}


	public Locale locale()
	{
		return (null != this.locale) ? this.locale : Locale.getDefault();
	}

	public Charset charset()
	{
		return (null != this.charset) ? this.charset : getAnsiCodePage( locale() );
	}

	public DecimalFormat decimalFormat()
	{
		return (DecimalFormat) NumberFormat.getNumberInstance( locale() );
	}

	public DecimalFormatSymbols decimalFormatSymbols()
	{
		return (null != this.decimalFormatSymbols) ? this.decimalFormatSymbols : decimalFormat()
				.getDecimalFormatSymbols();
	}

	public TimeZone timeZone()
	{
		return (null != this.timeZone) ? this.timeZone : TimeZone.getDefault();
	}

	public Collator newCollator()
	{
		return Collator.getInstance( locale() );
	}


	/**
	 * Parses a string containing a date and/or time component. The reason for this routine is that
	 * the standard Java date parsers are quite picky about input strings having <em>all</em>
	 * components specified in the format. Handles century completion for 2-digit-years the way Excel
	 * does. Handles ISO standard format as well as locale-specific format.
	 */
	public Date parseDateAndOrTime( String _s ) throws ParseException
	{
		if (isISODate( _s, '-' )) return parseISODateAndOptionallyTime( _s, '-' );
		if (isISODate( _s, '/' )) return parseISODateAndOptionallyTime( _s, '/' );

		final TimeZone tz = timeZone();
		final Locale loc = locale();

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
		format.setTimeZone( tz );

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
		final Date parsed = format.parse( _s );

		if (hasDate) {
			// Java will parse 6/3/7 as 6/3/0007, but Excel as 6/3/2007
			Calendar cal = Calendar.getInstance( tz, loc );
			cal.setTime( parsed );
			final int year = cal.get( Calendar.YEAR );
			if (year < 10 && !_s.contains( "000" )) {
				cal.set( Calendar.YEAR, year + 2000 );
				final Date fixed = cal.getTime();
				return fixed;
			}
			return parsed;
		}
		else {
			// Clear out the date part.
			Calendar cal = Calendar.getInstance( tz, loc );
			cal.setTime( parsed );
			cal.set( Calendar.YEAR, 1899 );
			cal.set( Calendar.MONTH, 11 );
			cal.set( Calendar.DAY_OF_MONTH, 31 );
			return cal.getTime();
		}

	}

	private boolean isISODate( String _s, char _dateSep )
	{
		return (_s.length() >= 5
				&& Character.isDigit( _s.charAt( 0 ) ) && Character.isDigit( _s.charAt( 1 ) )
				&& Character.isDigit( _s.charAt( 2 ) ) && Character.isDigit( _s.charAt( 3 ) ) && _s.charAt( 4 ) == _dateSep);
	}

	private Date parseISODateAndOptionallyTime( String _s, char _dateSep ) throws ParseException
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

		final SimpleDateFormat format = new SimpleDateFormat( pattern, locale() );
		format.setTimeZone( timeZone() );
		format.setLenient( true );
		final Date parsed = format.parse( _s );

		return parsed;
	}


	private char firstNonPatternCharIn( String _pattern )
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

	private int countOccurrences( String _s, char _c )
	{
		int r = 0;
		for (int i = 0; i < _s.length(); i++)
			if (_c == _s.charAt( i )) r++;
		return r;
	}

	private Charset getAnsiCodePage( Locale loc )
	{
		Integer codePage = LANG_CODEPAGE_MAP.get( loc.getLanguage() );

		if (codePage == null) {
			codePage = DEFAULT_CODEPAGE;
		}

		return Charset.forName( "cp" + codePage );
	}

}
