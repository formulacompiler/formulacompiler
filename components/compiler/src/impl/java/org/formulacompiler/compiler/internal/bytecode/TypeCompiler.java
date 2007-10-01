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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.NumericTypeImpl;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


abstract class TypeCompiler
{
	private final ByteCodeEngineCompiler engineCompiler;
	private final NumericTypeImpl typeImpl;
	private String typeDescriptor;
	private String typeGetterDesc;

	public TypeCompiler( ByteCodeEngineCompiler _engineCompiler )
	{
		super();
		this.engineCompiler = _engineCompiler;
		this.typeImpl = (NumericTypeImpl) _engineCompiler.getNumericType();
	}

	public ByteCodeEngineCompiler engineCompiler()
	{
		return this.engineCompiler;
	}

	public SectionCompiler rootCompiler()
	{
		return engineCompiler().rootCompiler();
	}

	protected abstract DataType dataType();
	protected abstract Type runtimeType();
	protected abstract Type type();

	protected final String typeDescriptor()
	{
		if (this.typeDescriptor == null) this.typeDescriptor = type().getDescriptor();
		return this.typeDescriptor;
	}

	protected final String typeGetterDesc()
	{
		if (this.typeGetterDesc == null) this.typeGetterDesc = "()" + typeDescriptor();
		return this.typeGetterDesc;
	}


	protected abstract int returnOpcode();

	protected abstract void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException;

	protected abstract void compileZero( GeneratorAdapter _mv ) throws CompilerException;

	protected void compileMinValue( GeneratorAdapter _mv ) throws CompilerException
	{
		compileConst( _mv, this.typeImpl.getMinValue() );
	}

	protected void compileMaxValue( GeneratorAdapter _mv ) throws CompilerException
	{
		compileConst( _mv, this.typeImpl.getMaxValue() );
	}

	protected final void compileRuntimeMethod( GeneratorAdapter _mv, String _methodName, String _methodSig )
	{
		_mv.visitMethodInsn( Opcodes.INVOKESTATIC, runtimeType().getInternalName(), _methodName, _methodSig );
	}


}
