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
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


final class SubSectionCompiler extends SectionCompiler
{
	private final SectionCompiler parentSectionCompiler;
	private final String arrayDescriptor;
	private final Type arrayType;
	private final String getterName;
	private final String getterDescriptor;


	SubSectionCompiler( SectionCompiler _parent, SectionModel _model )
	{
		super( _parent.engineCompiler(), _model, _parent.engineCompiler().newSubClassName() );
		this.parentSectionCompiler = _parent;
		this.arrayDescriptor = "[" + classDescriptor();
		this.arrayType = Type.getType( arrayDescriptor() );
		this.getterName = "get" + className();
		this.getterDescriptor = "()" + arrayDescriptor();
		_parent.addSubSectionCompiler( _model, this );
	}


	@Override
	protected SectionCompiler parentSectionCompiler()
	{
		return this.parentSectionCompiler;
	}

	@Override
	protected RootSectionCompiler rootSectionCompiler()
	{
		return parentSectionCompiler().rootSectionCompiler();
	}


	String arrayDescriptor()
	{
		return this.arrayDescriptor;
	}

	Type arrayType()
	{
		return this.arrayType;
	}

	String getterName()
	{
		return this.getterName;
	}

	String getterDescriptor()
	{
		return this.getterDescriptor;
	}


	private Type parentType()
	{
		return parentSectionCompiler().classType();
	}

	private Type rootType()
	{
		return rootSectionCompiler().classType();
	}


	@Override
	protected void buildMembers()
	{
		super.buildMembers();
		buildParentMember();
		buildRootMember();
	}

	private void buildParentMember()
	{
		// Package visible so subsections can read it.
		newField( Opcodes.ACC_FINAL, PARENT_MEMBER_NAME, parentType().getDescriptor() );
	}

	private void buildRootMember()
	{
		// Package visible so subsections can read it.
		newField( Opcodes.ACC_FINAL, ROOT_MEMBER_NAME, rootType().getDescriptor() );
	}


	@Override
	protected void buildConstructorWithInputs() throws CompilerException
	{
		GeneratorAdapter mv = newMethod( 0, "<init>", "("
				+ inputType().getDescriptor() + parentType().getDescriptor() + ")V" );

		// super( _inputs ); or super(); or super( _inputs, _parent );
		callInheritedConstructor( mv, 1 );

		// this.parent = _parent;
		mv.loadThis();
		mv.loadArg( 1 );
		mv.putField( classType(), PARENT_MEMBER_NAME, parentType() );
		// this.root = _parent.root();
		mv.loadThis();
		mv.loadArg( 1 );
		final Type rootType = rootSectionCompiler().classType();
		if (!(parentSectionCompiler() instanceof RootSectionCompiler)) {
			// parent.root is package visible
			mv.getField( parentType(), ROOT_MEMBER_NAME, rootType );
		}
		mv.putField( this.classType(), ROOT_MEMBER_NAME, rootType );

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
	protected void finalizeConstructor() throws CompilerException
	{
		// Finalized already.
	}


	@Override
	protected boolean callConstructorWithInputs( GeneratorAdapter _mv, int _inputsVar )
	{
		final int P_PARENT = 2;

		// try super( _inputs, _parent );
		try {
			// ensure it is here and accessible
			outputClass().getConstructor( inputClass(), parentSectionCompiler().model().getOutputClass() );
		}
		catch (NoSuchMethodException e) {
			return super.callConstructorWithInputs( _mv, _inputsVar );
		}

		_mv.loadThis();
		if (0 <= _inputsVar) {
			_mv.visitVarInsn( Opcodes.ALOAD, _inputsVar );
		}
		else {
			_mv.visitInsn( Opcodes.ACONST_NULL );
		}
		_mv.visitVarInsn( Opcodes.ALOAD, P_PARENT );
		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, outputType().getInternalName(), "<init>", "("
				+ inputType().getDescriptor() + parentSectionCompiler().outputType().getDescriptor() + ")V" );

		return true;
	}


	@Override
	protected void compileEnvironmentAccess( GeneratorAdapter _mv )
	{
		final Type rootType = rootType();
		_mv.loadThis();
		_mv.getField( classType(), ROOT_MEMBER_NAME, rootType );
		_mv.getField( rootType, ENV_MEMBER_NAME, ENV_CLASS );
	}

	@Override
	protected void compileComputationModeAccess( final GeneratorAdapter _mv )
	{
		final Type rootType = rootType();
		_mv.loadThis();
		_mv.getField( classType(), ROOT_MEMBER_NAME, rootType );
		rootSectionCompiler().compileComputationModeAccessGivenThis( _mv );
	}

	@Override
	protected void compileComputationTimeAccess( GeneratorAdapter _mv )
	{
		final Type rootType = rootType();
		_mv.loadThis();
		_mv.getField( classType(), ROOT_MEMBER_NAME, rootType );
		rootSectionCompiler().compileComputationTimeAccessGivenThis( _mv );
	}


}
