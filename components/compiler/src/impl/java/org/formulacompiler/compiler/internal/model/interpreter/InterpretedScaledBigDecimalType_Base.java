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

package org.formulacompiler.compiler.internal.model.interpreter;

import java.math.BigDecimal;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;


abstract class InterpretedScaledBigDecimalType_Base extends InterpretedBigDecimalType
{
	private final int scale;
	private final int roundingMode;


	public InterpretedScaledBigDecimalType_Base( NumericType _type, ComputationMode _mode, Environment _env )
	{
		super( _type, _mode, _env );
		this.scale = _type.scale();
		this.roundingMode = _type.roundingMode();
	}


	public BigDecimal adjustScale( BigDecimal _value )
	{
		if (RuntimeBigDecimal_v2.EXTREMUM != _value && NumericType.UNDEFINED_SCALE != this.scale) {
			return _value.setScale( this.scale, this.roundingMode );
		}
		else {
			return _value;
		}
	}

	@Override
	protected BigDecimal adjustConvertedValue( BigDecimal _value )
	{
		return adjustScale( _value );
	}


	// Conversions for generated code:

	protected final boolean needsValueAdjustment()
	{
		return (NumericType.UNDEFINED_SCALE != this.scale);
	}

	protected final BigDecimal adjustReturnedValue( BigDecimal _b )
	{
		return adjustScale( _b );
	}

}