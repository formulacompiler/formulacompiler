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
package sej.internal.bytecode.compiler;

import org.objectweb.asm.Opcodes;

import sej.compiler.CompilerException;
import sej.compiler.NumericType;
import sej.runtime.ScaledLong;
import sej.runtime.ScaledLongSupport;

abstract class ExpressionCompilerForDoubles_Base extends ExpressionCompilerForNumbers
{

	public ExpressionCompilerForDoubles_Base(MethodCompiler _methodCompiler, NumericType _numericType)
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

}
