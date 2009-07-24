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
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.runtime.ScaledLongSupport;
import org.objectweb.asm.Opcodes;


abstract class ExpressionCompilerForDoubles_Base extends ExpressionCompilerForNumbers
{

	public ExpressionCompilerForDoubles_Base( MethodCompiler _methodCompiler, NumericType _numericType )
	{
		super( _methodCompiler, _numericType );
	}

	@Override
	protected boolean isScaled()
	{
		return false;
	}

	@Override
	protected boolean isNativeType( Class _type )
	{
		return _type == Double.TYPE;
	}


	@Override
	protected boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		compile_util_fromScaledLong( ScaledLongSupport.ONE[ _scale.value() ] );
		return true;
	}

	@Override
	protected boolean compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		compile_util_toScaledLong( ScaledLongSupport.ONE[ _scale.value() ] );
		return true;
	}


	@Override
	protected int compileComparison( int _ifOpcode, int _comparisonOpcode ) throws CompilerException
	{
		mv().visitInsn( _comparisonOpcode );
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
	protected void compileExceptionalValueTest( boolean _testForErrors ) throws CompilerException
	{
		if (_testForErrors) {
			compile_util_testForErrors();
		}
		else {
			super.compileExceptionalValueTest( _testForErrors );
		}
	}


	@Override
	protected void compileNewArray()
	{
		mv().visitIntInsn( Opcodes.NEWARRAY, Opcodes.T_DOUBLE );
	}

	@Override
	protected int arrayStoreOpcode()
	{
		return Opcodes.DASTORE;
	}


	protected abstract void compile_util_fromScaledLong( long _scale ) throws CompilerException;
	protected abstract void compile_util_toScaledLong( long _scale ) throws CompilerException;
	protected abstract void compile_util_testForErrors() throws CompilerException;

}
