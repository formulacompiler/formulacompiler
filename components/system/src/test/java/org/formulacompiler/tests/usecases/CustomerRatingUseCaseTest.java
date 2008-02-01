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

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;


public class CustomerRatingUseCaseTest extends AbstractUseCaseTest
{


	public void testComputeCustomerRating() throws Exception
	{
		runUseCase( "CustomerRating", new UseCase()
		{

			public void defineEngine( EngineBuilder _builder, Spreadsheet _model, Section _root ) throws Exception
			{
				_root.defineOutputCell( _model.getCell( "Rating" ), Outputs.class.getMethod( "rating" ) );
				Section sales = _root.defineRepeatingSection( _model.getRange( "LastSales" ), Orientation.VERTICAL,
						Inputs.class.getMethod( "lastSales" ), Sale.class, null, null );
				sales.defineInputCell( _model.getCell( "LastSale" ), Sale.class.getMethod( "total" ) );
			}


			public void useEngine( SaveableEngine _engine ) throws Exception
			{
				// First, we leave the number of sales figures as is.
				assertRating( 3, _engine, 1000, 2000, 1000, 1500, 1000 );
				// Let's pass fewer values.
				assertRating( 2, _engine, 5000, 3000 );
				// And more values.
				assertRating( 1, _engine, 1000, 2000, 1000, 1500, 1000, 10000 );
			}


			private void assertRating( int _rating, Engine _engine, double... _sales ) throws Exception
			{
				Inputs inputs = new Inputs( _sales );
				Outputs c = (Outputs) _engine.getComputationFactory().newComputation( inputs );
				int rating = c.rating();
				assertEquals( _rating, rating );
			}


		}, Inputs.class, Outputs.class );
	}


	public static final class Inputs
	{
		private final Sale[] sales;

		public Inputs( double[] _sales )
		{
			super();
			this.sales = new Sale[ _sales.length ];
			for (int i = 0; i < _sales.length; i++) {
				this.sales[ i ] = new Sale( _sales[ i ] );
			}
		}

		public Sale[] lastSales()
		{
			return this.sales;
		}

	}

	public static final class Sale
	{
		private final double total;

		public Sale( double _total )
		{
			super();
			this.total = _total;
		}

		public double total()
		{
			return this.total;
		}

	}

	public static interface Outputs extends Resettable
	{
		public abstract int rating();
	}

}
