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
import java.lang.reflect.Modifier;
import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CallFrame;
import sej.CompilerException;
import sej.internal.expressions.DataType;
import sej.internal.expressions.ExpressionNode;


abstract class MethodCompiler
{
	private final SectionCompiler section;
	private final String methodName;
	private final String methodDescriptor;
	private final GeneratorAdapter mv;

	private ExpressionCompilerForStrings stringCompiler;
	private ExpressionCompilerForNumbers numericCompiler;


	MethodCompiler(SectionCompiler _section, int _access, String _methodName, String _descriptor)
	{
		super();
		this.section = _section;
		this.methodName = _methodName;
		this.methodDescriptor = _descriptor;
		this.mv = section().newMethod( _access | Opcodes.ACC_FINAL, _methodName, _descriptor );
	}


	final SectionCompiler section()
	{
		return this.section;
	}

	final ExpressionCompiler expressionCompiler( DataType _type )
	{
		switch (_type) {
			case STRING:
				return stringCompiler();
			default:
				return numericCompiler();
		}
	}

	final ExpressionCompilerForStrings stringCompiler()
	{
		if (null == this.stringCompiler) this.stringCompiler = new ExpressionCompilerForStrings( this );
		return this.stringCompiler;
	}

	final ExpressionCompilerForNumbers numericCompiler()
	{
		if (null == this.numericCompiler)
			this.numericCompiler = ExpressionCompilerForNumbers.compilerFor( this, section().engineCompiler()
					.getNumericType() );
		return this.numericCompiler;
	}

	final String methodName()
	{
		return this.methodName;
	}

	final String methodDescriptor()
	{
		return this.methodDescriptor;
	}

	final ClassWriter cw()
	{
		return section().cw();
	}

	final GeneratorAdapter mv()
	{
		return this.mv;
	}


	final void compile() throws CompilerException
	{
		beginCompilation();
		compileBody();
		endCompilation();
	}


	protected void beginCompilation()
	{
		mv().visitCode();
	}


	protected void endCompilation()
	{
		section().endMethod( mv() );
	}


	protected abstract void compileBody() throws CompilerException;


	final void compileInputGetterCall( CallFrame _callChainToCall ) throws CompilerException
	{
		final CallFrame[] frames = _callChainToCall.getFrames();
		final boolean isStatic = Modifier.isStatic( frames[ 0 ].getMethod().getModifiers() );

		if (!isStatic) {
			mv().loadThis();
			mv().getField( section().classType(), ByteCodeEngineCompiler.INPUTS_MEMBER_NAME, section().inputType() );
		}

		Class contextClass = section().inputClass();
		for (CallFrame frame : frames) {
			final Method method = frame.getMethod();
			final Object[] args = frame.getArgs();
			if (null != args) {
				final Class[] types = method.getParameterTypes();
				for (int i = 0; i < args.length; i++) {
					final Object arg = args[ i ];
					final Class type = types[ i ];
					pushConstParam( type, arg );
				}
			}
			int opcode = Opcodes.INVOKEVIRTUAL;
			if (contextClass.isInterface()) opcode = Opcodes.INVOKEINTERFACE;
			else if (isStatic) opcode = Opcodes.INVOKESTATIC;

			mv().visitMethodInsn( opcode, Type.getType( contextClass ).getInternalName(), method.getName(),
					Type.getMethodDescriptor( method ) );

			contextClass = method.getReturnType();
		}
	}

	private final void pushConstParam( Class _type, Object _constantValue ) throws CompilerException
	{
		if (null == _constantValue) {
			mv().visitInsn( Opcodes.ACONST_NULL );
		}

		else if (_type == Byte.TYPE) {
			mv().push( ((Number) _constantValue).byteValue() );
		}
		else if (_type == Byte.class) {
			mv().push( ((Number) _constantValue).byteValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Byte", "(B)Ljava/lang/Byte;", Byte.TYPE );
		}

		else if (_type == Short.TYPE) {
			mv().push( ((Number) _constantValue).shortValue() );
		}
		else if (_type == Short.class) {
			mv().push( ((Number) _constantValue).shortValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Short", "(S)Ljava/lang/Short;", Short.TYPE );
		}

		else if (_type == Integer.TYPE) {
			mv().push( ((Number) _constantValue).intValue() );
		}
		else if (_type == Integer.class) {
			mv().push( ((Number) _constantValue).intValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Integer", "(I)Ljava/lang/Integer;", Integer.TYPE );
		}

		else if (_type == Long.TYPE) {
			mv().push( ((Number) _constantValue).longValue() );
		}
		else if (_type == Long.class) {
			mv().push( ((Number) _constantValue).longValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Long", "(J)Ljava/lang/Long;", Long.TYPE );
		}

		else if (_type == Double.TYPE) {
			mv().push( ((Number) _constantValue).doubleValue() );
		}
		else if (_type == Double.class) {
			mv().push( ((Number) _constantValue).doubleValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Double", "(D)Ljava/lang/Double;", Double.TYPE );
		}

		else if (_type == Float.TYPE) {
			mv().push( ((Number) _constantValue).floatValue() );
		}
		else if (_type == Float.class) {
			mv().push( ((Number) _constantValue).floatValue() );
			ByteCodeEngineCompiler.compileValueOf( mv(), "java/lang/Float", "(F)Ljava/lang/Float;", Float.TYPE );
		}

		else if (_type == Character.TYPE) {
			mv().push( ((Character) _constantValue).charValue() );
		}
		else if (_type == Character.class) {
			mv().push( ((Character) _constantValue).charValue() );
			ByteCodeEngineCompiler
					.compileValueOf( mv(), "java/lang/Character", "(C)Ljava/lang/Character;", Character.TYPE );
		}

		else if (_type == Boolean.TYPE) {
			mv().push( ((Boolean) _constantValue).booleanValue() );
		}
		else if (_type == Boolean.class) {
			mv().push( ((Boolean) _constantValue).booleanValue() );
			// I know JRE 1.4 handles this:
			mv().visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;" );
		}

		else if (_type == String.class) {
			mv().visitLdcInsn( _constantValue );
		}

		else if (_type == Date.class) {
			mv().visitLdcInsn( _constantValue );
		}

		else if (_constantValue instanceof Enum) {
			final Enum enumValue = (Enum) _constantValue;
			final Type enumType = Type.getType( enumValue.getDeclaringClass() );
			final Type instanceType = Type.getType( enumValue.getClass() );
			mv().getStatic( enumType, enumValue.name(), instanceType );
		}

		else {
			throw new CompilerException.UnsupportedDataType( "The data type '"
					+ _type + "' is not supported as an input method parameter." );
		}
	}
	
	
	protected final void compileExpression( ExpressionNode _node ) throws CompilerException
	{
		expressionCompiler( _node.getDataType() ).compile( _node );
	}

	
}
