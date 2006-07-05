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
package sej.tutorials;

import java.lang.reflect.Method;

import sej.CallFrame;
import sej.CompilerException;
import sej.EngineBuilder;
import sej.Orientation;
import sej.SEJ;
import sej.Spreadsheet;
import sej.Spreadsheet.Cell;
import sej.Spreadsheet.Range;
import sej.SpreadsheetBinder.Section;
import sej.runtime.Engine;
import junit.framework.TestCase;

public class CustomerRatingWithOrders extends TestCase
{

	// LATER Use MATCH to get the rating given the total
	// LATER Use INDEX to get a string rating instead of a numeric one
	// FIXME Test reset() with sections


	public void testCustomerRating() throws Exception
	{
		String path = "src/test-system/testdata/sej/tutorials/CustomerRating.xls";

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( CustomerRatingFactory.class );

		// TODO Make orders for last N days bindable automatically
		// builder.bindAllByName();
		bindElements( builder );

		Engine engine = builder.compile();
		CustomerRatingFactory factory = (CustomerRatingFactory) engine.getComputationFactory();

		// Original sheet has five rows in the section. First, we pass the same number of values.
		assertRating( 2, factory, 1000, 2000, 1000, 1500, 1000 );

		// Let's pass fewer values.
		assertRating( 3, factory, 5000, 3000 );

		// And more values.
		assertRating( 4, factory, 1000, 2000, 1000, 1500, 1000, 10000 );
	}


	private void bindElements( EngineBuilder _builder ) throws CompilerException, NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();
		// ---- bindOrders
		Section binder = _builder.getRootBinder();
		// ---- bindOrders

		Cell ratingCell = sheet.getCell( "Rating" );
		binder.defineOutputCell( ratingCell, new CallFrame( _builder.getOutputClass().getMethod( "rating" ) ) );

		// ---- bindOrders
		Range range = sheet.getRange( "OrdersForLastThreeMonths" );
		Method mtd = /**/CustomerData/**/.class.getMethod( /**/"ordersForLastNDays"/**/, Integer.TYPE );
		CallFrame call = new CallFrame( mtd, 90 ); // last 3 months is 90 days back
		Orientation orient = Orientation.VERTICAL;
		Class input = /**/OrderData/**/.class;

		Section /**/orders/**/ = binder./**/defineRepeatingSection/**/( range, orient, call, input, null, null );
		// ---- bindOrders

		// ---- bindOrderValues
		Cell totalCell = sheet.getCell( "OrderTotal" );
		Method totalMethod = /**/OrderData/**/.class.getMethod( "total" );
		/**/orders/**/.defineInputCell( totalCell, new CallFrame( totalMethod ) );
		// ---- bindOrderValues
	}


	private void assertRating( int _expected, CustomerRatingFactory _factory, double... _orderTotals )
	{
		CustomerData customer = new CustomerDataImpl( _orderTotals );
		CustomerRating ratingStrategy = _factory.newRating( customer );
		int rating = ratingStrategy.rating();
		assertEquals( _expected, rating );
	}


	public static interface CustomerRatingFactory
	{
		public CustomerRating newRating( CustomerData _data );
	}


	public static interface CustomerRating
	{
		public int rating();
	}


	// ---- CustomerData
	public static interface CustomerData
	{
		public /**/OrderData[]/**/ ordersForLastNDays( int _days );
	}

	// ---- CustomerData

	// ---- OrderData
	public static interface OrderData
	{
		public double total();
	}

	// ---- OrderData


	private static class CustomerDataImpl implements CustomerData
	{
		private final OrderDataImpl[] orders;

		public CustomerDataImpl(double[] _orderTotals)
		{
			super();
			this.orders = new OrderDataImpl[ _orderTotals.length ];
			for (int i = 0; i < _orderTotals.length; i++) {
				this.orders[ i ] = new OrderDataImpl( _orderTotals[ i ] );
			}
		}

		public OrderData[] ordersForLastNDays( int _days )
		{
			return this.orders;
		}
	}

	private static class OrderDataImpl implements OrderData
	{
		private final double total;

		public OrderDataImpl(double _total)
		{
			super();
			this.total = _total;
		}

		public double total()
		{
			return this.total;
		}
	}

}
