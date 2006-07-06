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

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.expressions.ExpressionNode;
import sej.internal.model.CellModel;
import sej.internal.model.SectionModel;


class ByteCodeSectionCompiler extends ByteCodeClassCompiler
{
	private final Map<SectionModel, ByteCodeSubSectionCompiler> subSectionCompilers = new HashMap<SectionModel, ByteCodeSubSectionCompiler>();
	private final Map<CellModel, ByteCodeCellComputation> cellComputations = new HashMap<CellModel, ByteCodeCellComputation>();
	private final Map<Method, ByteCodeOutputDistributorCompiler> outputDistributors = new HashMap<Method, ByteCodeOutputDistributorCompiler>();
	private final SectionModel model;
	private final ByteCodeNumericType numericType;
	private final Type inputs;
	private final Type outputs;


	ByteCodeSectionCompiler(ByteCodeEngineCompiler _compiler, SectionModel _model, String _name)
	{
		super( _compiler, _name, false );
		this.model = _model;
		this.numericType = ByteCodeNumericType.typeFor( _compiler.getNumericType(), this );
		this.inputs = typeFor( inputClass() );
		this.outputs = typeFor( outputClass() );
	}

	ByteCodeSectionCompiler(ByteCodeEngineCompiler _compiler, SectionModel _model)
	{
		this( _compiler, _model, ByteCodeEngineCompiler.GEN_ROOT_NAME );
	}

	private Type typeFor( Class _inputClass )
	{
		return (null == _inputClass) ? Type.getType( Object.class ) : Type.getType( _inputClass );
	}


	ByteCodeSectionCompiler parentSectionCompiler()
	{
		return null;
	}

	ByteCodeSubSectionCompiler subSectionCompiler( SectionModel _section )
	{
		return this.subSectionCompilers.get( _section );
	}

	void addSubSectionCompiler( SectionModel _section, ByteCodeSubSectionCompiler _compiler )
	{
		this.subSectionCompilers.put( _section, _compiler );
	}

	ByteCodeCellComputation cellComputation( CellModel _cell )
	{
		return this.cellComputations.get( _cell );
	}

	void addCellComputation( CellModel _cell, ByteCodeCellComputation _compiler )
	{
		this.cellComputations.put( _cell, _compiler );
	}

	SectionModel model()
	{
		return this.model;
	}

	ByteCodeNumericType numericType()
	{
		return this.numericType;
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

	protected Type parentType()
	{
		return null;
	}

	boolean hasParent()
	{
		return (null != parentType());
	}


	private int getterId;

	String newGetterName()
	{
		return "get$" + (this.getterId++);
	}

	
	private boolean compilationStarted = false;

	void beginCompilation() throws CompilerException
	{
		if (this.compilationStarted) return;
		this.compilationStarted = true;
		
		initializeClass( outputClass(), this.outputs, ByteCodeEngineCompiler.COMPUTATION_INTF );
		if (numericType().buildStaticMembers( cw() )) {
			buildStaticInitializer();
		}
		if (hasParent()) buildParentMember();
		if (hasInputs()) buildInputMember();
		buildConstructorWithInputs();
		if (engineCompiler().canCache()) {
			buildReset();
		}
	}

	void compileAccessTo( ByteCodeSubSectionCompiler _sub ) throws CompilerException
	{
		newField( Opcodes.ACC_PRIVATE, _sub.getterName(), _sub.arrayDescriptor() );
		new ByteCodeSubSectionGetterCompiler( this, _sub ).compile();

		// In reset(), do:
		// $<section> = null;
		GeneratorAdapter r = resetter();
		r.loadThis();
		r.visitInsn( Opcodes.ACONST_NULL );
		r.putField( classType(), _sub.getterName(), _sub.arrayType() );
	}

	public void compileCallToGetterFor( GeneratorAdapter _mv, ByteCodeSubSectionCompiler _sub )
	{
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, classInternalName(), _sub.getterName(), _sub.getterDescriptor() );
	}


	public ByteCodeSectionNumericMethodCompiler compileExpr( ExpressionNode _node ) throws CompilerException
	{
		beginCompilation();
		ByteCodeHelperCompilerForSubExpr result = new ByteCodeHelperCompilerForSubExpr( this, _node );
		result.compile();
		return result;
	}

	void endCompilation()
	{
		finalizeOutputDistributors();
		finalizeReset();
		finalizeStaticInitializer();
		finalizeClass();
	}


	@Override
	protected void buildStaticInitializer()
	{
		super.buildStaticInitializer();
		numericType().compileStaticInitialization( initializer(), this.classType() );
	}

	@Override
	protected void finalizeStaticInitializer()
	{
		if (initializer() != null) {
			numericType().finalizeStaticInitialization( initializer(), this.classType() );
		}
		super.finalizeStaticInitializer();
	}


	private GeneratorAdapter resetter;

	private void buildReset()
	{
		this.resetter = newMethod( Opcodes.ACC_PUBLIC, "reset", "()V" );
	}

	private void finalizeReset()
	{
		if (this.resetter != null) {
			this.resetter.visitInsn( Opcodes.RETURN );
			endMethod( this.resetter );
			this.resetter = null;
		}
	}

	GeneratorAdapter resetter()
	{
		assert null != this.resetter : "Resetter is null";
		return this.resetter;
	}


	public ByteCodeOutputDistributorCompiler getOutputDistributorFor( Method _method )
	{
		ByteCodeOutputDistributorCompiler dist = this.outputDistributors.get( _method );
		if (dist == null) {
			dist = new ByteCodeOutputDistributorCompiler( this, _method );
			this.outputDistributors.put( _method, dist );
			dist.beginCompilation();
		}
		return dist;
	}


	private void finalizeOutputDistributors()
	{
		for (ByteCodeOutputDistributorCompiler dist : this.outputDistributors.values()) {
			dist.endCompilation();
		}
	}


	private void buildParentMember()
	{
		if (!hasParent()) throw new IllegalStateException();
		newField( Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, ByteCodeEngineCompiler.PARENT_MEMBER_NAME, parentType()
				.getDescriptor() );
	}

	private void buildInputMember()
	{
		if (!hasInputs()) throw new IllegalStateException();
		newField( Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, ByteCodeEngineCompiler.INPUTS_MEMBER_NAME, inputType()
				.getDescriptor() );
	}

	private void storeInputs( GeneratorAdapter _mv )
	{
		if (!hasInputs()) throw new IllegalStateException();
		_mv.putField( classType(), ByteCodeEngineCompiler.INPUTS_MEMBER_NAME, inputType() );
	}

	private void buildConstructorWithInputs() throws CompilerException
	{
		GeneratorAdapter mv = newMethod( 0, "<init>", "("
				+ this.inputs.getDescriptor() + (hasParent() ? parentType().getDescriptor() : "") + ")V" );

		// super( _inputs ); or super();
		callInheritedConstructor( mv, 1 );

		// this.parent = _parent;
		if (hasParent()) {
			mv.loadThis();
			mv.loadArg( 1 );
			mv.putField( classType(), ByteCodeEngineCompiler.PARENT_MEMBER_NAME, parentType() );
		}

		// this.inputs = _inputs;
		if (hasInputs()) {
			mv.loadThis();
			mv.loadArg( 0 );
			storeInputs( mv );
		}

		mv.visitInsn( Opcodes.RETURN );
		endMethod( mv );
	}

	private void callInheritedConstructor( GeneratorAdapter _mv, int _inputsVar ) throws CompilerException
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

	private boolean callConstructorWithInputs( GeneratorAdapter _mv, int _inputsVar )
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
	void collectClassNamesAndBytes( HashMap<String, byte[]> _result )
	{
		super.collectClassNamesAndBytes( _result );
		for (ByteCodeClassCompiler sub : this.subSectionCompilers.values()) {
			sub.collectClassNamesAndBytes( _result );
		}
	}

}