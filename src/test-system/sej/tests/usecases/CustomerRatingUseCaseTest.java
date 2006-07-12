package sej.tests.usecases;

import sej.CallFrame;
import sej.Orientation;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.SpreadsheetBinder.Section;
import sej.runtime.Engine;
import sej.runtime.Resettable;

public class CustomerRatingUseCaseTest extends AbstractUseCaseTest
{


	public void testComputeCustomerRating() throws Exception
	{
		runUseCase( "CustomerRating", new UseCase()
		{

			public void defineEngine( Spreadsheet _model, Section _root ) throws Exception
			{
				_root.defineOutputCell( _model.getCell( "Rating" ), new CallFrame( Outputs.class.getMethod( "rating" ) ) );
				Section sales = _root.defineRepeatingSection( _model.getRange( "LastSales" ), Orientation.VERTICAL,
						new CallFrame( Inputs.class.getMethod( "lastSales" ) ), Sale.class, null, null );
				sales.defineInputCell( _model.getCell( "LastSale" ), new CallFrame( Sale.class.getMethod( "total" ) ) );
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

		public Inputs(double[] _sales)
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
		
		public Sale(double _total)
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
