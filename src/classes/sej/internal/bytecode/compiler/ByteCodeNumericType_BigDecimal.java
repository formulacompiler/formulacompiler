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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

import sej.NumericType;
import sej.api.CompilerError;
import sej.api.expressions.Operator;
import sej.internal.runtime.RuntimeBigDecimal_v1;
import sej.internal.runtime.RuntimeDouble_v1;

final class ByteCodeNumericType_BigDecimal extends ByteCodeNumericType
{
	private static final boolean JRE14 = System.getProperty( "java.version" ).startsWith( "1.4." );
	private static final String BNAME = ByteCodeEngineCompiler.BIGDECIMAL.getInternalName();
	private static final String B = ByteCodeEngineCompiler.BIGDECIMAL.getDescriptor();
	private static final String V2B = "()" + B;
	private static final String I2B = "(" + Type.INT_TYPE.getDescriptor() + ")" + B;
	private static final String L2B = "(" + Type.LONG_TYPE.getDescriptor() + ")" + B;
	private static final String LI2B = "(" + Type.LONG_TYPE.getDescriptor() + "I)" + B;
	private static final String S2B = "(Ljava/lang/String;)" + B;
	private static final String B2B = "(" + B + ")" + B;
	private static final String BB2B = "(" + B + B + ")" + B;
	private static final String BII2B = "(" + B + "II)" + B;
	private final int scale;
	private final int roundingMode;


	ByteCodeNumericType_BigDecimal(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super( _type, _compiler );
		this.scale = _type.getScale();
		this.roundingMode = _type.getRoundingMode();
	}


	@Override
	Type getType()
	{
		return Type.getType( BigDecimal.class );
	}


	@Override
	int getReturnOpcode()
	{
		return Opcodes.ARETURN;
	}


	private static final Type RUNTIME_TYPE = Type.getType( RuntimeBigDecimal_v1.class );

	@Override
	Type getRuntimeType()
	{
		return RUNTIME_TYPE;
	}


	private Type classType;
	private ClassWriter classWriter;
	private GeneratorAdapter classInitializer;
	private Map<String, String> constantPool = new HashMap<String, String>();

	@Override
	boolean buildStaticMembers( ClassWriter _writer )
	{
		this.classWriter = _writer;
		return true; // Not defined yet, but we might. So make sure we get a class initializer.
	}

	@Override
	void compileStaticInitialization( GeneratorAdapter _mv, Type _engineType )
	{
		this.classInitializer = _mv;
		this.classType = _engineType;
	}

	@Override
	void finalizeStaticInitialization( GeneratorAdapter _mv, Type _engineType )
	{
		this.classInitializer = null;
		this.classWriter = null;
		this.classType = null;
	}


	/** The max value of a long is 9,223,372,036,854,775,807, so its max precision is 6 * 3 = 18. */
	private static final int MAX_LONG_PREC = 18;

	private String defineOrReuseStaticConstant( String _value )
	{
		String result = this.constantPool.get( _value );
		if (result == null) {
			result = "C$" + Integer.toString( this.constantPool.size() );
			this.classWriter.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_PRIVATE + Opcodes.ACC_FINAL, result, B, null,
					null ).visitEnd();
			try {
				final long longValue = Long.parseLong( _value );
				this.classInitializer.push( longValue );
				this.classInitializer.visitMethodInsn( Opcodes.INVOKESTATIC, BNAME, "valueOf", L2B );
				compileScaleAdjustment( this.classInitializer );
			}
			catch (NumberFormatException e) {
				final BigDecimal bigValue = new BigDecimal( _value );
				if (!JRE14 && bigValue.precision() <= MAX_LONG_PREC) { // JRE 1.4 cannot handle precision()
					final long longValue = bigValue.unscaledValue().longValue();
					this.classInitializer.push( longValue );
					this.classInitializer.push( bigValue.scale() );
					this.classInitializer.visitMethodInsn( Opcodes.INVOKESTATIC, BNAME, "valueOf", LI2B );
					if (bigValue.scale() != this.scale) {
						compileScaleAdjustment( this.classInitializer );
					}
				}
				else {
					this.classInitializer.push( _value );
					compileRuntimeMethod( this.classInitializer, "newBigDecimal", S2B );
					compileScaleAdjustment( this.classInitializer );
				}
			}
			this.classInitializer.visitFieldInsn( Opcodes.PUTSTATIC, this.classType.getInternalName(), result, B );
			this.constantPool.put( _value, result );
		}
		return result;
	}

	private void compileStaticConstant( MethodVisitor _mv, String _value )
	{
		final String constName = defineOrReuseStaticConstant( _value );
		_mv.visitFieldInsn( Opcodes.GETSTATIC, getCompiler().engine.getInternalName(), constName, B );
	}


	@Override
	void compile( GeneratorAdapter _mv, Operator _operator, int _numberOfArguments ) throws CompilerError
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
	void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws CompilerError
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
			final double dbl = RuntimeDouble_v1.dateToExcel( (Date) _constantValue );
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
	protected String getRoundMethodSignature()
	{
		final String d = getDescriptor();
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

}