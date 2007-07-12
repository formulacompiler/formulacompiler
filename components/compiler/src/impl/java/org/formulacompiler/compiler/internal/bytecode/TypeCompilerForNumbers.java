/*
 * Copyright (c) 2006 by Abacus Research AG, Switzerland.
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
package org.formulacompiler.compiler.internal.bytecode;

import java.math.BigDecimal;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.expressions.DataType;
import org.objectweb.asm.Type;


abstract class TypeCompilerForNumbers extends TypeCompiler
{
	protected final static Type NUMBER_CLASS = Type.getType( Number.class );
	protected final static String N = NUMBER_CLASS.getDescriptor();


	public static TypeCompilerForNumbers compilerFor( ByteCodeEngineCompiler _engineCompiler, NumericType _numericType )
	{
		if (Double.TYPE == _numericType.valueType()) {
			return new TypeCompilerForDoubles( _engineCompiler, _numericType );
		}
		else if (Long.TYPE == _numericType.valueType()) {
			return new TypeCompilerForScaledLongs( _engineCompiler, _numericType );
		}
		else if (BigDecimal.class == _numericType.valueType()) {
			if (null != _numericType.mathContext()) {
				return new TypeCompilerForPrecisionBigDecimals( _engineCompiler, _numericType );
			}
			else {
				return new TypeCompilerForScaledBigDecimals( _engineCompiler, _numericType );
			}
		}
		else {
			throw new IllegalArgumentException( "Unsupported data type " + _numericType + " for byte code compilation." );
		}
	}

	private final NumericType numericType;

	public TypeCompilerForNumbers(ByteCodeEngineCompiler _engineCompiler, NumericType _numericType)
	{
		super( _engineCompiler );
		this.numericType = _numericType;
	}

	@Override
	protected DataType dataType()
	{
		return DataType.NUMERIC;
	}

	protected final NumericType numericType()
	{
		return this.numericType;
	}

}
