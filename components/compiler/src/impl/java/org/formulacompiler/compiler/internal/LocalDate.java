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

import java.util.Date;
import java.util.TimeZone;

import org.formulacompiler.runtime.internal.RuntimeDouble_v2;

public final class LocalDate extends Number
{
	private final double value;

	public LocalDate( double _value )
	{
		super();
		this.value = _value;
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

	@Override
	public String toString()
	{
		return Double.toString( value() );
	}

	@Override
	public boolean equals( final Object obj )
	{
		return (obj instanceof LocalDate)
				&& (Double.doubleToLongBits( ((LocalDate) obj).value ) == Double.doubleToLongBits( this.value ));
	}

	/**
	 * Similar to {@link Long#hashCode()}.
	 */
	@Override
	public int hashCode()
	{
		long bits = Double.doubleToLongBits( this.value );
		return (int) (bits ^ (bits >>> 32));
	}
}
