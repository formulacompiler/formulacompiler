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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.engine.AbstractOptimizedModelToEngineCompiler;
import org.formulacompiler.compiler.internal.engine.OptimizedModelToEngineCompiler;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.formulacompiler.compiler.internal.model.AbstractComputationModelVisitor;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.runtime.internal.ComputationTime;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.bytecode.ByteCodeEngine;
import org.formulacompiler.runtime.spreadsheet.SectionInfo;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;


public class ByteCodeEngineCompiler extends AbstractOptimizedModelToEngineCompiler
{
	static final String GEN_PACKAGE_PATH = ByteCodeEngine.GEN_PACKAGE_NAME.replace( '.', '/' );
	static final String GEN_FACTORY_PATH = GEN_PACKAGE_PATH + ByteCodeEngine.GEN_FACTORY_NAME;
	static final String GEN_FACTORY_DESC = "L" + GEN_FACTORY_PATH + ";";
	static final String GEN_ROOT_NAME = "$Root";
	static final String GEN_ROOT_PATH = GEN_PACKAGE_PATH + GEN_ROOT_NAME;
	static final String GEN_ROOT_DESC = "L" + GEN_PACKAGE_PATH + GEN_ROOT_NAME + ";";
	static final String INPUTS_MEMBER_NAME = "$inputs";
	static final String PARENT_MEMBER_NAME = "$parent";
	static final String ROOT_MEMBER_NAME = "$root";

	static final Type INDEX_TYPE = Type.INT_TYPE;
	static final String INDEX_MEMBER_NAME = "$index";

	static final Type GEN_FACTORY_CLASS = Type.getType( GEN_FACTORY_DESC );
	static final Type GEN_ROOT_CLASS = Type.getType( GEN_ROOT_DESC );

	static final Type ENGINE_INTF = Type.getType( Engine.class );
	static final Type COMPUTATION_INTF = Type.getType( Computation.class );
	static final Type FACTORY_INTF = Type.getType( ComputationFactory.class );
	static final Type MATH_CLASS = Type.getType( Math.class );
	static final Type BIGDECIMAL_CLASS = Type.getType( BigDecimal.class );
	static final Type BIGINTEGER_CLASS = Type.getType( BigInteger.class );
	static final Type STRING_CLASS = Type.getType( String.class );

	static final Type ENV_CLASS = Type.getType( Environment.class );
	static final String ENV_DESC = ENV_CLASS.getDescriptor();
	static final String ENV_MEMBER_NAME = "$environment";

	static final Type SECTION_INFO_CLASS = Type.getType( SectionInfo.class );
	static final String SECTION_INFO_DESC = SECTION_INFO_CLASS.getDescriptor();
	static final String SECTION_INFO_MEMBER_NAME = "$info";

	static final Type COMP_MODE_CLASS = Type.getType( ComputationMode.class );
	static final String COMP_MODE_DESC = COMP_MODE_CLASS.getDescriptor();
	static final String COMP_MODE_MEMBER_NAME = "$computationMode";

	static final Type COMP_TIME_CLASS = Type.getType( ComputationTime.class );
	static final String COMP_TIME_DESC = COMP_TIME_CLASS.getDescriptor();
	static final String COMP_TIME_MEMBER_NAME = "$computationTime";

	static final Type ILLEGALARGUMENT_CLASS = Type.getType( IllegalArgumentException.class );

	private final TypeCompilerForNumbers numberCompiler = TypeCompilerForNumbers.compilerFor( this, this
			.getNumericType() );
	private final TypeCompiler stringCompiler = new TypeCompilerForStrings( this );
	private final boolean isResettable;


	public static final class Factory implements OptimizedModelToEngineCompiler.Factory
	{
		public OptimizedModelToEngineCompiler newInstance( Config _config )
		{
			return new ByteCodeEngineCompiler( _config );
		}
	}

	public ByteCodeEngineCompiler( Config _config )
	{
		super( _config );
		this.isResettable = Resettable.class.isAssignableFrom( getModel().getRoot().getOutputClass() );
	}

	boolean isResettable()
	{
		return this.isResettable;
	}


	private int nextSubClassNumber = 0;

	String newSubClassName()
	{
		return "$Sect" + Integer.toString( this.nextSubClassNumber++ );
	}


	// ------------------------------------------------ Compilation


	private SectionCompiler rootCompiler;


	@Override
	public SaveableEngine compile() throws CompilerException, EngineException
	{
		final Map<String, byte[]> classNamesAndBytes = New.map();

		final SectionCompiler rootCompiler = new RootSectionCompiler( this, getModel().getRoot(), isComputationListenerEnabled() );
		this.rootCompiler = rootCompiler;
		try {

			getModel().traverse( new ElementCreator( rootCompiler ) );
			getModel().traverse( new ElementCompiler( rootCompiler ) );

			final FactoryCompiler factoryCompiler = new FactoryCompiler( this, getFactoryClass(), getFactoryMethod() );
			factoryCompiler.compile();

			factoryCompiler.collectClassNamesAndBytes( classNamesAndBytes );
			rootCompiler.collectClassNamesAndBytes( classNamesAndBytes );

		}
		finally {
			this.rootCompiler = null;
		}

		return new SaveableByteCodeEngine( getParentClassLoader(), classNamesAndBytes );
	}


	public SectionCompiler rootCompiler()
	{
		return this.rootCompiler;
	}


	final class ElementCreator extends AbstractElementVisitor
	{

		public ElementCreator( SectionCompiler _rootCompiler )
		{
			super( _rootCompiler );
		}

		@Override
		protected SubSectionCompiler accessSubSection( SectionModel _section )
		{
			return new SubSectionCompiler( getSection(), _section, isComputationListenerEnabled() );
		}

		@Override
		protected void visitTargetCell( CellModel _cell ) throws CompilerException
		{
			new CellComputation( getSection(), _cell ).validate();
		}

	}


	final class ElementCompiler extends AbstractElementVisitor
	{

		public ElementCompiler( SectionCompiler _rootCompiler )
		{
			super( _rootCompiler );
		}

		@Override
		protected boolean visitSection( SectionModel _section ) throws CompilerException
		{
			final boolean result = super.visitSection( _section );
			getSection().beginCompilation();
			return result;
		}

		@Override
		protected boolean visitedSection( SectionModel _section ) throws CompilerException
		{
			getSection().endCompilation();
			return super.visitedSection( _section );
		}

		@Override
		protected SectionCompiler accessSubSection( SectionModel _section ) throws CompilerException
		{
			final SubSectionCompiler subCompiler = getSection().subSectionCompiler( _section );
			getSection().compileAccessTo( subCompiler );
			return subCompiler;
		}

		@Override
		protected void visitTargetCell( CellModel _cell ) throws CompilerException
		{
			getSection().cellComputation( _cell ).compile();
		}

	}


	abstract class AbstractElementVisitor extends AbstractComputationModelVisitor
	{
		private final SectionCompiler root;
		private SectionCompiler section;

		public AbstractElementVisitor( SectionCompiler _rootCompiler )
		{
			this.root = _rootCompiler;
		}

		SectionCompiler getSection()
		{
			return this.section;
		}

		ClassWriter cw()
		{
			return getSection().cw();
		}

		@Override
		protected boolean visitSection( SectionModel _section ) throws CompilerException
		{
			if (null == this.section) {
				this.section = this.root;
			}
			else {
				this.section = accessSubSection( _section );
			}
			assert null != this.section;
			return true;
		}

		@Override
		protected boolean visitedSection( SectionModel _section ) throws CompilerException
		{
			this.section = this.section.parentSectionCompiler();
			return true;
		}

		protected abstract SectionCompiler accessSubSection( SectionModel _section ) throws CompilerException;

		@Override
		protected boolean visitCell( CellModel _cell ) throws CompilerException
		{
			final int refCnt = _cell.getReferenceCount();
			if ((_cell.isInput() && refCnt >= 1) || _cell.isOutput() || refCnt >= 2) {
				visitTargetCell( _cell );
			}
			return true;
		}

		protected abstract void visitTargetCell( CellModel _cell ) throws CompilerException;
	}


	public TypeCompiler typeCompiler( DataType _type )
	{
		switch (_type) {
			case STRING:
				return stringCompiler();
			default:
				return numberCompiler();
		}
	}


	public TypeCompilerForNumbers numberCompiler()
	{
		return this.numberCompiler;
	}


	public TypeCompiler stringCompiler()
	{
		return this.stringCompiler;
	}


}
