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

import java.text.ParseException;
import java.util.Locale;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.internal.Environment;


public class ScaledLongTypeTest extends AbstractNumericTypeTest
{
	private static final NumericType type = FormulaCompiler.LONG_SCALE4;
	private static final AbstractLongType dec2 = new ScaledLongType( 2 );

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
		final Environment env = Environment.getInstance( new Computation.Config( _locale ) );

		assertParse( 12345, "123.45", env );
		assertParse( 12345, "123.452", env );
		assertParse( 12340, "123.4", env );
		assertParse( 12340, "123.400", env );
		assertParse( 12300, "123", env );
		assertParse( 12300, "123.0", env );
		assertParse( -12300, "-123", env );
		assertParse( -12300, "-123.0", env );
		assertParse( -12345, "-123.452", env );
		assertParse( -12340, "-123.4", env );

		assertParse( 12345, "123.454", env );
		assertParse( 12346, "123.455", env );
		assertParse( 12346, "123.456", env );

		assertParse( -12345, "-123.454", env );
		assertParse( -12346, "-123.455", env );
		assertParse( -12346, "-123.456", env );

		assertParse( -2, "-0.015", env );
		assertParse( -1, "-0.014", env );
		assertParse( -1, "-0.005", env );
		assertParse( 0, "-0.004", env );
		assertParse( 0, "0.004", env );
		assertParse( 1, "0.005", env );
		assertParse( 1, "0.014", env );
		assertParse( 2, "0.015", env );
	}

	private void assertParse( long _expected, String _input, Environment _env ) throws ParseException
	{
		String input = _input.replace( '.', _env.decimalFormatSymbols().getDecimalSeparator() );
		assertEquals( _expected, dec2.valueOf( input, _env ) );
	}


	public void testScaledLongToString_de()
	{
		doTestScaledLongToString( Locale.GERMANY );
	}

	public void doTestScaledLongToString( Locale _locale )
	{
		final Environment env = Environment.getInstance( new Computation.Config( _locale ) );

		assertFormat( "123.45", 12345, env );
		assertFormat( "123.4", 12340, env );
		assertFormat( "123", 12300, env );
		assertFormat( "-123.45", -12345, env );
		assertFormat( "-123.4", -12340, env );
		assertFormat( "-123", -12300, env );

		assertFormat( "0", 0, env );
		assertFormat( "0.01", 1, env );
		assertFormat( "0.1", 10, env );
		assertFormat( "1", 100, env );

		assertFormat( "0", -0, env );
		assertFormat( "-0.01", -1, env );
		assertFormat( "-0.1", -10, env );
		assertFormat( "-1", -100, env );
	}

	private void assertFormat( String _expected, long _input, Environment _env )
	{
		String expected = _expected.replace( '.', _env.decimalFormatSymbols().getDecimalSeparator() );
		assertEquals( expected, dec2.convertToString( _input, _env ) );
	}


	public void testDoubleToScaledLong()
	{
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
