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

import java.lang.reflect.Method;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import sej.CompilerException;
import sej.Operator;

final class ExpressionCompilerForStrings extends ExpressionCompiler
{

	ExpressionCompilerForStrings(MethodCompiler _methodCompiler)
	{
		super( _methodCompiler );
	}

	@Override
	protected TypeCompiler typeCompiler()
	{
		return section().engineCompiler().stringCompiler();
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		if (String.class == _class) {
			return;
		}
		else if (Object.class.isAssignableFrom( _class )) {
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, Type.getInternalName( _class ), "toString",
					"()Ljava/lang/String;" );
		}
		else {
			super.compileConversionFrom( _class );
		}
	}


	@Override
	protected void innerCompileConversionFromResultOf( Method _method ) throws CompilerException
	{
		Class returnType = _method.getReturnType();
		compileConversionFrom( returnType );
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (String.class == _class) {
			return;
		}
		else {
			super.compileConversionTo( _class );
		}
	}


	@Override
	protected void innerCompileConversionToResultOf( Method _method ) throws CompilerException
	{
		Class returnType = _method.getReturnType();
		compileConversionTo( returnType );
	}


	@Override
	protected void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		switch (_operator) {

			case CONCAT:
				switch (_numberOfArguments) {
					case 0:
						unsupported( "CONCAT needs at least one argument." );
						break;
					case 1:
						return;
					default:
						
				}
				break;
				
		}
		unsupported( "Operator " + _operator + " is not supported for strings." );
	}


	@Override
	protected void compileComparison( int _comparisonOpcode ) throws CompilerException
	{
		// LATER compare strings
		unsupported( "String comparison is not implemented yet." );
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/String", "compareTo", "(Ljava/lang/String;)I" );
	}

	
	@Override
	public String toString()
	{
		return "string";
	}
	
}
