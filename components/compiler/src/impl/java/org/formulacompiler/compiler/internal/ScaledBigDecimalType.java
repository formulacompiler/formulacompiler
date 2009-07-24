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

import java.math.BigDecimal;

import org.formulacompiler.compiler.NumericType;

public final class ScaledBigDecimalType extends AbstractBigDecimalType
{
	private final BigDecimal zero = adjustConvertedValue( BigDecimal.ZERO );
	private final BigDecimal one = adjustConvertedValue( BigDecimal.ONE );

	protected ScaledBigDecimalType( int _scale, int _roundingMode )
	{
		super( _scale, _roundingMode );
	}

	@Override
	public Number getZero()
	{
		return this.zero;
	}

	@Override
	public Number getOne()
	{
		return this.one;
	}

	@Override
	protected BigDecimal adjustConvertedValue( BigDecimal _value )
	{
		if (NumericType.UNDEFINED_SCALE != scale()) {
			return _value.setScale( scale(), roundingMode() );
		}
		return _value;
	}

}