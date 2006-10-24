/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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

import sej.CompilerException;
import sej.NumericType;
import sej.runtime.ScaledLong;
import sej.runtime.ScaledLongSupport;

final class ExpressionCompilerForScaledLongs extends ExpressionCompilerForScaledLongs_Generated
{

	public ExpressionCompilerForScaledLongs(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
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
	protected void compileComparison( int _comparisonOpcode ) throws CompilerException
	{
		mv().visitInsn( Opcodes.LCMP );
	}


	@Override
	protected void compileDup()
	{
		mv().visitInsn( Opcodes.DUP2 );
	}
	
}
