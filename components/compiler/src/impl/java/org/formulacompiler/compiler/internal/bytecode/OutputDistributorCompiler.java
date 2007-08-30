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
import java.lang.reflect.Modifier;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.CompilerException.UnsupportedDataType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class OutputDistributorCompiler
{
	private final SectionCompiler section;
	private final Method method;
	private final String caseMethodPrefix;
	private final String name;
	private final Class[] params;
	private final Type[] paramTypes;
	private final Type returnType;
	private final org.objectweb.asm.commons.Method methodType;
	private final GeneratorAdapter mv;
	private final String getterDescriptor;
	private int nextCaseNumber;


	OutputDistributorCompiler(SectionCompiler _section, Method _method)
	{
		super();
		this.section = _section;
		this.method = _method;
		this.caseMethodPrefix = _method.getName() + "__";

		this.name = this.method.getName();
		this.params = this.method.getParameterTypes();

		this.paramTypes = new Type[ this.params.length ];
		this.returnType = Type.getType( this.method.getReturnType() );
		for (int i = 0; i < this.params.length; i++) {
			this.paramTypes[ i ] = Type.getType( this.params[ i ] );
		}
		this.methodType = new org.objectweb.asm.commons.Method( this.name, this.returnType, this.paramTypes );
		this.getterDescriptor = "()" + this.returnType.getDescriptor();

		final int access = Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC;
		this.mv = new GeneratorAdapter( access, this.methodType, null, null, _section.cw() );
	}


	GeneratorAdapter mv()
	{
		return this.mv;
	}


	void beginCompilation()
	{
		mv().visitCode();
	}

	String compileCase( CallFrame _callFrame ) throws UnsupportedDataType
	{
		final String caseMethodName = this.caseMethodPrefix + this.nextCaseNumber++;
		
		final Label next = mv().newLabel();
		for (int i = 0; i < this.params.length; i++) {
			final Class argClass = this.params[ i ];
			final Type argType = this.paramTypes[ i ];
			final Object argValue = _callFrame.getArgs()[ i ];
			
			mv().loadArg( i );
			
			if (argClass == Integer.TYPE) {
				mv().push( ((Number) argValue).intValue() );
				mv().ifCmp( argType, mv().NE, next );
			}
			else if (argClass == Long.TYPE) {
				mv().push( ((Number) argValue).longValue() );
				mv().ifCmp( argType, mv().NE, next );
			}
			else if (argClass.isPrimitive()) {
				throw new CompilerException.UnsupportedDataType( "The type '" + argClass + "' is not supported as an output parameter type for '" + this.method + "'." );
			}
			else {
				mv().visitLdcInsn( argValue );
				mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/Object", "equals", "(Ljava/lang/Object;)Z" );
				mv().ifZCmp( mv().EQ, next );
			}

		}
		
		mv().loadThis();
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, this.section.classInternalName(), caseMethodName, this.getterDescriptor );
		mv().returnValue();
		
		mv().visitLabel( next );
		
		return caseMethodName;
	}

	void endCompilation()
	{
		tryToCallSuper();
		mv().endMethod();
		mv().visitEnd();
	}


	private void tryToCallSuper()
	{
		Class superClass = this.method.getDeclaringClass();
		if (!Modifier.isInterface( superClass.getModifiers() ) && !Modifier.isAbstract( this.method.getModifiers() )) {

			mv().loadThis();
			for (int i = 0; i < this.paramTypes.length; i++) {
				mv().loadArg( i );
			}

			final String superTypeName = Type.getType( superClass ).getInternalName();
			final String superMethodDesc = this.methodType.getDescriptor();
			mv().visitMethodInsn( Opcodes.INVOKESPECIAL, superTypeName, this.method.getName(), superMethodDesc );

			mv().returnValue();
		}
		else {
			failWhenNoMatch();
		}
	}


	private void failWhenNoMatch()
	{
		mv().throwException( ByteCodeEngineCompiler.ILLEGALARGUMENT_CLASS, "Given argument values not bound in '" + this.method + "'." );
	}

}
