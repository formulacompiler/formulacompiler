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

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.MathContext;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.Date;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.internal.Environment;


/**
 * Immutable class representing the type to be used by the numeric computations of generated
 * engines.
 * 
 * @author peo
 */
public abstract class NumericTypeImpl implements NumericType
{


	public static final class Factory implements NumericType.Factory
	{

		public NumericType getInstance( Class _valueType, int _scale, int _roundingMode )
		{
			if (_valueType == Double.TYPE) {
				if (_scale != NumericTypeImpl.UNDEFINED_SCALE)
					throw new IllegalArgumentException( "Scale is not supported for double" );
				if (_roundingMode != BigDecimal.ROUND_DOWN)
					throw new IllegalArgumentException( "Rounding is not supported for double" );
				return new DoubleType();
			}
			else if (_valueType == Long.TYPE) {
				if (_scale == 0) {
					if (_roundingMode != BigDecimal.ROUND_DOWN)
						throw new IllegalArgumentException( "Unscaled long can only be rounded down" );
					return new LongType();
				}
				else {
					if (_roundingMode != BigDecimal.ROUND_DOWN)
						throw new IllegalArgumentException( "Scaled long can only be rounded down" );
					return new ScaledLongType( _scale );
				}
			}
			else if (_valueType == BigDecimal.class) {
				return new ScaledBigDecimalType( _scale, _roundingMode );
			}
			throw new IllegalArgumentException( "Unsupported numeric type" );
		}

		public NumericType getInstance( Class _valueType, MathContext _mathContext )
		{
			if (_valueType == BigDecimal.class) {
				return new PrecisionBigDecimalType( _mathContext );
			}
			throw new IllegalArgumentException( "Unsupported numeric type" );
		}

	}


	/*
	 * The combination of MathContext and explicit scale/rounding mode looks kludgy - and it is. We
	 * would really need multiple inheritance here to have common base for BigDec-based types and
	 * scaled vs precision-based types.
	 */
	private final Class valueType;
	private final int scale;
	private final int roundingMode;
	private final MathContext mathContext;


	protected NumericTypeImpl( Class _valueType, int _scale, int _roundingMode )
	{
		super();
		this.valueType = _valueType;
		this.scale = _scale;
		this.roundingMode = _roundingMode;
		this.mathContext = null;
	}

	protected NumericTypeImpl( Class _valueType, MathContext _mathContext )
	{
		super();
		this.valueType = _valueType;
		this.mathContext = _mathContext;
		this.scale = UNDEFINED_SCALE;
		this.roundingMode = _mathContext.getRoundingMode().ordinal();
	}


	public Class valueType()
	{
		return this.valueType;
	}

	public int scale()
	{
		return this.scale;
	}

	public int roundingMode()
	{
		return this.roundingMode;
	}

	public MathContext mathContext()
	{
		return this.mathContext;
	}


	public abstract Number getZero();
	public abstract Number getOne();


	public Number valueOf( Number _value )
	{
		if (null == _value) return null;
		return assertProperNumberType( convertFromAnyNumber( _value ) );
	}

	@Deprecated
	public final Number valueOf( String _value ) throws ParseException
	{
		return valueOf( _value, Environment.DEFAULT );
	}

	public final Number valueOf( String _value, Computation.Config _config ) throws ParseException
	{
		return valueOf( _value, Environment.getInstance( _config ) );
	}

	public final Number valueOf( String _value, Environment _env ) throws ParseException
	{
		if (null == _value) return getZero();
		if (0 == _value.length()) return getZero();
		return assertProperNumberType( convertFromString( _value, _env ) );
	}

	@Deprecated
	public final String valueToString( Number _value )
	{
		return valueToString( _value, Environment.DEFAULT );
	}

	public final String valueToString( Number _value, Computation.Config _config )
	{
		return valueToString( _value, Environment.getInstance( _config ) );
	}

	public final String valueToString( Number _value, Environment _env )
	{
		if (null == _value) return "";
		return convertToString( assertProperNumberType( _value ), _env );
	}

	@Deprecated
	public final String valueToConciseString( Number _value )
	{
		return valueToConciseString( _value, Environment.DEFAULT );
	}

	public final String valueToConciseString( Number _value, Computation.Config _config )
	{
		return valueToConciseString( _value, Environment.getInstance( _config ) );
	}

	public final String valueToConciseString( Number _value, Environment _env )
	{
		if (null == _value) return "";
		return convertToConciseString( assertProperNumberType( _value ), _env );
	}


	protected abstract Number convertFromAnyNumber( Number _value );
	protected abstract Number assertProperNumberType( Number _value );

	protected Number convertFromString( String _value, Environment _env ) throws ParseException
	{
		ParsePosition parsePosition = new ParsePosition( 0 );
		final NumberFormat format = _env.decimalFormat();
		final Number number = format.parse( normalizeExponentialChar( _value, format ), parsePosition );
		if (parsePosition.getIndex() < _value.length()) {
			throw new ParseException( "Unparseable number: \"" + _value + "\"", parsePosition.getIndex() );
		}
		return convertFromAnyNumber( number );
	}

	protected String normalizeExponentialChar( String _value, NumberFormat _format )
	{
		// LATER DecimalFormatSymbols.getExponentialSymbol(); once it's made public
		return _value.replace( 'e', 'E' );
	}

	protected String convertToString( Number _value, Environment _env )
	{
		return NumberFormat.getNumberInstance( _env.locale() ).format( _value );
	}

	protected String convertToConciseString( Number _value, Environment _env )
	{
		return convertToString( _value, _env );
	}


	public void validateReturnTypeForCell( Method _method ) throws CompilerException
	{
		final Class returnType = _method.getReturnType();

		if (Byte.TYPE == returnType || Byte.class == returnType) return;
		if (Short.TYPE == returnType || Short.class == returnType) return;
		if (Integer.TYPE == returnType || Integer.class == returnType) return;
		if (Long.TYPE == returnType || Long.class == returnType) return;

		if (Float.TYPE == returnType || Float.class == returnType) return;
		if (Double.TYPE == returnType || Double.class == returnType) return;

		if (Boolean.TYPE == returnType || Boolean.class == returnType) return;
		if (Date.class == returnType) return;

		if (BigInteger.class == returnType) return;
		if (BigDecimal.class == returnType) return;

		if (String.class == returnType) return;

		throw new CompilerException.UnsupportedDataType( "The method " + _method + " has an unsupported return type" );
	}


	@Override
	public String toString()
	{
		if (null != mathContext()) {
			return valueType().getName() + "." + mathContext().toString();
		}
		return valueType().getName() + ((UNDEFINED_SCALE != scale())? "." + Integer.toString( scale() ) : "");
	}

}
