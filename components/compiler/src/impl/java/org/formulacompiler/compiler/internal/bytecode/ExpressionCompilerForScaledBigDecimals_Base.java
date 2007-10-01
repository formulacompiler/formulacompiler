/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are prohibited, unless you have been explicitly granted
 * more rights by Abacus Research AG.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
 * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
 * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS
 * OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
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

	protected final int fixedScale;
	protected final int roundingMode;


	public ExpressionCompilerForScaledBigDecimals_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
		this.fixedScale = _numericType.scale();
		this.roundingMode = _numericType.roundingMode();
	}


	@Override
	protected final boolean isScaled()
	{
		return false;
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
	

	protected abstract void compile_util_adjustValue() throws CompilerException;
	protected abstract void compile_util_fromScaledLong( int _b ) throws CompilerException;
	protected abstract void compile_util_toScaledLong( int _b ) throws CompilerException;

}
