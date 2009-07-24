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

package org.formulacompiler.tests.usecases;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.StringTokenizer;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;


public class LotsOfPossibleInputsUseCaseTest extends AbstractUseCaseTest
{

	public void testLotsOfPossibleInputs() throws Exception
	{
		runUseCase( "LotsOfPossibleInputs1", new LotsOfPossibleInputsUseCase(), Inputs.class, Outputs.class );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( LotsOfPossibleInputsUseCaseTest.class );
	}


	static final class LotsOfPossibleInputsUseCase implements UseCase
	{


		public void defineEngine( EngineBuilder _builder, Spreadsheet _model, SpreadsheetBinder.Section _root )
				throws Exception
		{
			final Class inputs = Inputs.class;
			final Method[] methods = inputs.getMethods();
			for (Map.Entry<String, Spreadsheet.Range> def : _model.getRangeNames().entrySet()) {
				final Spreadsheet.Range range = def.getValue();
				if (range instanceof Spreadsheet.Cell) {
					final Spreadsheet.Cell cell = (Spreadsheet.Cell) range;
					final String cellName = def.getKey();

					String methodName = "get" + cellName;
					int paramSep = methodName.indexOf( '_' );
					if (0 <= paramSep) {
						methodName = methodName.substring( 0, paramSep );
					}
					for (Method method : methods) {
						if (method.getName().equalsIgnoreCase( methodName )) {
							_root.defineInputCell( cell, method, getArguments( method, cellName ) );
							break;
						}
					}
				}
			}

			final Class outputs = Outputs.class;
			_root.defineOutputCell( _model.getCell( "A" ), outputs.getMethod( "getA" ) );
			_root.defineOutputCell( _model.getCell( "B" ), outputs.getMethod( "getB" ) );
			_root.defineOutputCell( _model.getCell( "ISOK" ), outputs.getMethod( "isOK" ) );
			_root.defineOutputCell( _model.getCell( "WHEN" ), outputs.getMethod( "when" ) );
		}


		public void useEngine( SaveableEngine _engine )
		{
			final Inputs inputs = new Inputs( 13, 14 );
			final Outputs outputs = (Outputs) _engine.getComputationFactory().newComputation( inputs );

			assertEquals( true, outputs.isOK() );
			assertEquals( 127, outputs.getA(), 0.0001 );
			assertEquals( 247, outputs.getB(), 0.0001 );

			final Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.set( 1994, 7 - 1, 31 );

			final Date expectedDate = cal.getTime();
			final Date actualDate = outputs.when();
			assertTrue( expectedDate.equals( actualDate ) );
		}


		private Object[] getArguments( Method _method, String _name )
		{
			final Collection<Object> args = New.collection();
			final StringTokenizer tokenizer = new StringTokenizer( _name, "_" );
			tokenizer.nextToken();
			final Class[] paramTypes = _method.getParameterTypes();
			int paramIndex = 0;
			while (tokenizer.hasMoreTokens()) {
				String argAsString = tokenizer.nextToken();
				Class paramType = paramTypes[ paramIndex++ ];
				Object arg;
				if (Byte.TYPE == paramType || Byte.class == paramType) {
					arg = Byte.valueOf( argAsString );
				}
				else if (Short.TYPE == paramType || Short.class == paramType) {
					arg = Short.valueOf( argAsString );
				}
				else if (Integer.TYPE == paramType || Integer.class == paramType) {
					arg = Integer.valueOf( argAsString );
				}
				else if (Long.TYPE == paramType || Long.class == paramType) {
					arg = Long.valueOf( argAsString );
				}
				else if (Float.TYPE == paramType || Float.class == paramType) {
					arg = Float.valueOf( argAsString );
				}
				else if (Double.TYPE == paramType || Double.class == paramType) {
					arg = Double.valueOf( argAsString );
				}
				else if (Boolean.TYPE == paramType || Boolean.class == paramType) {
					arg = Boolean.valueOf( argAsString );
				}
				else {
					throw new IllegalArgumentException( "Parameters of type " + paramType.getName() + " are not supported." );
				}
				args.add( arg );
			}
			return args.toArray( new Object[ args.size() ] );
		}

	}


	public static final class Inputs
	{
		private double one;
		private double two;

		public Inputs( double _one, double _two )
		{
			super();
			this.one = _one;
			this.two = _two;
		}

		public double getOne()
		{
			return this.one;
		}

		public double getTwo()
		{
			return this.two;
		}

		public Date getDate( int _year, int _month, int _day )
		{
			Calendar cal = Calendar.getInstance();
			cal.clear();
			cal.set( _year, _month - 1, _day );
			return cal.getTime();
		}

		public double getFact( int _x )
		{
			double f = 1;
			int x = _x;
			while (0 < x) {
				f *= x--;
			}
			return f;
		}

	}


	public static abstract class Outputs
	{
		public abstract double getA();
		public abstract double getB();

		public double getC()
		{
			return getA() + getB();
		}

		public abstract boolean isOK();
		public abstract Date when();
	}


}
