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
package org.formulacompiler.compiler.internal;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;


public class ScaledLongTypeTest extends AbstractNumericTypeTest
{
	private static final NumericType type = FormulaCompiler.SCALEDLONG4;
	private static final NumericTypeImpl.AbstractLongType dec2 = new NumericTypeImpl.ScaledLongType( 2 );

	@Override
	protected NumericType getType()
	{
		return this.type;
	}


	public void testStringToScaledLong_de() throws Exception
	{
		doTestStringToScaledLong( Locale.GERMANY );
	}

	public void testStringToScaledLong_us() throws Exception
	{
		doTestStringToScaledLong( Locale.US );
	}


	public void doTestStringToScaledLong( Locale _locale ) throws ParseException
	{
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance( _locale );
		
		assertParse( 12345, "123.45", _locale, df );
		assertParse( 12345, "123.452", _locale, df );
		assertParse( 12340, "123.4", _locale, df );
		assertParse( 12340, "123.400", _locale, df );
		assertParse( 12300, "123", _locale, df );
		assertParse( 12300, "123.0", _locale, df );
		assertParse( -12300, "-123", _locale, df );
		assertParse( -12300, "-123.0", _locale, df );
		assertParse( -12345, "-123.452", _locale, df );
		assertParse( -12340, "-123.4", _locale, df );

		assertParse( 12345, "123.454", _locale, df );
		assertParse( 12346, "123.455", _locale, df );
		assertParse( 12346, "123.456", _locale, df );

		assertParse( -12345, "-123.454", _locale, df );
		assertParse( -12346, "-123.455", _locale, df );
		assertParse( -12346, "-123.456", _locale, df );

		assertParse( -2, "-0.015", _locale, df );
		assertParse( -1, "-0.014", _locale, df );
		assertParse( -1, "-0.005", _locale, df );
		assertParse( 0, "-0.004", _locale, df );
		assertParse( 0, "0.004", _locale, df );
		assertParse( 1, "0.005", _locale, df );
		assertParse( 1, "0.014", _locale, df );
		assertParse( 2, "0.015", _locale, df );
	}

	private void assertParse( long _expected, String _input, Locale _locale, DecimalFormat _df ) throws ParseException
	{
		String input = _input.replace( '.', _df.getDecimalFormatSymbols().getDecimalSeparator() );
		assertEquals( _expected, dec2.valueOf( input, _locale ) );
	}


	public void testScaledLongToString_de()
	{
		doTestScaledLongToString( Locale.GERMANY );
	}

	public void doTestScaledLongToString( Locale _locale )
	{
		DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance( _locale );

		assertFormat( "123.45", 12345, _locale, df );
		assertFormat( "123.4", 12340, _locale, df );
		assertFormat( "123", 12300, _locale, df );
		assertFormat( "-123.45", -12345, _locale, df );
		assertFormat( "-123.4", -12340, _locale, df );
		assertFormat( "-123", -12300, _locale, df );

		assertFormat( "0", 0, _locale, df );
		assertFormat( "0.01", 1, _locale, df );
		assertFormat( "0.1", 10, _locale, df );
		assertFormat( "1", 100, _locale, df );

		assertFormat( "0", -0, _locale, df );
		assertFormat( "-0.01", -1, _locale, df );
		assertFormat( "-0.1", -10, _locale, df );
		assertFormat( "-1", -100, _locale, df );
	}

	private void assertFormat( String _expected, long _input, Locale _locale, DecimalFormat _df )
	{
		String expected = _expected.replace( '.', _df.getDecimalFormatSymbols().getDecimalSeparator() );
		assertEquals( expected, dec2.convertToString( _input, _locale ) );
	}


	public void testDoubleToScaledLong()
	{
		final NumericTypeImpl.AbstractLongType dec2 = new NumericTypeImpl.ScaledLongType( 2 );

		assertEquals( -2, dec2.valueOf( -0.015 ).longValue() );
		assertEquals( -1, dec2.valueOf( -0.014 ).longValue() );
		assertEquals( -1, dec2.valueOf( -0.005 ).longValue() );
		assertEquals( 0, dec2.valueOf( -0.004 ).longValue() );
		assertEquals( 0, dec2.valueOf( 0.004 ).longValue() );
		assertEquals( 1, dec2.valueOf( 0.005 ).longValue() );
		assertEquals( 1, dec2.valueOf( 0.014 ).longValue() );
		assertEquals( 2, dec2.valueOf( 0.015 ).longValue() );
	}


}
