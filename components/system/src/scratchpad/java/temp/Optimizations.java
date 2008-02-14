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

package temp;

public class Optimizations
{

	private static final String A = "a";
	private static final String B = "b";
	@SuppressWarnings( "unused" )
	private static final String C = A + B;


	public double a( double x )
	{
		return x - 1e10 + 1e-10;
	}

	public double b( double x )
	{
		return x - 1e2 + 1e-2;
	}

	public double c( double x )
	{
		return 1e10 + 1e-10 + x;
	}

	public int ai( int x )
	{
		return x - 99999 + 1;
	}

	public int bi( int x )
	{
		return x + 99999 + 1;
	}


	public String as( String x )
	{
		return "a" + "b" + x;
	}

}
