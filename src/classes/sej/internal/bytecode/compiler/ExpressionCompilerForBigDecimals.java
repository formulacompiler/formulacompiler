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

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.NumericType;
import sej.Operator;
import sej.internal.expressions.ExpressionNodeForFunction;
import sej.runtime.ScaledLong;

final class ExpressionCompilerForBigDecimals extends ExpressionCompilerForNumbers
{
	private static final String BNAME = TypeCompilerForBigDecimals.BNAME;
	private static final String B = TypeCompilerForBigDecimals.B;
	private static final String V2B = TypeCompilerForBigDecimals.V2B;
	private static final String I2B = TypeCompilerForBigDecimals.I2B;
	private static final String L2B = TypeCompilerForBigDecimals.L2B;
	private static final String D2B = TypeCompilerForBigDecimals.D2B;
	private static final String B2B = TypeCompilerForBigDecimals.B2B;
	private static final String BB2B = TypeCompilerForBigDecimals.BB2B;
	private static final String BII2B = TypeCompilerForBigDecimals.BII2B;
	private static final String N2L = TypeCompilerForBigDecimals.N2L;
	private static final String N2D = TypeCompilerForBigDecimals.N2D;

	private final int scale;
	private final int roundingMode;

	
	public ExpressionCompilerForBigDecimals(MethodCompiler _methodCompiler, NumericType _numericType)
	{
		super( _methodCompiler, _numericType );
		this.scale = _numericType.getScale();
		this.roundingMode = _numericType.getRoundingMode();
	}
	
	
	private final TypeCompilerForBigDecimals bigCompiler()
	{
		return (TypeCompilerForBigDecimals) typeCompiler();
	}


	@Override
	protected String roundMethodSignature()
	{
		return "(Ljava/math/BigDecimal;I)Ljava/math/BigDecimal;";
	}


	private final boolean isScaled()
	{
		return bigCompiler().isScaled();
	}


	private final void compileScaleAdjustment()
	{
		bigCompiler().compileScaleAdjustment( mv() );
	}


	@Override
	protected void compileConversionFrom( Class _class ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();

		if (_class == BigDecimal.class) {
			final Label notNull = mv.newLabel();
			mv.dup();
			mv.ifNonNull( notNull );
			mv.pop();
			compileZero();
			mv.mark( notNull );
		}

		else if (compileConversionViaLong( _class, Long.TYPE, Long.class ))
		;
		else if (compileConversionViaLong( _class, Integer.TYPE, Integer.class, Opcodes.I2L ))
		;
		else if (compileConversionViaLong( _class, Short.TYPE, Short.class, Opcodes.I2L ))
		;
		else if (compileConversionViaLong( _class, Byte.TYPE, Byte.class, Opcodes.I2L ))
		;
		else if (compileConversionViaDouble( _class, Double.TYPE, Double.class ))
		;
		else if (compileConversionViaDouble( _class, Float.TYPE, Float.class, Opcodes.F2D ))
		;

		else if (_class == BigInteger.class) {
			compileRuntimeMethod( "newBigDecimal", "(" + ByteCodeEngineCompiler.BIGINTEGER_CLASS.getDescriptor() + ")" + B );
		}

		else {
			super.compileConversionFrom( _class );
		}

		compileScaleAdjustment();
	}


	private final boolean compileConversionViaLong( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		return compileConversionVia( _returnType, _unboxed, _boxed, L2B, "numberToLong", N2L, _conversionOpcodes );
	}


	private final boolean compileConversionViaDouble( Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		return compileConversionVia( _returnType, _unboxed, _boxed, D2B, "numberToDouble", N2D, _conversionOpcodes );
	}


	private final boolean compileConversionVia( Class _returnType, Class _unboxed, Class _boxed, String _valueOfSig,
			String _numberConverterName, String _numberConverterSig, int... _conversionOpcodes )
	{
		if (_returnType == _unboxed) {
			compileInstructions( _conversionOpcodes );
			compileRuntimeMethod( "newBigDecimal", _valueOfSig );
		}
		else if (_returnType == _boxed) {
			compileRuntimeMethod( _numberConverterName, _numberConverterSig );
			compileRuntimeMethod( "newBigDecimal", _valueOfSig );
		}
		else {
			return false;
		}
		return true;
	}


	@Override
	protected boolean compileConversionFrom( ScaledLong _scale ) throws CompilerException
	{
		mv().push( _scale.value() );
		compileRuntimeMethod( "fromScaledLong", "(JI)" + B );
		compileScaleAdjustment();
		return true;
	}


	@Override
	protected void compileConversionTo( Class _class ) throws CompilerException
	{
		if (_class == BigDecimal.class) {
			return;
		}

		else if (compileConversionToBoxable( _class, Long.TYPE, Long.class, "longValue" ))
		;
		else if (compileConversionToBoxable( _class, Integer.TYPE, Integer.class, "intValue" ))
		;
		else if (compileConversionToBoxable( _class, Short.TYPE, Short.class, "shortValue" ))
		;
		else if (compileConversionToBoxable( _class, Byte.TYPE, Byte.class, "byteValue" ))
		;
		else if (compileConversionToBoxable( _class, Double.TYPE, Double.class, "doubleValue" ))
		;
		else if (compileConversionToBoxable( _class, Float.TYPE, Float.class, "floatValue" ))
		;

		else if (_class == BigInteger.class) {
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "toBigInteger",
					"()" + ByteCodeEngineCompiler.BIGINTEGER_CLASS.getDescriptor() );
		}
		else {
			super.compileConversionTo( _class );
		}
	}


	private final boolean compileConversionToBoxable( Class _returnType, Class _unboxed, Class _boxed,
			String _valueGetterName )
	{
		if (_returnType == _unboxed) {
			final String valueGetterSig = "()" + Type.getType( _unboxed ).getDescriptor();
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, _valueGetterName, valueGetterSig );
		}
		else if (_returnType == _boxed) {
			final String valueGetterSig = "()" + Type.getType( _unboxed ).getDescriptor();
			mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, _valueGetterName, valueGetterSig );
			compileConversionToBoxable( _returnType, _unboxed, _boxed );
		}
		else {
			return false;
		}
		return true;
	}


	@Override
	protected boolean compileConversionTo( ScaledLong _scale ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		mv.push( _scale.value() );
		mv.push( numericType().getRoundingMode() );
		compileRuntimeMethod( "toScaledLong", "(" + B + "II)J" );
		return true;
	}


	@Override
	protected void compileConversionFromInt() throws CompilerException
	{
		mv().visitInsn( Opcodes.I2L );
		compileRuntimeMethod( "newBigDecimal", L2B );
		compileScaleAdjustment();
	}

	@Override
	protected void compileConversionToInt() throws CompilerException
	{
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "intValue", "()I" );
	}


	@Override
	protected void compileOperator( Operator _operator, int _numberOfArguments ) throws CompilerException
	{
		final GeneratorAdapter mv = mv();
		switch (_operator) {

			case PLUS:
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "add", B2B );
				break;

			case MINUS:
				if (1 == _numberOfArguments) {
					mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "negate", V2B );
				}
				else {
					mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "subtract", B2B );
				}
				break;

			case TIMES:
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "multiply", B2B );
				compileScaleAdjustment();
				break;

			case DIV:
				if (isScaled()) {
					mv.push( this.scale );
					mv.push( this.roundingMode );
					mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "divide", BII2B );
				}
				else {
					if (ByteCodeEngineCompiler.JRE14) {
						throw new CompilerException.UnsupportedOperator( "Cannot divide unscaled BigDecimals on JRE 1.4." );
					}
					mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "divide", B2B );
				}
				break;

			case PERCENT:
				mv.push( 2 );
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "movePointLeft", I2B );
				compileScaleAdjustment();
				break;

			case EXP:
				compileRuntimeMethod( "pow", BB2B );
				compileScaleAdjustment();
				break;

			case MIN:
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "min", B2B );
				break;

			case MAX:
				mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "max", B2B );
				break;

			default:
				super.compileOperator( _operator, _numberOfArguments );
		}
	}
	
	
	@Override
	protected void compileStdFunction( ExpressionNodeForFunction _node ) throws CompilerException
	{
		super.compileStdFunction( _node );
		compileScaleAdjustment();
	}
	
	
	@Override
	protected void compileRound()
	{
		super.compileRound();
		compileScaleAdjustment();
	}
	
	
	@Override
	protected void compileComparison( int _comparisonOpcode ) throws CompilerException
	{
		mv().visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "compareTo", "(" + B + ")I" );
	}

	
}
