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

package org.formulacompiler.runtime;

/**
 * Support class for handling scaled longs.
 * <p>
 * See the <a target="_top" href="{@docRoot}/../tutorial/numeric_type.htm#long"
 * target="_top">tutorial</a> for details.
 * 
 * @author peo
 * 
 * @see ScaledLong
 */
public final class ScaledLongSupport
{

	/**
	 * The number 1 for the scaled {@code long} type at the different supported scales. Use it to
	 * scale unscaled values by multiplying them with the appropriate {@code ONE}.
	 * 
	 * @see #scale(long, int)
	 */
	public static final long[] ONE = new long[] { 1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000,
			1000000000, 10000000000L, 100000000000L, 1000000000000L, 10000000000000L, 100000000000000L, 1000000000000000L,
			10000000000000000L, 100000000000000000L, 1000000000000000000L };


	/**
	 * Returns a scaled version of an unscaled long value.
	 * 
	 * @param _unscaled is the unscaled value.
	 * @param _scale is the desired number of decimal places to scale by.
	 * @return the scaled number.
	 */
	public static long scale( long _unscaled, int _scale )
	{
		return _unscaled * ONE[ _scale ];
	}


	private ScaledLongSupport()
	{
		// never instantiate
	}
}
