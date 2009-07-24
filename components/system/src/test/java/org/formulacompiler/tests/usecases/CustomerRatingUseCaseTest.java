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

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;


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


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( CustomerRatingUseCaseTest.class );
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
