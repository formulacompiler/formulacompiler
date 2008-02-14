/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

import org.formulacompiler.runtime.internal.bytecode.ByteCodeEngine;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class FactoryCompiler extends ClassCompiler
{
	static final String ENV_CONSTRUCTOR_SIG = "(" + ByteCodeEngineCompiler.ENV_DESC + ")V";

	private final Class userFactoryClass;
	private final Method userFactoryMethod;
	private final Type userFactoryType;
	private final Class userInputClass;
	private final Type userInputType;


	FactoryCompiler( ByteCodeEngineCompiler _compiler, Class _factoryClass, Method _factoryMethod )
	{
		super( _compiler, ByteCodeEngine.GEN_FACTORY_NAME, true );
		this.userFactoryClass = _factoryClass;
		this.userFactoryMethod = _factoryMethod;
		this.userFactoryType = (_factoryClass != null) ? Type.getType( _factoryClass ) : null;
		this.userInputClass = engineCompiler().getModel().getInputClass();
		this.userInputType = Type.getType( this.userInputClass );
	}


	void compile()
	{
		final Type parentType = initializeClass( this.userFactoryClass, this.userFactoryType,
				ByteCodeEngineCompiler.FACTORY_INTF );
		buildEnvironmentField();
		buildDefaultConstructor( parentType );
		buildComputationFactoryMethod();
		if (this.userFactoryMethod != null) {
			buildUserFactoryMethod();
		}
		finalizeClass();
	}


	private void buildEnvironmentField()
	{
		newField( Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, ByteCodeEngineCompiler.ENV_MEMBER_NAME,
				ByteCodeEngineCompiler.ENV_DESC );
	}


	private void buildDefaultConstructor( Type _parentType )
	{
		GeneratorAdapter mv = newMethod( Opcodes.ACC_PUBLIC, "<init>", ENV_CONSTRUCTOR_SIG );
		mv.visitCode();
		mv.loadThis();
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, _parentType.getInternalName(), "<init>", "()V" );
		mv.loadThis();
		mv.loadArg( 0 );
		mv.putField( this.classType(), ByteCodeEngineCompiler.ENV_MEMBER_NAME, ByteCodeEngineCompiler.ENV_CLASS );
		mv.visitInsn( Opcodes.RETURN );
		endMethod( mv );
	}


	private void buildComputationFactoryMethod()
	{
		final GeneratorAdapter mv = newMethod( "newComputation", "(Ljava/lang/Object;)"
				+ ByteCodeEngineCompiler.COMPUTATION_INTF.getDescriptor() );
		mv.newInstance( ByteCodeEngineCompiler.GEN_ROOT_CLASS );
		mv.dup();
		mv.loadArg( 0 );
		compileClassRef( this.userInputClass, this.userInputType );
		mv.checkCast( this.userInputType );
		mv.loadThis();
		mv.getField( classType(), ByteCodeEngineCompiler.ENV_MEMBER_NAME, ByteCodeEngineCompiler.ENV_CLASS );
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, ByteCodeEngineCompiler.GEN_ROOT_CLASS.getInternalName(), "<init>", "("
				+ this.userInputType.getDescriptor() + ByteCodeEngineCompiler.ENV_DESC + ")V" );
		mv.visitInsn( Opcodes.ARETURN );
		endMethod( mv );
	}


	private void buildUserFactoryMethod()
	{
		final GeneratorAdapter mv = newMethod( this.userFactoryMethod.getName(), Type
				.getMethodDescriptor( this.userFactoryMethod ) );
		mv.newInstance( ByteCodeEngineCompiler.GEN_ROOT_CLASS );
		mv.dup();
		mv.loadArg( 0 );
		mv.loadThis();
		mv.getField( classType(), ByteCodeEngineCompiler.ENV_MEMBER_NAME, ByteCodeEngineCompiler.ENV_CLASS );
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, ByteCodeEngineCompiler.GEN_ROOT_CLASS.getInternalName(), "<init>", "("
				+ this.userInputType.getDescriptor() + ByteCodeEngineCompiler.ENV_DESC + ")V" );
		mv.visitInsn( Opcodes.ARETURN );
		endMethod( mv );
	}


	private GeneratorAdapter newMethod( String _name, String _signature )
	{
		final String name = _name;
		final String signature = _signature;
		final int access = Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC;
		return new GeneratorAdapter( cw().visitMethod( access, name, signature, null, null ), access, name, signature );
	}

}
