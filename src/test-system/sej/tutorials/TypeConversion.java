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
package sej.tutorials;

import java.io.FileOutputStream;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import sej.EngineBuilder;
import sej.NumericType;
import sej.SEJ;
import sej.SaveableEngine;
import sej.runtime.ScaledLong;
import sej.tests.utils.Util;
import junit.framework.TestCase;

public class TypeConversion extends TestCase
{

	private static final long ONE_DAY = 1000 * 60 * 60 * 24; // ms
	private static final Date CREATION_TIME = new Date( 100 * ONE_DAY );


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
		testAllTypesWith( SEJ.LONG4, true );
	}


	private void testAllTypesWith( final NumericType _numericType, boolean _truncatesAt4 ) throws Exception
	{
		String path = "src/test-system/testdata/sej/tutorials/UsingAllNumericTypes.xls";

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( AllTypesFactory.class );
		builder.setNumericType( _numericType );
		builder.bindAllByName();
		SaveableEngine engine = builder.compile();


		engine.saveTo( new FileOutputStream( "D:/Temp/typeseng.jar" ) );


		AllTypesFactory factory = (AllTypesFactory) engine.getComputationFactory();

		// ---- checkAllTypes
		AllPossibleInput input = new AllPossibleInputImpl();
		AllPossibleOutput output = factory.newInstance( input );
		assertEquals( input.getByte() * 2, output.calcByte() );
		assertEquals( input.getShort() * 2, output.calcShort() );
		assertEquals( input.getInt() * 2, output.calcInt() );
		assertEquals( input.getDouble() * 2, output.calcDouble(), 0.0001 );
		assertEquals( input.getFloat() * 2, output.calcFloat(), 0.0001F );

		assertEquals( Util.trimTrailingZerosAndPoint( input.getBigDecimal().add( BigDecimal.ONE ).toPlainString() ), Util
				.trimTrailingZerosAndPoint( output.calcBigDecimal().toPlainString() ) );
		assertEquals( Util.trimTrailingZerosAndPoint( input.getBigInteger().add( BigInteger.ONE ).toString() ), Util
				.trimTrailingZerosAndPoint( output.calcBigInteger().toString() ) );

		assertEquals( input.getDate().getTime() + ONE_DAY, output.calcDate().getTime() );
		assertEquals( !input.getBoolean(), output.calcBoolean() );
		
		if (_truncatesAt4) {
			assertEquals( input.getLong6() / 100 * 2 * 10, output.calcLong5() );
		}
		else {
			assertEquals( input.getLong6() * 2 / 10, output.calcLong5() );
		}
		// ---- checkAllTypes
	}


	// ---- TypeIO
	public static interface AllPossibleInput
	{
		public byte getByte();
		public short getShort();
		public int getInt();
		public long getLong();
		public double getDouble();
		public float getFloat();
		public boolean getBoolean();

		public Byte getBoxedByte();
		public Short getBoxedShort();
		public Integer getBoxedInt();
		public Long getBoxedLong();
		public Double getBoxedDouble();
		public Float getBoxedFloat();
		public Boolean getBoxedBoolean();

		public BigDecimal getBigDecimal();
		public BigInteger getBigInteger();

		public Date getDate();

		public Byte getNullByte();
		public Short getNullShort();
		public Integer getNullInt();
		public Long getNullLong();
		public Double getNullDouble();
		public Float getNullFloat();
		public Boolean getNullBoolean();
		public BigDecimal getNullBigDecimal();
		public BigInteger getNullBigInteger();
		public Date getNullDate();

		@ScaledLong(6)
		public long getLong6();
		
		@ScaledLong(6)
		public Long getBoxedLong6();
		
		@ScaledLong(6)
		public Long getNullLong6();
	}

	public static interface AllPossibleOutput
	{
		public byte calcByte();
		public short calcShort();
		public int calcInt();
		public long calcLong();
		public double calcDouble();
		public float calcFloat();
		public boolean calcBoolean();

		public Byte calcBoxedByte();
		public Short calcBoxedShort();
		public Integer calcBoxedInt();
		public Long calcBoxedLong();
		public Double calcBoxedDouble();
		public Float calcBoxedFloat();
		public Boolean calcBoxedBoolean();

		public BigDecimal calcBigDecimal();
		public BigInteger calcBigInteger();
		public Date calcDate();
		
		@ScaledLong(5)
		public long calcLong5();
	}

	// ---- TypeIO


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
