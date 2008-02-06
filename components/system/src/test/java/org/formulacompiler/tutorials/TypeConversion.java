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
package org.formulacompiler.tutorials;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.MillisecondsSinceUTC1970;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.Milliseconds;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.Util;

import junit.framework.TestCase;

public class TypeConversion extends TestCase
{
	private static final long ONE_HOUR = 1000 * 60 * 60; // ms
	private static final long ONE_DAY = ONE_HOUR * 24; // ms
	private static final Date CREATION_TIME;
	static {
		final Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set( Calendar.DAY_OF_YEAR, 100 );
		CREATION_TIME = calendar.getTime();
	}


	public void testAllTypesWithDouble() throws Exception
	{
		testAllTypesWith( SpreadsheetCompiler.DOUBLE );
	}

	public void testAllTypesWithBigDecimal() throws Exception
	{
		testAllTypesWith( SpreadsheetCompiler.getNumericType( BigDecimal.class, 6, BigDecimal.ROUND_DOWN ) );
	}

	public void testAllTypesWithLong6() throws Exception
	{
		testAllTypesWith( SpreadsheetCompiler.LONG_SCALE6 );
	}


	private void testAllTypesWith( final NumericType _numericType ) throws Exception
	{
		String path = "src/test/data/org/formulacompiler/tutorials/TypeConversion.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( AllTypesFactory.class );
		builder.setNumericType( _numericType );
		builder.bindAllByName();
		Engine engine = builder.compile();

		AllTypesFactory factory = (AllTypesFactory) engine.getComputationFactory();

		AllPossibleInput input = new AllPossibleInputImpl();
		AllPossibleOutput output = factory.newInstance( input );

		// ---- checkAllTypes
		// Native types
		assertEquals( "1", input.getByte() * 2, output.calcByte() );
		assertEquals( "2", input.getShort() * 2, output.calcShort() );
		assertEquals( "3", input.getInt() * 2, output.calcInt() );
		assertEquals( "4", input.getDouble() * 2, output.calcDouble(), 0.0001 );
		assertEquals( "5", input.getFloat() * 2, output.calcFloat(), 0.0001F );
		assertEquals( "6", !input.getBoolean(), output.calcBoolean() );

		// Boxed native types
		assertEquals( "1a", input.getByte() * 2, output.calcBoxedByte().byteValue() );
		assertEquals( "2a", input.getShort() * 2, output.calcBoxedShort().shortValue() );
		assertEquals( "3a", input.getInt() * 2, output.calcBoxedInt().intValue() );
		assertEquals( "4a", input.getDouble() * 2, output.calcBoxedDouble().doubleValue(), 0.0001 );
		assertEquals( "5a", input.getFloat() * 2, output.calcBoxedFloat().floatValue(), 0.0001F );
		assertEquals( "6a", !input.getBoolean(), output.calcBoxedBoolean().booleanValue() );

		// Big types
		assertEquals( "1b", asString( input.getBigDecimal().add( BigDecimal.ONE ) ), asString( output.calcBigDecimal() ) );
		assertEquals( "2b", asString( input.getBigInteger().add( new BigInteger( "1" ) ) ), asString( output
				.calcBigInteger() ) );

		// Date
		assertEquals( "1c", input.getDate().getTime() + ONE_DAY, output.calcDate().getTime() );
		assertEquals( "2c", input.getDateMs() + ONE_DAY, output.calcDateMs() );
		assertEquals( "3c", input.getDateMs() + ONE_DAY, output.calcBoxedDateMs().longValue() );

		// Time
		assertEquals( "4c", input.getTime() * 2, output.calcTime() );

		// Scaled long
		assertTrue( "1d", Math.abs( input.getLong7() * 2 / 100 - output.calcLong5() ) <= 1 );
		assertTrue( "2d", Math.abs( input.getLong7() * 2 / 100 - output.calcBoxedLong5().longValue() ) <= 1 );

		// String
		assertEquals( "Hello, world!", output.calcString() );
		// ---- checkAllTypes
	}

	private String asString( BigDecimal _value )
	{
		return Util.trimTrailingZerosAndPoint( _value.toPlainString() );
	}

	private String asString( BigInteger _value )
	{
		return Util.trimTrailingZerosAndPoint( _value.toString() );
	}


	public static interface AllPossibleInput
	{

		// ---- Input
		// Native types
		byte getByte();
		short getShort();
		int getInt();
		long getLong();
		@ScaledLong( 7 ) long getLong7();
		double getDouble();
		float getFloat();
		boolean getBoolean();

		// Boxed native types
		Byte getBoxedByte();
		Short getBoxedShort();
		Integer getBoxedInt();
		Long getBoxedLong();
		@ScaledLong( 7 ) Long getBoxedLong7();
		Double getBoxedDouble();
		Float getBoxedFloat();
		Boolean getBoxedBoolean();

		// Big types
		BigDecimal getBigDecimal();
		BigInteger getBigInteger();

		// Date is converted to a number as in Excel; can also use long value as returned by
		// Date.getTime().
		// These values are time-zone adjusted.
		Date getDate();
		@MillisecondsSinceUTC1970 long getDateMs();
		@MillisecondsSinceUTC1970 Long getBoxedDateMs();

		// With @Milliseconds annotation, long is treated as a time duration in milliseconds for an
		// Excel time cell.
		// These values or *not* time-zone adjusted.
		@Milliseconds long getTime();
		@Milliseconds Long getBoxedTime();

		// String cannot be used for numbers, but for string-valued cells
		String getString();
		// ---- Input

		// Null values are treated as zero
		Byte getNullByte();
		Short getNullShort();
		Integer getNullInt();
		Long getNullLong();
		@ScaledLong( 7 ) Long getNullLong7();
		Double getNullDouble();
		Float getNullFloat();
		Boolean getNullBoolean();
		BigDecimal getNullBigDecimal();
		BigInteger getNullBigInteger();
		Date getNullDate();
		@MillisecondsSinceUTC1970 Long getNullDateMs();
		@Milliseconds Long getNullTime();

	}


	public static interface AllPossibleOutput
	{
		// ---- Output
		// Native types
		byte calcByte();
		short calcShort();
		int calcInt();
		long calcLong();
		@ScaledLong( 5 ) long calcLong5();
		double calcDouble();
		float calcFloat();
		boolean calcBoolean();

		// Boxed native types
		Byte calcBoxedByte();
		Short calcBoxedShort();
		Integer calcBoxedInt();
		Long calcBoxedLong();
		@ScaledLong( 5 ) Long calcBoxedLong5();
		Double calcBoxedDouble();
		Float calcBoxedFloat();
		Boolean calcBoxedBoolean();

		// Big types
		BigDecimal calcBigDecimal();
		BigInteger calcBigInteger();

		// Date is converted from a number as in Excel; can also use long value as in "new
		// Date(long)".
		// These values are time-zone adjusted.
		Date calcDate();
		@MillisecondsSinceUTC1970 long calcDateMs();
		@MillisecondsSinceUTC1970 Long calcBoxedDateMs();

		// With @Milliseconds annotation, long is converted from an Excel time cell to a time duration
		// in milliseconds.
		// These values are *not* time-zone adjusted.
		@Milliseconds long calcTime();
		@Milliseconds Long calcBoxedTime();

		// Strings are converted according to Java's settings
		String calcString();
		// ---- Output
	}


	public static class AllPossibleInputImpl implements AllPossibleInput
	{
		public byte getByte()
		{
			return 12;
		}
		public short getShort()
		{
			return 1234;
		}
		public int getInt()
		{
			return 12345;
		}
		public long getLong()
		{
			return 123456;
		}
		public double getDouble()
		{
			return 12345.67;
		}
		public float getFloat()
		{
			return 12345.678F;
		}
		public boolean getBoolean()
		{
			return false;
		}

		public Byte getBoxedByte()
		{
			return getByte();
		}
		public Short getBoxedShort()
		{
			return getShort();
		}
		public Integer getBoxedInt()
		{
			return getInt();
		}
		public Long getBoxedLong()
		{
			return getLong();
		}
		public Double getBoxedDouble()
		{
			return getDouble();
		}
		public Float getBoxedFloat()
		{
			return getFloat();
		}
		public Boolean getBoxedBoolean()
		{
			return getBoolean();
		}

		public BigDecimal getBigDecimal()
		{
			return BigDecimal.valueOf( 123456789, 3 );
		}
		public BigInteger getBigInteger()
		{
			return BigInteger.valueOf( 1234567890 );
		}

		public Date getDate()
		{
			return CREATION_TIME;
		}

		public long getDateMs()
		{
			return CREATION_TIME.getTime();
		}

		public Long getBoxedDateMs()
		{
			return getDateMs();
		}

		public long getTime()
		{
			return ONE_HOUR * 2;
		}

		public Long getBoxedTime()
		{
			return getTime();
		}

		public String getString()
		{
			return "Hello, world!";
		}

		public Byte getNullByte()
		{
			return null;
		}
		public Short getNullShort()
		{
			return null;
		}
		public Integer getNullInt()
		{
			return null;
		}
		public Long getNullLong()
		{
			return null;
		}
		public Double getNullDouble()
		{
			return null;
		}
		public Float getNullFloat()
		{
			return null;
		}
		public Boolean getNullBoolean()
		{
			return null;
		}
		public BigDecimal getNullBigDecimal()
		{
			return null;
		}
		public BigInteger getNullBigInteger()
		{
			return null;
		}
		public Date getNullDate()
		{
			return null;
		}

		public Long getNullDateMs()
		{
			return null;
		}

		public Long getNullTime()
		{
			return null;
		}

		public long getLong7()
		{
			return 12345678;
		}
		public Long getBoxedLong7()
		{
			return getLong7();
		}
		public Long getNullLong7()
		{
			return null;
		}
	}


	public static interface AllTypesFactory
	{
		AllPossibleOutput newInstance( AllPossibleInput _input );
	}

}
