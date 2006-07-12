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
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.CompilerException;
import sej.NumericType;
import sej.Operator;
import sej.internal.runtime.RuntimeBigDecimal_v1;
import sej.internal.runtime.RuntimeDouble_v1;
import sej.runtime.ScaledLong;

final class ByteCodeNumericType_BigDecimal extends ByteCodeNumericType
{
	private static final boolean JRE14 = System.getProperty( "java.version" ).startsWith( "1.4." );
	private static final String BNAME = ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getInternalName();
	private static final String B = ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getDescriptor();
	private static final String V2B = "()" + B;
	private static final String I2B = "(I)" + B;
	private static final String L2B = "(" + Type.LONG_TYPE.getDescriptor() + ")" + B;
	private static final String LI2B = "(" + Type.LONG_TYPE.getDescriptor() + "I)" + B;
	private static final String D2B = "(D)" + B;
	private static final String S2B = "(Ljava/lang/String;)" + B;
	private static final String B2I = "(" + B + ")I";
	private static final String B2B = ("(" + B + ")") + B;
	private static final String BB2B = "(" + B + B + ")" + B;
	private static final String BII2B = "(" + B + "II)" + B;
	private static final String N2L = "(" + N + ")" + Type.LONG_TYPE.getDescriptor();
	private static final String N2D = "(" + N + ")D";
	private final int scale;
	private final int roundingMode;


	ByteCodeNumericType_BigDecimal(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super( _type, _compiler );
		this.scale = _type.getScale();
		this.roundingMode = _type.getRoundingMode();
	}


	@Override
	Type type()
	{
		return Type.getType( BigDecimal.class );
	}


	@Override
	int returnOpcode()
	{
		return Opcodes.ARETURN;
	}


	private static final Type RUNTIME_TYPE = Type.getType( RuntimeBigDecimal_v1.class );

	@Override
	Type runtimeType()
	{
		return RUNTIME_TYPE;
	}


	private Map<String, String> constantPool = new HashMap<String, String>();


	/** The max value of a long is 9,223,372,036,854,775,807, so its max precision is 6 * 3 = 18. */
	private static final int MAX_LONG_PREC = 18;

	private String defineOrReuseStaticConstant( String _value )
	{
		String result = this.constantPool.get( _value );
		if (result == null) {
			final ClassWriter cw = compiler().cw();
			final GeneratorAdapter ci = compiler().initializer();
			result = "C$" + Integer.toString( this.constantPool.size() );
			cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, result, B, null,
					null ).visitEnd();
			try {
				final long longValue = Long.parseLong( _value );
				ci.push( longValue );
				ci.visitMethodInsn( Opcodes.INVOKESTATIC, BNAME, "valueOf", L2B );
				compileScaleAdjustment( ci );
			}
			catch (NumberFormatException e) {
				final BigDecimal bigValue = new BigDecimal( _value );
				if (!JRE14 && bigValue.precision() <= MAX_LONG_PREC) { // JRE 1.4 lacks "precision()"
					final long longValue = bigValue.unscaledValue().longValue();
					ci.push( longValue );
					ci.push( bigValue.scale() );
					ci.visitMethodInsn( Opcodes.INVOKESTATIC, BNAME, "valueOf", LI2B );
					if (bigValue.scale() != this.scale) {
						compileScaleAdjustment( ci );
					}
				}
				else {
					ci.push( _value );
					compileRuntimeMethod( ci, "newBigDecimal", S2B );
					compileScaleAdjustment( ci );
				}
			}
			ci.visitFieldInsn( Opcodes.PUTSTATIC, compiler().classInternalName(), result, B );
			this.constantPool.put( _value, result );
		}
		return result;
	}

	private void compileStaticConstant( MethodVisitor _mv, String _value )
	{
		final String constName = defineOrReuseStaticConstant( _value );
		_mv.visitFieldInsn( Opcodes.GETSTATIC, compiler().classInternalName(), constName, B );
	}


	@Override
	void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws CompilerException
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
				compileRuntimeMethod( _mv, "pow", BB2B );
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
	void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws CompilerException
	{
		if (null == _constantValue) {
			compileZero( _mv );
		}
		else if (_constantValue instanceof Number) {
			String val = _constantValue.toString();
			compileStaticConstant( _mv, val );
		}
		else if (_constantValue instanceof Boolean) {
			if ((Boolean) _constantValue) {
				compileStaticConstant( _mv, "1" );
			}
			else {
				compileStaticConstant( _mv, "0" );
			}
		}
		else if (_constantValue instanceof Date) {
			final double dbl = RuntimeDouble_v1.dateToNum( (Date) _constantValue );
			compileStaticConstant( _mv, Double.toString( dbl ) );
		}
		else {
			super.compileConst( _mv, _constantValue );
		}
	}


	@Override
	void compileZero( GeneratorAdapter _mv )
	{
		compileStaticConstant( _mv, "0" );
	}


	@Override
	void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode )
	{
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "compareTo", "(" + B + ")I" );
	}


	@Override
	protected String roundMethodSignature()
	{
		final String d = descriptor();
		return "(" + d + "I)" + d;
	}


	void compileScaleAdjustment( GeneratorAdapter _mv )
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


	@Override
	protected boolean compileToNum( GeneratorAdapter _mv, Class _returnType )
	{
		if (_returnType == BigDecimal.class) {
			final Label notNull = _mv.newLabel();
			_mv.dup();
			_mv.ifNonNull( notNull );
			_mv.pop();
			_mv.getStatic( RUNTIME_TYPE, "ZERO", ByteCodeEngineCompiler.BIGDECIMAL_CLASS );
			_mv.mark( notNull );
		}

		else if (toNumViaLong( _mv, _returnType, Long.TYPE, Long.class ))
		;
		else if (toNumViaLong( _mv, _returnType, Integer.TYPE, Integer.class, Opcodes.I2L ))
		;
		else if (toNumViaLong( _mv, _returnType, Short.TYPE, Short.class, Opcodes.I2L ))
		;
		else if (toNumViaLong( _mv, _returnType, Byte.TYPE, Byte.class, Opcodes.I2L ))
		;
		else if (toNumViaDouble( _mv, _returnType, Double.TYPE, Double.class ))
		;
		else if (toNumViaDouble( _mv, _returnType, Float.TYPE, Float.class, Opcodes.F2D ))
		;

		else if (_returnType == BigInteger.class) {
			compileRuntimeMethod( _mv, "newBigDecimal", "("
					+ ByteCodeEngineCompiler.BIGINTEGER_CLASS.getDescriptor() + ")" + B );
		}

		else {
			return false;
		}

		compileScaleAdjustment( _mv );
		return true;
	}

	private boolean toNumViaLong( GeneratorAdapter _mv, Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		return toNumVia( _mv, _returnType, _unboxed, _boxed, L2B, "numberToLong", N2L, _conversionOpcodes );
	}

	private boolean toNumViaDouble( GeneratorAdapter _mv, Class _returnType, Class _unboxed, Class _boxed,
			int... _conversionOpcodes )
	{
		return toNumVia( _mv, _returnType, _unboxed, _boxed, D2B, "numberToDouble", N2D, _conversionOpcodes );
	}

	private boolean toNumVia( GeneratorAdapter _mv, Class _returnType, Class _unboxed, Class _boxed, String _valueOfSig,
			String _numberConverterName, String _numberConverterSig, int... _conversionOpcodes )
	{
		if (_returnType == _unboxed) {
			convertUnboxed( _mv, _conversionOpcodes );
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, RUNTIME_TYPE.getInternalName(), "newBigDecimal", _valueOfSig );
		}
		else if (_returnType == _boxed) {
			compileRuntimeMethod( _mv, _numberConverterName, _numberConverterSig );
			_mv.visitMethodInsn( Opcodes.INVOKESTATIC, RUNTIME_TYPE.getInternalName(), "newBigDecimal", _valueOfSig );
		}
		else {
			return false;
		}
		return true;
	}


	@Override
	protected boolean compileReturnFromNum( GeneratorAdapter _mv, Class _returnType )
	{
		if (_returnType == BigDecimal.class) {
			_mv.visitInsn( returnOpcode() );
		}

		else if (returnDualType( _mv, _returnType, Long.TYPE, Long.class, Opcodes.LRETURN, "longValue" ))
		;
		else if (returnDualType( _mv, _returnType, Integer.TYPE, Integer.class, Opcodes.IRETURN, "intValue" ))
		;
		else if (returnDualType( _mv, _returnType, Short.TYPE, Short.class, Opcodes.IRETURN, "shortValue" ))
		;
		else if (returnDualType( _mv, _returnType, Byte.TYPE, Byte.class, Opcodes.IRETURN, "byteValue" ))
		;
		else if (returnDualType( _mv, _returnType, Double.TYPE, Double.class, Opcodes.DRETURN, "doubleValue" ))
		;
		else if (returnDualType( _mv, _returnType, Float.TYPE, Float.class, Opcodes.FRETURN, "floatValue" ))
		;

		else if (_returnType == BigInteger.class) {
			_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "toBigInteger", "()"
					+ ByteCodeEngineCompiler.BIGINTEGER_CLASS.getDescriptor() );
			_mv.visitInsn( Opcodes.ARETURN );
		}

		else {
			return false;
		}
		return true;
	}

	private boolean returnDualType( MethodVisitor _mv, Class _returnType, Class _unboxed, Class _boxed,
			int _returnOpcode, String _valueGetterName )
	{
		if (_returnType == _unboxed) {
			final String valueGetterSig = "()" + Type.getType( _unboxed ).getDescriptor();
			_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, _valueGetterName, valueGetterSig );
			_mv.visitInsn( _returnOpcode );
		}
		else if (_returnType == _boxed) {
			final String valueGetterSig = "()" + Type.getType( _unboxed ).getDescriptor();
			_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, _valueGetterName, valueGetterSig );
			returnBoxedType( _mv, _returnType, _unboxed, _boxed );
		}

		else {
			return false;
		}
		return true;
	}


	@Override
	protected boolean compileToNum( GeneratorAdapter _mv, ScaledLong _scale )
	{
		_mv.push( _scale.value() );
		compileRuntimeMethod( _mv, "fromScaledLong", "(JI)" + B );
		compileScaleAdjustment( _mv );
		return true;
	}

	@Override
	protected boolean compileFromNum( GeneratorAdapter _mv, ScaledLong _scale )
	{
		_mv.push( _scale.value() );
		_mv.push( numericType().getRoundingMode() );
		compileRuntimeMethod( _mv, "toScaledLong", "(" + B + "II)J" );
		return true;
	}

	@Override
	void compileIntFromNum( GeneratorAdapter _mv )
	{
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "intValue", B2I );
	}
	
}