/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
package sej.tests;

import sej.Aggregator;
import sej.CallFrame;
import sej.EngineBuilder;
import sej.NumericType;
import sej.Orientation;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBuilder;
import sej.SpreadsheetBinder.Section;
import sej.SpreadsheetBuilder.CellRef;
import sej.runtime.ComputationFactory;
import sej.runtime.Resettable;
import junit.framework.TestCase;

public class RepeatingSectionTest extends TestCase
{


	public void testDoubleAggregators() throws Exception
	{
		testAggregators( SEJ.DOUBLE );
	}

	public void testBigDecimalAggregators() throws Exception
	{
		testAggregators( SEJ.BIGDECIMAL8 );
	}

	public void testLong4Aggregators() throws Exception
	{
		testAggregators( SEJ.LONG4 );
	}


	private void testAggregators( NumericType _numericType ) throws Exception
	{
		long[] vals = new long[] { 1, 2, 3, 4 };
		for (Aggregator agg : Aggregator.values()) {
			switch (agg) {

				case AND:
				case OR:
					// not supported over sections yet
					break;

				default:
					SpreadsheetBuilder sb = SEJ.newSpreadsheetBuilder();
					sb.newCell( sb.cst( "" ) );
					sb.newCell( sb.cst( 0 ) );
					sb.nameCell( "Value" );
					CellRef rangeStart = sb.currentCell();
					sb.newCell( sb.cst( 0 ) );
					sb.newCell( sb.cst( 0 ) );
					CellRef rangeEnd = sb.currentCell();
					sb.nameRange( sb.range( rangeStart, rangeEnd ), "Section" );

					sb.newRow();
					sb.newCell( sb.agg( agg, sb.ref( rangeStart ) ) );
					sb.nameCell( "Result" );

					EngineBuilder eb = SEJ.newEngineBuilder();
					Spreadsheet ss = sb.getSpreadsheet();
					eb.setSpreadsheet( ss );
					eb.setInputClass( Input.class );
					eb.setOutputClass( Output.class );
					eb.setNumericType( _numericType );
					Section bnd = eb.getRootBinder();
					bnd.defineOutputCell( ss.getCell( "Result" ), out( "result" ) );
					Section sub = bnd.defineRepeatingSection( ss.getRange( "Section" ), Orientation.HORIZONTAL,
							inp( "subs" ), Input.class, null, null );
					sub.defineInputCell( ss.getCell( "Value" ), inp( "value" ) );

					SaveableEngine e = eb.compile();
					ComputationFactory f = e.getComputationFactory();

					final int s = (agg == Aggregator.AVERAGE) ? 1 : 0;
					for (int l = s; l < vals.length; l++) {
						Input i = new Input( 0, l, vals );
						Output o = (Output) f.newComputation( i );
						double actual = o.result();
						double expected = expected( agg, i );
						assertEquals( agg.toString() + "@" + l, expected, actual, 0.000001 );
					}
			}

		}
	}


	private double expected( Aggregator _agg, Input _inp )
	{
		if (_inp.subs().length > 0) {
			boolean first = true;
			double r = 0.0;
			for (Input sub : _inp.subs()) {
				if (first && _agg != Aggregator.COUNT) {
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

		public Input(long _value, int _n, long[] _subs)
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


	private CallFrame inp( String _name ) throws NoSuchMethodException
	{
		return call( Input.class, _name );
	}

	private CallFrame out( String _name ) throws NoSuchMethodException
	{
		return call( Output.class, _name );
	}

	private CallFrame call( Class _class, String _name ) throws NoSuchMethodException
	{
		return new CallFrame( _class.getMethod( _name ) );
	}

}