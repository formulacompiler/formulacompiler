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
package org.formulacompiler.compiler;

import java.math.BigDecimal;
import java.math.MathContext;

import org.formulacompiler.runtime.ImplementationLocator;
import org.formulacompiler.runtime.FormulaRuntime;


/**
 * Static class defining factory methods for the various elements of the core AFC compiler. This
 * class is extends by {@link org.formulacompiler.runtime.FormulaRuntime} which provides factory
 * methods for the run-time-only elements.
 * 
 * @author peo
 */
public class FormulaCompiler extends FormulaRuntime
{


	/**
	 * Returns the numeric type instance with the specified attributes.
	 * 
	 * @param _valueType must be either {@code Double.TYPE}, {@code Long.TYPE}, or
	 *           {@code BigDecimal.class}.
	 * @param _scale defines the number of decimal places after the point to which the base type is
	 *           scaled. Only supported for Long and BigDecimal.
	 * @param _roundingMode defines the rounding mode to use when establishing the scale. Only
	 *           supported for BigDecimal.
	 * @return the instance.
	 */
	public static NumericType getNumericType( Class _valueType, int _scale, int _roundingMode )
	{
		return NUMERIC_TYPE_FACTORY.getInstance( _valueType, _scale, _roundingMode );
	}

	private static final NumericType.Factory NUMERIC_TYPE_FACTORY = ImplementationLocator
			.getInstance( NumericType.Factory.class );


	/**
	 * Same as {@link #getNumericType(Class, int, int)} with an undefined scale and truncating
	 * results.
	 */
	public static NumericType getNumericType( Class _valueType )
	{
		return getNumericType( _valueType, NumericType.UNDEFINED_SCALE, BigDecimal.ROUND_DOWN );
	}

	/**
	 * Same as {@link #getNumericType(Class, int, int)} and truncating results.
	 */
	public static NumericType getNumericType( Class _valueType, int _scale )
	{
		return getNumericType( _valueType, _scale, BigDecimal.ROUND_DOWN );
	}

	/**
	 * Returns the numeric type instance with the specified attributes.
	 * 
	 * @param _valueType must be {@code BigDecimal.class}.
	 * @param _mathContext defines the precision and rounding mode used to limit intermediate and
	 *           final results.
	 * @return the instance.
	 */
	public static NumericType getNumericType( Class _valueType, MathContext _mathContext )
	{
		return NUMERIC_TYPE_FACTORY.getInstance( _valueType, _mathContext );
	}


	/**
	 * Default type, consistent with the type used internally by Excel and other spreadsheet
	 * applications.
	 */
	public static final NumericType DOUBLE = getNumericType( Double.TYPE );

	/**
	 * BigDecimal with the {@link java.math.MathContext#DECIMAL32} math context.
	 */
	public static final NumericType BIGDECIMAL32 = getNumericType( BigDecimal.class, MathContext.DECIMAL32 );

	/**
	 * BigDecimal with the {@link java.math.MathContext#DECIMAL64} math context.
	 */
	public static final NumericType BIGDECIMAL64 = getNumericType( BigDecimal.class, MathContext.DECIMAL64 );

	/**
	 * BigDecimal with the {@link java.math.MathContext#DECIMAL128} math context.
	 */
	public static final NumericType BIGDECIMAL128 = getNumericType( BigDecimal.class, MathContext.DECIMAL128 );

	/**
	 * BigDecimal with a fixed scale of 8 and using {@link BigDecimal#ROUND_HALF_UP}.
	 */
	public static final NumericType BIGDECIMAL_SCALE8 = getNumericType( BigDecimal.class, 8, BigDecimal.ROUND_HALF_UP );

	/**
	 * BigDecimal with a fixed scale of 9 and using {@link BigDecimal#ROUND_HALF_UP}. This type has
	 * the same precision as {@link #DOUBLE} for the automated tests.
	 */
	public static final NumericType BIGDECIMAL_SCALE9 = getNumericType( BigDecimal.class, 9, BigDecimal.ROUND_HALF_UP );

	/**
	 * Unscaled {@code long} for fast, strictly integer computations.
	 */
	public static final NumericType LONG = getNumericType( Long.TYPE, 0 );

	/**
	 * {@code long} scaled to 4 decimal places for fast, fixed point computations (similar to the
	 * currency type found in Borland Delphi). Beware: this type has insufficient precision for
	 * seconds in time values.
	 */
	public static final NumericType LONG_SCALE4 = getNumericType( Long.TYPE, 4 );

	/**
	 * {@code long} scaled to 6 decimal places for fast, fixed point computations with sufficient
	 * precision for seconds in time values.
	 */
	public static final NumericType LONG_SCALE6 = getNumericType( Long.TYPE, 6 );

	/**
	 * Default type used when no explicit type is specified.
	 */
	public static final NumericType DEFAULT_NUMERIC_TYPE = DOUBLE;


}
