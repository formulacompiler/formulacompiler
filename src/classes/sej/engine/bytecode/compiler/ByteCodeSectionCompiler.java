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
package sej.engine.bytecode.compiler;

import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.ModelError;
import sej.engine.compiler.model.CellModel;
import sej.engine.compiler.model.SectionModel;


final class ByteCodeSectionCompiler
{
	private final ByteCodeCompiler modelCompiler;
	private final ByteCodeSectionCompiler parentSectionCompiler;
	private final Map<SectionModel, ByteCodeSectionCompiler> subSectionCompilers = new HashMap<SectionModel, ByteCodeSectionCompiler>();
	private final Map<CellModel, ByteCodeCellComputation> cellComputations = new HashMap<CellModel, ByteCodeCellComputation>();
	private final ClassWriter cw = new ClassWriter( true );
	private final SectionModel model;
	private final ByteCodeNumericType numericType;

	private int getterId;

	boolean hasInputs;
	Type inputs;
	Type outputs;
	Type engine;


	ByteCodeSectionCompiler(ByteCodeCompiler _compiler, SectionModel _model)
	{
		super();
		this.modelCompiler = _compiler;
		this.parentSectionCompiler = null;
		this.model = _model;
		this.numericType = ByteCodeNumericType.typeFor( _compiler.getNumericType(), this );
		initialize();
	}

	ByteCodeSectionCompiler(ByteCodeSectionCompiler _parent, SectionModel _model)
	{
		super();
		if (null == _parent) throw new IllegalArgumentException();
		this.modelCompiler = _parent.getModelCompiler();
		this.parentSectionCompiler = _parent;
		this.model = _model;
		this.numericType = _parent.getNumericType();
		_parent.subSectionCompilers.put( _model, this );
		initialize();
	}

	private void initialize()
	{
		this.inputs = (null == getInputs()) ? Type.getType( Object.class ) : Type.getType( getInputs() );
		this.outputs = Type.getType( getOutputs() );
		this.engine = Type.getType( "L" + getPackageOf( this.outputs.getInternalName() ) + "$SEJEngine;" );
	}


	private String getPackageOf( String _internalName )
	{
		int p = _internalName.lastIndexOf( '/' );
		if (0 <= p) return _internalName.substring( 0, p + 1 );
		else return "";
	}

	ByteCodeCompiler getModelCompiler()
	{
		return this.modelCompiler;
	}

	public ByteCodeNumericType getNumericType()
	{
		return this.numericType;
	}

	ByteCodeSectionCompiler getParentSectionCompiler()
	{
		return this.parentSectionCompiler;
	}

	ByteCodeSectionCompiler getSubSectionCompiler( SectionModel _section )
	{
		return this.subSectionCompilers.get( _section );
	}

	ByteCodeCellComputation getCellComputation( CellModel _cell )
	{
		return this.cellComputations.get( _cell );
	}

	void addCellComputation( CellModel _cell, ByteCodeCellComputation _compiler )
	{
		this.cellComputations.put( _cell, _compiler );
	}

	Class getInputs()
	{
		return getModelCompiler().getInputs();
	}

	boolean hasInputs()
	{
		return (null != getInputs());
	}

	Class getOutputs()
	{
		return getModelCompiler().getOutputs();
	}

	ClassWriter cw()
	{
		return this.cw;
	}

	SectionModel getModel()
	{
		return this.model;
	}

	public String getNewGetterName()
	{
		return "get$" + (this.getterId++);
	}


	void beginCompilation() throws ModelError
	{
		initializeClass();
		if (getNumericType().buildStaticMembers( cw() )) {
			buildStaticInitializer();
		}
		if (hasInputs()) buildInputMember();
		buildAnonymousConstructor();
		buildConstructorWithInputs();
		buildNewEngine();
		if (getModelCompiler().canCache()) {
			buildReset();
		}
	}

	void compileAccessTo( ByteCodeSectionCompiler _subCompiler )
	{
		// TODO Auto-generated method stub
	}

	void endCompilation()
	{
		finalizeReset();
		finalizeStaticInitializer();
		finalizeClass();
	}

	byte[] getClassBytes()
	{
		return cw().toByteArray();
	}


	private void initializeClass()
	{
		String parentClass;
		String[] interfaces;
		if (getOutputs().isInterface()) {
			parentClass = "java/lang/Object";
			interfaces = new String[] { ByteCodeCompiler.ENGINE_INTF.getInternalName(), this.outputs.getInternalName() };
		}
		else {
			parentClass = this.outputs.getInternalName();
			interfaces = new String[] { ByteCodeCompiler.ENGINE_INTF.getInternalName() };
		}
		cw().visit( Opcodes.V1_4, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, this.engine.getInternalName(), null,
				parentClass, interfaces );
		cw().visitSource( null, null );
	}


	private GeneratorAdapter initializer;

	private void buildStaticInitializer()
	{
		MethodVisitor mv = cw().visitMethod( Opcodes.ACC_STATIC, "<clinit>", "()V", null, null );
		GeneratorAdapter ma = new GeneratorAdapter( mv, Opcodes.ACC_STATIC, "<clinit>", "()V" );
		ma.visitCode();
		getNumericType().compileStaticInitialization( ma, this.engine );
		this.initializer = ma;
	}

	private void finalizeStaticInitializer()
	{
		if (this.initializer != null) {
			GeneratorAdapter ma = this.initializer;
			getNumericType().finalizeStaticInitialization( ma, this.engine );
			ma.visitInsn( Opcodes.RETURN );
			ma.visitMaxs( 0, 0 );
			ma.visitEnd();
			this.initializer = null;
		}
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
	
	GeneratorAdapter getResetter() {
		assert null != this.resetter : "Resetter is null";
		return this.resetter;
	}

	
	private void buildInputMember()
	{
		if (!hasInputs()) throw new IllegalStateException();
		FieldVisitor fv = cw().visitField( Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, ByteCodeCompiler.INPUTS_MEMBER_NAME,
				this.inputs.getDescriptor(), null, null );
		fv.visitEnd();
	}

	private void storeInputs( MethodVisitor _mv )
	{
		if (!hasInputs()) throw new IllegalStateException();
		_mv.visitFieldInsn( Opcodes.PUTFIELD, this.engine.getInternalName(), ByteCodeCompiler.INPUTS_MEMBER_NAME,
				this.inputs.getDescriptor() );
	}

	private void buildAnonymousConstructor() throws ModelError
	{
		MethodVisitor mv = cw().visitMethod( Opcodes.ACC_PUBLIC, "<init>", "()V", null, null );
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel( start );

		// super( null ); or super();
		callInheritedConstructor( mv, -1 );

		// this.inputs = null;
		if (hasInputs()) {
			mv.visitVarInsn( Opcodes.ALOAD, 0 );
			mv.visitInsn( Opcodes.ACONST_NULL );
			storeInputs( mv );
		}

		mv.visitInsn( Opcodes.RETURN );

		Label end = new Label();
		mv.visitLabel( end );
		mv.visitLocalVariable( "this", this.engine.getDescriptor(), null, start, end, 0 );
		mv.visitMaxs( 2, 1 );
		mv.visitEnd();
	}

	private void buildConstructorWithInputs() throws ModelError
	{
		MethodVisitor mv = cw().visitMethod( Opcodes.ACC_PRIVATE, "<init>", "(" + this.inputs.getDescriptor() + ")V",
				null, null );
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
		mv.visitLocalVariable( "this", this.engine.getDescriptor(), null, start, end, 0 );
		mv.visitLocalVariable( "_inputs", this.inputs.getDescriptor(), null, start, end, 1 );
		mv.visitMaxs( 2, 2 );
		mv.visitEnd();
	}

	private void callInheritedConstructor( MethodVisitor _mv, int _inputsVar ) throws ModelError
	{
		try {
			if (getOutputs().isInterface()) {
				_mv.visitVarInsn( Opcodes.ALOAD, 0 );
				_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V" );
			}
			else if (!callConstructorWithInputs( _mv, _inputsVar )) {
				getOutputs().getConstructor(); // ensure it is here and accessible
				_mv.visitVarInsn( Opcodes.ALOAD, 0 );
				_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, this.outputs.getInternalName(), "<init>", "()V" );
			}
		}
		catch (NoSuchMethodException e) {
			throw new ModelError.ConstructorMissing(
					"There is no default constructor and none with the input type as sole parameter.", e );
		}
	}

	private boolean callConstructorWithInputs( MethodVisitor _mv, int _inputsVar )
	{
		try {
			getOutputs().getConstructor( getInputs() ); // ensure it is here and accessible
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

	private void buildNewEngine()
	{
		MethodVisitor mv = cw().visitMethod( Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, "newComputation",
				"(Ljava/lang/Object;)Ljava/lang/Object;", null, null );
		mv.visitCode();
		Label start = new Label();
		mv.visitLabel( start );

		// if (null == _inputs) throw new IllegalArgumentException();
		if (hasInputs()) {
			mv.visitVarInsn( Opcodes.ALOAD, 1 );
			Label l2 = new Label();
			mv.visitJumpInsn( Opcodes.IFNONNULL, l2 );
			mv.visitTypeInsn( Opcodes.NEW, "java/lang/IllegalArgumentException" );
			mv.visitInsn( Opcodes.DUP );
			mv.visitMethodInsn( Opcodes.INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>", "()V" );
			mv.visitInsn( Opcodes.ATHROW );
			mv.visitLabel( l2 );
		}

		// return new GeneratedEngine( (Inputs) _inputs );
		mv.visitTypeInsn( Opcodes.NEW, this.engine.getInternalName() );
		mv.visitInsn( Opcodes.DUP );
		mv.visitVarInsn( Opcodes.ALOAD, 1 );
		mv.visitTypeInsn( Opcodes.CHECKCAST, this.inputs.getInternalName() );
		mv.visitMethodInsn( Opcodes.INVOKESPECIAL, this.engine.getInternalName(), "<init>", "("
				+ this.inputs.getDescriptor() + ")V" );
		mv.visitInsn( Opcodes.ARETURN );

		Label end = new Label();
		mv.visitLabel( end );
		mv.visitLocalVariable( "this", this.engine.getDescriptor(), null, start, end, 0 );
		mv.visitLocalVariable( "_inputs", "Ljava/lang/Object;", null, start, end, 1 );
		mv.visitMaxs( 3, 2 );
		mv.visitEnd();
	}

	private void finalizeClass()
	{
		cw().visitEnd();
	}

}