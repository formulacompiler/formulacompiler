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

package org.formulacompiler.compiler.internal.bytecode;

import java.math.BigDecimal;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.ScaledLong;
import org.objectweb.asm.Opcodes;


abstract class ExpressionCompilerForScaledBigDecimals_Base extends ExpressionCompilerForNumbers
{
	private static final String BNAME = TypeCompilerForScaledBigDecimals.BNAME;
	private static final String B = TypeCompilerForScaledBigDecimals.B;
	private static final String B2I = "(" + B + ")I";

	private final int fixedScale;
	private final int roundingMode;


	public ExpressionCompilerForScaledBigDecimals_Base( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		super( _methodCompiler, _numericType );
		this.fixedScale = _numericType.scale();
		this.roundingMode = _numericType.roundingMode();
	}


	protected final void compile_fixedScale()
	{
		mv().push( this.fixedScale );
	}

	protected final void compile_roundingMode()
	{
		mv().push( this.roundingMode );
	}


	protected final boolean needsValueAdjustment()
	{
		return (this.fixedScale != NumericType.UNDEFINED_SCALE);
	}

	protected final void compileValueAdjustment() throws CompilerException
	{
		if (needsValueAdjustment()) {
			compile_util_adjustValue();
		}
	}

	@Override
	protected boolean isNativeType( Class _type )
	{
		return _type == BigDecimal.class;
	}


	@Override
	protected void compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		compile_util_fromScaledLong( _scale.value() );
	}

	@Override
	protected void compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		compile_util_toScaledLong( _scale.value() );
	}


	@Override
	protected int compileComparison( int _ifOpcode, int _comparisonOpcode ) throws CompilerException
	{
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "compareTo", B2I );
		return _ifOpcode;
	}


	@Override
	protected void compileNewArray()
	{
		mv().visitTypeInsn( Opcodes.ANEWARRAY, BNAME );
	}

	@Override
	protected int arrayStoreOpcode()
	{
		return Opcodes.AASTORE;
	}


	protected abstract void compile_util_adjustValue() throws CompilerException;
	protected abstract void compile_util_fromScaledLong( int _b ) throws CompilerException;
	protected abstract void compile_util_toScaledLong( int _b ) throws CompilerException;

}
