/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

import java.util.Formatter;
import java.util.Locale;

import org.formulacompiler.runtime.internal.Runtime_v2;

public final class Duration extends Number
{
	private final double value;

	public Duration( double _value )
	{
		this.value = _value;
	}

	public Duration( long _milliseconds )
	{
		this.value = Runtime_v2.msToDouble( _milliseconds );
	}

	public double value()
	{
		return this.value;
	}


	@Override
	public double doubleValue()
	{
		return value();
	}

	@Override
	public float floatValue()
	{
		return (float) value();
	}

	@Override
	public int intValue()
	{
		return (int) value();
	}

	@Override
	public long longValue()
	{
		return (long) value();
	}

	public long getMilliseconds()
	{
		return Runtime_v2.msFromDouble( this.value );
	}

	@Override
	public String toString()
	{
		final long milliseconds = getMilliseconds();
		final boolean negative = milliseconds < 0;
		final long absMillis = Math.abs( milliseconds );
		final long minutes = absMillis / 60000;
		final long hours = minutes / 60;
		final Formatter formatter = new Formatter( Locale.ENGLISH );
		formatter.format( "%s%d:%02d:%06.3f", negative ? "-" : "", hours, minutes % 60, (absMillis % 60000) / 1000.0 );
		return formatter.toString();
	}

	@Override
	public boolean equals( final Object obj )
	{
		return (obj instanceof Duration)
				&& (Double.doubleToLongBits( ((Duration) obj).value ) == Double.doubleToLongBits( this.value ));
	}

	/**
	 * Similar to {@link Double#hashCode()}.
	 */
	@Override
	public int hashCode()
	{
		long bits = Double.doubleToLongBits( this.value );
		return (int) (bits ^ (bits >>> 32));
	}
}
