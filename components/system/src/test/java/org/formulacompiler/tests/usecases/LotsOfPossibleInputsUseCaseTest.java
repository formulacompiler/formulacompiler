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
package org.formulacompiler.tests.usecases;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.StringTokenizer;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;



@SuppressWarnings("unchecked")
public class LotsOfPossibleInputsUseCaseTest extends AbstractUseCaseTest
{

	public void testLotsOfPossibleInputs() throws Exception
	{
		runUseCase( "LotsOfPossibleInputs1", new LotsOfPossibleInputsUseCase(), Inputs.class, Outputs.class );
	}


	static final class LotsOfPossibleInputsUseCase implements UseCase
	{


		public void defineEngine( Spreadsheet _model, SpreadsheetBinder.Section _root ) throws Exception
		{
			final Class inputs = Inputs.class;
			final Method[] methods = inputs.getMethods();
			for (Spreadsheet.NameDefinition def : _model.getDefinedNames()) {
				if (def instanceof Spreadsheet.CellNameDefinition) {
					final Spreadsheet.CellNameDefinition cellDef = (Spreadsheet.CellNameDefinition) def;
					final String cellName = cellDef.getName();

					String methodName = "get" + cellName;
					int paramSep = methodName.indexOf( '_' );
					if (0 <= paramSep) {
						methodName = methodName.substring( 0, paramSep );
					}
					for (Method method : methods) {
						if (method.getName().equalsIgnoreCase( methodName )) {
							_root
									.defineInputCell( cellDef.getCell(),
											new CallFrame( method, getArguments( method, cellName ) ) );
							break;
						}
					}
				}
			}

			final Class outputs = Outputs.class;
			_root.defineOutputCell( _model.getCell( "A" ), new CallFrame( outputs.getMethod( "getA" ) ) );
			_root.defineOutputCell( _model.getCell( "B" ), new CallFrame( outputs.getMethod( "getB" ) ) );
			_root.defineOutputCell( _model.getCell( "ISOK" ), new CallFrame( outputs.getMethod( "isOK" ) ) );
			_root.defineOutputCell( _model.getCell( "WHEN" ), new CallFrame( outputs.getMethod( "when" ) ) );
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
			final Collection<Object> args = New.newCollection();
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

		public Inputs(double _one, double _two)
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
