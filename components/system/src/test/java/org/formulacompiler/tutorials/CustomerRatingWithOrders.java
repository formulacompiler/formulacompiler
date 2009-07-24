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

package org.formulacompiler.tutorials;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

import junit.framework.Test;

public class CustomerRatingWithOrders extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	private static enum AccessorVersion {
		ARRAY, ITERABLE, ITERATOR;
	}


	public void testCustomerRatingWithArray() throws Exception
	{
		doTestCustomerRating( AccessorVersion.ARRAY );
	}

	public void testCustomerRatingWithIterable() throws Exception
	{
		doTestCustomerRating( AccessorVersion.ITERABLE );
	}

	public void testCustomerRatingWithIterator() throws Exception
	{
		doTestCustomerRating( AccessorVersion.ITERATOR );
	}


	public void doTestCustomerRating( AccessorVersion _version ) throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( getPath() );
		builder.setFactoryClass( CustomerRatingFactory.class );

		// LATER Make orders for last N days bindable automatically
		// builder.bindAllByName();
		bindElements( builder, _version );

		Engine engine = builder.compile();
		if (_version == AccessorVersion.ARRAY) {
			FormulaDecompiler.decompile( engine ).saveTo( "temp/test/decompiled/customerRatingWithOrders" );
		}
		CustomerRatingFactory factory = (CustomerRatingFactory) engine.getComputationFactory();

		// Original sheet has five rows in the section. First, we pass the same number of values.
		assertRating( "Average", factory, 1000, 2000, 1000, 1500, 1000 );

		// Let's pass fewer values.
		assertRating( "Good", factory, 5000, 3000 );

		// And more values.
		assertRating( "Excellent", factory, 1000, 2000, 1000, 1500, 1000, 10000 );
	}


	private String getPath()
	{
		return "src/test/data/org/formulacompiler/tutorials/CustomerRating" + getSpreadsheetExtension();
	}


	private void bindElements( EngineBuilder _builder, AccessorVersion _version ) throws CompilerException,
			NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();
		// ---- bindOrders
		Section binder = _builder.getRootBinder();
		// ---- bindOrders

		// ---- bindRating
		Cell ratingCell = sheet.getCell( "Rating" );
		Method ratingMethod = CustomerRating.class.getMethod( "rating" );
		/**/binder/**/.defineOutputCell( ratingCell, ratingMethod );
		// ---- bindRating

		// ---- bindOrders
		Range range = sheet.getRange( "OrdersForLastThreeMonths" );
		Method mtd = /**/CustomerData/**/.class.getMethod( /**/"ordersForLastNDays"/**/, Integer.TYPE );
		// -- omit
		switch (_version) {
			case ITERABLE:
				mtd = CustomerData.class.getMethod( "ordersForLastNDaysIterable", Integer.TYPE );
				break;
			case ITERATOR:
				mtd = CustomerData.class.getMethod( "ordersForLastNDaysIterator", Integer.TYPE );
				break;
		}
		// -- omit
		CallFrame call = _builder.newCallFrame( mtd, 90 ); // last 3 months is 90 days back
		Orientation orient = Orientation.VERTICAL;
		Class input = /**/OrderData/**/.class;

		Section /**/orders/**/ = binder./**/defineRepeatingSection/**/( range, orient, call, input, null, null );
		// ---- bindOrders

		// ---- bindOrderValues
		Cell totalCell = sheet.getCell( "OrderTotal" );
		Method totalMethod = /**/OrderData/**/.class.getMethod( "total" );
		/**/orders/**/.defineInputCell( totalCell, totalMethod );
		// ---- bindOrderValues
	}


	private void assertRating( String _expected, CustomerRatingFactory _factory, double... _orderTotals )
	{
		CustomerData customer = new CustomerDataImpl( _orderTotals );
		CustomerRating ratingStrategy = _factory.newRating( customer );
		String rating = ratingStrategy.rating();
		assertEquals( _expected, rating );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( CustomerRatingWithOrders.class );
	}


	public static interface CustomerRatingFactory
	{
		public CustomerRating newRating( CustomerData _data );
	}


	public static interface CustomerRating
	{
		public String rating();
	}


	// ---- CustomerData
	public static interface CustomerData
	{
		public /**/OrderData[]/**/ ordersForLastNDays( int _days );
		// -- CustomerDataAlternatives
		public /**/Iterable<OrderData>/**/ ordersForLastNDaysIterable( int _days );
		public /**/Iterator<OrderData>/**/ ordersForLastNDaysIterator( int _days );
		// -- CustomerDataAlternatives
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

		public CustomerDataImpl( double[] _orderTotals )
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

		public Iterable<OrderData> ordersForLastNDaysIterable( int _days )
		{
			final Collection<OrderData> result = New.collection( this.orders.length );
			Collections.addAll( result, this.orders );
			return result;
		}

		public Iterator<OrderData> ordersForLastNDaysIterator( int _days )
		{
			return ordersForLastNDaysIterable( _days ).iterator();
		}
	}

	private static class OrderDataImpl implements OrderData
	{
		private final double total;

		public OrderDataImpl( double _total )
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
