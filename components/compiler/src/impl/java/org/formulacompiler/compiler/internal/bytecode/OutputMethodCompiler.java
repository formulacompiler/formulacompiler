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

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.objectweb.asm.Opcodes;


final class OutputMethodCompiler extends TypedMethodCompiler
{
	private final Method implementedMethod;
	private final CellMethodCompiler cellMethodCompiler;


	public OutputMethodCompiler( SectionCompiler _section, String _methodName, String _methodSignature,
			CellMethodCompiler _cellMethodCompiler, Method _implements )
	{
		super( _section, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, _methodName, _methodSignature, _cellMethodCompiler.dataType() );
		this.implementedMethod = _implements;
		this.cellMethodCompiler = _cellMethodCompiler;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		expressionCompiler().compileCallTo( this.cellMethodCompiler );
		expressionCompiler().compileConversionToResultOf( this.implementedMethod );
		compileReturnOf( this.implementedMethod.getReturnType() );
	}


	private final void compileReturnOf( Class _returnType )
	{
		if (_returnType == Character.TYPE
				|| _returnType == Byte.TYPE || _returnType == Short.TYPE || _returnType == Integer.TYPE
				|| _returnType == Boolean.TYPE) {
			mv().visitInsn( Opcodes.IRETURN );
		}
		else if (_returnType == Long.TYPE) {
			mv().visitInsn( Opcodes.LRETURN );
		}
		else if (_returnType == Float.TYPE) {
			mv().visitInsn( Opcodes.FRETURN );
		}
		else if (_returnType == Double.TYPE) {
			mv().visitInsn( Opcodes.DRETURN );
		}
		else {
			mv().visitInsn( Opcodes.ARETURN );
		}
	}


}
