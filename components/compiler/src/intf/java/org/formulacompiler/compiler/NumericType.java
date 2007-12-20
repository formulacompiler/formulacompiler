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

import java.math.MathContext;
import java.text.ParseException;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.FormulaRuntime;

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
	public static final int UNDEFINED_SCALE = FormulaRuntime.UNDEFINED_SCALE;

	/**
	 * Indicates no final rounding due to number formats with a maximum number of fractional digits. 
	 */
	public static final int UNLIMITED_FRACTIONAL_DIGITS = Integer.MAX_VALUE;
	
	/**
	 * Returns the Java class of the base type.
	 */
	public Class valueType();

	/**
	 * Returns the MathContext to use, or else {@code null}.
	 */
	public MathContext mathContext();

	/**
	 * Returns the fixed scale, or else {@link #UNDEFINED_SCALE}.
	 */
	public int scale();

	/**
	 * Returns the rounding mode.
	 */
	public int roundingMode();

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
	 * 
	 * @return an instance of the corresponding (boxed) Java number type, or null.
	 * 
	 * @see #valueType()
	 */
	public Number valueOf( Number _value );

	/**
	 * Parses a string into a value using the default environment config. Null and the empty string
	 * return zero (see {@link #getZero()}).
	 * 
	 * @return an instance of the corresponding (boxed) Java number type.
	 * 
	 * @throws ParseException
	 * 
	 * @see #valueOf(String, Computation.Config)
	 * @see #valueType()
	 */
	public Number valueOf( String _value ) throws ParseException;

	/**
	 * Parses a string into a value using the given environment config. Null and the empty string
	 * return zero (see {@link #getZero()}).
	 * 
	 * @param _config determines formatting options; must not be {@code null}.
	 * @return an instance of the corresponding (boxed) Java number type.
	 * @throws ParseException
	 * 
	 * @see #valueType()
	 */
	public Number valueOf( String _value, Computation.Config _config ) throws ParseException;

	/**
	 * Returns the value as a string in its canonical representation using the default environment
	 * config. Null returns the empty string.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * 
	 * @see #valueToString(Number, Computation.Config)
	 * @see #valueType()
	 */
	public String valueToString( Number _value );

	/**
	 * Returns the value as a string in its canonical representation using the given environment
	 * config. Null returns the empty string.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * @param _config determines formatting options; must not be {@code null}.
	 * 
	 * @see #valueType()
	 */
	public String valueToString( Number _value, Computation.Config _config );

	/**
	 * Returns the value as a string with no superfluous leading or trailing zeroes and decimal point
	 * using the default environment config. Null returns the empty string. Uses scientific display
	 * the way Excel does.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * 
	 * @see #valueToConciseString(Number, Computation.Config)
	 * @see #valueType()
	 */
	public String valueToConciseString( Number _value );

	/**
	 * Returns the value as a string with no superfluous leading or trailing zeroes and decimal point
	 * using the given environment config. Null returns the empty string. Uses scientific display the
	 * way Excel does.
	 * 
	 * @param _value must be an instance of the corresponding (boxed) Java number type, or null.
	 * @param _config determines formatting options; must not be {@code null}.
	 * 
	 * @see #valueType()
	 */
	public String valueToConciseString( Number _value, Computation.Config _config );


	/**
	 * Factory interface for
	 * {@link org.formulacompiler.runtime.ImplementationLocator#getInstance(Class)}.
	 */
	public static interface Factory
	{
		/**
		 * Factory method.
		 */
		NumericType getInstance( Class _valueType, int _scale, int _roundingMode );

		/**
		 * Factory method.
		 */
		NumericType getInstance( Class _valueType, MathContext _mathContext );
	}

}
