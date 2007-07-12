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
package org.formulacompiler.compiler.internal.templates;

import static org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2.TENTH;
import static org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2.TWELVE;
import static org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2.TWO;
import static org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2.ZERO;
import static org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2.HIGHPREC;
import static org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2.UNLIMITED;

import java.math.BigDecimal;

import org.formulacompiler.runtime.FormulaRuntime;
import org.formulacompiler.runtime.internal.RuntimeScaledBigDecimal_v2;


public final class ExpressionTemplatesForScaledBigDecimals extends AbstractExpressionTemplatesForBigDecimals
{
	final boolean isScaled;
	final int fixedScale;
	final int roundingMode;


	public ExpressionTemplatesForScaledBigDecimals(int _scale, int _roundingMode)
	{
		super();
		this.isScaled = (_scale != FormulaRuntime.UNDEFINED_SCALE);
		this.fixedScale = _scale;
		this.roundingMode = _roundingMode;
	}


	// ------------------------------------------------ Utils


	@ReturnsAdjustedValue
	BigDecimal util_adjustValue( BigDecimal a )
	{
		return a.setScale( this.fixedScale, this.roundingMode );
	}


	// ------------------------------------------------ Operators


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- op_PLUS
	@ReturnsAdjustedValue
	public BigDecimal op_PLUS( BigDecimal a, BigDecimal b )
	{
		return a.add( b );
	}
	// ---- op_PLUS

	@ReturnsAdjustedValue
	public BigDecimal op_MINUS( BigDecimal a, BigDecimal b )
	{
		return a.subtract( b );
	}

	public BigDecimal op_TIMES( BigDecimal a, BigDecimal b )
	{
		return a.multiply( b );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_DIV__if_needsValueAdjustment( BigDecimal a, BigDecimal b )
	{
		return a.divide( b, this.fixedScale, this.roundingMode );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_DIV( BigDecimal a, BigDecimal b )
	{
		return a.divide( b );
	}

	public BigDecimal op_EXP( BigDecimal a, BigDecimal b )
	{
		return RuntimeScaledBigDecimal_v2.fun_POWER( a, b, UNLIMITED );
	}

	public BigDecimal op_PERCENT( BigDecimal a )
	{
		return a.movePointLeft( 2 );
	}

	
	// ------------------------------------------------ Numeric Functions


	public BigDecimal fun_DEGREES( BigDecimal a )
	{
		return RuntimeScaledBigDecimal_v2.fun_DEGREES( a );
	}

	public BigDecimal fun_RADIANS( BigDecimal a )
	{
		return RuntimeScaledBigDecimal_v2.fun_RADIANS( a );
	}

	public BigDecimal fun_POWER( BigDecimal n, BigDecimal p )
	{
		return RuntimeScaledBigDecimal_v2.fun_POWER( n, p, UNLIMITED );
	}

	public BigDecimal fun_SQRT( BigDecimal n )
	{
		return RuntimeScaledBigDecimal_v2.fun_SQRT( n, HIGHPREC );
	}


	// ------------------------------------------------ Combinatorics


	public BigDecimal fun_FACT( BigDecimal a )
	{
		return RuntimeScaledBigDecimal_v2.fun_FACT( a, UNLIMITED );
	}


	// ------------------------------------------------ Financials


	public BigDecimal fun_IRR( BigDecimal[] _values, BigDecimal _guess )
	{
		return RuntimeScaledBigDecimal_v2.fun_IRR( _values, _guess, HIGHPREC  );
	}

	public BigDecimal fun_DB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period, BigDecimal _month )
	{
		return RuntimeScaledBigDecimal_v2.fun_DB( _cost, _salvage, _life, _period, _month, HIGHPREC );
	}

	public BigDecimal fun_DB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period )
	{
		return RuntimeScaledBigDecimal_v2.fun_DB( _cost, _salvage, _life, _period, TWELVE, HIGHPREC );
	}

	public BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period, BigDecimal _factor )
	{
		return RuntimeScaledBigDecimal_v2.fun_DDB( _cost, _salvage, _life, _period, _factor, HIGHPREC );
	}

	public BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period )
	{
		return RuntimeScaledBigDecimal_v2.fun_DDB( _cost, _salvage, _life, _period, TWO, HIGHPREC );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv, BigDecimal _type, BigDecimal _guess )
	{
		return RuntimeScaledBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, _fv, _type, _guess, HIGHPREC );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv, BigDecimal _type )
	{
		return RuntimeScaledBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, _fv, _type, TENTH, HIGHPREC );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv )
	{
		return RuntimeScaledBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, _fv, ZERO, TENTH, HIGHPREC );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv )
	{
		return RuntimeScaledBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, ZERO, ZERO, TENTH, HIGHPREC );
	}


	// ------------------------------------------------ Date Functions


	public BigDecimal fun_TIME( BigDecimal _hour, BigDecimal _minute, BigDecimal _second )
	{
		return RuntimeScaledBigDecimal_v2.fun_TIME( _hour, _minute, _second, HIGHPREC );
	}


}
