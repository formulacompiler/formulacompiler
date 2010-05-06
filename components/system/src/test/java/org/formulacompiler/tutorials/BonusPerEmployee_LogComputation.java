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

import static org.formulacompiler.tutorials.BonusPerEmployee.*;

import java.io.File;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.decompiler.FormulaDecompiler;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.New;
import org.formulacompiler.runtime.event.CellComputationEvent;
import org.formulacompiler.runtime.event.CellComputationListener;
import org.formulacompiler.runtime.spreadsheet.SpreadsheetCellComputationEvent;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.MultiFormatTestFactory;

import junit.framework.Test;

public class BonusPerEmployee_LogComputation extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testBonusPerEmployee() throws Exception
	{
		// ---- compileEngine
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setNumericType( SpreadsheetCompiler.LONG_SCALE4 );
		builder.loadSpreadsheet( getFile() );
		builder.setFactoryClass( BonusComputationFactory.class );
		builder.setOutputClass( BonusComputationDefaults.class );
		builder./**/setComputationListenerEnabled( true )/**/;
		builder./**/setFullCaching( true )/**/;

		bindElements( builder );
		SaveableEngine engine = builder.compile();
		// ---- compileEngine

		FormulaDecompiler.decompile( engine ).saveTo( new File( "temp/test/decompiled/bonusPerEmployee_withLogging" ) );

		// ---- setListener
		TestComputationListener listener = new TestComputationListener();
		Computation.Config config = /**/new Computation.Config()/**/;
		config./**/cellComputationListener/**/ = listener;
		BonusComputationFactory factory = (BonusComputationFactory) engine.getComputationFactory( /**/config/**/ );
		// ---- setListener

		{
			long bonusTotal = 200000000L;
			long overtimeSalaryPerHour = 500000L;
			long[] salaries = { 56000000L, 54000000L, 55000000L };
			int[] hoursOvertime = { 20, 15, 0 };
			// Expected results; not the same as in the sample because LONG4 is less precise than the
			// DOUBLE in Excel
			long[] bonuses = { 72320000L, 67380000L, 60260000L };

			BonusDataImpl data = new BonusDataImpl( bonusTotal, overtimeSalaryPerHour );
			for (int i = 0; i < salaries.length; i++) {
				EmployeeBonusData emp = new EmployeeBonusDataImpl( salaries[ i ], hoursOvertime[ i ] );
				data.addEmployee( emp );
			}
			BonusComputation computation = factory.newComputation( data );

			EmployeeBonusComputation[] emps = computation.employees();
			for (int i = 0; i < bonuses.length; i++) {
				long expected = bonuses[ i ];
				long actual = /**/emps[ i ].bonusAmount()/**/;
				assertEquals( expected, actual );
			}
		}

		// ---- events
		final List<SpreadsheetCellComputationEvent> computationEvents = listener.events;
		assertEquals( 18, computationEvents.size() );
		assertEvents( new String[]{
				"-> 20000.0000 in Sheet1!B7(BonusTotal) in section: ROOT",
				"-> 5600.0000 in Sheet1!B2(BaseSalary) in section: Sheet1!A2:Sheet1!F4(Employees)[0]",
				"-> 20.0000 in Sheet1!C2(HoursOvertime) in section: Sheet1!A2:Sheet1!F4(Employees)[0]",
				"-> 50.0000 in Sheet1!B6(OvertimeSalaryPerHour) in section: ROOT",
				"6600.0000 in Sheet1!D2 in section: Sheet1!A2:Sheet1!F4(Employees)[0]",
				"-> 5400.0000 in Sheet1!B2(BaseSalary) in section: Sheet1!A2:Sheet1!F4(Employees)[1]",
				"-> 15.0000 in Sheet1!C2(HoursOvertime) in section: Sheet1!A2:Sheet1!F4(Employees)[1]",
				"6150.0000 in Sheet1!D2 in section: Sheet1!A2:Sheet1!F4(Employees)[1]",
				"-> 5500.0000 in Sheet1!B2(BaseSalary) in section: Sheet1!A2:Sheet1!F4(Employees)[2]",
				"-> 0 in Sheet1!C2(HoursOvertime) in section: Sheet1!A2:Sheet1!F4(Employees)[2]",
				"5500.0000 in Sheet1!D2 in section: Sheet1!A2:Sheet1!F4(Employees)[2]",
				"18250.0000 in Sheet1!D5 in section: ROOT",
				"0.3616 in Sheet1!E2 in section: Sheet1!A2:Sheet1!F4(Employees)[0]",
				"<- 7232.0000 in Sheet1!F2(BonusAmount) in section: Sheet1!A2:Sheet1!F4(Employees)[0]",
				"0.3369 in Sheet1!E2 in section: Sheet1!A2:Sheet1!F4(Employees)[1]",
				"<- 6738.0000 in Sheet1!F2(BonusAmount) in section: Sheet1!A2:Sheet1!F4(Employees)[1]",
				"0.3013 in Sheet1!E2 in section: Sheet1!A2:Sheet1!F4(Employees)[2]",
				"<- 6026.0000 in Sheet1!F2(BonusAmount) in section: Sheet1!A2:Sheet1!F4(Employees)[2]"
		}, computationEvents );
		// ---- events
	}

	private File getFile()
	{
		return new File( "src/test/data/org/formulacompiler/tutorials/BonusPerEmployee" + getSpreadsheetExtension() );
	}


	private void bindElements( EngineBuilder _builder ) throws CompilerException,
			NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();

		Section binder = _builder.getRootBinder();
		Cell bonusTotalCell = sheet.getCell( "BonusTotal" );
		binder.defineInputCell( bonusTotalCell, "bonusTotal" );

		Cell overtimeRateCell = sheet.getCell( "OvertimeSalaryPerHour" );
		binder.defineInputCell( overtimeRateCell, "overtimeSalaryPerHour" );
		Range range = sheet.getRange( "Employees" );
		Method inputMethod = BonusData.class.getMethod( "employees" );
		Class inputType = EmployeeBonusData.class;
		Method outputMethod = BonusComputation.class.getMethod( "employees" );
		Class outputType = EmployeeBonusComputation.class;

		Orientation orient = Orientation.VERTICAL;

		Section employees = binder.defineRepeatingSection( range, orient, inputMethod, inputType,
				outputMethod, outputType );
		Cell salaryCell = sheet.getCell( "BaseSalary" );
		Method salaryMethod = inputType.getMethod( "baseSalary" );
		employees.defineInputCell( salaryCell, salaryMethod );

		Cell overtimeCell = sheet.getCell( "HoursOvertime" );
		employees.defineInputCell( overtimeCell, "hoursOvertime" );
		Cell bonusCell = sheet.getCell( "BonusAmount" );
		Method bonusMethod = outputType.getMethod( "bonusAmount" );
		employees.defineOutputCell( bonusCell, bonusMethod );
	}


	private static void assertEvent( int eventNo, String _expected, SpreadsheetCellComputationEvent _event )
	{
		final String actual = String.format( Locale.ENGLISH, "%s%s in %s in section: %s",
				_event.isInput() ? "-> " : _event.isOutput() ? "<- " : "",
				_event.getValue(), _event.getCellInfo(), _event.getSectionInfo() );
		assertEquals( "Event #" + eventNo, _expected, actual );
	}

	private static void assertEvents( String[] _expected, Iterable<SpreadsheetCellComputationEvent> _events )
	{
		final Iterator<SpreadsheetCellComputationEvent> iterator = _events.iterator();
		for (int i = 0; i < _expected.length; i++) {
			String exp = _expected[ i ];
			assertEvent( i, exp, iterator.next() );
		}
		if (iterator.hasNext()) fail( "Too many events." );
	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( BonusPerEmployee_LogComputation.class );
	}

	private class TestComputationListener implements CellComputationListener
	{
		final List<SpreadsheetCellComputationEvent> events = New.list();

		public void cellCalculated( CellComputationEvent _event )
		{
			this.events.add( (SpreadsheetCellComputationEvent) _event );
		}
	}

}
