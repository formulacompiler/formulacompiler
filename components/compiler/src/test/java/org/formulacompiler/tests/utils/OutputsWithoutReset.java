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

package org.formulacompiler.tests.utils;

import java.math.BigDecimal;

import org.formulacompiler.runtime.ScaledLong;


/**
 * Used to be an interface, but since AFC checks that the output is fully implemented, I had to
 * revert to this class here.
 * 
 * @author peo
 */
@ScaledLong( 4 )
public class OutputsWithoutReset implements OutputInterface
{
	public double getResult()
	{
		throw new AbstractMethodError( "" );
	}
	public double getA()
	{
		throw new AbstractMethodError( "" );
	}
	public double getB()
	{
		throw new AbstractMethodError( "" );
	}
	public double getC()
	{
		throw new AbstractMethodError( "" );
	}
	public Iterable<OutputsWithoutReset> getDetails()
	{
		throw new AbstractMethodError( "" );
	}
	public Double getDoubleObj()
	{
		throw new AbstractMethodError( "" );
	}
	public System getUnsupported()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigDecimalA()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigDecimalB()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigDecimalC()
	{
		throw new AbstractMethodError( "" );
	}
	public BigDecimal getBigResult()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongA()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongB()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongC()
	{
		throw new AbstractMethodError( "" );
	}
	public long getScaledLongResult()
	{
		throw new AbstractMethodError( "" );
	}
}