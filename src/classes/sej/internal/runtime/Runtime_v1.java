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
package sej.internal.runtime;

import java.math.BigDecimal;
import java.text.BreakIterator;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sej.internal.BigDecimalHelper;

public abstract class Runtime_v1
{

	// ---- Excel date conversion; copied from JExcelAPI (DateRecord.java)

	static final long SECS_PER_DAY = 24 * 60 * 60;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;
	static final int NON_LEAP_DAY = 61;
	static final int UTC_OFFSET_DAYS = 25569;
	static final int UTC_OFFSET_DAYS_1904 = 24107;
	static final boolean BASED_ON_1904 = false;


	public static boolean unboxBoolean( Boolean _boxed )
	{
		return (_boxed == null) ? false : _boxed;
	}

	public static long unboxLong( Long _boxed )
	{
		return (_boxed == null) ? 0L : _boxed;
	}


	public static Date today()
	{
		long now = Calendar.getInstance().getTime().getTime();
		long today = now / MS_PER_DAY * MS_PER_DAY;
		return new Date( today );
	}


	public static StringBuilder newStringBuilder( String _first )
	{
		return new StringBuilder( _first );
	}

	public static StringBuffer newStringBuffer( String _first )
	{
		return new StringBuffer( _first );
	}

	public static String stringFromObject( Object _obj )
	{
		return (_obj == null) ? "" : _obj.toString();
	}

	public static String stringFromString( String _str )
	{
		return (_str == null) ? "" : _str;
	}

	public static String stringFromBigDecimal( BigDecimal _value )
	{
		if (_value.compareTo( BigDecimal.ZERO ) == 0) return "0"; // avoid "0.0"
		final BigDecimal stripped = _value.stripTrailingZeros();
		final int scale = stripped.scale();
		final int prec = BigDecimalHelper.precision( stripped );
		final int ints = prec - scale;
		if (ints > 20) {
			return stripped.toString();
		}
		else {
			return stripped.toPlainString();
		}
	}

	public static String emptyString()
	{
		return "";
	}

	private static String notNull( String _s )
	{
		return (_s == null) ? "" : _s;
	}


	public static String stdMID( String _s, int _start, int _len )
	{
		final int start = _start - 1;
		if (start < 0) return "";
		if (start >= _s.length()) return "";
		if (_len < 0) return "";
		final int pastEnd = (start + _len >= _s.length()) ? _s.length() : start + _len;
		return _s.substring( start, pastEnd );
	}

	public static String stdLEFT( String _s, int _len )
	{
		if (_len < 1) return "";
		if (_len >= _s.length()) return _s;
		return _s.substring( 0, _len );
	}

	public static String stdRIGHT( String _s, int _len )
	{
		if (_len < 1) return "";
		if (_len >= _s.length()) return _s;
		final int max = _s.length();
		final int len = (_len > max) ? max : _len;
		return _s.substring( max - len );
	}

	public static String stdSUBSTITUTE( String _s, String _src, String _tgt )
	{
		if (_s == null || _s.equals( "" )) return _s;
		if (_src == null || _src.equals( "" ) || _src.equals( _tgt )) return _s;
		return _s.replace( notNull( _src ), notNull( _tgt ) );
	}

	public static String stdSUBSTITUTE( String _s, String _src, String _tgt, int _occurrence )
	{
		if (_occurrence <= 0) return _s;
		if (_s == null || _s.equals( "" )) return _s;
		if (_src == null || _src == "" || _src.equals( _tgt )) return _s;
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

	public static String stdREPLACE( String _s, int _at, int _len, String _repl )
	{
		if (_at < 1) return "";
		if (_len < 0) return "";
		if (_s == null || _s.equals( "" )) return _repl;
		final int at = _at - 1;
		if (at >= _s.length()) return _s + _repl;
		if (at + _len >= _s.length()) return _s.substring( 0, at ) + _repl;
		return _s.substring( 0, at ) + _repl + _s.substring( at + _len );
	}

	public static boolean stdEXACT( String _a, String _b )
	{
		return _a.equals( _b );
	}

	public static int stdFIND( String _what, String _within, int _startingAt )
	{
		if (_what == null || _what.equals( "" )) return 1;
		if (_within == null || _within.equals( "" )) return 0;
		return _within.indexOf( _what, _startingAt - 1 ) + 1;
	}

	public static int stdSEARCH( String _what, String _within, int _startingAt )
	{
		if (_within == null || _within.equals( "" )) return 0;
		if (_what == null || _what.equals( "" )) return 1;
		if (_startingAt > _within.length()) return 0;

		final Pattern pattern = patternFor( _what.toLowerCase() );
		final Matcher matcher = pattern.matcher( _within.toLowerCase() );
		if (matcher.find( _startingAt - 1 )) {
			return matcher.start() + 1;
		}
		else {
			return 0;
		}
	}

	private static final Pattern patternFor( String _stringWithWildcards )
	{
		final StringBuilder src = new StringBuilder(); // using "(?i)" has trouble with ä and Ä!
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
						char cc = _stringWithWildcards.charAt( i++ );
						switch (cc) {
							case '?':
							case '*':
								appendLiteral( src, cc );
								break;
							default:
								appendLiteral( src, c );
								appendLiteral( src, cc );
						}
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
		_src.append( "\\x" );
		_src.append( Integer.toHexString( _char ) );
	}


	public static String stdLOWER( String _s )
	{
		return _s.toLowerCase();
	}

	public static String stdUPPER( String _s )
	{
		return _s.toUpperCase();
	}


	static final String toTitleCase( String _str ) // LATER stdPROPER
	{
		// Unfinished! needs unit tests
		final StringBuffer sb = new StringBuffer();
		final String str = _str.toLowerCase();
		final BreakIterator i = BreakIterator.getWordInstance();
		i.setText( str );
		int wasAt = 0;
		int at = i.next();
		if (at >= 0) {
			sb.append( str.substring( wasAt, at ) );
			sb.append( str.substring( at, 1 ).toUpperCase() );
			wasAt = at + 1;
		}
		sb.append( str.substring( wasAt ));
		return sb.toString();
	}


}
