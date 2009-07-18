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

import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.Environment;


abstract class InterpretedDoubleType_Base extends InterpretedNumericType
{

	public InterpretedDoubleType_Base( NumericType _type, ComputationMode _mode, Environment _env )
	{
		super( _type, _mode, _env );
	}


	@Override
	public Object adjustConstantValue( Object _value )
	{
		return _value;
	}


	@Override
	public Number toNumeric( Number _value )
	{
		return valueToDoubleOrZero( _value );
	}


	@Override
	protected int compareNumerically( Object _a, Object _b )
	{
		double a = valueToDoubleOrZero( _a );
		double b = valueToDoubleOrZero( _b );
		return Double.compare( a, b );
	}


	private final double valueToDouble( Object _value, double _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).doubleValue();
		if (_value instanceof String) return Double.valueOf( (String) _value );
		if (_value instanceof Boolean) return ((Boolean) _value) ? 1 : 0;
		if (_value instanceof Date) {
			throw new IllegalArgumentException( "Cannot interpret java.util.Date - it is runtime time-zone specific." );
		}
		return _ifNull;
	}

	private final double valueToDoubleOrZero( Object _value )
	{
		return valueToDouble( _value, 0.0 );
	}


	// Conversions for generated code:

	protected final double to_double( Object _value )
	{
		return valueToDoubleOrZero( _value );
	}

	protected final double[] to_array( Object _value )
	{
		final Object[] consts = asArrayOfConsts( _value );
		final double[] r = new double[ consts.length ];
		int i = 0;
		for (Object cst : consts) {
			r[ i++ ] = to_double( cst );
		}
		return r;
	}

}