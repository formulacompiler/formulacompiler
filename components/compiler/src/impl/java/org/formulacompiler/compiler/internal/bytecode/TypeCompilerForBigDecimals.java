/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler.internal.bytecode;

import java.math.BigDecimal;
import java.util.Map;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;

abstract class TypeCompilerForBigDecimals extends TypeCompilerForNumbers
{
	static final String BNAME = ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getInternalName();
	static final String B = ByteCodeEngineCompiler.BIGDECIMAL_CLASS.getDescriptor();
	private static final String LI2B = "(" + Type.LONG_TYPE.getDescriptor() + "I)" + B;
	private static final String S2B = "(Ljava/lang/String;)" + B;

	protected TypeCompilerForBigDecimals( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		super( _engineCompiler, _numericType );
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


	abstract boolean needsAdjustment();
	abstract boolean needsAdjustment( BigDecimal _value );
	abstract void compileAdjustment( GeneratorAdapter _mv );


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
	protected void compileMinValue( GeneratorAdapter _mv ) throws CompilerException
	{
		compileExtremum( _mv );
	}

	@Override
	protected void compileMaxValue( GeneratorAdapter _mv ) throws CompilerException
	{
		compileExtremum( _mv );
	}

	private void compileExtremum( GeneratorAdapter _mv )
	{
		_mv.getStatic( runtimeType(), "EXTREMUM", ByteCodeEngineCompiler.BIGDECIMAL_CLASS );
	}

	@Override
	protected void compileConst( GeneratorAdapter _mv, Object _value ) throws CompilerException
	{
		if (null == _value) {
			compileZero( _mv );
		}
		else if (_value == RuntimeBigDecimal_v2.EXTREMUM) {
			compileExtremum( _mv );
		}
		else if (_value instanceof Number) {
			final String val = _value.toString();
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
		else {
			throw new CompilerException.UnsupportedDataType( "BigDecimal constant cannot be of type "
					+ _value.getClass().getName() );
		}
	}


	private final Map<String, String> constantPool = New.map();

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
				if (needsAdjustment( bigValue )) {
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
