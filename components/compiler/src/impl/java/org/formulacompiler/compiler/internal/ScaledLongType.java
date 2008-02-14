/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.formulacompiler.runtime.internal.RuntimeLong_v2;

public final class ScaledLongType extends AbstractLongType
{
	private static long[] SCALING_FACTORS = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000,
			10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
			10000000000000000L, 100000000000000000L, 1000000000000000000L };

	private final long scalingFactor;

	protected ScaledLongType( int _scale )
	{
		super( _scale, BigDecimal.ROUND_DOWN );
		if (_scale < 1 || _scale >= SCALING_FACTORS.length) {
			throw new IllegalArgumentException( "Scale is out of range" );
		}
		this.scalingFactor = SCALING_FACTORS[ _scale ];
	}

	@Override
	public long one()
	{
		return this.scalingFactor;
	}

	@Override
	protected final Number convertFromString( String _value, Environment _env ) throws ParseException
	{
		return parse( _value, _env );
	}

	public long parse( String _value, Environment _env ) throws ParseException
	{
		final DecimalFormat format = _env.decimalFormat();
		final DecimalFormatSymbols syms = _env.decimalFormatSymbols();
		final char decSep = syms.getDecimalSeparator();
		final char minusSign = syms.getMinusSign();
		final char expSign = 'E'; // LATER syms.getExponentialSymbol(); once it's made public

		String value = _value;
		if (value.indexOf( Character.toLowerCase( expSign ) ) >= 0
				|| value.indexOf( Character.toUpperCase( expSign ) ) >= 0) {
			_env.decimalFormat().setParseBigDecimal( true );
			final Number number = super.convertFromString( _value, _env );
			return fromAnyNumber( number );
		}
		if (format.isGroupingUsed()) {
			value = value.replace( String.valueOf( syms.getGroupingSeparator() ), "" );
		}
		final int posOfDecPoint = value.indexOf( decSep );
		if (posOfDecPoint < 0) {
			return Long.parseLong( value ) * this.scalingFactor;
		}
		else {
			final int scaleOfResult = scale();
			final int scaleOfValue = value.length() - posOfDecPoint - 1;
			final int scaleOfDigits = (scaleOfValue > scaleOfResult) ? scaleOfResult : scaleOfValue;
			final String digits = value.substring( 0, posOfDecPoint )
					+ value.substring( posOfDecPoint + 1, posOfDecPoint + 1 + scaleOfDigits );
			final boolean roundUp;
			if (scaleOfValue > scaleOfDigits) {
				final char nextDigit = value.charAt( posOfDecPoint + 1 + scaleOfDigits );
				roundUp = nextDigit >= '5';
			}
			else {
				roundUp = false;
			}
			long unscaled = Long.parseLong( digits );
			if (roundUp) {
				final boolean negative = value.charAt( 0 ) == minusSign;
				unscaled += negative ? -1 : 1;
			}
			if (scaleOfDigits == scaleOfResult) {
				return unscaled;
			}
			else {
				assert scaleOfDigits < scaleOfResult;
				final long rescalingFactor = SCALING_FACTORS[ scaleOfResult - scaleOfDigits ];
				return unscaled * rescalingFactor;
			}
		}
	}

	@Override
	protected Number convertFromAnyNumber( Number _value )
	{
		return fromAnyNumber( _value );
	}

	private final long fromAnyNumber( Number _value )
	{
		if (_value instanceof Long) return _value.longValue() * one();
		return Math.round( RuntimeDouble_v2.round( _value.doubleValue(), scale() ) * one() );
	}

	@Override
	protected String convertToString( Number _value, Environment _env )
	{
		return RuntimeLong_v2.toExcelString( (Long) _value, scale(), _env );
	}

}