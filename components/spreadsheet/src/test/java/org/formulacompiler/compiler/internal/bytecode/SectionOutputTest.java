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
package org.formulacompiler.compiler.internal.bytecode;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.Operator;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder.CellRef;
import org.formulacompiler.tests.utils.AbstractIOTestBase;



/**
 * This is the prototype of how AFC implements sums over variably sized sections.
 * 
 * @author peo
 */
public class SectionOutputTest extends AbstractIOTestBase
{

	public void testArray() throws Exception
	{
		assertValues( new int[] { 11, 3, 4, 5, 6 }, new RootInput( 10, 2, 3, 4, 5 ) );
	}

	private void assertValues( int[] _expected, RootInput _input ) throws Exception
	{
		assertValues( _expected, new RootPrototype( _input ), _input, "Proto" );
		assertValues( _expected, newRootEngine( _input ), _input, "AFC" );
	}

	private void assertValues( int[] _expected, RootOutput _output, RootInput _input, String _engName )
	{
		for (int i = 0; i <= _expected.length; i++) {
			assertValues( _expected, i, _output, _input, _engName );
		}
	}

	private void assertValues( int[] _expected, int _len, RootOutput _output, RootInput _input, String _engName )
	{
		_input.inputsLen = _len;
		_output.reset();
		assertArray( _expected, _len, _output );
		assertIterator( _expected, _len, _output );
		assertIterable( _expected, _len, _output );
		assertCollection( _expected, _len, _output );
		assertList( _expected, _len, _output );
	}

	private void assertArray( int[] _expected, int _len, RootOutput _output )
	{
		final DetailOutput[] arr = _output.outputsArray();
		for (int i = 0; i < _len; i++) {
			long expected = _expected[ i ];
			long actual = arr[ i ].output();
			assertEquals( expected, actual );
		}
	}

	private void assertIterator( int[] _expected, int _len, RootOutput _output )
	{
		final Iterator<DetailOutput> iter = _output.outputsIterator();
		assertIterator( _expected, _len, iter );
	}

	private void assertIterator( int[] _expected, int _len, final Iterator<DetailOutput> _iter )
	{
		for (int i = 0; i < _len; i++) {
			assertTrue( _iter.hasNext() );
			long expected = _expected[ i ];
			long actual = _iter.next().output();
			assertEquals( expected, actual );
		}
		assertFalse( _iter.hasNext() );
	}

	private void assertIterable( int[] _expected, int _len, RootOutput _output )
	{
		final Iterator<DetailOutput> iter = _output.outputsIterable().iterator();
		assertIterator( _expected, _len, iter );
	}

	private void assertCollection( int[] _expected, int _len, RootOutput _output )
	{
		final Iterator<DetailOutput> iter = _output.outputsCollection().iterator();
		assertIterator( _expected, _len, iter );
	}

	private void assertList( int[] _expected, int _len, RootOutput _output )
	{
		final List<DetailOutput> lst = _output.outputsList();
		for (int i = 0; i < _len; i++) {
			long expected = _expected[ i ];
			long actual = lst.get( i ).output();
			assertEquals( expected, actual );
		}
	}


	private RootOutput newRootEngine( RootInput _input ) throws Exception
	{
		final String[] types = { "Array", "Iterator", "Iterable", "Collection", "List" };

		SpreadsheetBuilder bld = SpreadsheetCompiler.newSpreadsheetBuilder();

		for (String type : types)
			buildSectionFor( bld, type );

		Spreadsheet sht = bld.getSpreadsheet();

		EngineBuilder cmp = SpreadsheetCompiler.newEngineBuilder();
		cmp.setSpreadsheet( sht );
		cmp.setInputClass( RootInput.class );
		cmp.setOutputClass( RootOutput.class );
		cmp.setNumericType( SpreadsheetCompiler.LONG );
		Section bnd = cmp.getRootBinder();

		for (String type : types)
			bindSectionFor( sht, bnd, type );

		SaveableEngine engine = cmp.compile();
		checkEngine( engine );

		return (RootOutput) engine.getComputationFactory().newComputation( _input );
	}

	private void buildSectionFor( SpreadsheetBuilder _bld, String _type )
	{
		_bld.newCell( _bld.cst( 1 ) );
		_bld.nameCell( "SectionInput" + _type );
		CellRef inp = _bld.currentCell();

		_bld.newCell( _bld.op( Operator.PLUS, _bld.ref( inp ), _bld.ref( _bld.cst( 1 ) ) ) );
		_bld.nameCell( "SectionOutput" + _type );
		CellRef outp = _bld.currentCell();

		_bld.nameRange( _bld.range( inp, outp ), "Section" + _type );

		_bld.newRow();
	}

	private void bindSectionFor( Spreadsheet _sht, Section _bnd, String _type ) throws Exception, CompilerException
	{
		Range rng = _sht.getRange( "Section" + _type );
		CallFrame inps = call( RootInput.class, "inputs" );
		CallFrame outps = call( RootOutput.class, "outputs" + _type );
		Section dets = _bnd.defineRepeatingSection( rng, Orientation.VERTICAL, inps, DetailInput.class, outps,
				DetailOutput.class );
		dets.defineInputCell( _sht.getCell( "SectionInput" + _type ), call( DetailInput.class, "input" ) );
		dets.defineOutputCell( _sht.getCell( "SectionOutput" + _type ), call( DetailOutput.class, "output" ) );
	}

	@SuppressWarnings("unchecked")
	private CallFrame call( Class _cls, String _mtd ) throws Exception
	{
		return new CallFrame( _cls.getMethod( _mtd ) );
	}


	public static abstract class RootOutput implements Resettable
	{
		public abstract DetailOutput[] outputsArray();
		public abstract Iterator<DetailOutput> outputsIterator();
		public Iterable<DetailOutput> outputsIterable()
		{
			return null;
		}
		public abstract Collection<DetailOutput> outputsCollection();
		public abstract List<DetailOutput> outputsList();
	}

	public static interface DetailOutput
	{
		long output();
	}


	public static final class RootPrototype extends RootOutput implements Computation
	{
		private final RootInput inputs;

		public RootPrototype(RootInput _inputs)
		{
			this.inputs = _inputs;
		}

		private DetailPrototype[] outs;

		DetailPrototype[] outs()
		{
			if (this.outs == null) {
				final DetailInput[] ds = this.inputs.inputs();
				if (ds != null) {
					final int dl = ds.length;

					final DetailPrototype[] di = new DetailPrototype[ dl ];
					for (int i = 0; i < dl; i++) {
						di[ i ] = new DetailPrototype( ds[ i ], this );
					}

					this.outs = di;
				}
				else {
					this.outs = new DetailPrototype[ 0 ];
				}
			}
			return this.outs;
		}

		@Override
		public DetailOutput[] outputsArray()
		{
			return outs();
		}

		@Override
		public Iterator<DetailOutput> outputsIterator()
		{
			return outputsList().iterator();
		}

		@Override
		public Iterable<DetailOutput> outputsIterable()
		{
			return outputsList();
		}

		@Override
		public Collection<DetailOutput> outputsCollection()
		{
			return outputsList();
		}

		@Override
		public List<DetailOutput> outputsList()
		{
			final DetailPrototype[] arr = outs();
			final List<DetailOutput> res = New.newList( arr.length );
			for (DetailPrototype det : arr)
				res.add( det );
			return res;
		}

		public void reset()
		{
			this.outs = null;
		}

	}


	public static final class DetailPrototype implements DetailOutput
	{
		private final RootPrototype parent;
		private final DetailInput inputs;

		public DetailPrototype(DetailInput _inputs, RootPrototype _parent)
		{
			this.parent = _parent;
			this.inputs = _inputs;
		}

		public long output()
		{
			return this.inputs.input() + 1;
		}

		void parentRef() // make warning go away
		{
			this.parent.toString();
		}

	}


	public static final class RootInput
	{
		private DetailInput[] inputs;
		public int inputsLen;

		public RootInput(long... _detailValues)
		{
			this.inputs = new DetailInput[ _detailValues.length ];
			for (int i = 0; i < _detailValues.length; i++) {
				this.inputs[ i ] = new DetailInput( _detailValues[ i ] );
			}
			this.inputsLen = this.inputs.length;
		}

		public DetailInput[] inputs()
		{
			DetailInput[] r = new DetailInput[ this.inputsLen ];
			System.arraycopy( this.inputs, 0, r, 0, r.length );
			return r;
		}
	}

	public static final class DetailInput
	{
		private final long value;

		public DetailInput(long _value)
		{
			this.value = _value;
		}

		public long input()
		{
			return this.value;
		}
	}

}
