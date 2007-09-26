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
package org.formulacompiler.tests.utils;

import java.math.BigDecimal;
import java.util.Collection;

import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.ScaledLong;



@ScaledLong(4)
public class Inputs implements InputInterface
{
	private double one;
	private double two;
	private double three;
	private BigDecimal bigOne;
	private BigDecimal bigTwo;
	private BigDecimal bigThree;
	private final Collection<Inputs> details = New.collection();
	private final Collection<Inputs> otherDetails = New.collection();
	private double doubleIncr = 1;

	public Inputs(final double _one, final double _two, final double _three)
	{
		super();
		this.one = _one;
		this.two = _two;
		this.three = _three;
	}

	public Inputs()
	{
		this( 1, 2, 3 );
	}

	public Inputs(double[] _values)
	{
		super();
		if (null == _values) return;
		if (_values.length > 0) this.one = _values[ 0 ];
		if (_values.length > 1) this.two = _values[ 1 ];
		if (_values.length > 2) this.three = _values[ 2 ];
	}

	public Inputs(BigDecimal[] _values)
	{
		super();
		if (null == _values) return;
		if (_values.length > 0) this.bigOne = _values[ 0 ];
		if (_values.length > 1) this.bigTwo = _values[ 1 ];
		if (_values.length > 2) this.bigThree = _values[ 2 ];
	}

	public double getOne()
	{
		return this.one;
	}

	public double getTwo()
	{
		return this.two;
	}

	public double getThree()
	{
		return this.three;
	}

	public BigDecimal getBigOne()
	{
		return this.bigOne;
	}

	public BigDecimal getBigTwo()
	{
		return this.bigTwo;
	}

	public BigDecimal getBigThree()
	{
		return this.bigThree;
	}

	public Collection<Inputs> getDetails()
	{
		return this.details;
	}

	public Collection<Inputs> getSubDetails()
	{
		return this.details;
	}

	public Collection<Inputs> getOtherDetails()
	{
		return this.otherDetails;
	}

	public double getPlusOne( double _value )
	{
		return _value + 1;
	}

	public Inner getInner( double _base )
	{
		return new Inner( _base );
	}

	public int getInt()
	{
		return 123;
	}

	public Double getDoubleObj()
	{
		return 123.45;
	}

	public Double getDoubleNull()
	{
		return null;
	}

	public System getUnsupported()
	{
		return null;
	}

	public double getDoubleA()
	{
		return 100.34;
	}

	public double getDoubleB()
	{
		return 3.0;
	}

	public double getDoubleC()
	{
		return 47.11;
	}

	public double getDoubleD()
	{
		return 47.11;
	}

	public double getDoubleE()
	{
		return 47.11;
	}

	public double getDoubleIncr()
	{
		return this.doubleIncr++;
	}

	public void setDoubleIncr( double _value )
	{
		this.doubleIncr = _value;
	}

	public BigDecimal getBigDecimalA()
	{
		return BigDecimal.valueOf( 10034, 2 );
	}

	public BigDecimal getBigDecimalB()
	{
		return BigDecimal.valueOf( 3 );
	}


	public long getScaledLongA()
	{
		return 1003400L;
	}

	public long getScaledLongB()
	{
		return 30000L;
	}


	public class Inner
	{
		private double base;

		public Inner(double _base)
		{
			super();
			this.base = _base;
		}

		public double getTimesTwo()
		{
			return this.base * 2;
		}

	}

	public static enum MyEnum {
		ONE, TWO;
	}

	public double hasEnumParam( MyEnum _param )
	{
		switch (_param) {
			case ONE:
				return 1;
			case TWO:
				return 2;
		}
		return 0;
	}

	public void setOne( int _i )
	{
		this.one = _i;
	}

}