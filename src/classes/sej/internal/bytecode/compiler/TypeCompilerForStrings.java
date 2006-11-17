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
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.DataType;
import sej.internal.runtime.Runtime_v1;

final class TypeCompilerForStrings extends TypeCompiler
{
	private static final Type RUNTIME_TYPE = Type.getType( Runtime_v1.class );
	static final String SNAME = ByteCodeEngineCompiler.STRING_CLASS.getInternalName();
	static final String S = ByteCodeEngineCompiler.STRING_CLASS.getDescriptor();

	public TypeCompilerForStrings(ByteCodeEngineCompiler _engineCompiler)
	{
		super( _engineCompiler );
	}

	@Override
	protected DataType dataType()
	{
		return DataType.STRING;
	}

	@Override
	protected Type runtimeType()
	{
		return RUNTIME_TYPE;
	}

	@Override
	protected Type type()
	{
		return ByteCodeEngineCompiler.STRING_CLASS;
	}

	@Override
	protected int returnOpcode()
	{
		return Opcodes.ARETURN;
	}


	@Override
	protected void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException
	{
		if (null == _value) {
			_mv.visitInsn( Opcodes.ACONST_NULL );
		}
		else if (_value instanceof String) {
			_mv.push( (String) _value );
		}
		else {
			throw new CompilerException.UnsupportedDataType( "String constant cannot be of type "
					+ _value.getClass().getName() + "." );
		}
	}


	@Override
	protected void compileZero( GeneratorAdapter _mv ) throws CompilerException
	{
		compileRuntimeMethod( _mv, "emptyString", "()Ljava/lang/String;" );
	}


}
