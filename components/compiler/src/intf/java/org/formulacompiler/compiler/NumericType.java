/*
 * Copyright (c) 2006-2009 by Abacus Research AG, Switzerland.
 * All rights reserved.
 *
 * This file is part of the Abacus Formula Compiler (AFC).
 *
 * For commercial licensing, please contact sales(at)formulacompiler.com.
 *
 * AFC is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * AFC is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with AFC.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.formulacompiler.compiler;

import java.math.MathContext;
import java.text.ParseException;

import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.FormulaRuntime;

/**
 * Immutable class representing the type to be used by the numeric computations of generated
 * engines.
 * <p>
 * <em>This interface is an API only. Do not implement it yourself.</em>
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
