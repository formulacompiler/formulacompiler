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
package sej.tutorials;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;

import sej.compiler.NumericType;
import sej.runtime.Engine;
import sej.runtime.ScaledLong;
import sej.spreadsheet.EngineBuilder;
import sej.spreadsheet.SEJ;
import sej.tests.utils.Util;
import junit.framework.TestCase;

public class TypeConversion extends TestCase
{
	private static final long ONE_DAY = 1000 * 60 * 60 * 24; // ms
	private static final Date CREATION_TIME;

	static {
		final Calendar calendar = Calendar.getInstance();
		calendar.clear();
		calendar.set( Calendar.DAY_OF_YEAR, 100 );
		CREATION_TIME = calendar.getTime();
	}


	public void testAllTypesWithDouble() throws Exception
	{
		testAllTypesWith( SEJ.DOUBLE, false );
	}

	public void testAllTypesWithBigDecimal() throws Exception
	{
		testAllTypesWith( SEJ.getNumericType( BigDecimal.class, 4, BigDecimal.ROUND_DOWN ), true );
	}

	public void testAllTypesWithLong4() throws Exception
	{
		testAllTypesWith( SEJ.SCALEDLONG4, true );
	}


	private void testAllTypesWith( final NumericType _numericType, boolean _truncatesAt4 ) throws Exception
	{
		String path = "src/test-system/testdata/sej/tutorials/TypeConversion.xls";

		EngineBuilder builder = SEJ.newEngineBuilder();
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
		assertEquals( "1b", asString( input.getBigDecimal().add( BigDecimal.ONE ) ), 
				asString( output.calcBigDecimal() ) );
		assertEquals( "2b", asString( input.getBigInteger().add( new BigInteger( "1" ) ) ),
				asString( output.calcBigInteger() ) );

		// Date
		assertEquals( "1c", input.getDate().getTime() + ONE_DAY, output.calcDate().getTime() );

		// Scaled long
		if (_truncatesAt4) {
			assertEquals( "1d", input.getLong6() / 100 * 2 * 10, output.calcLong5() );
			assertEquals( "2d", input.getLong6() / 100 * 2 * 10, output.calcBoxedLong5().longValue() );
		}
		else {
			assertEquals( "1d", input.getLong6() * 2 / 10, output.calcLong5() );
			assertEquals( "2d", input.getLong6() * 2 / 10, output.calcBoxedLong5().longValue() );
		}
		
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
		@ScaledLong(6) long getLong6();
		double getDouble();
		float getFloat();
		boolean getBoolean();

		// Boxed native types
		Byte getBoxedByte();
		Short getBoxedShort();
		Integer getBoxedInt();
		Long getBoxedLong();
		@ScaledLong(6) Long getBoxedLong6();
		Double getBoxedDouble();
		Float getBoxedFloat();
		Boolean getBoxedBoolean();

		// Big types
		BigDecimal getBigDecimal();
		BigInteger getBigInteger();

		// Date is converted to a number a la Excel
		Date getDate();
		
		// String cannot be used for numbers, but for string-valued cells
		String getString();
		// ---- Input
		
		// Null values are treated as zero
		Byte getNullByte();
		Short getNullShort();
		Integer getNullInt();
		Long getNullLong();
		@ScaledLong(6) Long getNullLong6();
		Double getNullDouble();
		Float getNullFloat();
		Boolean getNullBoolean();
		BigDecimal getNullBigDecimal();
		BigInteger getNullBigInteger();
		Date getNullDate();
	}


	public static interface AllPossibleOutput
	{
		// ---- Output
		// Native types
		byte calcByte();
		short calcShort();
		int calcInt();
		long calcLong();
		@ScaledLong(5) long calcLong5();
		double calcDouble();
		float calcFloat();
		boolean calcBoolean();

		// Boxed native types
		Byte calcBoxedByte();
		Short calcBoxedShort();
		Integer calcBoxedInt();
		Long calcBoxedLong();
		@ScaledLong(5) Long calcBoxedLong5();
		Double calcBoxedDouble();
		Float calcBoxedFloat();
		Boolean calcBoxedBoolean();

		// Big types
		BigDecimal calcBigDecimal();
		BigInteger calcBigInteger();

		// Date is converted from a number � la Excel
		Date calcDate();
		
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

		public long getLong6()
		{
			return 12345678;
		}
		public Long getBoxedLong6()
		{
			return getLong6();
		}
		public Long getNullLong6()
		{
			return null;
		}
	}


	public static interface AllTypesFactory
	{
		AllPossibleOutput newInstance( AllPossibleInput _input );
	}

}
