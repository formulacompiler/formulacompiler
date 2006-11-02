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

import sej.CompilerException;
import sej.NumericType;
import sej.internal.runtime.RuntimeBigDecimal_v1;
import sej.internal.runtime.RuntimeDouble_v1;


public class TypeCompilerForBigDecimals extends TypeCompilerForNumbers
{
	static final String BNAME = ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getInternalName();
	static final String B = ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getDescriptor();
	static final String V2B = "()" + B;
	static final String I2B = "(I)" + B;
	static final String L2B = "(" + Type.LONG_TYPE.getDescriptor() + ")" + B;
	static final String LI2B = "(" + Type.LONG_TYPE.getDescriptor() + "I)" + B;
	static final String D2B = "(D)" + B;
	static final String S2B = "(Ljava/lang/String;)" + B;
	static final String B2B = ("(" + B + ")") + B;
	static final String BB2B = "(" + B + B + ")" + B;
	static final String BII2B = "(" + B + "II)" + B;
	static final String N2L = "(" + N + ")" + Type.LONG_TYPE.getDescriptor();
	static final String N2D = "(" + N + ")D";

	private static final Type RUNTIME_TYPE = Type.getType( RuntimeBigDecimal_v1.class );

	private final int fixedScale;
	private final int roundingMode;


	protected TypeCompilerForBigDecimals(ByteCodeEngineCompiler _engineCompiler, NumericType _numericType)
	{
		super( _engineCompiler, _numericType );
		this.fixedScale = _numericType.getScale();
		this.roundingMode = _numericType.getRoundingMode();
	}

	@Override
	protected Type runtimeType()
	{
		return RUNTIME_TYPE;
	}

	@Override
	protected Type type()
	{
		return ByteCodeEngineCompiler.BIGDECIMAL_CLASS;
	}

	@Override
	protected int returnOpcode()
	{
		return Opcodes.ARETURN;
	}


	final boolean needsAdjustment()
	{
		return NumericType.UNDEFINED_SCALE != this.fixedScale;
	}

	final void compileAdjustment( GeneratorAdapter _mv )
	{
		if (needsAdjustment()) {
			_mv.push( this.fixedScale );
			_mv.push( this.roundingMode );
			_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "setScale", "(II)" + B );
		}
	}


	@Override
	protected void compileZero( GeneratorAdapter _mv ) throws CompilerException
	{
		_mv.getStatic( runtimeType(), "ZERO", ByteCodeEngineCompiler.BIGDECIMAL_CLASS );
		compileAdjustment( _mv );
	}


	private final void compileOne( GeneratorAdapter _mv )
	{
		_mv.getStatic( runtimeType(), "ONE", ByteCodeEngineCompiler.BIGDECIMAL_CLASS );
		compileAdjustment( _mv );
	}


	@Override
	protected void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException
	{
		if (null == _value) {
			compileZero( _mv );
		}
		else if (_value instanceof Number) {
			String val = _value.toString();
			compileStaticConstant( _mv, val );
		}
		else if (_value instanceof Boolean) {
			if ((Boolean) _value) {
				compileOne( _mv );
			}
			else {
				compileZero( _mv );
			}
		}
		else if (_value instanceof Date) {
			final double dbl = RuntimeDouble_v1.dateToNum( (Date) _value );
			compileStaticConstant( _mv, Double.toString( dbl ) );
		}
		else {
			throw new CompilerException.UnsupportedDataType( "BigDecimal constant cannot be of type "
					+ _value.getClass().getName() );
		}
	}


	private final Map<String, String> constantPool = new HashMap<String, String>();

	/** The max value of a long is 9,223,372,036,854,775,807, so its max precision is 6 * 3 = 18. */
	private static final int MAX_LONG_PREC = 18;

	private final String defineOrReuseStaticConstant( String _value )
	{
		String result = this.constantPool.get( _value );
		if (result == null) {
			final ClassWriter cw = rootCompiler().cw();
			final GeneratorAdapter ci = rootCompiler().initializer();
			result = "C$" + Integer.toString( this.constantPool.size() );
			cw.visitField( Opcodes.ACC_STATIC + Opcodes.ACC_FINAL, result, B, null, null ).visitEnd();
			final BigDecimal bigValue = new BigDecimal( _value );
			if (bigValue.precision() <= MAX_LONG_PREC) {
				final long longValue = bigValue.unscaledValue().longValue();
				ci.push( longValue );
				ci.push( bigValue.scale() );
				ci.visitMethodInsn( Opcodes.INVOKESTATIC, BNAME, "valueOf", LI2B );
				if (bigValue.scale() != this.fixedScale) {
					compileAdjustment( ci );
				}
			}
			else {
				ci.push( _value );
				compileRuntimeMethod( ci, "newBigDecimal", S2B );
				compileAdjustment( ci );
			}
			ci.visitFieldInsn( Opcodes.PUTSTATIC, rootCompiler().classInternalName(), result, B );
			this.constantPool.put( _value, result );
		}
		return result;
	}


	private final void compileStaticConstant( MethodVisitor _mv, String _value )
	{
		final String constName = defineOrReuseStaticConstant( _value );
		_mv.visitFieldInsn( Opcodes.GETSTATIC, rootCompiler().classInternalName(), constName, B );
	}


}
