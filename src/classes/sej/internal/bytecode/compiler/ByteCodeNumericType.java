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
import java.math.BigDecimal;
import java.util.Date;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.NumericTypeImpl;
import sej.runtime.ScaledLong;


abstract class ByteCodeNumericType
{
	protected static final Type BOOLEAN_CLASS = Type.getType( Boolean.class );
	protected static final Type BOOLEAN_TYPE = Type.BOOLEAN_TYPE;
	protected static final String BOOL2Z = "(" + BOOLEAN_CLASS.getDescriptor() + ")" + BOOLEAN_TYPE.getDescriptor();
	protected final static Type LONG_CLASS = Type.getType( Long.class );
	protected final static Type LONG_TYPE = Type.LONG_TYPE;
	protected static final String J2LONG = "(" + LONG_TYPE.getDescriptor() + ")" + LONG_CLASS.getDescriptor();
	protected static final String LONG2J = "(" + LONG_CLASS.getDescriptor() + ")" + LONG_TYPE.getDescriptor();
	protected final static Type NUMBER_CLASS = Type.getType( Number.class );
	protected final static String N = NUMBER_CLASS.getDescriptor();

	private final NumericTypeImpl num;
	private final ByteCodeSectionCompiler compiler;


	ByteCodeNumericType(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super();
		this.num = (NumericTypeImpl) _type;
		this.compiler = _compiler;
	}

	static ByteCodeNumericType typeFor( NumericType _type, ByteCodeSectionCompiler _compiler )
	{
		if (Double.TYPE == _type.getValueType()) {
			return new ByteCodeNumericType_Double( _type, _compiler );
		}
		else if (Long.TYPE == _type.getValueType()) {
			return new ByteCodeNumericType_ScaledLong( _type, _compiler );
		}
		else if (BigDecimal.class == _type.getValueType()) {
			return new ByteCodeNumericType_BigDecimal( _type, _compiler );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for byte code compilation." );
		}
	}


	NumericTypeImpl numericType()
	{
		return this.num;
	}

	ByteCodeSectionCompiler compiler()
	{
		return this.compiler;
	}

	abstract Type type();
	abstract int returnOpcode();
	abstract Type runtimeType();


	String descriptor()
	{
		return type().getDescriptor();
	}


	void buildStaticMembers()
	{
		// Overridable stub
	}

	protected abstract String roundMethodSignature();


	protected void compileRuntimeMethod( MethodVisitor _mv, String _methodName, String _methodSig )
	{
		_mv.visitMethodInsn( Opcodes.INVOKESTATIC, runtimeType().getInternalName(), _methodName, _methodSig );
	}

	void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		switch (_operator) {

			case NOOP:
				return;

			default:
				throw new CompilerException.UnsupportedOperator( "The operator '"
						+ _operator.getSymbol() + "' is not supported here." );
		}
	}

	void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws CompilerException
	{
		throw new CompilerException.UnsupportedDataType( "The data type "
				+ _constantValue.getClass().getName() + " is not supported for constant " + _constantValue.toString() );
	}

	abstract void compileZero( GeneratorAdapter _mv );

	abstract void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode );

	void compileStdFunction( GeneratorAdapter _mv, Function _function, String _argumentDescriptor )
	{
		compileRuntimeMethod( _mv, "std" + _function.getName(), "(" + _argumentDescriptor + ")" + descriptor() );
	}

	void compileRound( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "round", roundMethodSignature() );
	}


	public final void compileToNum( GeneratorAdapter _mv, Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();

		if (java.util.Date.class == returnType) {
			compileDateToNum( _mv );
		}

		else if (Boolean.TYPE == returnType) {
			compileBooleanToNum( _mv );
		}
		else if (Boolean.class == returnType) {
			compileRuntimeMethod( _mv, "unboxBoolean", BOOL2Z );
			compileBooleanToNum( _mv );
		}

		else {
			if (returnType == Long.TYPE || returnType == Long.class) {
				final ScaledLong scale = scaleOf( _method );
				if (scale != null && scale.value() != 0) {
					if (returnType == Long.class) {
						compileRuntimeMethod( _mv, "unboxLong", LONG2J );
					}
					if (!compileToNum( _mv, scale )) {
						throw new CompilerException.UnsupportedDataType( "Scaled long return type of input '"
								+ _method + "' is not supported" );
					}
					return;
				}
			}
			if (!compileToNum( _mv, returnType )) {
				throw new CompilerException.UnsupportedDataType( "Return type of input '" + _method + "' is not supported" );
			}
		}
	}

	protected abstract boolean compileToNum( GeneratorAdapter _mv, Class _returnType );

	protected boolean compileToNum( GeneratorAdapter _mv, ScaledLong _scale )
	{
		return false;
	}


	public final void compileReturnFromNum( GeneratorAdapter _mv, Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();

		if (Date.class == returnType) {
			compileDateFromNum( _mv );
			_mv.visitInsn( Opcodes.ARETURN );
		}
		else if (Boolean.TYPE == returnType) {
			compileBooleanFromNum( _mv );
			_mv.visitInsn( Opcodes.IRETURN );
		}
		else if (Boolean.class == returnType) {
			compileBooleanFromNum( _mv );
			returnBoxedType( _mv, returnType, Boolean.TYPE, Boolean.class );
		}

		else {
			if (returnType == Long.TYPE || returnType == Long.class) {
				final ScaledLong scale = scaleOf( _method );
				if (scale != null && scale.value() != 0) {
					if (compileFromNum( _mv, scale )) {
						if (returnType == Long.class) {
							_mv.visitMethodInsn( Opcodes.INVOKESTATIC, LONG_CLASS.getInternalName(), "valueOf", J2LONG );
							_mv.visitInsn( Opcodes.ARETURN );
						}
						else {
							_mv.visitInsn( Opcodes.LRETURN );
						}
					}
					else {
						throw new CompilerException.UnsupportedDataType( "Scaled long return type of output '"
								+ _method + "' is not supported" );
					}
					return;
				}
			}
			if (!compileReturnFromNum( _mv, returnType )) {
				throw new CompilerException.UnsupportedDataType( "Return type of output '" + _method + "' is not supported" );
			}
		}
	}

	protected abstract boolean compileReturnFromNum( GeneratorAdapter _mv, Class _returnType );

	protected boolean compileFromNum( GeneratorAdapter _mv, ScaledLong _scale )
	{
		return false;
	}

	protected final boolean returnBoxedType( MethodVisitor _mv, Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		if (_returnType == _boxed) {
			convertUnboxed( _mv, _conversionOpcodes );
			final Type unboxedType = Type.getType( _unboxed );
			final Type boxedType = Type.getType( _boxed );
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, boxedType.getInternalName(), "valueOf", "("
					+ unboxedType.getDescriptor() + ")" + boxedType.getDescriptor() );
			_mv.visitInsn( Opcodes.ARETURN );
			return true;
		}
		else {
			return false;
		}
	}

	protected boolean returnDualType( MethodVisitor _mv, Class _returnType, Class _unboxed, Class _boxed,
			int _returnOpcode, int... _conversionOpcodes )
	{
		if (_returnType == _unboxed) {
			convertUnboxed( _mv, _conversionOpcodes );
			_mv.visitInsn( _returnOpcode );
			return true;
		}
		else {
			return returnBoxedType( _mv, _returnType, _unboxed, _boxed, _conversionOpcodes );
		}
	}

	protected final void convertUnboxed( MethodVisitor _mv, int... _conversionOpcodes )
	{
		for (int conv : _conversionOpcodes) {
			_mv.visitInsn( conv );
		}
	}


	private ScaledLong scaleOf( Method _method )
	{
		final ScaledLong typeScale = _method.getDeclaringClass().getAnnotation( ScaledLong.class );
		final ScaledLong mtdScale = _method.getAnnotation( ScaledLong.class );
		final ScaledLong scale = (mtdScale != null) ? mtdScale : typeScale;
		return scale;
	}


	void compileDateToNum( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "dateToNum", "(Ljava/util/Date;)" + descriptor() );
	}

	void compileDateFromNum( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "dateFromNum", "(" + descriptor() + ")Ljava/util/Date;" );
	}

	void compileBooleanToNum( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "booleanToNum", "(Z)" + descriptor() );
	}

	void compileBooleanFromNum( MethodVisitor _mv )
	{
		compileRuntimeMethod( _mv, "booleanFromNum", "(" + descriptor() + ")Z" );
	}

	abstract void compileIntFromNum( GeneratorAdapter _mv );

}
