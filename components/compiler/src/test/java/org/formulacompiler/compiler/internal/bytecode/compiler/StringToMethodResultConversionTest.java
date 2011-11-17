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


public class StringToMethodResultConversionTest extends AbstractStandardInputsOutputsTestCase
{
	private static final Environment EN_US_ENV = getEnvironment( Locale.US );
	private static final Environment RU_RU_ENV = getEnvironment( new Locale( "ru", "RU" ) );


	public void testConvertStringToLongMethodResult() throws Exception
	{
		final LongValue outputs = getComputation( "1", LongValue.class, Environment.DEFAULT );
		final long result = outputs.get();
		assertEquals( 1, result );
	}

	public void testConvertStringToIntMethodResult() throws Exception
	{
		final IntValue outputs = getComputation( "1", IntValue.class, Environment.DEFAULT );
		final long result = outputs.get();
		assertEquals( 1, result );
	}

	public void testConvertStringToShortMethodResult() throws Exception
	{
		final ShortValue outputs = getComputation( "1", ShortValue.class, Environment.DEFAULT );
		final long result = outputs.get();
		assertEquals( 1, result );
	}

	public void testConvertStringToByteMethodResult() throws Exception
	{
		final ByteValue outputs = getComputation( "1", ByteValue.class, Environment.DEFAULT );
		final long result = outputs.get();
		assertEquals( 1, result );
	}

	public void testConvertStringToBigIntegerMethodResult() throws Exception
	{
		final BigIntegerValue outputs = getComputation( "1", BigIntegerValue.class, Environment.DEFAULT );
		final BigInteger result = outputs.get();
		assertEquals( BigInteger.valueOf( 1 ), result );
	}

	public void testConvertStringToBigDecimalMethodResult_en_US() throws Exception
	{
		final BigDecimalValue outputs = getComputation( "1.5", BigDecimalValue.class, EN_US_ENV );
		final BigDecimal result = outputs.get();
		assertEquals( BigDecimal.valueOf( 1.5 ), result );
	}

	public void testConvertStringToBigDecimalMethodResult_ru_RU() throws Exception
	{
		final BigDecimalValue outputs = getComputation( "1,5", BigDecimalValue.class, RU_RU_ENV );
		final BigDecimal result = outputs.get();
		assertEquals( BigDecimal.valueOf( 1.5 ), result );
	}

	public void testConvertStringToFloatMethodResult_en_US() throws Exception
	{
		final FloatValue outputs = getComputation( "1.5", FloatValue.class, EN_US_ENV );
		final float result = outputs.get();
		assertEquals( 1.5, result, 1e-10 );
	}

	public void testConvertStringToFloatMethodResult_ru_RU() throws Exception
	{
		final FloatValue outputs = getComputation( "1,5", FloatValue.class, RU_RU_ENV );
		final float result = outputs.get();
		assertEquals( 1.5, result, 1e-10 );
	}

	public void testConvertStringToDoubleMethodResult_en_US() throws Exception
	{
		final DoubleValue outputs = getComputation( "1.5", DoubleValue.class, EN_US_ENV );
		final double result = outputs.get();
		assertEquals( 1.5, result, 1e-10 );
	}

	public void testConvertStringToDoubleMethodResult_ru_RU() throws Exception
	{
		final DoubleValue outputs = getComputation( "1,5", DoubleValue.class, RU_RU_ENV );
		final double result = outputs.get();
		assertEquals( 1.5, result, 1e-10 );
	}

	public void testConvertStringToScaledLongMethodResult_en_US() throws Exception
	{
		final ScaledLongValue outputs = getComputation( "1.5", ScaledLongValue.class, EN_US_ENV );
		final long result = outputs.get();
		assertEquals( 150, result );
	}

	public void testConvertStringToScaledLongMethodResult_ru_RU() throws Exception
	{
		final ScaledLongValue outputs = getComputation( "1,5", ScaledLongValue.class, RU_RU_ENV );
		final long result = outputs.get();
		assertEquals( 150, result );
	}

	public void testConvertStringToBooleanMethodResult() throws Exception
	{
		final BooleanValue outputsTrue = getComputation( "1", BooleanValue.class, Environment.DEFAULT );
		assertTrue( outputsTrue.get() );

		final BooleanValue outputsFalse = getComputation( "0", BooleanValue.class, Environment.DEFAULT );
		assertFalse( outputsFalse.get() );
	}

	public void testConvertStringToDateMethodResult() throws Exception
	{
		final TimeZone timeZone = TimeZone.getTimeZone( "GMT-4" );
		final Environment env = Environment.getInstance( new Computation.Config( timeZone ) );
		final DateValue outputs = getComputation( "2007-04-12 05:42:52", DateValue.class, env );
		final Date result = outputs.get();
		final GregorianCalendar calendar = new GregorianCalendar( timeZone );
		calendar.clear();
		calendar.set( 2007, Calendar.APRIL, 12, 5, 42, 52 );
		assertEquals( calendar.getTime(), result );
	}

	public void testConvertStringToMsSinceUTC1970MethodResult() throws Exception
	{
		final TimeZone timeZone = TimeZone.getTimeZone( "GMT-4" );
		final Environment env = Environment.getInstance( new Computation.Config( timeZone ) );
		final MsSinceUTC1970Value outputs = getComputation( "2007-04-12 05:42:52", MsSinceUTC1970Value.class, env );
		final long result = outputs.get();
		final GregorianCalendar calendar = new GregorianCalendar( timeZone );
		calendar.clear();
		calendar.set( 2007, Calendar.APRIL, 12, 5, 42, 52 );
		assertEquals( calendar.getTime().getTime(), result );
	}

	public void testConvertStringToMsMethodResult() throws Exception
	{
		final MsValue outputs = getComputation( "1954-10-03", MsValue.class, Environment.DEFAULT );
		final long result = outputs.get();
		assertEquals( 20000L * 24 * 60 * 60 * 1000, result );
	}

	private <T> T getComputation( final String input, Class<T> _outputClass, Environment _env ) throws Exception
	{
		final ComputationModel engineModel = new ComputationModel( StringValue.class, _outputClass, ComputationMode.EXCEL, _env );
		final SectionModel rootModel = engineModel.getRoot();
		final CellModel a = new CellModel( rootModel, "Input" );
		final CellModel b = new CellModel( rootModel, "Output" );
		a.setExpression( new ExpressionNodeForConstantValue( "text" ) );
		b.setExpression( new ExpressionNodeForOperator( Operator.CONCAT, new ExpressionNodeForCellModel( a ),
				new ExpressionNodeForConstantValue( "" ) ) );

		a.makeInput( FormulaCompiler.newCallFrame( StringValue.class.getMethod( "get" ) ) );
		b.makeOutput( FormulaCompiler.newCallFrame( _outputClass.getMethod( "get" ) ) );

		final ModelToEngineCompiler.Config ecc = new ModelToEngineCompiler.Config();
		ecc.model = engineModel;
		ecc.numericType = FormulaCompiler.DOUBLE;

		final ModelToEngineCompiler ec = new ModelToEngineCompilerImpl( ecc );
		final SaveableEngine engine = ec.compile();

		checkEngine( engine );

		final ComputationFactory factory = engine.getComputationFactory( new Computation.Config( _env.locale(), _env.timeZone() ) );

		@SuppressWarnings( "unchecked" )
		final T computation = (T) factory.newComputation( new StringValue()
		{
			public String get()
			{
				return input;
			}
		} );

		return computation;
	}

	private static Environment getEnvironment( Locale _locale )
	{
		final Computation.Config cfg = new Computation.Config();
		cfg.locale = _locale;
		return Environment.getInstance( cfg );
	}

}
