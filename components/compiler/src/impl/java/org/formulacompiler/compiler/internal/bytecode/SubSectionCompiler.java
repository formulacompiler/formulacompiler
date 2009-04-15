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
import org.formulacompiler.runtime.spreadsheet.RangeAddress;
import org.formulacompiler.runtime.spreadsheet.CellAddress;
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


	SubSectionCompiler( SectionCompiler _parent, SectionModel _model, boolean _computationListenerEnabled )
	{
		super( _parent.engineCompiler(), _model, _parent.engineCompiler().newSubClassName(), _computationListenerEnabled );
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
		if (this.isComputationListenerEnabled()) buildIndex();
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

	private void buildIndex()
	{
		newField( Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, INDEX_MEMBER_NAME, INDEX_TYPE.getDescriptor() );
	}


	@Override
	protected void buildConstructorWithInputs() throws CompilerException
	{
		final StringBuilder descriptor = new StringBuilder( "(" );
		descriptor.append( inputType().getDescriptor() );
		descriptor.append( parentType().getDescriptor() );
		if (this.isComputationListenerEnabled()) descriptor.append( INDEX_TYPE.getDescriptor() );
		descriptor.append( ")V" );

		final MethodCompiler constructorCompiler = new MethodCompiler( this, 0, "<init>", descriptor.toString() )
		{
			@Override
			protected void compileBody() throws CompilerException
			{
				final GeneratorAdapter mv = mv();

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
				mv.putField( SubSectionCompiler.this.classType(), ROOT_MEMBER_NAME, rootType );

				// this.inputs = _inputs;
				if (hasInputs()) {
					mv.loadThis();
					mv.loadArg( 0 );
					storeInputs( mv );
				}

				//this.sectionInfo = new SectionInfoImpl(...);
				if (isComputationListenerEnabled()) {
					mv.loadThis();
					mv.loadArg( 2 ); //section index
					final ExpressionCompilerForNumbers c = numericCompiler();
					final SectionModel sectionModel = model();
					final RangeAddress range = (RangeAddress) model().getSource();
					final CellAddress topLeft = range.getTopLeft();
					final CellAddress bottomRight = range.getBottomRight();
					c.compile_util_createSectionInfo( sectionModel.getName(),
							topLeft.getSheetName(), topLeft.getRowIndex(), topLeft.getColumnIndex(),
							bottomRight.getSheetName(), bottomRight.getRowIndex(), bottomRight.getColumnIndex() );
					mv.putField( section().classType(), SECTION_INFO_MEMBER_NAME, SECTION_INFO_CLASS );
				}

				mv.visitInsn( Opcodes.RETURN );
			}
		};
		constructorCompiler.compile();
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
