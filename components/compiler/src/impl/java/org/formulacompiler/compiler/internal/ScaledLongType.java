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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.Locale;

import org.formulacompiler.runtime.internal.RuntimeDouble_v1;
import org.formulacompiler.runtime.internal.RuntimeLong_v1;

public final class ScaledLongType extends AbstractLongType
{
	private static long[] SCALING_FACTORS = { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
			1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L,
			1000000000000000L, 10000000000000000L, 100000000000000000L, 1000000000000000000L };

	private final long scalingFactor;

	protected ScaledLongType(int _scale)
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
	protected final Number convertFromString( String _value, Locale _locale )
	{
		return parse( _value, _locale );
	}

	public long parse( String _value, Locale _locale )
	{
		final DecimalFormatSymbols syms = ((DecimalFormat) NumberFormat.getNumberInstance( _locale ))
				.getDecimalFormatSymbols();
		final char decSep = syms.getDecimalSeparator();
		final char minusSign = syms.getMinusSign();

		String value = _value;
		if (value.indexOf( 'E' ) >= 0 || value.indexOf( 'e' ) >= 0) {
			value = new BigDecimal( value ).toPlainString();
		}
		final int posOfDecPoint = value.indexOf( decSep );
		if (posOfDecPoint < 0) {
			return Long.parseLong( value ) * this.scalingFactor;
		}
		else {
			final int scaleOfResult = scale();
			final int scaleOfValue = value.length() - posOfDecPoint - 1;
			final int scaleOfDigits = (scaleOfValue > scaleOfResult)? scaleOfResult : scaleOfValue;
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
				unscaled += negative? -1 : 1;
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
		if (_value instanceof Long) return _value.longValue() * one();
		return Math.round( RuntimeDouble_v1.round( _value.doubleValue(), scale() ) * one() );
	}

	@Override
	protected String convertToString( Number _value, Locale _locale )
	{
		return RuntimeLong_v1.toExcelString( (Long) _value, scale(), _locale );
	}

}