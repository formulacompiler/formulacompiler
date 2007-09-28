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
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;


abstract class InterpretedScaledBigDecimalType_Base extends InterpretedBigDecimalType
{
	private final int scale;
	private final int roundingMode;


	public InterpretedScaledBigDecimalType_Base( NumericType _type, Environment _env )
	{
		super( _type, _env );
		this.scale = _type.scale();
		this.roundingMode = _type.roundingMode();
	}


	public BigDecimal adjustScale( BigDecimal _value )
	{
		if (RuntimeBigDecimal_v2.EXTREMUM != _value && NumericType.UNDEFINED_SCALE != this.scale) {
			return _value.setScale( this.scale, this.roundingMode );
		}
		else {
			return _value;
		}
	}

	@Override
	protected BigDecimal adjustConvertedValue( BigDecimal _value )
	{
		return adjustScale( _value );
	}


	// Conversions for generated code:

	protected final boolean needsValueAdjustment()
	{
		return (NumericType.UNDEFINED_SCALE != this.scale);
	}

	protected final BigDecimal adjustReturnedValue( BigDecimal _b )
	{
		return adjustScale( _b );
	}

}