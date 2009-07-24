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
