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

import java.math.BigDecimal;

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;

public final class DoubleType extends NumericTypeImpl
{

	protected DoubleType()
	{
		super( Double.TYPE, NumericTypeImpl.UNDEFINED_SCALE, BigDecimal.ROUND_UNNECESSARY );
	}

	@Override
	public Number getZero()
	{
		return Double.valueOf( 0.0 );
	}

	@Override
	public Number getOne()
	{
		return Double.valueOf( 1.0 );
	}

	@Override
	public Number getMinValue()
	{
		return MIN;
	}

	private static final Double MIN = Double.valueOf( -Double.MAX_VALUE );

	@Override
	public Number getMaxValue()
	{
		return MAX;
	}

	private static final Double MAX = Double.valueOf( Double.MAX_VALUE );

	@Override
	protected Double assertProperNumberType( Number _value )
	{
		return (Double) _value;
	}

	@Override
	protected Number convertFromAnyNumber( Number _value )
	{
		if (_value instanceof Double) return _value;
		return _value.doubleValue();
	}

	@Override
	protected String convertToConciseString( Number _value, Environment _env )
	{
		// We want to be sure this is a double here.
		return RuntimeDouble_v2.toExcelString( _value.doubleValue(), _env );
	}

}