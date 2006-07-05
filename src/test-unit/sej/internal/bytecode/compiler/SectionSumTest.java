/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.internal.bytecode.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import sej.Aggregator;
import sej.CallFrame;
import sej.CompilerException;
import sej.EngineBuilder;
import sej.Operator;
import sej.Orientation;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBuilder;
import sej.Spreadsheet.Range;
import sej.SpreadsheetBinder.Section;
import sej.SpreadsheetBuilder.CellRef;
import sej.runtime.Computation;
import junit.framework.TestCase;


/**
 * This is the prototype of how SEJ implements sums over variably sized sections.
 * 
 * @author peo
 */
public class SectionSumTest extends TestCase
{

	public void testSums() throws Exception
	{
		RootInput input = new RootInput( 10, 2, 3, 4, 5 );
		int expected = 10 + 4 * (2 + 3 + 4 + 5);
		assertSums( expected, input );
	}

	public void testEmptySums() throws Exception
	{
		RootInput input = new RootInput( 20 );
		int expected = 20;
		assertSums( expected, input );
	}

	private void assertSums( int _expected, RootInput _input ) throws Exception
	{
		assertSums( _expected, new RootPrototype( _input ) );
		assertSums( _expected, newRootEngine( _input ) );
	}

	private void assertSums( int _expected, RootOutput _output )
	{
		assertEquals( _expected, _output.arraySums() );
		assertEquals( _expected, _output.iteratorSums() );
		assertEquals( _expected, _output.iterableSums() );
		assertEquals( _expected, _output.collectionSums() );
	}

	private RootOutput newRootEngine( RootInput _input ) throws Exception
	{
		SpreadsheetBuilder bld = SEJ.newSpreadsheetBuilder();
		bld.newCell( bld.cst( 1 ) );
		bld.nameCell( "rootValue" );
		CellRef rootCell = bld.currentCell();

		buildSectionSum( bld, rootCell, "array" );
		buildSectionSum( bld, rootCell, "iterator" );
		buildSectionSum( bld, rootCell, "iterable" );
		buildSectionSum( bld, rootCell, "collection" );

		Spreadsheet sht = bld.getSpreadsheet();

		EngineBuilder cmp = SEJ.newEngineBuilder();
		cmp.setSpreadsheet( sht );
		cmp.setInputClass( RootInput.class );
		cmp.setOutputClass( RootOutput.class );
		cmp.setNumericType( SEJ.LONG );
		Section bnd = cmp.getRootBinder();

		bnd.defineInputCell( sht.getCell( "rootValue" ), call( RootInput.class, "getRootValue" ) );

		bindSectionSum( sht, bnd, "array" );
		bindSectionSum( sht, bnd, "iterator" );
		bindSectionSum( sht, bnd, "iterable" );
		bindSectionSum( sht, bnd, "collection" );

		SaveableEngine engine = cmp.compile();

		return (RootOutput) engine.getComputationFactory().newComputation( _input );
	}

	private void buildSectionSum( SpreadsheetBuilder _bld, CellRef _rootCell, String _prefix )
	{
		_bld.newRow();
		_bld.newCell( _bld.cst( 2 ) );
		_bld.nameCell( _prefix + "Dets" );
		CellRef detCell = _bld.currentCell();
		_bld.newCell( _bld.op( Operator.TIMES, _bld.ref( detCell ), _bld.ref( _bld.cst( 2 ) ) ) );
		CellRef detTwice = _bld.currentCell();
		_bld.newRow();
		_bld.newCell( _bld.agg( Aggregator.SUM, _bld.ref( detTwice ) ) );
		CellRef detSum = _bld.currentCell();
		_bld.newCell( _bld.agg( Aggregator.SUM, _bld.ref( _rootCell ), _bld.ref( detSum ), _bld.ref( detSum ) ) );
		_bld.nameCell( _prefix + "Sums" );
	}

	private void bindSectionSum( Spreadsheet _sht, Section _bnd, String _prefix ) throws CompilerException, Exception
	{
		Range rng = _sht.getRange( _prefix + "Dets" );
		CallFrame iter = call( RootInput.class, "get" + _prefix );
		Section dets = _bnd.defineRepeatingSection( rng, Orientation.VERTICAL, iter, DetailInput.class, null, null );
		dets.defineInputCell( _sht.getCell( _prefix + "Dets" ), call( DetailInput.class, "getDetailValue" ) );
		_bnd.defineOutputCell( _sht.getCell( _prefix + "Sums" ), call( RootOutput.class, _prefix + "Sums" ) );
	}

	private CallFrame call( Class _cls, String _mtd ) throws Exception
	{
		return new CallFrame( _cls.getMethod( _mtd ) );
	}


	public static interface RootOutput
	{
		long arraySums();
		long iteratorSums();
		long iterableSums();
		long collectionSums();
	}


	public static final class RootPrototype implements Computation, RootOutput
	{
		private final RootInput inputs;

		public RootPrototype(RootInput _inputs)
		{
			this.inputs = _inputs;
		}

		public long arraySums()
		{
			return this.inputs.getRootValue() + arraySum() + arraySum();
		}

		long arraySum()
		{
			final DetailPrototype[] ds = arrayDets();
			final int dl = ds.length;
			if (dl > 0) {
				long result = ds[ 0 ].detailValue();
				for (int i = 1; i < dl; i++) {
					result += ds[ i ].detailValue();
				}
				return result;
			}
			else {
				return 0;
			}
		}

		private DetailPrototype[] arrayDets;

		/**
		 * The simplest case: build an array from an array.
		 */
		DetailPrototype[] arrayDets()
		{
			if (this.arrayDets == null) {
				final DetailInput[] ds = this.inputs.getarray();
				if (ds != null) {
					final int dl = ds.length;

					final DetailPrototype[] di = new DetailPrototype[ dl ];
					for (int i = 0; i < dl; i++) {
						di[ i ] = new DetailPrototype( ds[ i ] );
					}
					
					this.arrayDets = di;
				}
				else {
					this.iteratorDets = new DetailPrototype[ 0 ];
				}
			}
			return this.arrayDets;
		}


		public long iteratorSums()
		{
			return this.inputs.getRootValue() + iteratorSum() + iteratorSum();
		}

		long iteratorSum()
		{
			long result = 0;
			final DetailPrototype[] ds = iteratorDets();
			final int dl = ds.length;
			for (int i = 0; i < dl; i++) {
				final DetailPrototype d = ds[ i ];
				result += d.detailValue();
			}
			return result;
		}

		private DetailPrototype[] iteratorDets;

		/**
		 * Build an array from an iterator.
		 */
		DetailPrototype[] iteratorDets()
		{
			if (this.iteratorDets == null) {
				final Iterator<DetailInput> ds = this.inputs.getiterator();
				if (ds != null) {

					final Collection<DetailPrototype> coll = new ArrayList<DetailPrototype>();
					while (ds.hasNext()) {
						coll.add( new DetailPrototype( ds.next() ) );
					}
					final DetailPrototype[] di = coll.toArray( new DetailPrototype[ coll.size() ] );

					this.iteratorDets = di;
				}
				else {
					this.iteratorDets = new DetailPrototype[ 0 ];
				}
			}
			return this.iteratorDets;
		}


		public long iterableSums()
		{
			return this.inputs.getRootValue() + iterableSum() + iterableSum();
		}

		long iterableSum()
		{
			long result = 0;
			final DetailPrototype[] ds = iterableDets();
			final int dl = ds.length;
			for (int i = 0; i < dl; i++) {
				final DetailPrototype d = ds[ i ];
				result += d.detailValue();
			}
			return result;
		}

		private DetailPrototype[] iterableDets;

		/**
		 * Build an array from an iterable's iterator.
		 */
		DetailPrototype[] iterableDets()
		{
			if (this.iterableDets == null) {
				final Iterator<DetailInput> ds = this.inputs.getiterable().iterator();
				if (ds != null) {
					
					final Collection<DetailPrototype> coll = new ArrayList<DetailPrototype>();
					while (ds.hasNext()) {
						final DetailInput d = ds.next();
						coll.add( new DetailPrototype( d ) );
					}
					final DetailPrototype[] di = coll.toArray( new DetailPrototype[ coll.size() ] );
					
					this.iterableDets = di;
				}
				else {
					this.iterableDets = new DetailPrototype[ 0 ];
				}
			}
			return this.iterableDets;
		}


		public long collectionSums()
		{
			return this.inputs.getRootValue() + collectionSum() + collectionSum();
		}

		long collectionSum()
		{
			long result = 0;
			final DetailPrototype[] ds = collectionDets();
			final int dl = ds.length;
			for (int i = 0; i < dl; i++) {
				final DetailPrototype d = ds[ i ];
				result += d.detailValue();
			}
			return result;
		}

		private DetailPrototype[] collectionDets;

		/**
		 * Build an array from a collection. Since we know the size of the collection, pre-allocate
		 * the array.
		 */
		DetailPrototype[] collectionDets()
		{
			if (this.collectionDets == null) {
				final Collection<DetailInput> dc = this.inputs.getcollection();
				if (dc != null) {
					
					final int dl = dc.size();
					final Iterator<DetailInput> ds = dc.iterator();
					final DetailPrototype[] di = new DetailPrototype[ dl ];
					for (int i = 0; i < dl; i++) {
						di[ i ] = new DetailPrototype( ds.next() );
					}
					
					this.collectionDets = di;
				}
				else {
					this.collectionDets = new DetailPrototype[ 0 ];
				}
			}
			return this.collectionDets;
		}

	}

	public static final class DetailPrototype
	{
		private final DetailInput inputs;

		public DetailPrototype(DetailInput _inputs)
		{
			this.inputs = _inputs;
		}

		long detailValue()
		{
			return this.inputs.getDetailValue() * 2;
		}

	}


	public static final class RootInput
	{
		private final long value;
		private final DetailInput[] details;
		private final Collection<DetailInput> detailCollection = new ArrayList<DetailInput>();

		public RootInput(long _rootValue, long... _detailValues)
		{
			this.value = _rootValue;
			this.details = new DetailInput[ _detailValues.length ];
			for (int i = 0; i < _detailValues.length; i++) {
				this.details[ i ] = new DetailInput( _detailValues[ i ] );
				this.detailCollection.add( this.details[ i ] );
			}
		}

		public long getRootValue()
		{
			return this.value;
		}

		public DetailInput[] getarray()
		{
			return this.details;
		}

		public Collection<DetailInput> getcollection()
		{
			return this.detailCollection;
		}

		public Iterable<DetailInput> getiterable()
		{
			return this.detailCollection;
		}

		public Iterator<DetailInput> getiterator()
		{
			return this.detailCollection.iterator();
		}

	}

	public static final class DetailInput
	{
		private final long value;

		public DetailInput(long _value)
		{
			this.value = _value;
		}

		public long getDetailValue()
		{
			return this.value;
		}
	}

}
