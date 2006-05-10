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
package sej;

import java.math.BigDecimal;


/**
 * Immutable class representing the type to be used by the numeric computations of generated
 * engines.
 * 
 * @author peo
 */
public final class NumericType
{

	/**
	 * For BigDecimal types, indicates that no explicit scaling should be performed by the engine.
	 */
	public static final int UNDEFINED_SCALE = Integer.MAX_VALUE;

	/**
	 * Default type, consistent with the type used internally by Excel and other spreadsheet
	 * applications.
	 */
	public static final NumericType DOUBLE = getInstance( Double.TYPE );

	/**
	 * BigDecimal with a fixed scale of 8 and using {@link BigDecimal#ROUND_HALF_UP}. A good choice
	 * for financial applications.
	 */
	public static final NumericType BIGDECIMAL8 = getInstance( BigDecimal.class, 8 );

	/**
	 * BigDecimal with a fixed scale of 9 and using {@link BigDecimal#ROUND_HALF_UP}. This type has
	 * the same precision as {@link #DOUBLE} for the automated tests.
	 */
	public static final NumericType BIGDECIMAL9 = getInstance( BigDecimal.class, 9 );

	/**
	 * Unscaled {@code long} for fast, strictly integer computations.
	 */
	public static final NumericType LONG = getInstance( Long.TYPE );

	/**
	 * Scaled {@code long} with 4 decimal places. Corresponds to the Currency type found in Microsoft
	 * COM and Borland Delphi.
	 */
	public static final NumericType CURRENCY = getInstance( Long.TYPE, 4 );

	private final Class valueType;
	private final int scale;
	private final int roundingMode;

	/**
	 * To ensure compatibility with JRE 1.4 I cannot use a MathContext here.
	 */
	private NumericType(Class _valueType, int _scale, int _roundingMode)
	{
		super();
		this.valueType = _valueType;
		this.scale = _scale;
		this.roundingMode = _roundingMode;
	}


	/**
	 * Same as {@link #getInstance(Class, int, int)} with an undefined scale and rounding halves up.
	 */
	public static NumericType getInstance( Class _valueType )
	{
		return getInstance( _valueType, UNDEFINED_SCALE, BigDecimal.ROUND_HALF_UP );
	}

	/**
	 * Same as {@link #getInstance(Class, int, int)} rounding halves up.
	 */
	public static NumericType getInstance( Class _valueType, int _scale )
	{
		return new NumericType( _valueType, _scale, BigDecimal.ROUND_HALF_UP );
	}

	/**
	 * Returns an instance with the specified attributes. 
	 */
	public static NumericType getInstance( Class _valueType, int _scale, int _roundingMode )
	{
		return new NumericType( _valueType, _scale, _roundingMode );
	}


	public Class getValueType()
	{
		return this.valueType;
	}

	public int getScale()
	{
		return this.scale;
	}

	public int getRoundingMode()
	{
		return this.roundingMode;
	}


	@Override
	public String toString()
	{
		return getValueType().getName() + ((UNDEFINED_SCALE != getScale()) ? "." + Integer.toString( getScale() ) : "");
	}

}
