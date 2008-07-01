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

import static org.formulacompiler.compiler.internal.bytecode.ByteCodeEngineCompiler.*;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.ComputationMode;
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


	private boolean computationModeCompiled = false;

	private void compileComputationMode()
	{
		if (!this.computationModeCompiled) {
			newField( Opcodes.ACC_FINAL, COMP_MODE_MEMBER_NAME, COMP_MODE_DESC );
			final ComputationMode computationMode = model().getEngine().getComputationMode();
			final GeneratorAdapter ga = this.constructor;
			ga.loadThis();
			ga.getStatic( COMP_MODE_CLASS, computationMode.name(), COMP_MODE_CLASS );
			ga.putField( classType(), COMP_MODE_MEMBER_NAME, COMP_MODE_CLASS );
			this.computationModeCompiled = true;
		}
	}

	@Override
	protected void compileComputationModeAccess( GeneratorAdapter _mv )
	{
		_mv.loadThis();
		compileComputationModeAccessGivenThis( _mv );
	}

	void compileComputationModeAccessGivenThis( GeneratorAdapter _mv )
	{
		compileComputationMode();
		_mv.getField( classType(), COMP_MODE_MEMBER_NAME, COMP_MODE_CLASS );
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
