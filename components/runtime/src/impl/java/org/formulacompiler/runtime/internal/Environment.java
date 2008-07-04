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
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
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

	private Charset getAnsiCodePage( Locale loc )
	{
		Integer codePage = LANG_CODEPAGE_MAP.get( loc.getLanguage() );

		if (codePage == null) {
			codePage = DEFAULT_CODEPAGE;
		}

		return Charset.forName( "cp" + codePage );
	}

}
