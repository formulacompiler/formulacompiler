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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

final class RootSectionCompiler extends SectionCompiler
{

	RootSectionCompiler( ByteCodeEngineCompiler _compiler, SectionModel _model )
	{
		super( _compiler, _model, ByteCodeEngineCompiler.GEN_ROOT_NAME );
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
		newField( Opcodes.ACC_FINAL, ByteCodeEngineCompiler.ENV_MEMBER_NAME, ByteCodeEngineCompiler.ENV_DESC );
	}


	@Override
	protected void buildConstructorWithInputs() throws CompilerException
	{
		GeneratorAdapter mv = newMethod( 0, "<init>", "("
				+ inputType().getDescriptor() + ByteCodeEngineCompiler.ENV_DESC + ")V" );

		// super( _inputs ); or super();
		callInheritedConstructor( mv, 1 );

		// this.environment = _environment;
		mv.loadThis();
		mv.loadArg( 1 );
		mv.putField( this.classType(), ByteCodeEngineCompiler.ENV_MEMBER_NAME, ByteCodeEngineCompiler.ENV_CLASS );

		// this.inputs = _inputs;
		if (hasInputs()) {
			mv.loadThis();
			mv.loadArg( 0 );
			storeInputs( mv );
		}

		mv.visitInsn( Opcodes.RETURN );
		endMethod( mv );
	}

	
	@Override
	protected void compileEnvironmentAccess( GeneratorAdapter _mv )
	{
		_mv.loadThis();
		_mv.getField( classType(), ByteCodeEngineCompiler.ENV_MEMBER_NAME, ByteCodeEngineCompiler.ENV_CLASS );
	}

	
}
