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
import org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

public class TypeCompilerForScaledBigDecimals extends TypeCompilerForBigDecimals
{
	private static final Type RUNTIME_TYPE = Type.getType( RuntimeScaledBigDecimal_v2.class );

	private final int fixedScale;
	private final int roundingMode;


	protected TypeCompilerForScaledBigDecimals( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		super( _engineCompiler, _numericType );
		this.fixedScale = _numericType.scale();
		this.roundingMode = _numericType.roundingMode();
	}

	@Override
	protected Type runtimeType()
	{
		return RUNTIME_TYPE;
	}

	@Override
	final boolean needsAdjustment()
	{
		return NumericType.UNDEFINED_SCALE != this.fixedScale;
	}

	@Override
	final boolean needsAdjustment( BigDecimal _value )
	{
		return _value.scale() != this.fixedScale;
	}

	@Override
	final void compileAdjustment( GeneratorAdapter _mv )
	{
		if (needsAdjustment()) {
			_mv.push( this.fixedScale );
			_mv.push( this.roundingMode );
			_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "setScale", "(II)" + B );
		}
	}

}
