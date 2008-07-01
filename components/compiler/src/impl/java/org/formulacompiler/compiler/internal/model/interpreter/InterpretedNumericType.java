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
import org.formulacompiler.compiler.internal.AbstractLongType;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.Environment;


public abstract class InterpretedNumericType extends InterpretedNumericType_GeneratedStrings
{


	public static InterpretedNumericType typeFor( NumericType _type, ComputationMode _mode, Environment _env )
	{
		if (Double.TYPE == _type.valueType()) {
			return new InterpretedDoubleType( _type, _mode, _env );
		}
		else if (BigDecimal.class == _type.valueType()) {
			if (null != _type.mathContext()) {
				return new InterpretedPrecisionBigDecimalType( _type, _mode, _env );
			}
			else {
				return new InterpretedScaledBigDecimalType( _type, _mode, _env );
			}
		}
		else if (Long.TYPE == _type.valueType()) {
			return new InterpretedScaledLongType( (AbstractLongType) _type, _mode, _env );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for run-time interpretation." );
		}
	}

	public static InterpretedNumericType typeFor( NumericType _type )
	{
		Util.assertTesting();
		return typeFor( _type, ComputationMode.EXCEL, Environment.DEFAULT );
	}


	InterpretedNumericType( NumericType _type, ComputationMode _mode, Environment _env )
	{
		super( _type, _mode, _env );
	}


}
