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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.NumericTypeImpl;
import sej.internal.runtime.RuntimeDouble_v1;
import sej.internal.runtime.RuntimeLong_v1;
import sej.runtime.ScaledLong;
import sej.runtime.ScaledLongSupport;

final class ByteCodeNumericType_ScaledLong extends ByteCodeNumericType
{
	private static final Type DOUBLE_CLASS = Type.getType( Double.class );
	private static final Type FLOAT_CLASS = Type.getType( Float.class );
	private static final Type RUNTIME_TYPE = Type.getType( RuntimeLong_v1.class );
	private static final Type RUNTIME_CONTEXT_TYPE = Type.getType( RuntimeLong_v1.Context.class );
	private static final String RUNTIME_CONTEXT_NAME = "runtimeContext";
	private static final String JJ_J = "(JJ)J";
	private static final String JJx_J = "(JJ" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String JIx_J = "(JI" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String Dtx_J = "(Ljava/util/Date;" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String Jx_Dt = "(J" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")Ljava/util/Date;";
	private static final String Zx_J = "(Z" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String Dx_J = "(D" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String Jx_D = "(J" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")D";

	private final NumericType num;
	private final long one;

	ByteCodeNumericType_ScaledLong(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super( _type, _compiler );
		this.num = _type;
		this.one = ((NumericTypeImpl.AbstractLongType) _type).one();
	}


	@Override
	Type getType()
	{
		return Type.LONG_TYPE;
	}


	@Override
	int getReturnOpcode()
	{
		return Opcodes.LRETURN;
	}

	@Override
	Type getRuntimeType()
	{
		return RUNTIME_TYPE;
	}

	private int getScale()
	{
		return getNumericType().getScale();
	}

	private boolean isScaled()
	{
		return getScale() > 0;
	}


	@Override
	boolean buildStaticMembers( ClassWriter _cw )
	{
		FieldVisitor fv = _cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
				RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_TYPE.getDescriptor(), null, null );
		fv.visitEnd();
		return true;
	}


	@Override
	void compileStaticInitialization( GeneratorAdapter _mv, Type _engineType )
	{
		_mv.visitTypeInsn( Opcodes.NEW, RUNTIME_CONTEXT_TYPE.getInternalName() );
		_mv.visitInsn( Opcodes.DUP );
		_mv.push( getScale() );
		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, RUNTIME_CONTEXT_TYPE.getInternalName(), "<init>", "(I)V" );
		_mv.visitFieldInsn( Opcodes.PUTSTATIC, _engineType.getInternalName(), RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_TYPE
				.getDescriptor() );
	}


	@Override
	void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		switch (_operator) {

			case PLUS:
				_mv.visitInsn( Opcodes.LADD );
				break;

			case MINUS:
				if (1 == _numberOfArguments) {
					_mv.visitInsn( Opcodes.LNEG );
				}
				else {
					_mv.visitInsn( Opcodes.LSUB );
				}
				break;

			case TIMES:
				_mv.visitInsn( Opcodes.LMUL );
				if (isScaled()) {
					_mv.push( this.one );
					_mv.visitInsn( Opcodes.LDIV );
				}
				break;

			case DIV:
				// TODO Make scaled long div more efficient
				if (isScaled()) {
					_mv.swap( getType(), getType() );
					_mv.push( this.one );
					_mv.visitInsn( Opcodes.LMUL );
					_mv.swap( getType(), getType() );
				}
				_mv.visitInsn( Opcodes.LDIV );
				break;

			case PERCENT:
				_mv.visitLdcInsn( 100L );
				_mv.visitInsn( Opcodes.LDIV );
				break;

			case EXP:
				compileRuntimeMethodWithContext( _mv, "pow", JJx_J );
				break;

			case MIN:
				compileRuntimeMethod( _mv, "min", JJ_J );
				break;

			case MAX:
				compileRuntimeMethod( _mv, "max", JJ_J );
				break;

			default:
				super.compile( _mv, _operator, _numberOfArguments );
		}
	}


	@Override
	void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws CompilerException
	{
		if (null == _constantValue) {
			_mv.visitInsn( Opcodes.LCONST_0 );
		}
		else if (_constantValue instanceof Long) {
			long val = ((Long) _constantValue).longValue();
			_mv.push( val );
		}
		else if (_constantValue instanceof Number) {
			long val = (Long) this.num.valueOf( (Number) _constantValue );
			_mv.push( val );
		}
		else if (_constantValue instanceof Boolean) {
			long val = ((Boolean) _constantValue) ? this.num.getOne().longValue() : 0;
			_mv.push( val );
		}
		else if (_constantValue instanceof Date) {
			Date date = (Date) _constantValue;
			// TODO Native scaled long implementation of dateToExcel?
			long val = this.num.valueOf( RuntimeDouble_v1.dateToNum( date ) ).longValue();
			_mv.push( val );
		}
		else {
			super.compileConst( _mv, _constantValue );
		}
	}


	@Override
	void compileZero( GeneratorAdapter _mv )
	{
		_mv.push( 0L );
	}


	@Override
	void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode )
	{
		_mv.visitInsn( Opcodes.LCMP );
	}


	@Override
	void compileStdFunction( GeneratorAdapter _mv, Function _function, String _argumentDescriptor )
	{
		compileRuntimeMethodWithContext( _mv, "std" + _function.getName(), "("
				+ _argumentDescriptor + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")" + getDescriptor() );
	}


	@Override
	void compileRound( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "round", getRoundMethodSignature() );
	}


	private void compileRuntimeMethodWithContext( MethodVisitor _mv, String _methodName, String _methodSig )
	{
		_mv.visitFieldInsn( Opcodes.GETSTATIC, getCompiler().classInternalName(), RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_TYPE.getDescriptor() );
		compileRuntimeMethod( _mv, _methodName, _methodSig );
	}


	@Override
	protected String getRoundMethodSignature()
	{
		return JIx_J;
	}


	@Override
	void compileDateToNum( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "dateToNum", Dtx_J );
	}

	@Override
	void compileDateFromNum( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "dateFromNum", Jx_Dt );
	}

	@Override
	void compileBooleanToNum( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "booleanToNum", Zx_J );
	}


	@Override
	protected boolean compileToNum( GeneratorAdapter _mv, Class _returnType )
	{
		if (_returnType == Long.TYPE) {
			scaleUp( _mv );
		}

		else if (_returnType == Integer.TYPE) {
			_mv.visitInsn( Opcodes.I2L );
			scaleUp( _mv );
		}

		else if (_returnType == Short.TYPE) {
			_mv.visitInsn( Opcodes.I2L );
			scaleUp( _mv );
		}
		else if (_returnType == Byte.TYPE) {
			_mv.visitInsn( Opcodes.I2L );
			scaleUp( _mv );
		}

		else if (_returnType == Double.TYPE) {
			if (getScale() == 0) {
				_mv.visitInsn( Opcodes.D2L );
			}
			else {
				compileRuntimeMethodWithContext( _mv, "fromDouble", Dx_J );
			}
		}
		else if (_returnType == Double.class) {
			compileRuntimeMethodWithContext( _mv, "fromBoxedDouble", "(" + N + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J" );
		}

		else if (_returnType == Float.TYPE) {
			if (getScale() == 0) {
				_mv.visitInsn( Opcodes.F2L );
			}
			else {
				_mv.visitInsn( Opcodes.F2D );
				compileRuntimeMethodWithContext( _mv, "fromDouble", Dx_J );
			}
		}
		else if (_returnType == Float.class) {
			compileRuntimeMethodWithContext( _mv, "fromBoxedDouble", "(" + N + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J" );
		}

		else if (_returnType == BigDecimal.class) {
			compileRuntimeMethodWithContext( _mv, "fromBigDecimal", "("
					+ ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getDescriptor() + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J" );
		}

		else if (Number.class.isAssignableFrom( _returnType )) {
			compileRuntimeMethodWithContext( _mv, "fromNumber", "(" + N + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J" );
		}

		else {
			return false;
		}
		return true;
	}

	private void scaleUp( GeneratorAdapter _mv )
	{
		if (getScale() > 0) {
			_mv.push( this.one );
			_mv.visitInsn( Opcodes.LMUL );
		}
	}


	@Override
	protected boolean compileReturnFromNum( GeneratorAdapter _mv, Class _returnType )
	{

		if (returnScaledDownDualType( _mv, _returnType, Long.TYPE, Long.class, Opcodes.LRETURN ))
		;
		else if (returnScaledDownDualType( _mv, _returnType, Integer.TYPE, Integer.class, Opcodes.IRETURN, Opcodes.L2I ))
		;
		else if (returnScaledDownDualType( _mv, _returnType, Short.TYPE, Short.class, Opcodes.IRETURN, Opcodes.L2I,
				Opcodes.I2S ))
		;
		else if (returnScaledDownDualType( _mv, _returnType, Byte.TYPE, Byte.class, Opcodes.IRETURN, Opcodes.L2I,
				Opcodes.I2B ))
		;

		else if (_returnType == Double.TYPE || _returnType == Double.class) {
			if (getScale() == 0) {
				_mv.visitInsn( Opcodes.L2D );
			}
			else {
				compileRuntimeMethodWithContext( _mv, "toDouble", Jx_D );
			}

			if (_returnType == Double.TYPE) {
				_mv.visitInsn( Opcodes.DRETURN );
			}
			else {
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, DOUBLE_CLASS.getInternalName(), "valueOf", "(D)"
						+ DOUBLE_CLASS.getDescriptor() );
				_mv.visitInsn( Opcodes.ARETURN );
			}
		}

		else if (_returnType == Float.TYPE || _returnType == Float.class) {
			if (getScale() == 0) {
				_mv.visitInsn( Opcodes.L2F );
			}
			else {
				compileRuntimeMethodWithContext( _mv, "toDouble", Jx_D );
				_mv.visitInsn( Opcodes.D2F );
			}

			if (_returnType == Float.TYPE) {
				_mv.visitInsn( Opcodes.FRETURN );
			}
			else {
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, FLOAT_CLASS.getInternalName(), "valueOf", "(F)"
						+ FLOAT_CLASS.getDescriptor() );
				_mv.visitInsn( Opcodes.ARETURN );
			}
		}

		else if (_returnType == BigInteger.class) {
			scaleDown( _mv );
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeEngineCompiler.BIGINTEGER_CLASS.getInternalName(),
					"valueOf", "(J)" + ByteCodeEngineCompiler.BIGINTEGER_CLASS.getDescriptor() );
			_mv.visitInsn( Opcodes.ARETURN );
		}

		else if (_returnType == BigDecimal.class) {
			compileRuntimeMethodWithContext( _mv, "toBigDecimal", "(J"
					+ RUNTIME_CONTEXT_TYPE.getDescriptor() + ")" + ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getDescriptor() );
			_mv.visitInsn( Opcodes.ARETURN );
		}

		else {
			return false;
		}
		return true;
	}

	private boolean returnScaledDownDualType( GeneratorAdapter _mv, Class _returnType, Class _unboxed, Class _boxed,
			int _returnOpcode, int... _conversionOpcodes )
	{
		if (_returnType == _unboxed || _returnType == _boxed) {
			scaleDown( _mv );
			return returnDualType( _mv, _returnType, _unboxed, _boxed, _returnOpcode, _conversionOpcodes );
		}
		return false;
	}

	private void scaleDown( GeneratorAdapter _mv )
	{
		if (getScale() > 0) {
			_mv.push( this.one );
			_mv.visitInsn( Opcodes.LDIV );
		}
	}


	@Override
	protected boolean compileToNum( GeneratorAdapter _mv, ScaledLong _scale )
	{
		compileScaleCorrection( _mv, _scale.value(), getScale() );
		return true;
	}

	@Override
	protected boolean compileFromNum( GeneratorAdapter _mv, ScaledLong _scale )
	{
		compileScaleCorrection( _mv, getScale(), _scale.value() );
		return true;
	}

	private void compileScaleCorrection( GeneratorAdapter _mv, int _have, int _want )
	{
		if (_have > _want) {
			long correct = ScaledLongSupport.ONE[ _have ] / ScaledLongSupport.ONE[ _want ];
			_mv.push( correct );
			_mv.visitInsn( Opcodes.LDIV );
		}
		else if (_have < _want) {
			long correct = ScaledLongSupport.ONE[ _want ] / ScaledLongSupport.ONE[ _have ];
			_mv.push( correct );
			_mv.visitInsn( Opcodes.LMUL );
		}
	}

}
