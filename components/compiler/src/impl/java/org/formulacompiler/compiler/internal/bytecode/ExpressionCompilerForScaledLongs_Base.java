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
	protected static final String RUNTIME_CONTEXT_DESCRIPTOR = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_DESCRIPTOR;
	protected static final String RUNTIME_CONTEXT_NAME = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_NAME;

	protected final int scale;
	protected final long one;
	protected final TypeCompilerForScaledLongs longCompiler = ((TypeCompilerForScaledLongs) typeCompiler());


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


	@Override
	protected final void compileScaleUp() throws CompilerException
	{
		if (scale() > 0) {
			compile_util_scaleUp( this.one );
		}
	}

	@Override
	protected final void compileScaleDown() throws CompilerException
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
	protected boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		compileScaleCorrection( _scale.value(), scale() );
		return true;
	}

	@Override
	protected boolean compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		compileScaleCorrection( scale(), _scale.value() );
		return true;
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

}
