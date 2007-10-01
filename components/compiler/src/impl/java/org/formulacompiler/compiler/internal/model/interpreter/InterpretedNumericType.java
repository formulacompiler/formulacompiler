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
package org.formulacompiler.compiler.internal.model.interpreter;

import java.math.BigDecimal;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.internal.AbstractLongType;
import org.formulacompiler.compiler.internal.Util;
import org.formulacompiler.runtime.internal.Environment;


public abstract class InterpretedNumericType extends InterpretedNumericType_GeneratedStrings
{


	public static InterpretedNumericType typeFor( NumericType _type, Environment _env )
	{
		if (Double.TYPE == _type.valueType()) {
			return new InterpretedDoubleType( _type, _env );
		}
		else if (BigDecimal.class == _type.valueType()) {
			if (null != _type.mathContext()) {
				return new InterpretedPrecisionBigDecimalType( _type, _env );
			}
			else {
				return new InterpretedScaledBigDecimalType( _type, _env );
			}
		}
		else if (Long.TYPE == _type.valueType()) {
			return new InterpretedScaledLongType( (AbstractLongType) _type, _env );
		}
		else {
			throw new IllegalArgumentException( "Unsupported numeric type for run-time interpretation." );
		}
	}

	public static InterpretedNumericType typeFor( NumericType _type )
	{
		Util.assertTesting();
		return typeFor( _type, Environment.DEFAULT );
	}


	InterpretedNumericType( NumericType _type, Environment _env )
	{
		super( _type, _env );
	}


}
