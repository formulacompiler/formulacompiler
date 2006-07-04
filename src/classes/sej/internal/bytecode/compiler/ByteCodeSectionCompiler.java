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

import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.internal.model.CellModel;
import sej.internal.model.SectionModel;


final class ByteCodeSectionCompiler extends ByteCodeClassCompiler
{
	private final ByteCodeSectionCompiler parentSectionCompiler;
	private final Map<SectionModel, ByteCodeSectionCompiler> subSectionCompilers = new HashMap<SectionModel, ByteCodeSectionCompiler>();
	private final Map<CellModel, ByteCodeCellComputation> cellComputations = new HashMap<CellModel, ByteCodeCellComputation>();
	private final Map<Method, ByteCodeOutputDistributorCompiler> outputDistributors = new HashMap<Method, ByteCodeOutputDistributorCompiler>();
	private final SectionModel model;
	private final ByteCodeNumericType numericType;
	private final Type inputs;
	private final Type outputs;

	
	ByteCodeSectionCompiler(ByteCodeEngineCompiler _compiler, SectionModel _model)
	{
		super( _compiler, ByteCodeEngineCompiler.GEN_ROOT_NAME, false );
		this.model = _model;
		this.numericType = ByteCodeNumericType.typeFor( _compiler.getNumericType(), this );
		this.inputs = typeFor( inputClass() );
		this.outputs = typeFor( outputClass() );
		this.parentSectionCompiler = null;
	}

	ByteCodeSectionCompiler(ByteCodeSectionCompiler _parent, SectionModel _model)
	{
		super( _parent.engineCompiler(), _parent.engineCompiler().newSubClassName(), false );
		this.model = _model;
		this.numericType = _parent.numericType();
		this.inputs = typeFor( inputClass() );
		this.outputs = typeFor( outputClass() );
		this.parentSectionCompiler = _parent;
		_parent.subSectionCompilers.put( _model, this );
	}

	private Type typeFor( Class _inputClass )
	{
		return (null == _inputClass) ? Type.getType( Object.class ) : Type.getType( _inputClass );
	}


	ByteCodeSectionCompiler parentSectionCompiler()
	{
		return this.parentSectionCompiler;
	}

	ByteCodeSectionCompiler subSectionCompiler( SectionModel _section )
	{
		return this.subSectionCompilers.get( _section );
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

	
	private int getterId;

	String newGetterName()
	{
		return "get$" + (this.getterId++);
	}


	void beginCompilation() throws CompilerException
	{
		initializeClass( outputClass(), this.outputs, ByteCodeEngineCompiler.ENGINE_INTF );
		if (numericType().buildStaticMembers( cw() )) {
			buildStaticInitializer();
		}
		if (hasInputs()) buildInputMember();
		buildConstructorWithInputs();
		if (engineCompiler().canCache()) {
			buildReset();
		}
	}

	void compileAccessTo( ByteCodeClassCompiler _subCompiler )
	{
		// TODO Auto-generated method stub
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
		MethodVisitor mv = cw().visitMethod( Opcodes.ACC_PUBLIC, "reset", "()V", null, null );
		GeneratorAdapter ma = new GeneratorAdapter( mv, Opcodes.ACC_PUBLIC, "reset", "()V" );
		ma.visitCode();
		this.resetter = ma;
	}

	private void finalizeReset()
	{
		if (this.resetter != null) {
			GeneratorAdapter ma = this.resetter;
			ma.visitInsn( Opcodes.RETURN );
			ma.visitMaxs( 0, 0 );
			ma.visitEnd();
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


	private void buildInputMember()
	{
		if (!hasInputs()) throw new IllegalStateException();
		FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
				ByteCodeEngineCompiler.INPUTS_MEMBER_NAME, this.inputs.getDescriptor(), null, null );
		fv.visitEnd();
	}

	private void storeInputs( MethodVisitor _mv )
	{
		if (!hasInputs()) throw new IllegalStateException();
		_mv.visitFieldInsn( Opcodes.PUTFIELD, this.classInternalName(), ByteCodeEngineCompiler.INPUTS_MEMBER_NAME,
				this.inputs.getDescriptor() );
	}

	private void buildConstructorWithInputs() throws CompilerException
	{
		MethodVisitor mv = cw().visitMethod( 0, "<init>", "(" + this.inputs.getDescriptor() + ")V", null, null );
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel( start );

		// super( _inputs ); or super();
		callInheritedConstructor( mv, 1 );

		// this.inputs = _inputs;
		if (hasInputs()) {
			mv.visitVarInsn( Opcodes.ALOAD, 0 );
			mv.visitVarInsn( Opcodes.ALOAD, 1 );
			storeInputs( mv );
		}

		mv.visitInsn( Opcodes.RETURN );

		Label end = new Label();
		mv.visitLabel( end );
		mv.visitLocalVariable( "this", this.classDescriptor(), null, start, end, 0 );
		mv.visitLocalVariable( "_inputs", this.inputs.getDescriptor(), null, start, end, 1 );
		mv.visitMaxs( 2, 2 );
		mv.visitEnd();
	}

	private void callInheritedConstructor( MethodVisitor _mv, int _inputsVar ) throws CompilerException
	{
		try {
			if (outputClass().isInterface()) {
				_mv.visitVarInsn( Opcodes.ALOAD, 0 );
				_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V" );
			}
			else if (!callConstructorWithInputs( _mv, _inputsVar )) {
				outputClass().getConstructor(); // ensure it is here and accessible
				_mv.visitVarInsn( Opcodes.ALOAD, 0 );
				_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, this.outputs.getInternalName(), "<init>", "()V" );
			}
		}
		catch (NoSuchMethodException e) {
			throw new CompilerException.ConstructorMissing(
					"There is no default constructor and none with the input type as sole parameter.", e );
		}
	}

	private boolean callConstructorWithInputs( MethodVisitor _mv, int _inputsVar )
	{
		try {
			outputClass().getConstructor( inputClass() ); // ensure it is here and accessible
		}
		catch (NoSuchMethodException e) {
			return false;
		}

		_mv.visitVarInsn( Opcodes.ALOAD, 0 );
		if (0 <= _inputsVar) {
			_mv.visitVarInsn( Opcodes.ALOAD, _inputsVar );
		}
		else {
			_mv.visitInsn( Opcodes.ACONST_NULL );
		}
		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, this.outputs.getInternalName(), "<init>", "("
				+ this.inputs.getDescriptor() + ")V" );

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