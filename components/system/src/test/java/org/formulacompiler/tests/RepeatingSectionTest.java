/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

package org.formulacompiler.tests;

import java.lang.reflect.Method;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;

import junit.framework.TestCase;

public class RepeatingSectionTest extends TestCase
{


	public void testDoubleAggregators() throws Exception
	{
		testAggregators( SpreadsheetCompiler.DOUBLE );
	}

	public void testBigDecimalAggregators() throws Exception
	{
		testAggregators( SpreadsheetCompiler.BIGDECIMAL_SCALE8 );
	}

	public void testLong4Aggregators() throws Exception
	{
		testAggregators( SpreadsheetCompiler.LONG_SCALE4 );
	}

	private static final Function[] TESTED_AGGREGATORS = { Function.SUM, Function.PRODUCT, Function.MIN, Function.MAX,
			Function.COUNT, Function.AVERAGE };

	// LATER Add AND, OR above.

	private void testAggregators( NumericType _numericType ) throws Exception
	{
		long[] vals = new long[] { 1, 2, 3, 4 };
		for (Function agg : TESTED_AGGREGATORS) {

			SpreadsheetBuilder sb = SpreadsheetCompiler.newSpreadsheetBuilder();
			sb.newCell( sb.cst( "" ) );
			sb.newCell( sb.cst( 0 ) );
			sb.nameCell( "Value" );
			CellRef rangeStart = sb.currentCell();
			sb.newCell( sb.cst( 0 ) );
			sb.newCell( sb.cst( 0 ) );
			CellRef rangeEnd = sb.currentCell();
			sb.nameRange( sb.range( rangeStart, rangeEnd ), "Section" );

			sb.newRow();
			sb.newCell( sb.fun( agg, sb.ref( sb.range( rangeStart, rangeEnd ) ) ) );
			sb.nameCell( "Result" );

			EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
			Spreadsheet ss = sb.getSpreadsheet();
			eb.setSpreadsheet( ss );
			eb.setInputClass( Input.class );
			eb.setOutputClass( Output.class );
			eb.setNumericType( _numericType );
			Section bnd = eb.getRootBinder();
			bnd.defineOutputCell( ss.getCell( "Result" ), out( "result" ) );
			Section sub = bnd.defineRepeatingSection( ss.getRange( "Section" ), Orientation.HORIZONTAL, inp( "subs" ),
					Input.class, null, null );
			sub.defineInputCell( ss.getCell( "Value" ), inp( "value" ) );

			SaveableEngine e = eb.compile();
			ComputationFactory f = e.getComputationFactory();

			final int s = (agg == Function.AVERAGE || agg == Function.VARP) ? 1 : 0;
			for (int l = s; l < vals.length; l++) {
				Input i = new Input( 0, l, vals );
				Output o = (Output) f.newComputation( i );
				double actual = o.result();
				double expected = expected( agg, i );
				assertEquals( agg.toString() + "@" + l, expected, actual, 0.000001 );
			}

		}
	}

	private double expected( Function _agg, Input _inp )
	{
		if (_inp.subs().length > 0) {
			boolean first = true;
			double r = 0.0;
			for (Input sub : _inp.subs()) {
				if (first && _agg != Function.COUNT) {
					r = sub.value();
					first = false;
				}
				else {
					switch (_agg) {
						case SUM:
						case AVERAGE:
							r += sub.value();
							break;
						case PRODUCT:
							r *= sub.value();
							break;
						case MIN:
							r = Math.min( r, sub.value() );
							break;
						case MAX:
							r = Math.max( r, sub.value() );
							break;
						case COUNT:
							r += 1.0;
							break;
					}
				}
			}
			switch (_agg) {
				case AVERAGE:
					return r / _inp.subs().length;
			}
			return r;
		}
		else return 0.0;
	}


	public static interface OutputFactory
	{
		Output newOutput( Input _i );
	}

	public static interface Output extends Resettable
	{
		double result();
	}


	public static class Input
	{
		private final long value;
		private final Input[] subs;

		public Input( long _value, int _n, long[] _subs )
		{
			super();
			this.value = _value;
			this.subs = new Input[ _n ];
			for (int i = 0; i < _n; i++) {
				this.subs[ i ] = new Input( _subs[ i ], 0, null );
			}
		}

		public long value()
		{
			return this.value;
		}

		public Input[] subs()
		{
			return this.subs;
		}

	}


	private Method inp( String _name ) throws NoSuchMethodException
	{
		return Input.class.getMethod( _name );
	}

	private Method out( String _name ) throws NoSuchMethodException
	{
		return Output.class.getMethod( _name );
	}

}
