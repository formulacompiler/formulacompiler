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

package org.formulacompiler.compiler.internal.bytecode;

import java.math.BigDecimal;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.objectweb.asm.Type;


abstract class TypeCompilerForNumbers extends TypeCompiler
{
	protected final static Type NUMBER_CLASS = Type.getType( Number.class );
	protected final static String N = NUMBER_CLASS.getDescriptor();


	public static TypeCompilerForNumbers compilerFor( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		if (Double.TYPE == _numericType.valueType()) {
			return new TypeCompilerForDoubles( _engineCompiler, _numericType );
		}
		else if (Long.TYPE == _numericType.valueType()) {
			return new TypeCompilerForScaledLongs( _engineCompiler, _numericType );
		}
		else if (BigDecimal.class == _numericType.valueType()) {
			if (null != _numericType.mathContext()) {
				return new TypeCompilerForPrecisionBigDecimals( _engineCompiler, _numericType );
			}
			else {
				return new TypeCompilerForScaledBigDecimals( _engineCompiler, _numericType );
			}
		}
		else {
			throw new IllegalArgumentException( "Unsupported data type " + _numericType + " for byte code compilation." );
		}
	}

	private final NumericType numericType;

	public TypeCompilerForNumbers( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		super( _engineCompiler );
		this.numericType = _numericType;
	}

	@Override
	protected DataType dataType()
	{
		return DataType.NUMERIC;
	}

	protected final NumericType numericType()
	{
		return this.numericType;
	}

}
