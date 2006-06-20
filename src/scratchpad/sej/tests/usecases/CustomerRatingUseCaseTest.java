package sej.tests.usecases;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import sej.Orientation;
import sej.Spreadsheet;
import sej.runtime.Computation;
import sej.runtime.Engine;

public class CustomerRatingUseCaseTest extends AbstractUseCaseTest
{

	
	public void testComputeCustomerRating() throws IOException, ModelError, SecurityException, NoSuchMethodException, InvocationTargetException
	{
		runUseCase( "CustomerRating", new UseCase()
		{

			public void defineEngine( Spreadsheet _model, CompilerNameSpace _root ) throws ModelError
			{
				_root.defineOutputCell( "Rating", ValueType.DOUBLE );
				_root.defineBand( "LastSales", Orientation.VERTICAL ).defineInputCell( "LastSale", ValueType.DOUBLE );
			}


			public void useEngine( Engine _engine ) throws InvocationTargetException
			{
				// First, we leave the number of sales figures as is.
				assertRating( "C", _engine, 1000, 2000, 1000, 1500, 1000 );
				// Let's pass fewer values.
				assertRating( "B", _engine, 5000, 3000 );
				// And more values.
				assertRating( "A", _engine, 1000, 2000, 1000, 1500, 1000, 10000 );
			}


			private void assertRating( String _rating, Engine _engine, double... _sales ) throws InvocationTargetException
			{
				Computation c = _engine.newComputation();
				Band sales = c.getBand( "LastSales" );
				for (double sale : _sales) {
					sales.newElement().setDouble( "LastSale", sale );
				}
				String rating = c.getString( "Rating" );
				assertEquals( _rating, rating );
			}


		} );
	}


}
