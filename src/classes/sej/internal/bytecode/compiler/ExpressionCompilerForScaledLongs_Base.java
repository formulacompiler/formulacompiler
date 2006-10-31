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

import sej.NumericType;
import sej.internal.NumericTypeImpl;

abstract class ExpressionCompilerForScaledLongs_Base extends ExpressionCompilerForNumbers
{
	protected static final String RUNTIME_CONTEXT_DESCRIPTOR = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_DESCRIPTOR;
	protected static final String RUNTIME_CONTEXT_NAME = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_NAME;

	protected final int scale;
	protected final long one;
	protected final TypeCompilerForScaledLongs longCompiler = ((TypeCompilerForScaledLongs) typeCompiler());


	public ExpressionCompilerForScaledLongs_Base(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
		this.scale = _numericType.getScale();
		this.one = ((NumericTypeImpl.AbstractLongType) _numericType).one();
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

}
