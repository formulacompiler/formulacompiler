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

package org.formulacompiler.runtime.internal;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

public class RuntimeScaledBigDecimal_v2 extends RuntimeBigDecimal_v2
{
	public static final MathContext HIGHPREC = MathContext.DECIMAL128;
	public static final MathContext UNLIMITED = MathContext.UNLIMITED;


	public static BigDecimal fun_DEGREES( BigDecimal _a )
	{
		final BigDecimal product = _a.multiply( BigDecimal.valueOf( 180 ) );
		return product.divide( PI, RoundingMode.HALF_UP );
	}

	public static BigDecimal fun_RADIANS( BigDecimal _a )
	{
		final BigDecimal product = _a.multiply( PI );
		return product.divide( BigDecimal.valueOf( 180 ), RoundingMode.HALF_UP );
	}


}
