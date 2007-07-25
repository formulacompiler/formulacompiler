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
package org.formulacompiler.compiler.internal;

import java.math.BigDecimal;
import java.util.Locale;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeDouble_v2;

public final class DoubleType extends NumericTypeImpl
{

	protected DoubleType()
	{
		super( Double.TYPE, NumericTypeImpl.UNDEFINED_SCALE, BigDecimal.ROUND_UNNECESSARY );
	}

	@Override
	public Number getZero()
	{
		return Double.valueOf( 0.0 );
	}

	@Override
	public Number getOne()
	{
		return Double.valueOf( 1.0 );
	}

	@Override
	protected Double assertProperNumberType( Number _value )
	{
		return (Double) _value;
	}

	@Override
	protected Number convertFromAnyNumber( Number _value )
	{
		if (_value instanceof Double) return _value;
		return _value.doubleValue();
	}

	@Override
	protected String convertToConciseString( Number _value, Locale _locale )
	{
		final Environment environment = new Environment( new Computation.Config( _locale ) ); //FIXME Environment should be passed as a parameter
		// We want to be sure this is a double here.
		return RuntimeDouble_v2.toExcelString( _value.doubleValue(), environment );
	}

}