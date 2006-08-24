/*
 * Copyright � 2006 by Abacus Research AG, Switzerland.
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import sej.CallFrame;
import sej.CompilerException;
import sej.EngineBuilder;
import sej.Orientation;
import sej.SEJ;
import sej.SaveableEngine;
import sej.Spreadsheet;
import sej.Spreadsheet.Cell;
import sej.Spreadsheet.Range;
import sej.SpreadsheetBinder.Section;
import sej.runtime.Resettable;
import sej.runtime.ScaledLong;
import junit.framework.TestCase;

public class BonusPerEmployee extends TestCase
{
	private static final String SHEETPATH = "src/test-system/testdata/sej/tutorials/BonusPerEmployee.xls";

	
	private static enum AccessorVersion
	{
		ARRAY, LIST, COLLECTION, ITERATOR;
	}
	
	
	public void testWithArray() throws Exception
	{
		doTest( AccessorVersion.ARRAY );
	}

	public void testWithList() throws Exception
	{
		doTest( AccessorVersion.LIST );
	}

	public void testWithCollection() throws Exception
	{
		doTest( AccessorVersion.COLLECTION );
	}

	public void testWithIterator() throws Exception
	{
		doTest( AccessorVersion.ITERATOR );
	}

	
	public void doTest( AccessorVersion _version ) throws Exception
	{
		EngineBuilder builder = SEJ.newEngineBuilder();
		builder.setNumericType( SEJ.SCALEDLONG4 );
		builder.loadSpreadsheet( SHEETPATH );
		builder.setFactoryClass( BonusComputationFactory.class );
		builder.setOutputClass( BonusComputationDefaults.class );

		// builder.bindAllByName();
		bindElements( builder, _version );

		SaveableEngine engine = builder.compile();
		
		BonusComputationFactory factory = (BonusComputationFactory) engine.getComputationFactory();

		{
			long bonusTotal = 200000000L;
			long overtimeSalaryPerHour = 500000L;
			long[] salaries = new long[] { 56000000L, 54000000L, 55000000L };
			int[] hoursOvertime = new int[] { 20, 15, 0 };
			// Expected results; not the same as in the sample because LONG4 is less precise than the DOUBLE in Excel
			long[] bonuses = new long[] { 72320000L, 67380000L, 60260000L };  
			assertBonuses( factory, bonusTotal, overtimeSalaryPerHour, salaries, hoursOvertime, bonuses, _version );
		}
	}


	private void bindElements( EngineBuilder _builder, AccessorVersion _version ) throws CompilerException, NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();
		
		// LATER Create simplified section binding on EngineBuilder
		
		// ---- bindSections
		Section binder = _builder.getRootBinder();
		// ---- bindSections

		// ---- bindGlobals
		Cell bonusTotalCell = sheet.getCell( "BonusTotal" );
		Method bonusTotalMethod = BonusData.class.getMethod( "bonusTotal" );
		/**/binder/**/.defineInputCell( bonusTotalCell, new CallFrame( bonusTotalMethod ) );

		Cell overtimeRateCell = sheet.getCell( "OvertimeSalaryPerHour" );
		Method overtimeRateMethod = BonusData.class.getMethod( "overtimeSalaryPerHour" );
		/**/binder/**/.defineInputCell( overtimeRateCell, new CallFrame( overtimeRateMethod ) );
		// ---- bindGlobals

		// ---- bindSections
		Range range = sheet.getRange( "Employees" );
		
		// input
		Method inputMethod = /**/BonusData/**/.class.getMethod( /**/"employees"/**/);
		CallFrame inputCall = new CallFrame( inputMethod );
		Class inputType = /**/EmployeeBonusData/**/.class;

		// output
		Method outputMethod = /**/BonusComputation/**/.class.getMethod( /**/"employees"/**/);
		// -- omit
		switch (_version) {
			case LIST:
				outputMethod = BonusComputation.class.getMethod( "employeesList" );
				break;
			case COLLECTION:
				outputMethod = BonusComputation.class.getMethod( "employeesCollection" );
				break;
			case ITERATOR:
				outputMethod = BonusComputation.class.getMethod( "employeesIterator" );
				break;
		}
		// -- omit
		CallFrame outputCall = new CallFrame( outputMethod );
		Class outputType = /**/EmployeeBonusComputation/**/.class;

		Orientation orient = Orientation.VERTICAL;

		Section /**/employees/**/ = binder./**/defineRepeatingSection/**/( range, orient, inputCall, inputType,
				outputCall, outputType );
		// ---- bindSections
		
		// ---- bindEmployeeInputs
		Cell salaryCell = sheet.getCell( "BaseSalary" );
		Method salaryMethod = /**/inputType/**/.getMethod( "baseSalary" );
		/**/employees/**/.defineInputCell( salaryCell, new CallFrame( salaryMethod ) );

		Cell overtimeCell = sheet.getCell( "HoursOvertime" );
		Method overtimeMethod = /**/inputType/**/.getMethod( "hoursOvertime" );
		/**/employees/**/.defineInputCell( overtimeCell, new CallFrame( overtimeMethod ) );
		// ---- bindEmployeeInputs

		// ---- bindEmployeeOutputs
		Cell bonusCell = sheet.getCell( "BonusAmount" );
		Method bonusMethod = /**/outputType/**/.getMethod( "bonusAmount" );
		/**/employees/**/./**/defineOutputCell/**/( bonusCell, new CallFrame( bonusMethod ) );
		// ---- bindEmployeeOutputs
	}


	private void assertBonuses( BonusComputationFactory _factory, long _bonusTotal, long _overtimeSalaryPerHour,
			long[] _salaries, int[] _hoursOvertime, long[] _expectedBonusAmounts, AccessorVersion _version )
	{
		
		// ---- setupInputs
		BonusDataImpl data = new BonusDataImpl( _bonusTotal, _overtimeSalaryPerHour );
		for (int i = 0; i < _salaries.length; i++) {
			EmployeeBonusDataImpl emp = new EmployeeBonusDataImpl( _salaries[ i ], _hoursOvertime[ i ] );
			data.addEmployee( emp );
		}
		BonusComputation computation = _factory.newComputation( data );
		// ---- setupInputs
		
		switch (_version) {

			case ARRAY:
				// ---- consumeOutputs
				EmployeeBonusComputation[] /**/emps = computation.employees()/**/;
				for (int i = 0; i < _expectedBonusAmounts.length; i++) {
					long expected = _expectedBonusAmounts[ i ];
					long actual = /**/emps[ i ].bonusAmount()/**/;
					assertEquals( expected, actual );
				}
				// ---- consumeOutputs
				break;
		
			case LIST:
				// ---- consumeOutputsList
				List<EmployeeBonusComputation> /**/empList = computation.employeesList()/**/;
				for (int i = 0; i < _expectedBonusAmounts.length; i++) {
					long expected = _expectedBonusAmounts[ i ];
					long actual = /**/empList.get( i ).bonusAmount()/**/;
					assertEquals( expected, actual );
				}
				// ---- consumeOutputsList
				break;

			case COLLECTION:
				// ---- consumeOutputsCollection
				Collection<EmployeeBonusComputation> /**/empColl = computation.employeesCollection()/**/;
				assertEquals( _expectedBonusAmounts.length, empColl.size() );
				int ix = 0;
				for (EmployeeBonusComputation emp : empColl) {
					long expected = _expectedBonusAmounts[ ix++ ];
					long actual = /**/emp.bonusAmount()/**/;
					assertEquals( expected, actual );
				}
				// ---- consumeOutputsCollection
				break;
				
			case ITERATOR:
				// ---- consumeOutputsIterator
				Iterator<EmployeeBonusComputation> /**/empIter = computation.employeesIterator()/**/;
				for (int i = 0; i < _expectedBonusAmounts.length; i++) {
					assertTrue( empIter.hasNext() );
					long expected = _expectedBonusAmounts[ i ];
					long actual = /**/empIter.next().bonusAmount()/**/;
					assertEquals( expected, actual );
				}
				// ---- consumeOutputsIterator
				break;
		}
		
	}


	// ---- OutputFactory
	public static interface BonusComputationFactory
	{
		public BonusComputation newComputation( BonusData _data );
	}
	// ---- OutputFactory


	// ---- Outputs
	@ScaledLong(4)
	public static interface BonusComputation extends Resettable
	{
		/**/EmployeeBonusComputation[]/**/ employees();
		// -- OutputsAlternatives
		/**/List<EmployeeBonusComputation>/**/ employeesList();
		/**/Collection<EmployeeBonusComputation>/**/ employeesCollection();
		/**/Iterator<EmployeeBonusComputation>/**/ employeesIterator();
		// -- OutputsAlternatives
	}

	@ScaledLong(4)
	public static interface /**/EmployeeBonusComputation/**/
	{
		long bonusAmount();
	}
	// ---- Outputs

	
	public static abstract class BonusComputationDefaults implements BonusComputation
	{

		public EmployeeBonusComputation[] employees()
		{
			return null;
		}

		public Collection<EmployeeBonusComputation> employeesCollection()
		{
			return null;
		}

		public Iterator<EmployeeBonusComputation> employeesIterator()
		{
			return null;
		}

		public List<EmployeeBonusComputation> employeesList()
		{
			return null;
		}

	}
	

	// ---- Inputs
	@ScaledLong(4)
	public static interface BonusData
	{
		long bonusTotal();
		long overtimeSalaryPerHour();
		/**/EmployeeBonusData[]/**/ employees();
	}

	@ScaledLong(4)
	public static interface /**/EmployeeBonusData/**/
	{
		long baseSalary();
		int hoursOvertime();
	}
	// ---- Inputs


	static class BonusDataImpl implements BonusData
	{
		private final long bonusTotal;
		private final long overtimeSalaryPerHour;
		private final Collection<EmployeeBonusData> employees = new ArrayList<EmployeeBonusData>();

		public BonusDataImpl(long _bonusTotal, long _overtimeSalaryPerHour)
		{
			super();
			this.bonusTotal = _bonusTotal;
			this.overtimeSalaryPerHour = _overtimeSalaryPerHour;
		}

		public long bonusTotal()
		{
			return this.bonusTotal;
		}

		public long overtimeSalaryPerHour()
		{
			return this.overtimeSalaryPerHour;
		}

		public EmployeeBonusData[] employees()
		{
			return this.employees.toArray( new EmployeeBonusData[ this.employees.size() ] );
		}

		public void addEmployee( EmployeeBonusData _emp )
		{
			this.employees.add( _emp );
		}

	}


	static class EmployeeBonusDataImpl implements EmployeeBonusData
	{
		private final long baseSalary;
		private final int hoursOvertime;

		public EmployeeBonusDataImpl(long _baseSalary, int _hoursOvertime)
		{
			super();
			this.baseSalary = _baseSalary;
			this.hoursOvertime = _hoursOvertime;
		}

		public long baseSalary()
		{
			return this.baseSalary;
		}

		public int hoursOvertime()
		{
			return this.hoursOvertime;
		}

	}

}
