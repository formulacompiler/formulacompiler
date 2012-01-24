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
import org.formulacompiler.runtime.internal.RuntimeLong_v2;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class TypeCompilerForScaledLongs extends TypeCompilerForNumbers
{
	private static final Type RUNTIME_TYPE = Type.getType( RuntimeLong_v2.class );
	private static final Type RUNTIME_CONTEXT_TYPE = Type.getType( RuntimeLong_v2.Context.class );

	static final String RUNTIME_CONTEXT_DESCRIPTOR = RUNTIME_CONTEXT_TYPE.getDescriptor();
	static final String RUNTIME_CONTEXT_NAME = "runtimeContext";

	private final long one;


	protected TypeCompilerForScaledLongs( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		super( _engineCompiler, _numericType );
		this.one = ((AbstractLongType) _numericType).one();
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


	private boolean staticContextBuilt = false;

	final void buildStaticContext()
	{
		if (this.staticContextBuilt) return;
		this.staticContextBuilt = true;

		final SectionCompiler root = engineCompiler().rootCompiler();
		final ClassWriter cw = root.cw();
		final FieldVisitor fv = cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_TYPE.getDescriptor(), null, null );
		fv.visitEnd();

		final GeneratorAdapter mv = root.initializer();
		mv.visitTypeInsn( Opcodes.NEW, RUNTIME_CONTEXT_TYPE.getInternalName() );
		mv.visitInsn( Opcodes.DUP );
		mv.push( numericType().scale() );
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, RUNTIME_CONTEXT_TYPE.getInternalName(), "<init>", "(I)V" );
		mv.visitFieldInsn( Opcodes.PUTSTATIC, root.classInternalName(), RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_DESCRIPTOR );
	}


	@Override
	protected void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException
	{
		if (null == _value) {
			compileZero( _mv );
		}
		else if (_value instanceof Long) {
			final long val = ((Long) _value).longValue();
			_mv.push( val );
		}
		else if (_value instanceof Number) {
			final long val = (Long) numericType().valueOf( (Number) _value );
			_mv.push( val );
		}
		else if (_value instanceof Boolean) {
			final long val = ((Boolean) _value) ? this.one : 0;
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
