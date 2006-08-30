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

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.NumericType;
import sej.Operator;
import sej.runtime.ScaledLong;
import sej.runtime.ScaledLongSupport;

final class ExpressionCompilerForDoubles extends ExpressionCompilerForNumbers
{

	public ExpressionCompilerForDoubles(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
	}


	@Override
	protected String roundMethodSignature()
	{
		return "(DI)D";
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		if (_class == Double.TYPE) {
			return;
		}
		else if (_class == Long.TYPE) {
			mv.visitInsn( Opcodes.L2D );
		}
		else if (_class == Integer.TYPE) {
			mv.visitInsn( Opcodes.I2D );
		}
		else if (_class == Short.TYPE) {
			mv.visitInsn( Opcodes.I2D );
		}
		else if (_class == Byte.TYPE) {
			mv.visitInsn( Opcodes.I2D );
		}
		else if (_class == Float.TYPE) {
			mv.visitInsn( Opcodes.F2D );
		}
		else if (Number.class.isAssignableFrom( _class )) {
			compileRuntimeMethod( "numberToNum", "(" + N + ")D" );
		}
		else if (_class == String.class) {
			mv.visitMethodInsn( Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(Ljava/lang/String;)D" );
		}
		else {
			super.compileConversionFrom( _class );
		}
	}


	@Override
	protected boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		mv().push( ScaledLongSupport.ONE[ _scale.value() ] );
		compileRuntimeMethod( "fromScaledLong", "(JJ)D" );
		return true;
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (_class == Double.TYPE) {
			return;
		}
		else if (compileConversionToBoxed( _class, Double.TYPE, Double.class ))
		;
		else if (compileConversionToBoxable( _class, Integer.TYPE, Integer.class, Opcodes.D2I ))
		;
		else if (compileConversionToBoxable( _class, Long.TYPE, Long.class, Opcodes.D2L ))
		;
		else if (compileConversionToBoxable( _class, Short.TYPE, Short.class, Opcodes.D2I, Opcodes.I2S ))
		;
		else if (compileConversionToBoxable( _class, Byte.TYPE, Byte.class, Opcodes.D2I, Opcodes.I2B ))
		;
		else if (compileConversionToBoxable( _class, Float.TYPE, Float.class, Opcodes.D2F ))
		;
		else if (compileConversionToBoxed( _class, Double.TYPE, BigDecimal.class ))
		;
		else if (compileConversionToBoxed( _class, Long.TYPE, BigInteger.class, Opcodes.D2L ))
		;
		else if (_class == String.class) {
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, "java/lang/Double", "toString", "(D)Ljava/lang/String;" );
		}
		else {
			super.compileConversionTo( _class );
		}
	}


	@Override
	protected boolean compileConversionTo( ScaledLong _scale )
	{
		mv().push( ScaledLongSupport.ONE[ _scale.value() ] );
		compileRuntimeMethod( "toScaledLong", "(DJ)J" );
		return true;
	}


	@Override
	protected void compileConversionFromInt() throws CompilerException
	{
		mv().visitInsn( Opcodes.I2D );
	}

	@Override
	protected void compileConversionToInt() throws CompilerException
	{
		mv().visitInsn( Opcodes.D2I );
	}
	
	
	@Override
	protected void compileConversionToString() throws CompilerException
	{
		compileRuntimeMethod( "toExcelString", "(D)Ljava/lang/String;" );
	}


	@Override
	protected void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		switch (_operator) {

			case PLUS:
				mv.visitInsn( Opcodes.DADD );
				break;

			case MINUS:
				if (1 == _numberOfArguments) {
					mv.visitInsn( Opcodes.DNEG );
				}
				else {
					mv.visitInsn( Opcodes.DSUB );
				}
				break;

			case TIMES:
				mv.visitInsn( Opcodes.DMUL );
				break;

			case DIV:
				mv.visitInsn( Opcodes.DDIV );
				break;

			case PERCENT:
				mv.visitLdcInsn( 100.0 );
				mv.visitInsn( Opcodes.DDIV );
				break;

			case EXP:
				mv.visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeEngineCompiler.MATH_CLASS.getInternalName(), "pow",
						"(DD)D" );
				break;

			case MIN:
				compileRuntimeMethod( "min", "(DD)D" );
				break;

			case MAX:
				compileRuntimeMethod( "max", "(DD)D" );
				break;

			default:
				super.compileOperator( _operator, _numberOfArguments );
		}
	}


	@Override
	protected void compileComparison( int _comparisonOpcode ) throws CompilerException
	{
		mv().visitInsn( _comparisonOpcode );
	}


	/**
	 * Debugging aid. Protected so Eclipse does not flag it as an unused private method.
	 */
	protected final void compileLog( String _message )
	{
		final GeneratorAdapter mv = mv();
		mv.dup2();
		mv.push( _message );
		mv.visitMethodInsn( Opcodes.INVOKESTATIC, runtimeType().getInternalName(), "logDouble", "(DLjava/lang/String;)V" );
	}

}
