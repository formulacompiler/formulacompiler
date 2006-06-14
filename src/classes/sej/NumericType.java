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
public interface NumericType
{

	/**
	 * For BigDecimal types, indicates that no explicit scaling should be performed by the engine.
	 */
	public static final int UNDEFINED_SCALE = Integer.MAX_VALUE;

	/**
	 * Default type, consistent with the type used internally by Excel and other spreadsheet
	 * applications.
	 */
	public static final NumericType DOUBLE = SEJ.getNumericType( Double.TYPE );

	/**
	 * BigDecimal with a fixed scale of 8 and using {@link BigDecimal#ROUND_HALF_UP}. A good choice
	 * for financial applications.
	 */
	public static final NumericType BIGDECIMAL8 = SEJ.getNumericType( BigDecimal.class, 8, BigDecimal.ROUND_HALF_UP );

	/**
	 * BigDecimal with a fixed scale of 9 and using {@link BigDecimal#ROUND_HALF_UP}. This type has
	 * the same precision as {@link #DOUBLE} for the automated tests.
	 */
	public static final NumericType BIGDECIMAL9 = SEJ.getNumericType( BigDecimal.class, 9, BigDecimal.ROUND_HALF_UP );

	/**
	 * Unscaled {@code long} for fast, strictly integer computations.
	 */
	public static final NumericType LONG = SEJ.getNumericType( Long.TYPE, 0 );

	/**
	 * {@code long} scaled to 4 decimal places for fast, fixed point computations (similar to the
	 * currency type found in Microsoft COM and Borland Delphi).
	 */
	public static final NumericType LONG4 = SEJ.getNumericType( Long.TYPE, 4 );

	/**
	 * Scaled {@code long} with 4 decimal places. Corresponds to the Currency type found in Microsoft
	 * COM and Borland Delphi.
	 */
	public static final NumericType CURRENCY = SEJ.getNumericType( Long.TYPE, 4 );

	/**
	 * Default type used when no explicit type is specified.
	 */
	public static final NumericType DEFAULT = DOUBLE;


	/**
	 * Returns the Java class of the base type.
	 */
	public Class getValueType();
	
	/**
	 * Returns the fixed scale, or else {@link #UNDEFINED_SCALE}.
	 */
	public int getScale();
	
	/**
	 * Returns the rounding mode.
	 */
	public int getRoundingMode();

	/**
	 * Returns the number 0.
	 */
	public Number getZero();

	/**
	 * Returns the number 1.
	 */
	public Number getOne();

	/**
	 * Converts a number to this type. Null is returned as null.
	 */
	public Number valueOf( Number _value );

	/**
	 * Parses a string into a value. Null and the empty string return zero (see {@link #getZero()}).
	 */
	public Number valueOf( String _value );

	/**
	 * Returns the value as a string in its canonical representation. Null returns the empty string.
	 */
	public String valueToString( Number _value );

	/**
	 * Returns the value as a string with no superfluous leading or trailing zeroes and decimal
	 * point. Null returns the empty string.
	 */
	public String valueToConciseString( Number _value );

}
