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

import java.math.BigDecimal;
import java.math.MathContext;

import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.runtime.internal.RuntimeBigDecimal_v2;
import org.formulacompiler.runtime.internal.RuntimePrecisionBigDecimal_v2;


public final class ExpressionTemplatesForPrecisionBigDecimals extends AbstractExpressionTemplatesForBigDecimals
{
	final MathContext mathContext;

	public ExpressionTemplatesForPrecisionBigDecimals( MathContext _mathContext, Environment _env )
	{
		super( _env );
		this.mathContext = _mathContext;
	}


	// ------------------------------------------------ Utils


	@ReturnsAdjustedValue
	BigDecimal util_adjustValue( BigDecimal a )
	{
		/*
		 * If we wanted to always establish the MathContext, we would need
		 * 
		 * return a.round( this.mathContext );
		 * 
		 * here.
		 */
		return a;
	}


	// ------------------------------------------------ Operators


	// Leave this comment in. It is used to cite the code into the documentation.
	// ---- op_PLUS
	@ReturnsAdjustedValue
	public BigDecimal op_PLUS( BigDecimal a, BigDecimal b )
	{
		return a.add( b, this.mathContext );
	}
	// ---- op_PLUS

	@ReturnsAdjustedValue
	public BigDecimal op_MINUS( BigDecimal a, BigDecimal b )
	{
		return a.subtract( b, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_TIMES( BigDecimal a, BigDecimal b )
	{
		return a.multiply( b, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_DIV( BigDecimal a, BigDecimal b )
	{
		return a.divide( b, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_EXP( BigDecimal a, BigDecimal b )
	{
		return RuntimePrecisionBigDecimal_v2.fun_POWER( a, b, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal op_PERCENT( BigDecimal a )
	{
		return a.movePointLeft( 2 );
	}


	// ------------------------------------------------ Numeric Functions


	@ReturnsAdjustedValue
	public BigDecimal fun_DEGREES( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_DEGREES( a, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal fun_RADIANS( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_RADIANS( a, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal fun_CEILING( BigDecimal _number, BigDecimal _significance )
	{
		return RuntimeBigDecimal_v2.fun_CEILING( _number, _significance, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal fun_FLOOR( BigDecimal _number, BigDecimal _significance )
	{
		return RuntimeBigDecimal_v2.fun_FLOOR( _number, _significance, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal fun_POWER( BigDecimal n, BigDecimal p )
	{
		return RuntimeBigDecimal_v2.fun_POWER( n, p, this.mathContext );
	}

	@ReturnsAdjustedValue
	public BigDecimal fun_SQRT( BigDecimal n )
	{
		return RuntimeBigDecimal_v2.fun_SQRT( n, this.mathContext );
	}


	// ------------------------------------------------ Combinatorics


	@ReturnsAdjustedValue
	public BigDecimal fun_FACT( BigDecimal a )
	{
		return RuntimeBigDecimal_v2.fun_FACT( a, this.mathContext );
	}


	// ------------------------------------------------ Financials


	public BigDecimal fun_IRR( BigDecimal[] _values, BigDecimal _guess )
	{
		return RuntimePrecisionBigDecimal_v2.fun_IRR( _values, _guess, this.mathContext );
	}

	public BigDecimal fun_DB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period,
			BigDecimal _month )
	{
		return RuntimePrecisionBigDecimal_v2.fun_DB( _cost, _salvage, _life, _period, _month, this.mathContext );
	}

	public BigDecimal fun_DB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period )
	{
		return RuntimePrecisionBigDecimal_v2.fun_DB( _cost, _salvage, _life, _period, TWELVE, this.mathContext );
	}

	public BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period,
			BigDecimal _factor )
	{
		return RuntimePrecisionBigDecimal_v2.fun_DDB( _cost, _salvage, _life, _period, _factor, this.mathContext );
	}

	public BigDecimal fun_DDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _period )
	{
		return RuntimePrecisionBigDecimal_v2.fun_DDB( _cost, _salvage, _life, _period, TWO, this.mathContext );
	}

	public BigDecimal fun_VDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _start_period, BigDecimal _end_period,
			BigDecimal _factor, BigDecimal _no_switch )
	{
		boolean no_switch = _no_switch.intValue() == 0 ? false : true;
		return RuntimePrecisionBigDecimal_v2.fun_VDB( _cost, _salvage, _life, _start_period, _end_period, _factor, no_switch, this.mathContext );
	}

	public BigDecimal fun_VDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _start_period, BigDecimal _end_period,
			BigDecimal _factor )
	{
		return RuntimePrecisionBigDecimal_v2.fun_VDB( _cost, _salvage, _life, _start_period, _end_period, _factor, false, this.mathContext );
	}

	public BigDecimal fun_VDB( BigDecimal _cost, BigDecimal _salvage, BigDecimal _life, BigDecimal _start_period, BigDecimal _end_period )
	{
		return RuntimePrecisionBigDecimal_v2.fun_VDB( _cost, _salvage, _life, _start_period, _end_period, TWO, false, this.mathContext );
	}


	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv, BigDecimal _type,
			BigDecimal _guess )
	{
		return RuntimePrecisionBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, _fv, _type, _guess, this.mathContext );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv, BigDecimal _type )
	{
		return RuntimePrecisionBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, _fv, _type, TENTH, this.mathContext );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv, BigDecimal _fv )
	{
		return RuntimePrecisionBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, _fv, ZERO, TENTH, this.mathContext );
	}

	public BigDecimal fun_RATE( BigDecimal _nper, BigDecimal _pmt, BigDecimal _pv )
	{
		return RuntimePrecisionBigDecimal_v2.fun_RATE( _nper, _pmt, _pv, ZERO, ZERO, TENTH, this.mathContext );
	}


	// ------------------------------------------------ Date Functions


	public BigDecimal fun_TIME( BigDecimal _hour, BigDecimal _minute, BigDecimal _second )
	{
		return RuntimeBigDecimal_v2.fun_TIME( _hour, _minute, _second, this.mathContext );
	}


}
