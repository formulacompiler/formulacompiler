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

import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

abstract class ClassCompiler
{
	private final ByteCodeEngineCompiler engineCompiler;
	private final boolean classPublic;
	private final String className;
	private final String classInternalName;
	private final String classDescriptor;
	private final Type classType;
	private final ClassWriter cw = new ClassWriter( true );


	public ClassCompiler(ByteCodeEngineCompiler _compiler, String _className, boolean _public)
	{
		super();
		this.engineCompiler = _compiler;
		this.classPublic = _public;
		this.className = _className;
		this.classInternalName = ByteCodeEngineCompiler.GEN_PACKAGE_PATH + this.className;
		this.classDescriptor = "L" + this.classInternalName + ";";
		this.classType = Type.getType( this.classDescriptor );
	}

	final ByteCodeEngineCompiler engineCompiler()
	{
		return this.engineCompiler;
	}

	final String classInternalName()
	{
		return this.classInternalName;
	}

	final String classDescriptor()
	{
		return this.classDescriptor;
	}

	final String className()
	{
		return this.className;
	}

	final Type classType()
	{
		return this.classType;
	}

	final ClassWriter cw()
	{
		return this.cw;
	}


	protected Type initializeClass( Class _parentClassOrInterface, Type _parentTypeOrInterface, Type _otherInterface )
	{
		Type parentType;
		String[] interfaces;
		if (_parentClassOrInterface == null) {
			parentType = Type.getType( Object.class );
			interfaces = new String[] { _otherInterface.getInternalName() };
		}
		else if (_parentClassOrInterface.isInterface()) {
			parentType = Type.getType( Object.class );
			interfaces = new String[] { _otherInterface.getInternalName(), _parentTypeOrInterface.getInternalName() };
		}
		else {
			parentType = _parentTypeOrInterface;
			interfaces = new String[] { _otherInterface.getInternalName() };
		}
		final int access = Opcodes.ACC_FINAL | (this.classPublic ? Opcodes.ACC_PUBLIC : 0);
		cw().visit( Opcodes.V1_4, access, classInternalName(), null, parentType.getInternalName(), interfaces );

		if (_parentClassOrInterface != null) {
			compileClassRef( _parentClassOrInterface, _parentTypeOrInterface );
		}

		cw().visitSource( null, null );

		return parentType;
	}

	protected void finalizeClass()
	{
		finalizeStaticInitializer();
		cw().visitEnd();
	}


	GeneratorAdapter newMethod( int _access, String _name, String _descriptor )
	{
		MethodVisitor mv = cw().visitMethod( _access, _name, _descriptor, null, null );
		GeneratorAdapter ma = new GeneratorAdapter( mv, _access, _name, _descriptor );
		ma.visitCode();
		return ma;
	}

	void endMethod( GeneratorAdapter _mv )
	{
		_mv.endMethod();
		_mv.visitEnd();
	}

	void newField( int _access, String _name, String _descriptor )
	{
		cw().visitField( _access, _name, _descriptor, null, null ).visitEnd();
	}


	public void compileClassRef( Class _class )
	{
		compileClassRef( _class, Type.getType( _class ));
	}

	public void compileClassRef( Class _class, Type _type )
	{
		if (_class.isMemberClass()) {
			final Class outerClass = _class.getDeclaringClass();
			final Type outerType = Type.getType( outerClass );
			final String innerIntName = _type.getInternalName();
			final String outerIntName = outerType.getInternalName();
			final String innerName = innerIntName.substring( outerIntName.length() + 1 );
			cw().visitInnerClass( innerIntName, outerIntName, innerName, _class.getModifiers() );
		}
	}

	
	private GeneratorAdapter initializer;

	protected final GeneratorAdapter initializer()
	{
		if (this.initializer == null) {
			buildStaticInitializer();
		}
		return this.initializer;
	}

	private void buildStaticInitializer()
	{
		this.initializer = newMethod( Opcodes.ACC_STATIC, "<clinit>", "()V" );
	}

	private void finalizeStaticInitializer()
	{
		if (this.initializer != null) {
			GeneratorAdapter ma = this.initializer;
			ma.visitInsn( Opcodes.RETURN );
			endMethod( ma );
			this.initializer = null;
		}
	}


	final byte[] getClassBytes()
	{
		return cw().toByteArray();
	}


	void collectClassNamesAndBytes( Map<String, byte[]> _result )
	{
		_result.put( classInternalName().replace( '/', '.' ), getClassBytes() );
	}


	final static String getPackageOf( String _internalName )
	{
		int p = _internalName.lastIndexOf( '/' );
		if (0 <= p) return _internalName.substring( 0, p + 1 );
		else return "";
	}

}
