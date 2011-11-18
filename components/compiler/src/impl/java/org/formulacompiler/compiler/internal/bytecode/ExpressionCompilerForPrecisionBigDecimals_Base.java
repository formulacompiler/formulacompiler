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


abstract class ExpressionCompilerForPrecisionBigDecimals_Base extends ExpressionCompilerForNumbers
{
	protected static final String RUNTIME_CONTEXT_DESCRIPTOR = TypeCompilerForPrecisionBigDecimals.RUNTIME_CONTEXT_DESCRIPTOR;
	protected static final String RUNTIME_CONTEXT_NAME = TypeCompilerForPrecisionBigDecimals.RUNTIME_CONTEXT_NAME;

	private static final String BNAME = TypeCompilerForBigDecimals.BNAME;
	private static final String B = TypeCompilerForBigDecimals.B;
	private static final String B2I = "(" + B + ")I";

	protected final TypeCompilerForPrecisionBigDecimals bigCompiler = ((TypeCompilerForPrecisionBigDecimals) typeCompiler());


	public ExpressionCompilerForPrecisionBigDecimals_Base( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		super( _methodCompiler, _numericType );
	}


	protected final void compile_mathContext()
	{
		this.bigCompiler.buildStaticContext();
		mv().visitFieldInsn( Opcodes.GETSTATIC, typeCompiler().rootCompiler().classInternalName(), RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_DESCRIPTOR );
	}


	protected final void compileValueAdjustment()
	{
		// No adjustment as precision is only a minimum, not an absolute.
	}


	@Override
	protected boolean isNativeType( Class _type )
	{
		return _type == BigDecimal.class;
	}


	@Override
	protected boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		compile_util_fromScaledLong( _scale.value() );
		return true;
	}

	@Override
	protected boolean compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		compile_util_toScaledLong( _scale.value() );
		return true;
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


	protected abstract void compile_util_fromScaledLong( int _b ) throws CompilerException;
	protected abstract void compile_util_toScaledLong( int _b ) throws CompilerException;

}
