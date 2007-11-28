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
import org.formulacompiler.runtime.FormulaException;
import org.formulacompiler.runtime.NotAvailableException;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithError;

abstract class AbstractCellValues
{
	static final long SECS_PER_DAY = 24 * 60 * 60;
	static final long MS_PER_SEC = 1000;
	static final long MS_PER_DAY = SECS_PER_DAY * MS_PER_SEC;

	private final BindingType numberType;
	private final NumericType numericType;
	protected final Object[] vals;
	protected final BindingType[] types;

	public AbstractCellValues( Context _cx, CellInstance... _cells )
	{
		final int n = _cells.length;
		this.numberType = _cx.getNumberBindingType();
		this.numericType = _cx.getNumericType();
		this.vals = new Object[ n ];
		this.types = new BindingType[ n ];
		for (int i = 0; i < n; i++) {
			final CellInstance cell = _cells[ i ];
			if (cell instanceof CellWithError) {
				CellWithError errCell = (CellWithError) cell;
				String errText = errCell.getError();
				if (errText.equals( "#N/A" )) {
					set( i, NA, this.numberType );
				}
				else {
					set( i, ERR, this.numberType );
				}
			}
			else {
				parseAndSetValue( _cx, i, (cell == null)? null : cell.getValue() );
			}
		}
	}

	private void parseAndSetValue( Context _cx, int _index, Object _val )
	{
		if (null == _val) {
			set( _index, _val, this.numberType );
		}
		else if (_val instanceof Number) {
			Number num = (Number) _val;
			set( _index, this.numericType.valueOf( num ), this.numberType );
		}
		else if (_val instanceof String) {
			String str = (String) _val;

			if (str.startsWith( "!NUM:" )) {
				set( _index, expectedErrorFromList( _cx, str.substring( 5 ) ), this.numberType );
			}
			else if (str.startsWith( "!STR:" )) {
				set( _index, expectedErrorFromList( _cx, str.substring( 5 ) ), BindingType.STRING );
			}
			else if (str.startsWith( "!DATE:" )) {
				set( _index, expectedErrorFromList( _cx, str.substring( 6 ) ), BindingType.DATE );
			}
			else if (str.startsWith( "!BOOL:" )) {
				set( _index, expectedErrorFromList( _cx, str.substring( 6 ) ), BindingType.BOOLEAN );
			}
			else if (str.equals( "(now)" )) {
				set( _index, NOW, BindingType.DATE );
			}
			else if (str.equals( "(sinh 710)" )) {
				set( _index, Math.sinh( 710.0 ), this.numberType );
			}
			else if (str.equals( "(full days from 2006)" )) {
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
				// make sure it is not treated as an already scaled long
				set( _index, (int) days, this.numberType );
			}
			else {
				set( _index, _val, BindingType.STRING );
			}
		}
		else if (_val instanceof Date) {
			set( _index, _val, BindingType.DATE );
		}
		else if (_val instanceof Boolean) {
			set( _index, _val, BindingType.BOOLEAN );
		}
		else {
			throw new IllegalArgumentException( "Cannot guess type of " + _val );
		}
	}

	private Object expectedErrorFromList( Context _cx, String _error )
	{
		final String[] byType = _error.split( "/" );
		final int n = byType.length;
		if (n > 1) {
			final int i = Math.min( _cx.getNumberBindingType().ordinal(), n - 1 );
			return expectedError( byType[ i ].trim() );
		}
		else {
			return expectedError( _error );
		}
	}

	private Object expectedError( String _error )
	{
		if (_error.startsWith( "+Inf" )) return Double.POSITIVE_INFINITY;
		if (_error.startsWith( "-Inf" )) return Double.NEGATIVE_INFINITY;
		if (_error.equals( "NaN" )) return Double.NaN;
		if (_error.equals( "AE" )) return ArithmeticException.class;
		if (_error.equals( "FE" )) return FormulaException.class;
		if (_error.equals( "NA" )) return NotAvailableException.class;
		throw new IllegalArgumentException( "Unknown error string: " + _error );
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

	public static final String NOW = "(now)";
	public static final Object NA = "#N/A";
	public static final Object ERR = "#ERR!";


	private void set( int _index, Object _value, BindingType _type )
	{
		this.vals[ _index ] = _value;
		this.types[ _index ] = _type;
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
