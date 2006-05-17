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

import java.util.Date;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.ModelError;
import sej.NumericType;
import sej.expressions.Function;
import sej.expressions.Operator;
import sej.runtime.RuntimeDouble_v1;
import sej.runtime.RuntimeLong_v1;

final class ByteCodeScaledLongType extends ByteCodeNumericType
{
	private static final Type RUNTIME_TYPE = Type.getType( RuntimeLong_v1.class );
	private static final Type RUNTIME_CONTEXT_TYPE = Type.getType( RuntimeLong_v1.Context.class );
	private static final String RUNTIME_CONTEXT_NAME = "runtimeContext";
	private static final String JJ_J = "(JJ)J";
	private static final String JJx_J = "(JJ" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String JIx_J = "(JI" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String Dx_J = "(Ljava/util/Date;I" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";
	private static final String Jx_D = "(JI" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")Ljava/util/Date;";
	private static final String Zx_J = "(Z" + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")J";

	private final NumericType num;
	private final long one;

	public ByteCodeScaledLongType(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super( _type, _compiler );
		this.num = _type;
		this.one = ((NumericType.AbstractLongType) _type).one();
	}


	@Override
	public Type getType()
	{
		return Type.LONG_TYPE;
	}


	@Override
	public int getReturnOpcode()
	{
		return Opcodes.LRETURN;
	}

	@Override
	public Type getRuntimeType()
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
	public boolean buildStaticMembers( ClassWriter _cw )
	{
		FieldVisitor fv = _cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL,
				RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_TYPE.getDescriptor(), null, null );
		fv.visitEnd();
		return true;
	}


	@Override
	public void compileStaticInitialization( GeneratorAdapter _mv, Type _engineType )
	{
		_mv.visitTypeInsn( Opcodes.NEW, RUNTIME_CONTEXT_TYPE.getInternalName() );
		_mv.visitInsn( Opcodes.DUP );
		_mv.push( getScale() );
		_mv.visitMethodInsn( Opcodes.INVOKESPECIAL, RUNTIME_CONTEXT_TYPE.getInternalName(), "<init>", "(I)V" );
		_mv.visitFieldInsn( Opcodes.PUTSTATIC, _engineType.getInternalName(), RUNTIME_CONTEXT_NAME, RUNTIME_CONTEXT_TYPE
				.getDescriptor() );
	}


	@Override
	public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
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
	public void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws ModelError
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
			long val = this.num.valueOf( RuntimeDouble_v1.dateToExcel( date ) ).longValue();
			_mv.push( val );
		}
		else {
			super.compileConst( _mv, _constantValue );
		}
	}


	@Override
	public void compileZero( GeneratorAdapter _mv )
	{
		_mv.push( 0L );
	}


	@Override
	public void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode )
	{
		_mv.visitInsn( Opcodes.LCMP );
	}


	@Override
	public void compileStdFunction( GeneratorAdapter _mv, Function _function, String _argumentDescriptor )
	{
		compileRuntimeMethodWithContext( _mv, "std" + _function.getName(), "("
				+ _argumentDescriptor + RUNTIME_CONTEXT_TYPE.getDescriptor() + ")" + getDescriptor() );
	}


	@Override
	public void compileRound( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "round", getRoundMethodSignature() );
	}


	private void compileRuntimeMethodWithContext( MethodVisitor _mv, String _methodName, String _methodSig )
	{
		_mv.visitFieldInsn( Opcodes.GETSTATIC, getCompiler().engine.getInternalName(), RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_TYPE.getDescriptor() );
		compileRuntimeMethod( _mv, _methodName, _methodSig );
	}


	@Override
	protected String getRoundMethodSignature()
	{
		return JIx_J;
	}


	@Override
	public void compileDateToExcel( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "dateToExcel", Dx_J );
	}

	@Override
	public void compileDateFromExcel( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "dateFromExcel", Jx_D );
	}

	@Override
	public void compileBooleanToExcel( MethodVisitor _mv )
	{
		compileRuntimeMethodWithContext( _mv, "booleanToExcel", Zx_J );
	}

}
