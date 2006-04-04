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
package sej.tests.utils;

import java.util.ArrayList;
import java.util.Collection;

public class Inputs implements InputInterface
{
	private double one;
	private double two;
	private double three;
	private final Collection<Inputs> details = new ArrayList<Inputs>();

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

	public Collection<Inputs> getDetails()
	{
		return this.details;
	}

	public Collection<Inputs> getSubDetails()
	{
		return this.details;
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

}