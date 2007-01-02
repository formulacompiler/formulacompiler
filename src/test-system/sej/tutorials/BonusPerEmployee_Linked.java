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
import sej.runtime.Resettable;
import sej.runtime.ScaledLong;
import sej.tutorials.BonusPerEmployee.BonusData;
import sej.tutorials.BonusPerEmployee.BonusDataImpl;
import sej.tutorials.BonusPerEmployee.EmployeeBonusData;
import sej.tutorials.BonusPerEmployee.EmployeeBonusDataImpl;
import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public class BonusPerEmployee_Linked extends TestCase
{
	private static final String SHEETPATH = "src/test-system/testdata/sej/tutorials/BonusPerEmployee.xls";


	public void testBonusPerEmployee() throws Exception
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.setNumericType( SEJ.SCALEDLONG4 );
		builder.loadSpreadsheet( SHEETPATH );
		builder.setFactoryClass( BonusComputationFactory.class );

		// builder.bindAllByName();
		bindElements( builder );

		Engine engine = builder.compile();
		BonusComputationFactory factory = (BonusComputationFactory) engine.getComputationFactory();

		{
			long bonusTotal = 200000000L;
			long overtimeSalaryPerHour = 500000L;
			long[] salaries = { 56000000L, 54000000L, 55000000L };
			int[] hoursOvertime = { 20, 15, 0 };
			long[] bonuses = { 72328800L, 67397300L, 60274000L };
			assertBonuses( factory, bonusTotal, overtimeSalaryPerHour, salaries, hoursOvertime, bonuses );
		}
	}


	private void bindElements( EngineBuilder _builder ) throws CompilerException, NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();

		Section binder = _builder.getRootBinder();

		Cell bonusTotalCell = sheet.getCell( "BonusTotal" );
		Method bonusTotalMethod = BonusData.class.getMethod( "bonusTotal" );
		binder.defineInputCell( bonusTotalCell, new CallFrame( bonusTotalMethod ) );

		Cell overtimeRateCell = sheet.getCell( "OvertimeSalaryPerHour" );
		Method overtimeRateMethod = BonusData.class.getMethod( "overtimeSalaryPerHour" );
		binder.defineInputCell( overtimeRateCell, new CallFrame( overtimeRateMethod ) );

		Range range = sheet.getRange( "Employees" );

		// input
		Method inputMethod = BonusData.class.getMethod( "employees" );
		CallFrame inputCall = new CallFrame( inputMethod );
		Class inputType = EmployeeBonusData.class;

		// output
		Method outputMethod = BonusComputation.class.getMethod( "employees" );
		CallFrame outputCall = new CallFrame( outputMethod );
		Class outputType = EmployeeBonusComputation.class;

		Orientation orient = Orientation.VERTICAL;

		Section employees = binder.defineRepeatingSection( range, orient, inputCall, inputType, outputCall, outputType );

		Cell salaryCell = sheet.getCell( "BaseSalary" );
		Method salaryMethod = inputType.getMethod( "baseSalary" );
		employees.defineInputCell( salaryCell, new CallFrame( salaryMethod ) );

		Cell overtimeCell = sheet.getCell( "HoursOvertime" );
		Method overtimeMethod = inputType.getMethod( "hoursOvertime" );
		employees.defineInputCell( overtimeCell, new CallFrame( overtimeMethod ) );

		Cell bonusCell = sheet.getCell( "BonusAmount" );
		Method bonusMethod = outputType.getMethod( "bonusAmount" );
		employees.defineOutputCell( bonusCell, new CallFrame( bonusMethod ) );
	}


	private void assertBonuses( BonusComputationFactory _factory, long _bonusTotal, long _overtimeSalaryPerHour,
			long[] _salaries, int[] _hoursOvertime, long[] _expectedBonusAmounts )
	{
		BonusDataImpl data = new BonusDataImpl( _bonusTotal, _overtimeSalaryPerHour );
		for (int i = 0; i < _salaries.length; i++) {
			EmployeeBonusDataImpl emp = new EmployeeBonusDataImpl( _salaries[ i ], _hoursOvertime[ i ] );
			data.addEmployee( emp );
		}

		// ---- consumeOutputs
		BonusComputation comp = _factory.newComputation( data );
		EmployeeBonusComputation[] /**/empOutputs = comp.employees()/**/;
		EmployeeBonusData[] /**/empInputs = data.employees()/**/;
		for (int i = 0; i < _expectedBonusAmounts.length; i++) {
			/**/assertSame( empInputs[ i ], empOutputs[ i ].inputs() );/**/
		}
		// ---- consumeOutputs

	}


	public static interface BonusComputationFactory
	{
		public BonusComputation newComputation( BonusData _data );
	}


	// ---- Outputs
	@ScaledLong(4)
	public static interface BonusComputation extends Resettable
	{
		EmployeeBonusComputation[] employees();
	}

	@ScaledLong(4)
	public static abstract class EmployeeBonusComputation
	{
		private final EmployeeBonusData inputs;

		public EmployeeBonusComputation( /**/EmployeeBonusData _inputs/**/)
		{
			super();
			this.inputs = _inputs;
		}

		public/**/EmployeeBonusData inputs()/**/
		{
			return this.inputs;
		}

		public abstract long bonusAmount();
	}
	// ---- Outputs

}
