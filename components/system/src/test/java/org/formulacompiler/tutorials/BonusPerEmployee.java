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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormat;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;


@RunWith( MultiFormat.class )
public class BonusPerEmployee
{

	private static enum AccessorVersion {
		ARRAY, LIST, COLLECTION, ITERATOR
	}


	private final String spreadsheetExtension;

	public BonusPerEmployee( final String _spreadsheetExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
	}

	private String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	@Test
	public void testWithArray() throws Exception
	{
		doTest( AccessorVersion.ARRAY );
	}

	@Test
	public void testWithList() throws Exception
	{
		doTest( AccessorVersion.LIST );
	}

	@Test
	public void testWithCollection() throws Exception
	{
		doTest( AccessorVersion.COLLECTION );
	}

	@Test
	public void testWithIterator() throws Exception
	{
		doTest( AccessorVersion.ITERATOR );
	}


	public void doTest( AccessorVersion _version ) throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setNumericType( SpreadsheetCompiler.LONG_SCALE4 );
		builder.loadSpreadsheet( getFile() );
		builder.setFactoryClass( BonusComputationFactory.class );
		builder.setOutputClass( BonusComputationDefaults.class );

		// builder.bindAllByName();
		bindElements( builder, _version );

		SaveableEngine engine = builder.compile();
		if (_version == AccessorVersion.ARRAY) {
			FormulaDecompiler.decompile( engine ).saveTo( new File( "temp/test/decompiled/bonusPerEmployee" ) );
		}

		BonusComputationFactory factory = (BonusComputationFactory) engine.getComputationFactory();

		{
			long bonusTotal = 200000000L;
			long overtimeSalaryPerHour = 500000L;
			long[] salaries = { 56000000L, 54000000L, 55000000L };
			int[] hoursOvertime = { 20, 15, 0 };
			// Expected results; not the same as in the sample because LONG4 is less precise than the
			// DOUBLE in Excel
			long[] bonuses = { 72320000L, 67380000L, 60260000L };
			assertBonuses( factory, bonusTotal, overtimeSalaryPerHour, salaries, hoursOvertime, bonuses, _version );
		}
	}

	private File getFile()
	{
		return new File( "src/test/data/org/formulacompiler/tutorials/BonusPerEmployee" + getSpreadsheetExtension() );
	}


	private void bindElements( EngineBuilder _builder, AccessorVersion _version ) throws CompilerException,
			NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();

		// LATER Create simplified section binding on EngineBuilder

		// ---- bindSections
		Section binder = _builder.getRootBinder();
		// ---- bindSections

		// ---- bindGlobals
		Cell bonusTotalCell = sheet.getCell( "BonusTotal" );
		/**/binder/**/.defineInputCell( bonusTotalCell, "bonusTotal" );

		Cell overtimeRateCell = sheet.getCell( "OvertimeSalaryPerHour" );
		/**/binder/**/.defineInputCell( overtimeRateCell, "overtimeSalaryPerHour" );
		// ---- bindGlobals

		// ---- bindSections
		Range range = sheet.getRange( "Employees" );

		// input
		Method inputMethod = /**/BonusData/**/.class.getMethod( /**/"employees"/**/ );
		Class inputType = /**/EmployeeBonusData/**/.class;

		// output
		Method outputMethod = /**/BonusComputation/**/.class.getMethod( /**/"employees"/**/ );
		Class outputType = /**/EmployeeBonusComputation/**/.class;
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

		Orientation orient = Orientation.VERTICAL;

		Section /**/employees/**/ = binder./**/defineRepeatingSection/**/( range, orient, inputMethod, inputType,
				outputMethod, outputType );
		// ---- bindSections

		// ---- bindEmployeeInputs
		Cell salaryCell = sheet.getCell( "BaseSalary" );
		Method salaryMethod = /**/inputType/**/.getMethod( "baseSalary" );
		/**/employees/**/.defineInputCell( salaryCell, salaryMethod );

		Cell overtimeCell = sheet.getCell( "HoursOvertime" );
		/**/employees/**/.defineInputCell( overtimeCell, "hoursOvertime" ); // shorter form
		// ---- bindEmployeeInputs

		// ---- bindEmployeeOutputs
		Cell bonusCell = sheet.getCell( "BonusAmount" );
		Method bonusMethod = /**/outputType/**/.getMethod( "bonusAmount" );
		/**/employees/**/./**/defineOutputCell/**/( bonusCell, bonusMethod );
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
	@ScaledLong( 4 )
	public static interface BonusComputation extends Resettable
	{
		/**/EmployeeBonusComputation[]/**/ employees();
		// -- OutputsAlternatives
		/**/List<EmployeeBonusComputation>/**/ employeesList();
		/**/Collection<EmployeeBonusComputation>/**/ employeesCollection();
		/**/Iterator<EmployeeBonusComputation>/**/ employeesIterator();
		// -- OutputsAlternatives
	}

	@ScaledLong( 4 )
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
	@ScaledLong( 4 )
	public static interface BonusData
	{
		long bonusTotal();
		long overtimeSalaryPerHour();
		/**/EmployeeBonusData[]/**/ employees();
	}

	@ScaledLong( 4 )
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
		private final Collection<EmployeeBonusData> employees = New.collection();

		public BonusDataImpl( long _bonusTotal, long _overtimeSalaryPerHour )
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

		public EmployeeBonusDataImpl( long _baseSalary, int _hoursOvertime )
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
