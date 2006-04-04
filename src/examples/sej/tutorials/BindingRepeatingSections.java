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

import java.io.IOException;
import java.lang.reflect.Method;

import sej.CallFrame;
import sej.Compiler;
import sej.CompilerFactory;
import sej.ModelError;
import sej.Orientation;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;


public class BindingRepeatingSections
{

	public void bindingRepeatingSections() throws IOException, ModelError, NoSuchMethodException
	{
		Spreadsheet spreadsheet = SpreadsheetLoader.loadFromFile( "src/test-system/data/tutorials/BindingCells.xls" );

		// ---- createCompiler
		Class input = Input.class;
		Class output = Output.class;
		Compiler compiler = CompilerFactory.newDefaultCompiler( spreadsheet, input, output );
		Compiler.Section root = compiler.getRoot();
		// ---- createCompiler

		Method method, inputMethod, outputMethod;
		Spreadsheet.Range range;
		Spreadsheet.Cell cell;

		// ---- bindInputSection
		range = spreadsheet.getRange( "ORDERS" );
		inputMethod = input.getMethod( "getOrders" );
		Compiler.Section orders;
		orders = root.defineRepeatingSection( range, Orientation.VERTICAL, new CallFrame( inputMethod ), null );
		// ---- bindInputSection

		// ---- bindInputCell
		cell = spreadsheet.getCell( "ORDER_TOTAL" );
		method = Order.class.getMethod( "getTotal" );
		orders.defineInputCell( cell, new CallFrame( method ) );
		// ---- bindInputCell

		// ---- bindIOSection
		range = spreadsheet.getRange( "EMPLOYEES" );
		inputMethod = Input2.class.getMethod( "getEmployees" );
		outputMethod = Output.class.getMethod( "getEmployees" );
		Compiler.Section employees;
		employees = root.defineRepeatingSection( range, Orientation.VERTICAL, new CallFrame( inputMethod ),
				new CallFrame( outputMethod ) );
		// ---- bindIOSection
		
		// ---- bindOutputCell
		cell = spreadsheet.getCell( "BONUS_AMOUNT" );
		method = Output.Employee.class.getMethod( "getBonusAmount" );
		employees.defineOutputCell( cell, new CallFrame( method ) );
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
