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

import java.io.File;
import java.lang.reflect.Method;

import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;


public class BindingRepeatingSections
{

	public void bindingRepeatingSections() throws Exception
	{
		final File file = new File("src/test-system/data/tutorials/BindingCells.xls");

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( file );
		builder.setInputClass( Input.class );
		builder.setOutputClass( Output.class );
		Spreadsheet spreadsheet = builder.getSpreadsheet();
		SpreadsheetBinder.Section binder = builder.getRootBinder();

		Method method, inputMethod, outputMethod;
		Spreadsheet.Range range;
		Spreadsheet.Cell cell;

		// ---- bindInputSection
		/**/SpreadsheetBinder.Section orders;/**/
		range = spreadsheet.getRange( "ORDERS" );
		inputMethod = Input.class.getMethod( /**/"getOrders"/**/ );
		orders = binder./**/defineRepeatingSection/**/( range, Orientation.VERTICAL, inputMethod,
				/**/	Order.class/**/, null, null );
		// ---- bindInputSection

		// ---- bindInputCell
		cell = spreadsheet.getCell( "ORDER_TOTAL" );
		method = /**/Order.class/**/.getMethod( "getTotal" );
		/**/orders/**/.defineInputCell( cell, method );
		// ---- bindInputCell

		// ---- bindIOSection
		SpreadsheetBinder.Section employees;
		range = spreadsheet.getRange( "EMPLOYEES" );
		inputMethod = /**/Input2.class/**/.getMethod( "getEmployees" );
		outputMethod = /**/Output.class/**/.getMethod( "getEmployees" );
		employees = binder.defineRepeatingSection( range, Orientation.VERTICAL, inputMethod,
				/**/	Input2.Employee.class/**/, outputMethod, /**/Output.Employee.class/**/ );
		// ---- bindIOSection

		// ---- bindOutputCell
		cell = spreadsheet.getCell( "BONUS_AMOUNT" );
		method = /**/Output.Employee.class/**/.getMethod( "getBonusAmount" );
		/**/employees/**/.defineOutputCell( cell, method );
		// ---- bindOutputCell

	}


	// ---- Input
	public static interface Order
	{
		double getTotal();
	}

	public static interface Input
	{
		Order[] getOrders();
	}

	// ---- Input


	// ---- Input2
	public interface Input2
	{
		double getTotalBonusAmount();
		Iterable<Employee> getEmployees();

		public interface Employee
		{
			double getBaseSalaryAmount();
		}
	}

	// ---- Input2


	// ---- Output
	public static interface Output
	{
		Iterable<Employee> getEmployees();

		public interface Employee
		{
			double getBonusAmount();
		}
	}
	// ---- Output


}
