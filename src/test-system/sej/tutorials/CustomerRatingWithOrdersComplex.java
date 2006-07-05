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
import java.util.Calendar;
import java.util.Date;

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

public class CustomerRatingWithOrdersComplex extends TestCase
{
	private static final Calendar TODAY = today();
	
	private static final Calendar today()
	{
		Calendar now = Calendar.getInstance();
		now.set( Calendar.HOUR, 0 );
		now.set( Calendar.MINUTE, 0 );
		now.set( Calendar.SECOND, 0 );
		now.set( Calendar.MILLISECOND, 0 );
		return now;
	}
	
	private static final Date beforeToday( int _daysBack )
	{
		Calendar back = (Calendar) TODAY.clone();
		back.add( Calendar.DAY_OF_MONTH, -_daysBack );
		return back.getTime();
	}

	
	// LATER Use MATCH to get the rating given the total
	// LATER Use INDEX to get a string rating instead of a numeric one
	// FIXME Test reset() with sections


	public void testCustomerRating() throws Exception
	{
		String path = "src/test-system/testdata/sej/tutorials/CustomerRatingComplex.xls";

		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( CustomerRatingFactory.class );

		// TODO Make orders for last N days bindable automatically
		// builder.bindAllByName();
		bindElements( builder );

		Engine engine = builder.compile();
		CustomerRatingFactory factory = (CustomerRatingFactory) engine.getComputationFactory();

		// Let's pass fewer values.
		assertRating( 1, factory, new double[] {5000, 3000}, new int[] {10, 78} );

	}


	private void bindElements( EngineBuilder _builder ) throws CompilerException, NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();
		Section binder = _builder.getRootBinder();

		Cell ratingCell = sheet.getCell( "Rating" );
		binder.defineOutputCell( ratingCell, new CallFrame( _builder.getOutputClass().getMethod( "rating" ) ) );

		// Bind orders section
		{
			Range range = sheet.getRange( "OrdersForLastThreeMonths" );
			Method mtd = CustomerData.class.getMethod( "ordersForLastNDays", Integer.TYPE );
			CallFrame call = new CallFrame( mtd, 90 );
			Orientation orient = Orientation.VERTICAL;
			Class input = OrderData.class;

			Section orders = binder.defineRepeatingSection( range, orient, call, input, null, null );

			Cell totalCell = sheet.getCell( "OrderTotal" );
			orders.defineInputCell( totalCell, new CallFrame( OrderData.class.getMethod( "total" ) ) );
			Cell dateCell = sheet.getCell( "OrderDate" );
			orders.defineInputCell( dateCell, new CallFrame( OrderData.class.getMethod( "date" ) ) );
		}
	}


	private void assertRating( int _expected, CustomerRatingFactory _factory, double[] _orderTotals, int[] _daysBack )
	{
		CustomerData customer = new CustomerDataImpl( _orderTotals, _daysBack );
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
		public OrderData[] ordersForLastNDays( int _days );
	}

	// ---- CustomerData

	// ---- OrderData
	public static interface OrderData
	{
		public double total();
		public Date date();
	}

	// ---- OrderData


	private static class CustomerDataImpl implements CustomerData
	{
		private final OrderDataImpl[] orders;

		public CustomerDataImpl(double[] _orderTotals, int[] _daysBack)
		{
			super();
			this.orders = new OrderDataImpl[ _orderTotals.length ];
			for (int i = 0; i < _orderTotals.length; i++) {
				this.orders[ i ] = new OrderDataImpl( _orderTotals[ i ], _daysBack[ i ] );
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
		private final Date date;

		public OrderDataImpl(double _total, int _daysBack)
		{
			super();
			this.total = _total;
			this.date = beforeToday( _daysBack );
		}

		public double total()
		{
			return this.total;
		}
		
		public Date date()
		{
			return this.date;
		}
	}

}
