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
package org.formulacompiler.tests.reference.base;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;

abstract class AbstractCellValues
{
	static final long SECS_PER_DAY = 24 * 60 * 60;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;

	private final BindingType numberType;
	private final NumericType numericType;
	protected final Object[] vals;
	protected final BindingType[] types;

	public AbstractCellValues( Context _cx, Cell... _cells )
	{
		final int n = _cells.length;
		this.numberType = _cx.getNumberBindingType();
		this.numericType = _cx.getNumericType();
		this.vals = new Object[ n ];
		this.types = new BindingType[ n ];
		for (int i = 0; i < n; i++) {
			set( i, parseValue( _cx, _cells[ i ].getConstantValue() ) );
		}
	}

	private Object parseValue( Context _cx, Object _val )
	{
		if (_val instanceof Number) {
			Number num = (Number) _val;
			return this.numericType.valueOf( num );
		}
		if (_val instanceof String) {
			String str = (String) _val;
			if ("Infinity".equals( str )) return Double.POSITIVE_INFINITY;
			if ("-Infinity".equals( str )) return Double.NEGATIVE_INFINITY;
			if (str.equals( "(now)" )) return NOW;

			if (str.equals( "(full days from 2006)" )) {
				final Calendar calendar = new GregorianCalendar( getTimeZone( _cx ) );
				final int year = calendar.get( Calendar.YEAR );
				final int month = calendar.get( Calendar.MONTH );
				final int dayOfMonth = calendar.get( Calendar.DAY_OF_MONTH );
				calendar.setTimeZone( TimeZone.getTimeZone( "GMT" ) );
				calendar.clear();
				calendar.set( year, month, dayOfMonth );
				final long endMillis = calendar.getTimeInMillis();
				calendar.set( 2006, Calendar.JANUARY, 1 );
				final long startMillis = calendar.getTimeInMillis();
				final long days = (endMillis - startMillis) / MS_PER_DAY;
				return (int) days; // make sure it is not treated as an already scaled long
			}

		}
		return _val;
	}

	private TimeZone getTimeZone( Context _cx )
	{
		final Computation.Config config = _cx.getComputationConfig();
		TimeZone timeZone = config != null? config.timeZone : null;
		if (timeZone == null) {
			timeZone = TimeZone.getDefault();
		}
		return timeZone;
	}

	public static final Object NOW = new Object()
	{
		@Override
		public String toString()
		{
			return "(now)";
		}
	};


	public void set( int _index, Object _value, BindingType _type )
	{
		this.vals[ _index ] = _value;
		this.types[ _index ] = _type;
	}

	public void set( int _index, Object _value )
	{
		set( _index, _value, guessTypeOf( _value ) );
	}

	private BindingType guessTypeOf( Object _value )
	{
		if (null == _value) return this.numberType;
		if (_value instanceof Number) return this.numberType;
		if (_value instanceof String) return BindingType.STRING;
		if (_value instanceof Date) return BindingType.DATE;
		if (_value instanceof Boolean) return BindingType.BOOLEAN;
		if (_value instanceof Exception) return BindingType.EXCEPTION;
		if (_value == NOW) return BindingType.DATE;
		throw new IllegalArgumentException( "Cannot guess type of " + _value );
	}


	public Object get( int _index )
	{
		return this.vals[ _index ];
	}

	public BindingType type( int _index )
	{
		return this.types[ _index ];
	}

	public NumericType numericType()
	{
		return this.numericType;
	}

}
