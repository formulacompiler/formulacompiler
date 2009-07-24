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
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.runtime.internal.Runtime_v2;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class TypeCompilerForStrings extends TypeCompiler
{
	private static final Type RUNTIME_TYPE = Type.getType( Runtime_v2.class );
	static final String SNAME = ByteCodeEngineCompiler.STRING_CLASS.getInternalName();
	static final String S = ByteCodeEngineCompiler.STRING_CLASS.getDescriptor();

	public TypeCompilerForStrings( ByteCodeEngineCompiler _engineCompiler )
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
