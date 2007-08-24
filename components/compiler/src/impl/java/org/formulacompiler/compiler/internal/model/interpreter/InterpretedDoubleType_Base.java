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

import java.util.Date;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.internal.Environment;


abstract class InterpretedDoubleType_Base extends InterpretedNumericType
{

	public InterpretedDoubleType_Base( NumericType _type, Environment _env )
	{
		super( _type, _env );
	}


	@Override
	public Object adjustConstantValue( Object _value )
	{
		return _value;
	}


	@Override
	public Number toNumeric( Number _value )
	{
		return valueToDoubleOrZero( _value );
	}


	@Override
	protected int compareNumerically( Object _a, Object _b )
	{
		double a = valueToDoubleOrZero( _a );
		double b = valueToDoubleOrZero( _b );
		return Double.compare( a, b );
	}


	private final double valueToDouble( Object _value, double _ifNull )
	{
		if (_value instanceof Number) return ((Number) _value).doubleValue();
		if (_value instanceof String) return Double.valueOf( (String) _value );
		if (_value instanceof Date) {
			throw new IllegalArgumentException( "Cannot interpret java.util.Date - it is runtime time-zone specific." );
		}
		return _ifNull;
	}

	private final double valueToDoubleOrZero( Object _value )
	{
		return valueToDouble( _value, 0.0 );
	}


	// Conversions for generated code:

	protected final double to_double( Object _value )
	{
		return valueToDoubleOrZero( _value );
	}

	protected final double[] to_array( Object _value )
	{
		final Object[] consts = asArrayOfConsts( _value );
		final double[] r = new double[ consts.length ];
		int i = 0;
		for (Object cst : consts) {
			r[ i++ ] = to_double( cst );
		}
		return r;
	}

}