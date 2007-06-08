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
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.EngineException;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.runtime.internal.bytecode.ByteCodeEngine;
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

	static final Type GEN_FACTORY_CLASS = Type.getType( GEN_FACTORY_DESC );
	static final Type GEN_ROOT_CLASS = Type.getType( GEN_ROOT_DESC );

	static final Type ENGINE_INTF = Type.getType( Engine.class );
	static final Type COMPUTATION_INTF = Type.getType( Computation.class );
	static final Type FACTORY_INTF = Type.getType( org.formulacompiler.runtime.ComputationFactory.class );
	static final Type MATH_CLASS = Type.getType( Math.class );
	static final Type BIGDECIMAL_CLASS = Type.getType( BigDecimal.class );
	static final Type BIGINTEGER_CLASS = Type.getType( BigInteger.class );
	static final Type STRING_CLASS = Type.getType( String.class );

	static final Type ILLEGALARGUMENT_CLASS = Type.getType( IllegalArgumentException.class );

	private final TypeCompilerForNumbers numberCompiler = TypeCompilerForNumbers.compilerFor( this, this
			.getNumericType() );
	private final TypeCompiler stringCompiler = new TypeCompilerForStrings( this );
	private final boolean canCache;


	public ByteCodeEngineCompiler(Config _config)
	{
		super( _config );
		this.canCache = Resettable.class.isAssignableFrom( getModel().getRoot().getOutputClass() );
	}

	public static final class Factory implements OptimizedModelToEngineCompiler.Factory
	{
		public OptimizedModelToEngineCompiler newInstance( Config _config )
		{
			return new ByteCodeEngineCompiler( _config );
		}
	}


	boolean canCache()
	{
		return this.canCache;
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
		final Map<String, byte[]> classNamesAndBytes = New.newMap();

		final SectionCompiler rootCompiler = new SectionCompiler( this, getModel().getRoot() );
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

		public ElementCreator(SectionCompiler _rootCompiler)
		{
			super( _rootCompiler );
		}

		@Override
		protected SubSectionCompiler accessSubSection( SectionModel _section )
		{
			return new SubSectionCompiler( getSection(), _section );
		}

		@Override
		protected void visitTargetCell( CellModel _cell ) throws CompilerException
		{
			new CellComputation( getSection(), _cell ).validate();
		}

	}


	final class ElementCompiler extends AbstractElementVisitor
	{

		public ElementCompiler(SectionCompiler _rootCompiler)
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
		protected boolean visitedSection( SectionModel _section )
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

		public AbstractElementVisitor(SectionCompiler _rootCompiler)
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
		protected boolean visitedSection( SectionModel _section )
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
