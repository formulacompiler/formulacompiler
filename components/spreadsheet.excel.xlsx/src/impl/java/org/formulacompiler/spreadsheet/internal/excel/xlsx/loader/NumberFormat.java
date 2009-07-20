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

package org.formulacompiler.spreadsheet.internal.excel.xlsx.loader;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.formulacompiler.runtime.New;

public final class NumberFormat
{

	private static final Pattern NF_PATTERN;
	private static final Pattern DATE_PATTERN;
	private static final Pattern TIME_PATTERN;

	static {
		NF_PATTERN = Pattern.compile( "(?:\\[\\w+\\])?[^\\[\\w]*(?:\\[\\$-[0-9A-F]+\\])?((?:[^\\\";]|(?:\\\"[^\\\"]*\\\"))+);?" );
		DATE_PATTERN = Pattern.compile( "[dy]|(?:[\\w&&[^hm]]\\W*m)|(?:m\\W*[\\w&&[^sm]])" );
		TIME_PATTERN = Pattern.compile( "[hs]|(?:AM/PM)|(?:A/P)|(?:h\\W*m)(?:m\\W*s)" );
	}

	private static final Map<String, NumberFormat> PREDEFINED_NUMBER_FORMATS;

	static {
		// Predefined number formats initialization according to 
		// paragraph 3.8.30 of "Office Open XML Part 4 - Markup Language Reference.pdf"
		PREDEFINED_NUMBER_FORMATS = New.map();
		PREDEFINED_NUMBER_FORMATS.put( "0", new NumberFormat( "General" ) );
		PREDEFINED_NUMBER_FORMATS.put( "1", new NumberFormat( "0" ) );
		PREDEFINED_NUMBER_FORMATS.put( "2", new NumberFormat( "0.00" ) );
		PREDEFINED_NUMBER_FORMATS.put( "3", new NumberFormat( "#,##0" ) );
		PREDEFINED_NUMBER_FORMATS.put( "4", new NumberFormat( "#,##0.00" ) );
		PREDEFINED_NUMBER_FORMATS.put( "9", new NumberFormat( "0%" ) );
		PREDEFINED_NUMBER_FORMATS.put( "10", new NumberFormat( "0.00%" ) );
		PREDEFINED_NUMBER_FORMATS.put( "11", new NumberFormat( "0.00E+00" ) );
		PREDEFINED_NUMBER_FORMATS.put( "12", new NumberFormat( "# ?/?" ) );
		PREDEFINED_NUMBER_FORMATS.put( "13", new NumberFormat( "# ??/??" ) );
		PREDEFINED_NUMBER_FORMATS.put( "14", new NumberFormat( "mm-dd-yy" ) );
		PREDEFINED_NUMBER_FORMATS.put( "15", new NumberFormat( "d-mmm-yy" ) );
		PREDEFINED_NUMBER_FORMATS.put( "16", new NumberFormat( "d-mmm" ) );
		PREDEFINED_NUMBER_FORMATS.put( "17", new NumberFormat( "mmm-yy" ) );
		PREDEFINED_NUMBER_FORMATS.put( "18", new NumberFormat( "h:mm AM/PM" ) );
		PREDEFINED_NUMBER_FORMATS.put( "19", new NumberFormat( "h:mm:ss AM/PM" ) );
		PREDEFINED_NUMBER_FORMATS.put( "20", new NumberFormat( "h:mm" ) );
		PREDEFINED_NUMBER_FORMATS.put( "21", new NumberFormat( "h:mm:ss" ) );
		PREDEFINED_NUMBER_FORMATS.put( "22", new NumberFormat( "m/d/yy h:mm" ) );
		PREDEFINED_NUMBER_FORMATS.put( "37", new NumberFormat( "#,##0 ;(#,##0)" ) );
		PREDEFINED_NUMBER_FORMATS.put( "38", new NumberFormat( "#,##0 ;[Red](#,##0)" ) );
		PREDEFINED_NUMBER_FORMATS.put( "39", new NumberFormat( "#,##0.00;(#,##0.00)" ) );
		PREDEFINED_NUMBER_FORMATS.put( "40", new NumberFormat( "#,##0.00;[Red](#,##0.00)" ) );
		PREDEFINED_NUMBER_FORMATS.put( "45", new NumberFormat( "mm:ss" ) );
		PREDEFINED_NUMBER_FORMATS.put( "46", new NumberFormat( "[h]:mm:ss" ) );
		PREDEFINED_NUMBER_FORMATS.put( "47", new NumberFormat( "mmss.0" ) );
		PREDEFINED_NUMBER_FORMATS.put( "48", new NumberFormat( "##0.0E+0" ) );
		PREDEFINED_NUMBER_FORMATS.put( "49", new NumberFormat( "@" ) );
	}

	private final String format;
	private final boolean isDate;
	private final boolean isTime;

	public NumberFormat( String _format )
	{
		this.format = _format;

		final Matcher matcher = NF_PATTERN.matcher( _format );
		boolean isDate = false;
		boolean isTime = false;
		while (matcher.find()) {
			final String format = matcher.group( 1 ).replace( "\\ ", " " ).trim();
			if (!isDate) {
				final Matcher dtMatcher = DATE_PATTERN.matcher( format );
				isDate = dtMatcher.find();
			}
			if (!isTime) {
				final Matcher dtMatcher = TIME_PATTERN.matcher( format );
				isTime = dtMatcher.find();
			}
		}

		this.isDate = isDate;
		this.isTime = isTime;
	}

	public String getFormat()
	{
		return this.format;
	}

	public boolean isDate()
	{
		return this.isDate;
	}

	public boolean isTime()
	{
		return this.isTime;
	}

	public String toString()
	{
		return this.format;
	}

	public static NumberFormat getPredefinedNumberFormat( String _id )
	{
		return PREDEFINED_NUMBER_FORMATS.get( _id );
	}

	public static NumberFormat getPredefinedNumberFormat( int _id )
	{
		return getPredefinedNumberFormat( Integer.toString( _id ) );
	}
}
