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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.Function;
import sej.NumericType;
import sej.Operator;
import sej.internal.NumericTypeImpl;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.runtime.ScaledLong;
import sej.runtime.ScaledLongSupport;

final class ExpressionCompilerForScaledLongs extends ExpressionCompilerForNumbers
{
	private static final String RUNTIME_CONTEXT_DESCRIPTOR = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_DESCRIPTOR;
	private static final String RUNTIME_CONTEXT_NAME = TypeCompilerForScaledLongs.RUNTIME_CONTEXT_NAME;
	private static final String JJ_J = "(JJ)J";
	private static final String JJx_J = "(JJ" + RUNTIME_CONTEXT_DESCRIPTOR + ")J";
	private static final String JIx_J = "(JI" + RUNTIME_CONTEXT_DESCRIPTOR + ")J";
	private static final String Dtx_J = "(Ljava/util/Date;" + RUNTIME_CONTEXT_DESCRIPTOR + ")J";
	private static final String Jx_Dt = "(J" + RUNTIME_CONTEXT_DESCRIPTOR + ")Ljava/util/Date;";
	private static final String Zx_J = "(Z" + RUNTIME_CONTEXT_DESCRIPTOR + ")J";
	private static final String Dx_J = "(D" + RUNTIME_CONTEXT_DESCRIPTOR + ")J";
	private static final String Jx_D = "(J" + RUNTIME_CONTEXT_DESCRIPTOR + ")D";

	private final int scale;
	private final long one;
	private final TypeCompilerForScaledLongs longCompiler = ((TypeCompilerForScaledLongs) typeCompiler());


	public ExpressionCompilerForScaledLongs(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
		this.scale = _numericType.getScale();
		this.one = ((NumericTypeImpl.AbstractLongType) _numericType).one();
	}


	private final int scale()
	{
		return this.scale;
	}


	private final void compileScaleUp()
	{
		if (scale() > 0) {
			final GeneratorAdapter mv = mv();
			mv.push( this.one );
			mv.visitInsn( Opcodes.LMUL );
		}
	}


	private final void compileScaleDown()
	{
		if (scale() > 0) {
			final GeneratorAdapter mv = mv();
			mv.push( this.one );
			mv.visitInsn( Opcodes.LDIV );
		}
	}


	private final void compileScaleCorrection( int _have, int _want )
	{
		final GeneratorAdapter mv = mv();
		if (_have > _want) {
			long correct = ScaledLongSupport.ONE[ _have ] / ScaledLongSupport.ONE[ _want ];
			mv.push( correct );
			mv.visitInsn( Opcodes.LDIV );
		}
		else if (_have < _want) {
			long correct = ScaledLongSupport.ONE[ _want ] / ScaledLongSupport.ONE[ _have ];
			mv.push( correct );
			mv.visitInsn( Opcodes.LMUL );
		}
	}


	@Override
	protected String roundMethodSignature()
	{
		return JIx_J;
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();

		if (_class == Long.TYPE) {
			compileScaleUp();
		}

		else if (_class == Integer.TYPE) {
			mv.visitInsn( Opcodes.I2L );
			compileScaleUp();
		}

		else if (_class == Short.TYPE) {
			mv.visitInsn( Opcodes.I2L );
			compileScaleUp();
		}
		else if (_class == Byte.TYPE) {
			mv.visitInsn( Opcodes.I2L );
			compileScaleUp();
		}

		else if (_class == Double.TYPE) {
			if (scale() == 0) {
				mv.visitInsn( Opcodes.D2L );
			}
			else {
				compileRuntimeMethodWithContext( "fromDouble", Dx_J );
			}
		}
		else if (_class == Double.class) {
			compileRuntimeMethodWithContext( "fromBoxedDouble", "(" + N + RUNTIME_CONTEXT_DESCRIPTOR + ")J" );
		}

		else if (_class == Float.TYPE) {
			if (scale() == 0) {
				mv.visitInsn( Opcodes.F2L );
			}
			else {
				mv.visitInsn( Opcodes.F2D );
				compileRuntimeMethodWithContext( "fromDouble", Dx_J );
			}
		}
		else if (_class == Float.class) {
			compileRuntimeMethodWithContext( "fromBoxedDouble", "(" + N + RUNTIME_CONTEXT_DESCRIPTOR + ")J" );
		}

		else if (_class == BigDecimal.class) {
			compileRuntimeMethodWithContext( "fromBigDecimal", "("
					+ ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getDescriptor() + RUNTIME_CONTEXT_DESCRIPTOR + ")J" );
		}

		else if (Number.class.isAssignableFrom( _class )) {
			compileRuntimeMethodWithContext( "fromNumber", "(" + N + RUNTIME_CONTEXT_DESCRIPTOR + ")J" );
		}

		else if (_class == Boolean.TYPE) {
			compileRuntimeMethodWithContext( "booleanToNum", Zx_J );
		}
		else if (_class == Boolean.class) {
			compileRuntimeMethod( "unboxBoolean", BOOL2Z );
			compileRuntimeMethodWithContext( "booleanToNum", Zx_J );
		}
		else if (_class == Date.class) {
			compileRuntimeMethodWithContext( "dateToNum", Dtx_J );
		}

		else {
			super.compileConversionFrom( _class );
		}
	}


	@Override
	protected boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		compileScaleCorrection( _scale.value(), scale() );
		return true;
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();

		if (compileConversionToScaledDownBoxable( _class, Long.TYPE, Long.class ))
		;
		else if (compileConversionToScaledDownBoxable( _class, Integer.TYPE, Integer.class, Opcodes.L2I ))
		;
		else if (compileConversionToScaledDownBoxable( _class, Short.TYPE, Short.class, Opcodes.L2I, Opcodes.I2S ))
		;
		else if (compileConversionToScaledDownBoxable( _class, Byte.TYPE, Byte.class, Opcodes.L2I, Opcodes.I2B ))
		;

		else if (_class == Double.TYPE || _class == Double.class) {
			if (scale() == 0) {
				mv.visitInsn( Opcodes.L2D );
			}
			else {
				compileRuntimeMethodWithContext( "toDouble", Jx_D );
			}
			if (_class == Double.class) {
				ByteCodeEngineCompiler.compileValueOf( mv, "java/lang/Double", "(D)Ljava/lang/Double;", Double.TYPE );
			}
		}

		else if (_class == Float.TYPE || _class == Float.class) {
			if (scale() == 0) {
				mv.visitInsn( Opcodes.L2F );
			}
			else {
				compileRuntimeMethodWithContext( "toDouble", Jx_D );
				mv.visitInsn( Opcodes.D2F );
			}
			if (_class == Float.class) {
				ByteCodeEngineCompiler.compileValueOf( mv, "java/lang/Float", "(F)Ljava/lang/Float;", Float.TYPE );
			}
		}

		else if (_class == BigInteger.class) {
			compileScaleDown();
			ByteCodeEngineCompiler.compileValueOf( mv, "java/math/BigInteger", "(J)Ljava/math/BigInteger;", Long.TYPE );
		}

		else if (_class == BigDecimal.class) {
			compileRuntimeMethodWithContext( "toBigDecimal", "(J" + RUNTIME_CONTEXT_DESCRIPTOR + ")Ljava/math/BigDecimal;" );
		}

		else if (_class == Date.class) {
			compileRuntimeMethodWithContext( "dateFromNum", Jx_Dt );
		}

		else {
			super.compileConversionTo( _class );
		}
	}


	private final boolean compileConversionToScaledDownBoxable( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		if (_returnType == _unboxed || _returnType == _boxed) {
			compileScaleDown();
			return compileConversionToBoxable( _returnType, _unboxed, _boxed, _conversionOpcodes );
		}
		return false;
	}


	@Override
	protected boolean compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		compileScaleCorrection( scale(), _scale.value() );
		return true;
	}


	@Override
	protected void compileConversionFromInt() throws CompilerException
	{
		mv().visitInsn( Opcodes.I2L );
		compileScaleUp();
	}


	@Override
	protected void compileConversionToInt() throws CompilerException
	{
		compileScaleDown();
		mv().visitInsn( Opcodes.L2I );
	}


	@Override
	protected void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();

		switch (_operator) {

			case PLUS:
				mv.visitInsn( Opcodes.LADD );
				break;

			case MINUS:
				if (1 == _numberOfArguments) {
					mv.visitInsn( Opcodes.LNEG );
				}
				else {
					mv.visitInsn( Opcodes.LSUB );
				}
				break;

			case TIMES:
				mv.visitInsn( Opcodes.LMUL );
				if (scale() > 0) {
					mv.push( this.one );
					mv.visitInsn( Opcodes.LDIV );
				}
				break;

			case DIV:
				// LATER Make scaled long div more efficient
				if (scale() > 0) {
					mv.swap( type(), type() );
					mv.push( this.one );
					mv.visitInsn( Opcodes.LMUL );
					mv.swap( type(), type() );
				}
				mv.visitInsn( Opcodes.LDIV );
				break;

			case PERCENT:
				mv.visitLdcInsn( 100L );
				mv.visitInsn( Opcodes.LDIV );
				break;

			case EXP:
				compileRuntimeMethodWithContext( "pow", JJx_J );
				break;

			case MIN:
				compileRuntimeMethod( "min", JJ_J );
				break;

			case MAX:
				compileRuntimeMethod( "max", JJ_J );
				break;

			default:
				unsupported( "Operator " + _operator + " is not supported for scaled longs." );
		}
	}


	@Override
	protected void compileFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		final Function fun = _node.getFunction();
		switch (fun) {
			case ROUND:
			case TODAY:
		}
		super.compileFunction( _node );
	}


	@Override
	protected void compileComparison( int _comparisonOpcode ) throws CompilerException
	{
		mv().visitInsn( Opcodes.LCMP );
	}


	@Override
	protected void compileRound()
	{
		compileRuntimeMethodWithContext( "round", roundMethodSignature() );
	}


	@Override
	protected void compileRuntimeMethodWithContext( String _methodName, String _methodSig )
	{
		this.longCompiler.buildStaticContext();

		mv().visitFieldInsn( Opcodes.GETSTATIC, typeCompiler().rootCompiler().classInternalName(), RUNTIME_CONTEXT_NAME,
				RUNTIME_CONTEXT_DESCRIPTOR );
		compileRuntimeMethod( _methodName, _methodSig );
	}

	@Override
	protected boolean doesStdFunctionNeedContext( ExpressionNodeForFunction _node )
	{
		return true;
	}
	
	@Override
	protected void appendStdFunctionContext( StringBuilder _descriptorBuilder )
	{
		_descriptorBuilder.append( RUNTIME_CONTEXT_DESCRIPTOR );
	}

	
}
