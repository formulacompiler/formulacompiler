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

import java.lang.reflect.Method;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.objectweb.asm.Opcodes;


final class OutputMethodCompiler extends TypedMethodCompiler
{
	private final CellComputation computation;
	private final Method implementedMethod;


	public OutputMethodCompiler(SectionCompiler _section, String _methodName, String _methodSignature,
			CellComputation _computation, Method _implements)
	{
		super( _section, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, _methodName, _methodSignature, _computation.getCell()
				.getDataType() );
		this.computation = _computation;
		this.implementedMethod = _implements;
	}


	@Override
	protected void compileBody() throws CompilerException
	{
		final CellModel cell = this.computation.getCell();

		expressionCompiler().compileRef( this.computation );

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
