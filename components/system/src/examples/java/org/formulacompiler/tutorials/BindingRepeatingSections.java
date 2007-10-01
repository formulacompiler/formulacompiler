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

import org.formulacompiler.compiler.CallFrame;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;



public class BindingRepeatingSections
{

	public void bindingRepeatingSections() throws Exception
	{
		final String path = "src/test-system/data/tutorials/BindingCells.xls";

		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
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
		orders = binder./**/defineRepeatingSection/**/( range, Orientation.VERTICAL, new CallFrame( inputMethod ),
				/**/Order.class/**/, null, null );
		// ---- bindInputSection

		// ---- bindInputCell
		cell = spreadsheet.getCell( "ORDER_TOTAL" );
		method = /**/Order.class/**/.getMethod( "getTotal" );
		/**/orders/**/.defineInputCell( cell, new CallFrame( method ) );
		// ---- bindInputCell

		// ---- bindIOSection
		SpreadsheetBinder.Section employees;
		range = spreadsheet.getRange( "EMPLOYEES" );
		inputMethod = /**/Input2.class/**/.getMethod( "getEmployees" );
		outputMethod = /**/Output.class/**/.getMethod( "getEmployees" );
		employees = binder.defineRepeatingSection( range, Orientation.VERTICAL, new CallFrame( inputMethod ),
				/**/Input2.Employee.class/**/, new CallFrame( outputMethod ), /**/Output.Employee.class/**/ );
		// ---- bindIOSection

		// ---- bindOutputCell
		cell = spreadsheet.getCell( "BONUS_AMOUNT" );
		method = /**/Output.Employee.class/**/.getMethod( "getBonusAmount" );
		/**/employees/**/.defineOutputCell( cell, new CallFrame( method ) );
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
