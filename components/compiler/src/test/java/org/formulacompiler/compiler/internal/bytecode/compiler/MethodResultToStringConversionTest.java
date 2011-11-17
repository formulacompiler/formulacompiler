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

package org.formulacompiler.compiler.internal.bytecode.compiler;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;

import org.formulacompiler.compiler.FormulaCompiler;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.compiler.internal.engine.ModelToEngineCompiler;
import org.formulacompiler.compiler.internal.engine.ModelToEngineCompilerImpl;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForConstantValue;
import org.formulacompiler.compiler.internal.expressions.ExpressionNodeForOperator;
import org.formulacompiler.compiler.internal.model.CellModel;
import org.formulacompiler.compiler.internal.model.ComputationModel;
import org.formulacompiler.compiler.internal.model.ExpressionNodeForCellModel;
import org.formulacompiler.compiler.internal.model.SectionModel;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.internal.Environment;
import org.formulacompiler.tests.utils.AbstractStandardInputsOutputsTestCase;
import org.formulacompiler.tests.utils.types.BigDecimalValue;
import org.formulacompiler.tests.utils.types.BigIntegerValue;
import org.formulacompiler.tests.utils.types.BooleanValue;
import org.formulacompiler.tests.utils.types.ByteValue;
import org.formulacompiler.tests.utils.types.DoubleValue;
import org.formulacompiler.tests.utils.types.FloatValue;
import org.formulacompiler.tests.utils.types.IntValue;
import org.formulacompiler.tests.utils.types.LongValue;
import org.formulacompiler.tests.utils.types.MsSinceUTC1970Value;
import org.formulacompiler.tests.utils.types.MsValue;
import org.formulacompiler.tests.utils.types.ScaledLongValue;
import org.formulacompiler.tests.utils.types.ShortValue;
import org.formulacompiler.tests.utils.types.StringValue;


public class MethodResultToStringConversionTest extends AbstractStandardInputsOutputsTestCase
{
	private static final Environment EN_US_ENV = getEnvironment( Locale.US );
	private static final Environment RU_RU_ENV = getEnvironment( new Locale( "ru", "RU" ) );

	public void testConvertLongMethodResultToString() throws Exception
	{

		final String result = getComputation( LongValue.class, new LongValue()
		{
			public long get()
			{
				return 1;
			}
		}, Environment.DEFAULT );
		assertEquals( "1", result );
	}

	public void testConvertIntMethodResultToString() throws Exception
	{

		final String result = getComputation( IntValue.class, new IntValue()
		{
			public int get()
			{
				return 1;
			}
		}, Environment.DEFAULT );
		assertEquals( "1", result );
	}

	public void testConvertShortMethodResultToString() throws Exception
	{

		final String result = getComputation( ShortValue.class, new ShortValue()
		{
			public short get()
			{
				return 1;
			}
		}, Environment.DEFAULT );
		assertEquals( "1", result );
	}

	public void testConvertByteMethodResultToString() throws Exception
	{

		final String result = getComputation( ByteValue.class, new ByteValue()
		{
			public byte get()
			{
				return 1;
			}
		}, Environment.DEFAULT );
		assertEquals( "1", result );
	}

	public void testConvertBigIntegerMethodResultToString() throws Exception
	{

		final String result = getComputation( BigIntegerValue.class, new BigIntegerValue()
		{
			public BigInteger get()
			{
				return BigInteger.valueOf( 1 );
			}
		}, Environment.DEFAULT );
		assertEquals( "1", result );
	}

	public void testConvertBigDecimalMethodResultToString_en_US() throws Exception
	{
		final String result = getComputation( BigDecimalValue.class, new BigDecimalValue()
		{
			public BigDecimal get()
			{
				return BigDecimal.valueOf( 1.5 );
			}
		}, EN_US_ENV );
		assertEquals( "1.5", result );
	}

	public void testConvertBigDecimalMethodResultToString_ru_RU() throws Exception
	{
		final String result = getComputation( BigDecimalValue.class, new BigDecimalValue()
		{
			public BigDecimal get()
			{
				return BigDecimal.valueOf( 1.5 );
			}
		}, RU_RU_ENV );
		assertEquals( "1,5", result );
	}

	public void testConvertFloatMethodResultToString_en_US() throws Exception
	{
		final String result = getComputation( FloatValue.class, new FloatValue()
		{
			public float get()
			{
				return 1.5f;
			}
		}, EN_US_ENV );
		assertEquals( "1.5", result );
	}

	public void testConvertFloatMethodResultToString_ru_RU() throws Exception
	{
		final String result = getComputation( FloatValue.class, new FloatValue()
		{
			public float get()
			{
				return 1.5f;
			}
		}, RU_RU_ENV );
		assertEquals( "1,5", result );
	}

	public void testConvertDoubleMethodResultToString_en_US() throws Exception
	{
		final String result = getComputation( DoubleValue.class, new DoubleValue()
		{
			public double get()
			{
				return 1.5;
			}
		}, EN_US_ENV );
		assertEquals( "1.5", result );
	}

	public void testConvertDoubleMethodResultToString_ru_RU() throws Exception
	{
		final String result = getComputation( DoubleValue.class, new DoubleValue()
		{
			public double get()
			{
				return 1.5;
			}
		}, RU_RU_ENV );
		assertEquals( "1,5", result );
	}

	public void testConvertScaledLongMethodResultToString_en_US() throws Exception
	{
		final String result = getComputation( ScaledLongValue.class, new ScaledLongValue()
		{
			public long get()
			{
				return 150;
			}
		}, EN_US_ENV );
		assertEquals( "1.5", result );
	}

	public void testConvertScaledLongMethodResultToString_ru_RU() throws Exception
	{
		final String result = getComputation( ScaledLongValue.class, new ScaledLongValue()
		{
			public long get()
			{
				return 150;
			}
		}, RU_RU_ENV );
		assertEquals( "1,5", result );
	}

	public void testConvertBooleanMethodResultToString() throws Exception
	{
		{
			final String result = getComputation( BooleanValue.class, new BooleanValue()
			{
				public boolean get()
				{
					return true;
				}
			}, Environment.DEFAULT );
			assertEquals( "1", result );
		}
		{
			final String result = getComputation( BooleanValue.class, new BooleanValue()
			{
				public boolean get()
				{
					return false;
				}
			}, Environment.DEFAULT );
			assertEquals( "0", result );
		}
	}

	public void testConvertDateMethodResultToString() throws Exception
	{
		final TimeZone timeZone = TimeZone.getTimeZone( "GMT-4" );
		final Environment env = Environment.getInstance( new Computation.Config( timeZone ) );
		final String result = getComputation( DateValue.class, new DateValue()
		{
			public Date get()
			{
				final GregorianCalendar calendar = new GregorianCalendar( timeZone );
				calendar.clear();
				calendar.set( 2007, Calendar.APRIL, 12, 5, 42, 52 );
				return calendar.getTime();
			}
		}, env );
		assertEquals( "39184.2381", result );
	}

	public void testConvertMsSinceUTC1970MethodResultToString() throws Exception
	{
		final TimeZone timeZone = TimeZone.getTimeZone( "GMT-4" );
		final Environment env = Environment.getInstance( new Computation.Config( timeZone ) );
		final String result = getComputation( MsSinceUTC1970Value.class, new MsSinceUTC1970Value()
		{
			public long get()
			{
				final GregorianCalendar calendar = new GregorianCalendar( timeZone );
				calendar.clear();
				calendar.set( 2007, Calendar.APRIL, 12, 5, 42, 52 );
				return calendar.getTime().getTime();
			}
		}, env );
		assertEquals( "39184.2381", result );
	}

	public void testConvertMsMethodResultToString() throws Exception
	{
		final String result = getComputation( MsValue.class, new MsValue()
		{
			public long get()
			{
				return 20000L * 24 * 60 * 60 * 1000;
			}
		}, Environment.DEFAULT );
		assertEquals( "20000", result );
	}

	private <T> String getComputation( final Class<T> _inputClass, final T _input, final Environment _env ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( _inputClass, StringValue.class, ComputationMode.EXCEL, _env );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "Input" );
		final CellModel b = new CellModel( rootModel, "Output" );
		a.setExpression( new ExpressionNodeForConstantValue( "text" ) );
		b.setExpression( new ExpressionNodeForOperator( Operator.CONCAT, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForConstantValue( "" ) ) );

		a.makeInput( FormulaCompiler.newCallFrame( _inputClass.getMethod( "get" ) ) );
		b.makeOutput( FormulaCompiler.newCallFrame( StringValue.class.getMethod( "get" ) ) );

		final ModelToEngineCompiler.Config ecc = new ModelToEngineCompiler.Config();

		ecc.model = engineModel;
		ecc.numericType = FormulaCompiler.DOUBLE;

		final ModelToEngineCompiler ec = new ModelToEngineCompilerImpl( ecc );
		final SaveableEngine engine = ec.compile();

		checkEngine( engine );

		final ComputationFactory factory = engine.getComputationFactory( new Computation.Config( _env.locale(), _env.timeZone() ) );

		final StringValue outputs = (StringValue) factory.newComputation( _input );

		return outputs.get();
	}

	private static Environment getEnvironment( Locale _locale )
	{
		final Computation.Config cfg = new Computation.Config();
		cfg.locale = _locale;
		return Environment.getInstance( cfg );
	}
}
