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

import java.math.BigDecimal;
import java.util.Date;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.ModelError;
import sej.NumericType;
import sej.engine.RuntimeBigDecimal_v1;
import sej.engine.RuntimeDouble_v1;
import sej.expressions.Operator;


public abstract class ByteCodeNumericType
{
	static ByteCodeNumericType DOUBLE_TYPE = new DoubleType();
	static ByteCodeNumericType BIGDECIMAL_8_TYPE = new BigDecimalType( 8, BigDecimal.ROUND_HALF_UP );
	static ByteCodeNumericType BIGDECIMAL_9_TYPE = new BigDecimalType( 9, BigDecimal.ROUND_HALF_UP );


	ByteCodeNumericType()
	{
		super();
	}

	public static ByteCodeNumericType typeFor( NumericType _type )
	{
		if (Double.TYPE == _type.getValueType()) {
			return DOUBLE_TYPE;
		}
		else if (BigDecimal.class == _type.getValueType()) {
			if (8 == _type.getScale() && _type.getRoundingMode() == BigDecimal.ROUND_HALF_UP) return BIGDECIMAL_8_TYPE;
			if (9 == _type.getScale() && _type.getRoundingMode() == BigDecimal.ROUND_HALF_UP) return BIGDECIMAL_9_TYPE;
			return new BigDecimalType( _type.getScale(), _type.getRoundingMode() );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for byte code compilation." );
		}
	}


	public abstract Type getType();
	public abstract int getReturnOpcode();
	public abstract Type getRuntimeType();


	public String getDescriptor()
	{
		return getType().getDescriptor();
	}

	public String getRoundMethodSignature()
	{
		final String descriptor = getDescriptor();
		return "(" + descriptor + "I)" + descriptor;
	}


	public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
	{
		switch (_operator) {

			case NOOP:
				return;

			default:
				throw new ModelError.UnsupportedOperator( "The operator '"
						+ _operator.getSymbol() + "' is not supported here." );
		}
	}

	public void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws ModelError
	{
		throw new ModelError.UnsupportedDataType( "The data type "
				+ _constantValue.getClass().getName() + " is not supported for constant " + _constantValue.toString() );
	}

	public abstract void compileZero( GeneratorAdapter _mv );

	public abstract void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode );

	public abstract void compileFromLong( GeneratorAdapter _mv );


	static final class DoubleType extends ByteCodeNumericType
	{

		@Override
		public Type getType()
		{
			return Type.DOUBLE_TYPE;
		}


		@Override
		public int getReturnOpcode()
		{
			return Opcodes.DRETURN;
		}


		private static final Type RUNTIME_TYPE = Type.getType( RuntimeDouble_v1.class );

		@Override
		public Type getRuntimeType()
		{
			return RUNTIME_TYPE;
		}


		@Override
		public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
		{
			switch (_operator) {

				case PLUS:
					_mv.visitInsn( Opcodes.DADD );
					break;

				case MINUS:
					if (1 == _numberOfArguments) {
						_mv.visitInsn( Opcodes.DNEG );
					}
					else {
						_mv.visitInsn( Opcodes.DSUB );
					}
					break;

				case TIMES:
					_mv.visitInsn( Opcodes.DMUL );
					break;

				case DIV:
					_mv.visitInsn( Opcodes.DDIV );
					break;

				case PERCENT:
					_mv.visitLdcInsn( 100.0 );
					_mv.visitInsn( Opcodes.DDIV );
					break;

				case EXP:
					_mv.visitMethodInsn( Opcodes.INVOKESTATIC, ByteCodeCompiler.MATH.getInternalName(), "pow", "(DD)D" );
					break;

				case MIN:
					_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "min", "(DD)D" );
					break;

				case MAX:
					_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "max", "(DD)D" );
					break;

				default:
					super.compile( _mv, _operator, _numberOfArguments );
			}
		}


		@Override
		public void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws ModelError
		{
			if (null == _constantValue) {
				_mv.visitInsn( Opcodes.DCONST_0 );
			}
			else if (_constantValue instanceof Number) {
				double val = ((Number) _constantValue).doubleValue();
				_mv.push( val );
			}
			else if (_constantValue instanceof Boolean) {
				double val = ((Boolean) _constantValue) ? 1 : 0;
				_mv.push( val );
			}
			else if (_constantValue instanceof Date) {
				Date date = (Date) _constantValue;
				double val = RuntimeDouble_v1.dateToExcel( date );
				_mv.push( val );
			}
			else {
				super.compileConst( _mv, _constantValue );
			}
		}


		@Override
		public void compileZero( GeneratorAdapter _mv )
		{
			_mv.push( 0.0D );
		}


		@Override
		public void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode )
		{
			_mv.visitInsn( _comparisonOpcode );
		}


		@Override
		public void compileFromLong( GeneratorAdapter _mv )
		{
			_mv.visitInsn( Opcodes.I2D );
		}

	}


	static final class BigDecimalType extends ByteCodeNumericType
	{
		private static final String BNAME = ByteCodeCompiler.BIGDECIMAL.getInternalName();
		private static final String B = ByteCodeCompiler.BIGDECIMAL.getDescriptor();
		private static final String V2B = "()" + B;
		private static final String I2B = "(" + Type.INT_TYPE.getDescriptor() + ")" + B;
		private static final String D2B = "(" + Type.DOUBLE_TYPE.getDescriptor() + ")" + B;
		private static final String L2B = "(" + Type.LONG_TYPE.getDescriptor() + ")" + B;
		private static final String S2B = "(Ljava/lang/String;)" + B;
		private static final String B2B = "(" + B + ")" + B;
		private static final String BB2B = "(" + B + B + ")" + B;
		private static final String BII2B = "(" + B + "II)" + B;
		private final int scale;
		private final int roundingMode;


		public BigDecimalType(int _scale, int _roundingMode)
		{
			super();
			this.scale = _scale;
			this.roundingMode = _roundingMode;
		}


		@Override
		public Type getType()
		{
			return Type.getType( BigDecimal.class );
		}


		@Override
		public int getReturnOpcode()
		{
			return Opcodes.ARETURN;
		}


		private static final Type RUNTIME_TYPE = Type.getType( RuntimeBigDecimal_v1.class );

		@Override
		public Type getRuntimeType()
		{
			return RUNTIME_TYPE;
		}


		@Override
		public void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws ModelError
		{
			switch (_operator) {

				case PLUS:
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "add", B2B );
					break;

				case MINUS:
					if (1 == _numberOfArguments) {
						_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "negate", V2B );
					}
					else {
						_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "subtract", B2B );
					}
					break;

				case TIMES:
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "multiply", B2B );
					compileScaleAdjustment( _mv );
					break;

				case DIV:
					if (isScaled()) {
						_mv.push( this.scale );
						_mv.push( this.roundingMode );
						_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "divide", BII2B );
					}
					else {
						_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "divide", B2B );
					}
					break;

				case PERCENT:
					_mv.push( 2 );
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "movePointLeft", I2B );
					compileScaleAdjustment( _mv );
					break;

				case EXP:
					_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "pow", BB2B );
					compileScaleAdjustment( _mv );
					break;

				case MIN:
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "min", B2B );
					break;

				case MAX:
					_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "max", B2B );
					break;

				default:
					super.compile( _mv, _operator, _numberOfArguments );
			}
		}


		@Override
		public void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws ModelError
		{
			if (null == _constantValue) {
				compileZero( _mv );
			}
			else if (_constantValue instanceof Number) {
				String val = _constantValue.toString();
				_mv.push( val );
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "newBigDecimal", S2B );
				compileScaleAdjustment( _mv );
			}
			else if (_constantValue instanceof Boolean) {
				_mv.visitFieldInsn( Opcodes.GETSTATIC, BNAME, ((Boolean) _constantValue) ? "ONE" : "ZERO", B );
				compileScaleAdjustment( _mv );
			}
			else if (_constantValue instanceof Date) {
				DOUBLE_TYPE.compileConst( _mv, _constantValue );
				compileFromDouble( _mv );
				compileScaleAdjustment( _mv );
			}
			else {
				super.compileConst( _mv, _constantValue );
			}
		}


		@Override
		public void compileZero( GeneratorAdapter _mv )
		{
			final String jreVersion = System.getProperty( "java.version" );
			if (jreVersion.startsWith( "1.4." )) {
				_mv.push( 0L );
				_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "newBigDecimal", L2B );
			}
			else {
				_mv.visitFieldInsn( Opcodes.GETSTATIC, BNAME, "ZERO", B );
			}
			compileScaleAdjustment( _mv );
		}


		@Override
		public void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode )
		{
			_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "compareTo", "(" + B + ")I" );
		}


		@Override
		public void compileFromLong( GeneratorAdapter _mv )
		{
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "newBigDecimal", L2B );
			compileScaleAdjustment( _mv );
		}


		private void compileFromDouble( GeneratorAdapter _mv )
		{
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, getRuntimeType().getInternalName(), "newBigDecimal", D2B );
		}


		public void compileScaleAdjustment( GeneratorAdapter _mv )
		{
			if (isScaled()) {
				_mv.push( this.scale );
				_mv.push( this.roundingMode );
				_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "setScale", "(II)" + B );
			}
		}


		private boolean isScaled()
		{
			return NumericType.UNDEFINED_SCALE != this.scale;
		}

	}

}
