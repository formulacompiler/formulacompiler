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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.AbstractLongType;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.ScaledLongSupport;
import org.objectweb.asm.Opcodes;


abstract class ExpressionCompilerForScaledLongs_Base extends ExpressionCompilerForNumbers
{
	private static final String RUNTIME_CONTEXT_DESCRIPTOR = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_DESCRIPTOR;
	private static final String RUNTIME_CONTEXT_NAME = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_NAME;

	private final int scale;
	private final long one;
	private final TypeCompilerForScaledLongs longCompiler = ((TypeCompilerForScaledLongs) typeCompiler());


	public ExpressionCompilerForScaledLongs_Base( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		super( _methodCompiler, _numericType );
		this.scale = _numericType.scale();
		this.one = ((AbstractLongType) _numericType).one();
	}


	protected final int scale()
	{
		return this.scale;
	}


	@Override
	protected void compileConversionFromLong() throws CompilerException
	{
		compile_util_fromLong();
		compileScaleUp();
	}

	@Override
	protected void compileConversionFromInt() throws CompilerException
	{
		compile_util_fromInt();
		compileScaleUp();
	}

	@Override
	protected void compileConversionFromNumber() throws CompilerException
	{
		compile_util_fromNumber();
		compileScaleUp();
	}

	@Override
	protected void compileConversionFromBigInteger() throws CompilerException
	{
		compile_util_fromBigInteger();
		compileScaleUp();
	}

	@Override
	protected void compileConversionFromBigDecimal() throws CompilerException
	{
		if (isScaled()) {
			compile_util_fromBigDecimal_Scaled();
		}
		else {
			compile_util_fromBigDecimal();
		}
	}

	@Override
	protected void compileConversionFromFloat() throws CompilerException
	{
		if (isScaled()) {
			compile_util_fromFloat_Scaled();
		}
		else {
			compile_util_fromFloat();
		}
	}

	@Override
	protected void compileConversionFromDouble() throws CompilerException
	{
		if (isScaled()) {
			compile_util_fromDouble_Scaled();
		}
		else {
			compile_util_fromDouble();
		}
	}

	@Override
	protected void compileConversionToLong() throws CompilerException
	{
		compileScaleDown();
		compile_util_toLong();
	}

	@Override
	protected void compileConversionToInt() throws CompilerException
	{
		compileScaleDown();
		compile_util_toInt();
	}

	@Override
	protected void compileConversionToShort() throws CompilerException
	{
		compileScaleDown();
		compile_util_toShort();
	}

	@Override
	protected void compileConversionToByte() throws CompilerException
	{
		compileScaleDown();
		compile_util_toByte();
	}

	@Override
	protected void compileConversionToBigDecimal() throws CompilerException
	{
		if (isScaled()) {
			compile_util_toBigDecimal_Scaled();
		}
		else {
			compile_util_toBigDecimal();
		}
	}

	@Override
	protected void compileConversionToBigInteger() throws CompilerException
	{
		compileScaleDown();
		compile_util_toBigInteger();
	}

	@Override
	protected void compileConversionToFloat() throws CompilerException
	{
		if (isScaled()) {
			compile_util_toFloat_Scaled();
		}
		else {
			compile_util_toFloat();
		}
	}

	@Override
	protected void compileConversionToDouble() throws CompilerException
	{
		if (isScaled()) {
			compile_util_toDouble_Scaled();
		}
		else {
			compile_util_toDouble();
		}
	}

	@Override
	protected void compileConversionToBoolean() throws CompilerException
	{
		compileScaleDown();
		compile_util_toBoolean();
	}

	@Override
	protected void compileConversionToNumber() throws CompilerException
	{
		if (isScaled()) {
			compile_util_toNumber_Scaled();
		}
		else {
			compile_util_toNumber();
		}
	}

	protected final boolean isScaled()
	{
		return (scale() != 0);
	}


	protected final void compile_scale()
	{
		mv().push( scale() );
	}


	protected final void compile_context()
	{
		this.longCompiler.buildStaticContext();
		mv().visitFieldInsn( Opcodes.GETSTATIC, typeCompiler().rootCompiler().classInternalName(), RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_DESCRIPTOR );
	}


	protected final void compile_one()
	{
		mv().push( this.one );
	}


	@Override
	protected boolean isNativeType( Class _type )
	{
		return _type == Long.TYPE;
	}


	private void compileScaleUp() throws CompilerException
	{
		if (scale() > 0) {
			compile_util_scaleUp( this.one );
		}
	}

	private void compileScaleDown() throws CompilerException
	{
		if (scale() > 0) {
			compile_util_scaleDown( this.one );
		}
	}

	private final void compileScaleCorrection( int _have, int _want ) throws CompilerException
	{
		if (_have > _want) {
			long correct = ScaledLongSupport.ONE[ _have ] / ScaledLongSupport.ONE[ _want ];
			compile_util_scaleDown( correct );
		}
		else if (_have < _want) {
			long correct = ScaledLongSupport.ONE[ _want ] / ScaledLongSupport.ONE[ _have ];
			compile_util_scaleUp( correct );
		}
	}


	@Override
	protected void compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		compileScaleCorrection( _scale.value(), scale() );
	}

	@Override
	protected void compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		compileScaleCorrection( scale(), _scale.value() );
	}


	@Override
	protected int compileComparison( int _ifOpcode, int _comparisonOpcode ) throws CompilerException
	{
		mv().visitInsn( Opcodes.LCMP );
		return _ifOpcode;
	}


	@Override
	protected void compileDup()
	{
		mv().visitInsn( Opcodes.DUP2 );
	}

	@Override
	protected void compilePop()
	{
		mv().visitInsn( Opcodes.POP2 );
	}


	@Override
	protected void compileNewArray()
	{
		mv().visitIntInsn( Opcodes.NEWARRAY, Opcodes.T_LONG );
	}

	@Override
	protected int arrayStoreOpcode()
	{
		return Opcodes.LASTORE;
	}


	protected abstract void compile_util_scaleUp( long _one ) throws CompilerException;
	protected abstract void compile_util_scaleDown( long _one ) throws CompilerException;
	protected abstract void compile_util_toDouble_Scaled() throws CompilerException;
	protected abstract void compile_util_toFloat_Scaled() throws CompilerException;
	protected abstract void compile_util_toBigDecimal_Scaled() throws CompilerException;
	protected abstract void compile_util_toNumber_Scaled() throws CompilerException;
	protected abstract void compile_util_fromDouble_Scaled() throws CompilerException;
	protected abstract void compile_util_fromFloat_Scaled() throws CompilerException;
	protected abstract void compile_util_fromBigDecimal_Scaled() throws CompilerException;

}
