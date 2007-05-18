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

import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.compiler.CompilerException;
import sej.compiler.NumericType;
import sej.internal.NumericTypeImpl;
import sej.internal.runtime.RuntimeDouble_v1;
import sej.internal.runtime.RuntimeLong_v1;

final class TypeCompilerForScaledLongs extends TypeCompilerForNumbers
{
	private static final Type RUNTIME_TYPE = Type.getType( RuntimeLong_v1.class );
	private static final Type RUNTIME_CONTEXT_TYPE = Type.getType( RuntimeLong_v1.Context.class );

	static final String RUNTIME_CONTEXT_DESCRIPTOR = RUNTIME_CONTEXT_TYPE.getDescriptor();
	static final String RUNTIME_CONTEXT_NAME = "runtimeContext";

	private final long one;


	protected TypeCompilerForScaledLongs(ByteCodeEngineCompiler _engineCompiler, NumericType _numericType)
	{
		super( _engineCompiler, _numericType );
		this.one = ((NumericTypeImpl.AbstractLongType) _numericType).one();
	}


	@Override
	protected Type runtimeType()
	{
		return RUNTIME_TYPE;
	}

	@Override
	protected Type type()
	{
		return Type.LONG_TYPE;
	}

	@Override
	protected int returnOpcode()
	{
		return Opcodes.LRETURN;
	}

	
	private boolean staticContextBuilt = false;

	final void buildStaticContext()
	{
		if (this.staticContextBuilt) return;
		this.staticContextBuilt = true;
		
		final SectionCompiler root = engineCompiler().rootCompiler();
		final ClassWriter cw = root.cw();
		final FieldVisitor fv = cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_FINAL,
				RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_TYPE.getDescriptor(), null, null );
		fv.visitEnd();

		final GeneratorAdapter mv = root.initializer();
		mv.visitTypeInsn( Opcodes.NEW, RUNTIME_CONTEXT_TYPE.getInternalName() );
		mv.visitInsn( Opcodes.DUP );
		mv.push( numericType().getScale() );
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, RUNTIME_CONTEXT_TYPE.getInternalName(), "<init>", "(I)V" );
		mv.visitFieldInsn( Opcodes.PUTSTATIC, root.classInternalName(), RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_DESCRIPTOR );	}


	@Override
	protected void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException
	{
		if (null == _value) {
			compileZero( _mv );
		}
		else if (_value instanceof Long) {
			long val = ((Long) _value).longValue();
			_mv.push( val );
		}
		else if (_value instanceof Number) {
			long val = (Long) numericType().valueOf( (Number) _value );
			_mv.push( val );
		}
		else if (_value instanceof Boolean) {
			long val = ((Boolean) _value) ? this.one : 0;
			_mv.push( val );
		}
		else if (_value instanceof Date) {
			Date date = (Date) _value;
			// LATER Native scaled long implementation of dateToExcel?
			long val = numericType().valueOf( RuntimeDouble_v1.dateToNum( date ) ).longValue();
			_mv.push( val );
		}
		else {
			throw new CompilerException.UnsupportedDataType( "Scaled long constant cannot be of type "
					+ _value.getClass().getName() );
		}
	}


	@Override
	protected void compileZero( GeneratorAdapter _mv ) throws CompilerException
	{
		_mv.visitInsn( Opcodes.LCONST_0 );
	}


}
