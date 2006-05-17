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
import sej.expressions.Operator;
import sej.runtime.RuntimeBigDecimal_v1;

final class ByteCodeBigDecimalType extends ByteCodeNumericType
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
	private final ByteCodeNumericType doubleType;


	public ByteCodeBigDecimalType(NumericType _type, ByteCodeSectionCompiler _compiler)
	{
		super( _type, _compiler );
		this.scale = _type.getScale();
		this.roundingMode = _type.getRoundingMode();
		this.doubleType = ByteCodeNumericType.typeFor( NumericType.DOUBLE, _compiler );
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
	public void compileConst( GeneratorAdapter _mv, Object _constantValue ) throws ModelError
	{
		if (null == _constantValue) {
			compileZero( _mv );
		}
		else if (_constantValue instanceof Number) {
			String val = _constantValue.toString();
			_mv.push( val );
			compileRuntimeMethod( _mv, "newBigDecimal", S2B );
			compileScaleAdjustment( _mv );
		}
		else if (_constantValue instanceof Boolean) {
			_mv.visitFieldInsn( Opcodes.GETSTATIC, BNAME, ((Boolean) _constantValue) ? "ONE" : "ZERO", B );
			compileScaleAdjustment( _mv );
		}
		else if (_constantValue instanceof Date) {
			this.doubleType.compileConst( _mv, _constantValue );
			compileFromDouble( _mv );
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
			compileRuntimeMethod( _mv, "newBigDecimal", L2B );
			compileScaleAdjustment( _mv );
		}
		else {
			_mv.visitFieldInsn( Opcodes.GETSTATIC, BNAME, "ZERO", B );
			compileScaleAdjustment( _mv );
		}
	}


	@Override
	public void compileComparison( GeneratorAdapter _mv, int _comparisonOpcode )
	{
		_mv.visitMethodInsn( Opcodes.INVOKEVIRTUAL, BNAME, "compareTo", "(" + B + ")I" );
	}
	
	
	@Override
	protected String getRoundMethodSignature()
	{
		final String d = getDescriptor();
		return "(" + d + "I)" + d;
	}


	private void compileFromDouble( GeneratorAdapter _mv )
	{
		compileRuntimeMethod( _mv, "newBigDecimal", D2B );
		compileScaleAdjustment( _mv );
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