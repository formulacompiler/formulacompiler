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

import static org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler.*;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

final class RootSectionCompiler extends SectionCompiler
{
	private static final org.objectweb.asm.commons.Method EMPTY_CONSTRUCTOR_METHOD = org.objectweb.asm.commons.Method
			.getMethod( "void <init>()" );
	private static final org.objectweb.asm.commons.Method RESET_METHOD = org.objectweb.asm.commons.Method
			.getMethod( "void reset()" );


	RootSectionCompiler( ByteCodeEngineCompiler _compiler, SectionModel _model )
	{
		super( _compiler, _model, GEN_ROOT_NAME );
	}

	@Override
	protected SectionCompiler parentSectionCompiler()
	{
		return null;
	}

	@Override
	protected RootSectionCompiler rootSectionCompiler()
	{
		return this;
	}


	@Override
	protected void buildMembers()
	{
		super.buildMembers();
		buildEnvironmentMember();
	}

	private void buildEnvironmentMember()
	{
		// Package visible so subsections can read it.
		newField( Opcodes.ACC_FINAL, ENV_MEMBER_NAME, ENV_DESC );
	}


	private GeneratorAdapter constructor;

	@Override
	protected void buildConstructorWithInputs() throws CompilerException
	{
		GeneratorAdapter mv = newMethod( 0, "<init>", "(" + inputType().getDescriptor() + ENV_DESC + ")V" );

		// super( _inputs ); or super();
		callInheritedConstructor( mv, 1 );

		// this.environment = _environment;
		mv.loadThis();
		mv.loadArg( 1 );
		mv.putField( this.classType(), ENV_MEMBER_NAME, ENV_CLASS );

		// this.inputs = _inputs;
		if (hasInputs()) {
			mv.loadThis();
			mv.loadArg( 0 );
			storeInputs( mv );
		}

		this.constructor = mv;
	}

	@Override
	protected void finalizeConstructor() throws CompilerException
	{
		if (this.computationTimeCompiled) {
			this.constructor.loadThis();
			this.constructor.newInstance( COMP_TIME_CLASS );
			this.constructor.dup();
			this.constructor.invokeConstructor( COMP_TIME_CLASS, EMPTY_CONSTRUCTOR_METHOD );
			this.constructor.putField( this.classType(), COMP_TIME_MEMBER_NAME, COMP_TIME_CLASS );
		}

		GeneratorAdapter mv = this.constructor;
		mv.visitInsn( Opcodes.RETURN );
		endMethod( mv );
	}


	@Override
	protected void compileEnvironmentAccess( GeneratorAdapter _mv )
	{
		_mv.loadThis();
		_mv.getField( classType(), ENV_MEMBER_NAME, ENV_CLASS );
	}


	private boolean computationTimeCompiled = false;

	private void compileComputationTime()
	{
		if (!this.computationTimeCompiled) {
			newField( Opcodes.ACC_FINAL, COMP_TIME_MEMBER_NAME, COMP_TIME_DESC );
			if (hasReset()) {
				GeneratorAdapter mv = resetter();
				mv.loadThis();
				mv.getField( classType(), COMP_TIME_MEMBER_NAME, COMP_TIME_CLASS );
				mv.invokeVirtual( COMP_TIME_CLASS, RESET_METHOD );
			}
			this.computationTimeCompiled = true;
		}
	}

	@Override
	protected void compileComputationTimeAccess( GeneratorAdapter _mv )
	{
		_mv.loadThis();
		compileComputationTimeAccessGivenThis( _mv );
	}

	void compileComputationTimeAccessGivenThis( GeneratorAdapter _mv )
	{
		compileComputationTime();
		_mv.getField( classType(), COMP_TIME_MEMBER_NAME, COMP_TIME_CLASS );
	}

}
