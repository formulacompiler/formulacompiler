/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
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

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.internal.expressions.ArrayDescriptor;
import org.formulacompiler.compiler.internal.expressions.ExpressionNode;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForArrayReference;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.New;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;


abstract class SectionCompiler extends ClassCompiler
{
	private final Map<SectionModel, SubSectionCompiler> subSectionCompilers = New.map();
	private final Map<CellModel, CellMethodCompiler> cellMethodCompilers = New.map();
	private final SectionModel model;
	private final Type inputs;
	private final Type outputs;
	private final boolean computationListenerEnabled;


	protected SectionCompiler( ByteCodeEngineCompiler _compiler, SectionModel _model, String _name, boolean _computationListenerEnabled )
	{
		super( _compiler, _name, false );
		this.model = _model;
		this.inputs = typeFor( inputClass() );
		this.outputs = typeFor( outputClass() );
		this.computationListenerEnabled = _computationListenerEnabled;
	}

	boolean isComputationListenerEnabled()
	{
		return this.computationListenerEnabled;
	}

	private Type typeFor( Class _inputClass )
	{
		return (null == _inputClass) ? Type.getType( Object.class ) : Type.getType( _inputClass );
	}

	protected abstract SectionCompiler parentSectionCompiler();
	protected abstract RootSectionCompiler rootSectionCompiler();

	SubSectionCompiler subSectionCompiler( SectionModel _section )
	{
		return this.subSectionCompilers.get( _section );
	}

	void addSubSectionCompiler( SectionModel _section, SubSectionCompiler _compiler )
	{
		this.subSectionCompilers.put( _section, _compiler );
	}

	CellMethodCompiler cellMethodCompiler( CellModel _cell )
	{
		return this.cellMethodCompilers.get( _cell );
	}

	void addCellMethodCompiler( CellModel _cell, CellMethodCompiler _compiler )
	{
		this.cellMethodCompilers.put( _cell, _compiler );
	}

	SectionModel model()
	{
		return this.model;
	}

	Class inputClass()
	{
		return model().getInputClass();
	}

	boolean hasInputs()
	{
		return (null != inputClass());
	}

	Class outputClass()
	{
		return model().getOutputClass();
	}

	Type inputType()
	{
		return this.inputs;
	}

	Type outputType()
	{
		return this.outputs;
	}


	private int getterId;

	String newGetterName()
	{
		return "get$" + (this.getterId++);
	}


	private final Map<String, Integer> getterNameNums = New.map();

	String newGetterName( String _baseName )
	{
		final StringBuilder sb = new StringBuilder( "get$" );
		sb.append( _baseName );
		final Integer id = this.getterNameNums.get( _baseName );
		if (id == null) {
			this.getterNameNums.put( _baseName, 0 );
		}
		else {
			sb.append( '$' );
			sb.append( id );
			this.getterNameNums.put( _baseName, id + 1 );
		}
		return sb.toString();
	}


	private boolean compilationStarted = false;

	void beginCompilation() throws CompilerException
	{
		if (this.compilationStarted) return;
		this.compilationStarted = true;

		initializeClass( outputClass(), this.outputs, COMPUTATION_INTF );
		buildMembers();
		buildConstructorWithInputs();
		if (engineCompiler().isResettable()) {
			buildReset();
		}
	}

	protected void buildMembers()
	{
		if (hasInputs()) buildInputMember();
		if (isComputationListenerEnabled()) buildSectionInfoMember();
	}

	void compileAccessTo( SubSectionCompiler _sub ) throws CompilerException
	{
		newField( Opcodes.ACC_PRIVATE, _sub.getterName(), _sub.arrayDescriptor() );
		new SubSectionLazyGetterCompiler( this, _sub ).compile();

		final CallFrame[] callsToImplement = _sub.model().getCallsToImplement();
		for (CallFrame callToImplement : callsToImplement) {
			new SubSectionOutputAccessorCompiler( this, _sub, callToImplement ).compile();
		}

		// In reset(), do:
		if (hasReset()) {
			// $<section> = null;
			final GeneratorAdapter r = resetter();
			r.loadThis();
			r.visitInsn( Opcodes.ACONST_NULL );
			r.putField( classType(), _sub.getterName(), _sub.arrayType() );
		}
	}

	public void compileCallToGetterFor( GeneratorAdapter _mv, SubSectionCompiler _sub )
	{
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, classInternalName(), _sub.getterName(), _sub.getterDescriptor() );
	}


	void endCompilation() throws CompilerException
	{
		finalizeConstructor();
		finalizeOutputDistributors();
		finalizeReset();
		finalizeClass();
	}


	private GeneratorAdapter resetter;

	private void buildReset()
	{
		this.resetter = newMethod( Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC, "reset", "()V" );
	}

	private void finalizeReset()
	{
		if (this.resetter != null) {
			this.resetter.visitInsn( Opcodes.RETURN );
			endMethod( this.resetter );
			this.resetter = null;
		}
	}

	boolean hasReset()
	{
		return null != this.resetter;
	}

	GeneratorAdapter resetter()
	{
		assert null != this.resetter: "Resetter is null";
		return this.resetter;
	}


	private final Map<Method, OutputDistributorCompiler> outputDistributors = New.map();

	public OutputDistributorCompiler getOutputDistributorFor( Method _method )
	{
		OutputDistributorCompiler dist = this.outputDistributors.get( _method );
		if (dist == null) {
			dist = new OutputDistributorCompiler( this, _method );
			this.outputDistributors.put( _method, dist );
			dist.beginCompilation();
		}
		return dist;
	}

	private void finalizeOutputDistributors()
	{
		for (OutputDistributorCompiler dist : this.outputDistributors.values()) {
			dist.endCompilation();
		}
	}


	private void buildInputMember()
	{
		if (!hasInputs()) throw new IllegalStateException();
		newField( Opcodes.ACC_FINAL + Opcodes.ACC_PRIVATE, INPUTS_MEMBER_NAME, inputType().getDescriptor() );
	}

	private void buildSectionInfoMember()
	{
		if (!isComputationListenerEnabled()) throw new IllegalStateException();
		newField( Opcodes.ACC_FINAL + Opcodes.ACC_PRIVATE, SECTION_INFO_MEMBER_NAME, SECTION_INFO_DESC );
	}

	protected abstract void buildConstructorWithInputs() throws CompilerException;
	protected abstract void finalizeConstructor() throws CompilerException;

	protected void storeInputs( GeneratorAdapter _mv )
	{
		if (!hasInputs()) throw new IllegalStateException();
		_mv.putField( classType(), INPUTS_MEMBER_NAME, inputType() );
	}


	protected void callInheritedConstructor( GeneratorAdapter _mv, int _inputsVar ) throws CompilerException
	{
		try {
			if (outputClass() == null || outputClass().isInterface()) {
				_mv.loadThis();
				_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V" );
			}
			else if (!callConstructorWithInputs( _mv, _inputsVar )) {
				outputClass().getConstructor(); // ensure it is here and accessible
				_mv.loadThis();
				_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, outputType().getInternalName(), "<init>", "()V" );
			}
		}
		catch (NoSuchMethodException e) {
			throw new CompilerException.ConstructorMissing(
					"There is no default constructor and none with the input type as sole parameter.", e );
		}
	}

	protected boolean callConstructorWithInputs( GeneratorAdapter _mv, int _inputsVar )
	{
		try {
			outputClass().getConstructor( inputClass() ); // ensure it is here and accessible
		}
		catch (NoSuchMethodException e) {
			return false;
		}

		_mv.loadThis();
		if (0 <= _inputsVar) {
			_mv.visitVarInsn( Opcodes.ALOAD, _inputsVar );
		}
		else {
			_mv.visitInsn( Opcodes.ACONST_NULL );
		}
		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, outputType().getInternalName(), "<init>", "("
				+ inputType().getDescriptor() + ")V" );

		return true;
	}

	@Override
	void collectClassNamesAndBytes( Map<String, byte[]> _result )
	{
		super.collectClassNamesAndBytes( _result );
		for (ClassCompiler sub : this.subSectionCompilers.values()) {
			sub.collectClassNamesAndBytes( _result );
		}
	}

	protected abstract void compileEnvironmentAccess( GeneratorAdapter _mv );
	protected abstract void compileComputationModeAccess( GeneratorAdapter _mv );
	protected abstract void compileComputationTimeAccess( GeneratorAdapter _mv );

	protected void compileSectionInfoAccess( GeneratorAdapter _mv )
	{
		if (isComputationListenerEnabled()) {
			_mv.loadThis();
			_mv.getField( classType(), SECTION_INFO_MEMBER_NAME, SECTION_INFO_CLASS );
		}
		else throw new UnsupportedOperationException( "Section name access is not supported" );
	}


	private final Map<String, ArrayAccessorCompiler> arrayAccessorsForConstData = New.map();
	private int nextArrayAccessorForConstDataId = 0;

	public ArrayAccessorCompiler getArrayAccessorForConstDataOnly( ExpressionNodeForArrayReference _arrayNode )
			throws CompilerException
	{
		return getArrayAccessorForConstDataOnly( _arrayNode, false );
	}

	public ArrayAccessorCompiler getArrayAccessorForConstDataOnly( ExpressionNodeForArrayReference _arrayNode,
			boolean _trimTrailingNulls ) throws CompilerException
	{
		final String name = _arrayNode.arrayDescriptor().name() + (_trimTrailingNulls ? "$trimmed" : "");
		ArrayAccessorCompiler acc = this.arrayAccessorsForConstData.get( name );
		if (null == acc) {
			final String internalName = Integer.toString( this.nextArrayAccessorForConstDataId++ );
			acc = new ArrayAccessorForConstDataCompiler( this, internalName, _trimTrailingNulls
					? trimTrailingNulls( _arrayNode ) : _arrayNode );
			acc.compile();
			this.arrayAccessorsForConstData.put( name, acc );
		}
		return acc;
	}


	private final Map<String, ArrayAccessorCompiler> arrayAccessorsForFullData = New.map();
	private int nextArrayAccessorForFullDataId = 0;

	public ArrayAccessorCompiler getArrayAccessorForFullData( ExpressionNodeForArrayReference _arrayNode,
			boolean _trimTrailingNulls ) throws CompilerException
	{
		if (areAllConstant( _arrayNode.arguments() )) {
			return getArrayAccessorForConstDataOnly( _arrayNode, _trimTrailingNulls );
		}

		final String name = _arrayNode.arrayDescriptor().name() + (_trimTrailingNulls ? "$trimmed" : "");
		ArrayAccessorCompiler acc = this.arrayAccessorsForFullData.get( name );
		if (null == acc) {
			final String internalName = Integer.toString( this.nextArrayAccessorForFullDataId++ );
			acc = new ArrayAccessorForFullDataCompiler( this, internalName, _trimTrailingNulls
					? trimTrailingNulls( _arrayNode ) : _arrayNode );
			acc.compile();
			this.arrayAccessorsForFullData.put( name, acc );
		}
		return acc;
	}

	boolean areAllConstant( List<ExpressionNode> _arguments )
	{
		for (ExpressionNode arg : _arguments) {
			if (!(arg instanceof ExpressionNodeForConstantValue)) return false;
		}
		return true;
	}

	private ExpressionNodeForArrayReference trimTrailingNulls( ExpressionNodeForArrayReference _arrayNode )
	{
		final List<ExpressionNode> args = _arrayNode.arguments();
		final int n = args.size();
		final int m;
		{
			int i = n - 1;
			ExpressionNode arg;
			while (i >= 0
					&& (arg = args.get( i )) instanceof ExpressionNodeForConstantValue
					&& null == ((ExpressionNodeForConstantValue) arg).value()) {
				i--;
			}
			m = i + 1;
		}
		if (m < n) {
			final ArrayDescriptor dim = _arrayNode.arrayDescriptor();
			if (dim.numberOfColumns() > 1) return _arrayNode.subArray( 0, dim.numberOfRows(), 0, m );
			if (dim.numberOfRows() > 1) return _arrayNode.subArray( 0, m, 0, dim.numberOfColumns() );
		}
		return _arrayNode;
	}

	private final Map<String, IndexerCompiler> indexers = New.map();
	private int nextIndexerId = 0;

	public IndexerCompiler getIndexerFor( ExpressionNodeForArrayReference _arrayNode ) throws CompilerException
	{
		final String name = _arrayNode.arrayDescriptor().name();
		IndexerCompiler idx = this.indexers.get( name );
		if (null == idx) {
			final String internalName = Integer.toString( this.nextIndexerId++ );
			idx = new IndexerCompiler( this, internalName, _arrayNode );
			idx.compile();
			this.indexers.put( name, idx );
		}
		return idx;
	}


	private final Map<String, LinearizerCompiler> linearizers = New.map();

	public LinearizerCompiler getLinearizerFor( int _rows, int _cols ) throws CompilerException
	{
		final String name = _rows + "," + _cols;
		LinearizerCompiler mtd = this.linearizers.get( name );
		if (null == mtd) {
			mtd = new LinearizerCompiler( this, _rows, _cols );
			mtd.compile();
			this.linearizers.put( name, mtd );
		}
		return mtd;
	}

}