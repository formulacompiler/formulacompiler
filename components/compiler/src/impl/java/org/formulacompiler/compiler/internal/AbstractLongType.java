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

public abstract class AbstractLongType extends NumericTypeImpl
{
	public static final Long ZERO = Long.valueOf( 0L );

	protected AbstractLongType( int _scale, int _roundingMode )
	{
		super( Long.TYPE, _scale, _roundingMode );
	}

	@Override
	public final Number getZero()
	{
		return Long.valueOf( zero() );
	}

	@Override
	public Number getOne()
	{
		return Long.valueOf( one() );
	}

	@Override
	public final Number getMinValue()
	{
		return MIN;
	}

	private static final Long MIN = Long.valueOf( Long.MIN_VALUE );

	@Override
	public final Number getMaxValue()
	{
		return MAX;
	}

	private static final Long MAX = Long.valueOf( Long.MAX_VALUE );

	@Override
	protected final Long assertProperNumberType( Number _value )
	{
		return (Long) _value;
	}

	public final long zero()
	{
		return 0L;
	}

	public abstract long one();

}