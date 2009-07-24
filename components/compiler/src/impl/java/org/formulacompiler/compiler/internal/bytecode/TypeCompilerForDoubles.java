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
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class TypeCompilerForDoubles extends TypeCompilerForNumbers
{
	private static final Type RUNTIME_TYPE = Type.getType( RuntimeDouble_v2.class );

	protected TypeCompilerForDoubles( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		super( _engineCompiler, _numericType );
	}

	@Override
	protected Type runtimeType()
	{
		return RUNTIME_TYPE;
	}

	@Override
	protected Type type()
	{
		return Type.DOUBLE_TYPE;
	}

	@Override
	protected int returnOpcode()
	{
		return Opcodes.DRETURN;
	}


	@Override
	protected void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException
	{
		if (null == _value) {
			_mv.visitInsn( Opcodes.DCONST_0 );
		}
		else if (_value instanceof Number) {
			final double val = ((Number) _value).doubleValue();
			_mv.push( val );
		}
		else if (_value instanceof Boolean) {
			final double val = ((Boolean) _value) ? 1 : 0;
			_mv.push( val );
		}
		else {
			throw new CompilerException.UnsupportedDataType( "Double constant cannot be of type "
					+ _value.getClass().getName() );
		}
	}


	@Override
	protected void compileZero( GeneratorAdapter _mv ) throws CompilerException
	{
		_mv.visitInsn( Opcodes.DCONST_0 );
	}


}
