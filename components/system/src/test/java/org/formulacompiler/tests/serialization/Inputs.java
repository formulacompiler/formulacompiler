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

package org.formulacompiler.tests.serialization;

import java.math.BigDecimal;

import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.ScaledLongSupport;


// ---- Inputs
public final class Inputs
{
	final String a;
	final String b;

	public Inputs( String _a, String _b )
	{
		super();
		this.a = _a;
		this.b = _b;
	}

	public double getA_Double()
	{
		return Double.parseDouble( this.a );
	}

	public double getB_Double()
	{
		return Double.parseDouble( this.b );
	}

	@ScaledLong( 4 )
	public long getA_Long4()
	{
		return ScaledLongSupport.scale( (long) getA_Double(), 4 );
	}

	@ScaledLong( 4 )
	public long getB_Long4()
	{
		return ScaledLongSupport.scale( (long) getB_Double(), 4 );
	}

	public BigDecimal getA_BigDecimal()
	{
		return new BigDecimal( this.a );
	}

	public BigDecimal getB_BigDecimal()
	{
		return new BigDecimal( this.b );
	}

}
// ---- Inputs
