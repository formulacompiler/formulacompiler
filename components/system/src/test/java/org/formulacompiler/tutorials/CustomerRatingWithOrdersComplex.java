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
package org.formulacompiler.tutorials;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;

import junit.framework.TestCase;

@SuppressWarnings( "unchecked" )
public class CustomerRatingWithOrdersComplex extends TestCase
{
	private static final Calendar TODAY = today();

	private static final Calendar today()
	{
		Calendar now = Calendar.getInstance();
		now.set( Calendar.HOUR_OF_DAY, 0 );
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


	public void testCustomerRating() throws Exception
	{
		String path = "src/test/data/org/formulacompiler/tutorials/CustomerRatingComplex.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.setFactoryClass( CustomerRatingFactory.class );

		// LATER Make orders for last N days bindable automatically
		// builder.bindAllByName();
		bindElements( builder );

		Engine engine = builder.compile();
		CustomerRatingFactory factory = (CustomerRatingFactory) engine.getComputationFactory();

		// Let's pass fewer values.
		assertRating( 2, factory, new double[] { 5000, 3000 }, new int[] { 10, 78 } );

	}


	private void bindElements( EngineBuilder _builder ) throws CompilerException, NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();
		Section binder = _builder.getRootBinder();

		Cell ratingCell = sheet.getCell( "Rating" );
		binder.defineOutputCell( ratingCell, _builder.getOutputClass().getMethod( "rating" ) );

		Range range = sheet.getRange( "OrdersForLastThreeMonths" );
		Method mtd = CustomerData.class.getMethod( "ordersForLastNDays", Integer.TYPE );
		CallFrame call = _builder.newCallFrame( mtd, 90 );
		Orientation orient = Orientation.VERTICAL;
		Class input = OrderData.class;

		Section orders = binder.defineRepeatingSection( range, orient, call, input, null, null );

		// ---- bindOrderValues
		Cell totalCell = sheet.getCell( "OrderTotal" );
		/**/orders/**/.defineInputCell( totalCell, /**/OrderData/**/.class.getMethod( "total" ) );

		Cell dateCell = sheet.getCell( "OrderDate" );
		/**/orders/**/.defineInputCell( dateCell, "date" ); // shorter form
		// ---- bindOrderValues
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


	public static interface CustomerRating extends Resettable
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
		/**/public Date date();/**/
	}

	// ---- OrderData


	private static class CustomerDataImpl implements CustomerData
	{
		private final OrderDataImpl[] orders;

		public CustomerDataImpl( double[] _orderTotals, int[] _daysBack )
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

		public OrderDataImpl( double _total, int _daysBack )
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
