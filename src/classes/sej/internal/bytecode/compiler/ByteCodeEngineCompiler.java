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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;

import sej.CompilerError;
import sej.Engine;
import sej.EngineError;
import sej.Resettable;
import sej.SaveableEngine;
import sej.internal.AbstractEngineCompiler;
import sej.internal.Settings;
import sej.internal.bytecode.runtime.ByteCodeEngine;
import sej.internal.model.AbstractComputationModelVisitor;
import sej.internal.model.CellModel;
import sej.internal.model.SectionModel;


public class ByteCodeEngineCompiler extends AbstractEngineCompiler
{

	// ------------------------------------------------ Configuration & Factory

	public static void register()
	{
		setFactory( new Factory()
		{

			@Override
			protected AbstractEngineCompiler newInstance( Config _config )
			{
				return new ByteCodeEngineCompiler( _config );
			}

		} );
	}

	public static AbstractEngineCompiler newInstance( Config _config )
	{
		return new ByteCodeEngineCompiler( _config );
	}


	// ------------------------------------------------ Construction

	static final String GEN_ROOT_NAME = "$Root";
	static final String GEN_PACKAGE_PATH = ByteCodeEngine.GEN_PACKAGE_NAME.replace( '.', '/' );
	static final String GEN_FACTORY_PATH = "sej/gen/" + ByteCodeEngine.GEN_FACTORY_NAME;
	static final String GEN_ROOT_PATH = "sej/gen/" + GEN_ROOT_NAME;
	
	static final Type ENGINE_INTF = Type.getType( Engine.class );
	static final String INPUTS_MEMBER_NAME = "inputs";
	static final Type MATH = Type.getType( Math.class );
	static final Type BIGDECIMAL = Type.getType( BigDecimal.class );

	private final boolean canCache;


	public ByteCodeEngineCompiler(Config _config)
	{
		super( _config );
		this.canCache = Resettable.class.isAssignableFrom( getModel().getRoot().getOutputClass() );
	}


	boolean canCache()
	{
		return this.canCache;
	}


	// ------------------------------------------------ Compilation


	@Override
	public SaveableEngine compile() throws CompilerError, EngineError
	{
		final ByteCodeSectionCompiler rootCompiler = new ByteCodeSectionCompiler( this, getModel().getRoot() );

		getModel().traverse( new ElementCreator( rootCompiler ) );
		getModel().traverse( new ElementCompiler( rootCompiler ) );
		if (Settings.isDebugCompilationEnabled()) dumpClassBytes( rootCompiler.getClassBytes() );

		final ByteCodeFactoryCompiler factoryCompiler = new ByteCodeFactoryCompiler( this, getFactoryClass(),
				getFactoryMethod() );
		factoryCompiler.compile();

		final HashMap<String, byte[]> classNamesAndBytes = new HashMap<String, byte[]>();
		factoryCompiler.collectClassNamesAndBytes( classNamesAndBytes );
		rootCompiler.collectClassNamesAndBytes( classNamesAndBytes );

		return new SaveableByteCodeEngine( classNamesAndBytes );
	}


	final class ElementCreator extends AbstractElementVisitor
	{

		public ElementCreator(ByteCodeSectionCompiler _rootCompiler)
		{
			super( _rootCompiler );
		}

		@Override
		protected ByteCodeSectionCompiler accessSubSection( SectionModel _section )
		{
			return new ByteCodeSectionCompiler( getSection(), _section );
		}

		@Override
		protected void visitTargetCell( CellModel _cell ) throws CompilerError
		{
			new ByteCodeCellComputation( getSection(), _cell ).validate();
		}

	}


	final class ElementCompiler extends AbstractElementVisitor
	{

		public ElementCompiler(ByteCodeSectionCompiler _rootCompiler)
		{
			super( _rootCompiler );
		}

		@Override
		public boolean visit( SectionModel _section ) throws CompilerError
		{
			final boolean result = super.visit( _section );
			getSection().beginCompilation();
			return result;
		}

		@Override
		public boolean visited( SectionModel _section )
		{
			getSection().endCompilation();
			return super.visited( _section );
		}

		@Override
		protected ByteCodeSectionCompiler accessSubSection( SectionModel _section )
		{
			final ByteCodeSectionCompiler subCompiler = getSection().getSubSectionCompiler( _section );
			getSection().compileAccessTo( subCompiler );
			return subCompiler;
		}

		@Override
		protected void visitTargetCell( CellModel _cell ) throws CompilerError
		{
			getSection().getCellComputation( _cell ).compile();
		}

	}


	abstract class AbstractElementVisitor extends AbstractComputationModelVisitor
	{
		private final ByteCodeSectionCompiler root;
		private ByteCodeSectionCompiler section;

		public AbstractElementVisitor(ByteCodeSectionCompiler _rootCompiler)
		{
			this.root = _rootCompiler;
		}

		ByteCodeSectionCompiler getSection()
		{
			return this.section;
		}

		ClassWriter cw()
		{
			return getSection().cw();
		}

		@Override
		public boolean visit( SectionModel _section ) throws CompilerError
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
		public boolean visited( SectionModel _section )
		{
			this.section = this.section.getParentSectionCompiler();
			return true;
		}

		protected abstract ByteCodeSectionCompiler accessSubSection( SectionModel _section );

		@Override
		public boolean visit( CellModel _cell ) throws CompilerError
		{
			if (_cell.isInput() || _cell.isOutput() || 2 <= _cell.getReferenceCount()) {
				visitTargetCell( _cell );
			}
			return true;
		}

		protected abstract void visitTargetCell( CellModel _cell ) throws CompilerError;
	}


	private void dumpClassBytes( byte[] _classBytes )
	{
		try {
			FileOutputStream stream = new FileOutputStream( "D:/Temp/GeneratedEngine.class" );
			try {
				stream.write( _classBytes );
			}
			finally {
				stream.close();
			}
		}
		catch (FileNotFoundException e) {
			// just eat it
		}
		catch (IOException e) {
			// just eat it
		}
	}


}
