/*
 * Copyright (c) 2006, 2008 by Abacus Research AG, Switzerland.
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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;
import org.formulacompiler.tutorials.BonusPerEmployee.BonusData;
import org.formulacompiler.tutorials.BonusPerEmployee.BonusDataImpl;
import org.formulacompiler.tutorials.BonusPerEmployee.EmployeeBonusData;
import org.formulacompiler.tutorials.BonusPerEmployee.EmployeeBonusDataImpl;

import junit.framework.Test;

@SuppressWarnings( "unchecked" )
public class BonusPerEmployee_FullyLinked extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{

	public void testBonusPerEmployee() throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setNumericType( SpreadsheetCompiler.LONG_SCALE4 );
		builder.loadSpreadsheet( getPath() );
		builder.setFactoryClass( BonusComputationFactory.class );

		// builder.bindAllByName();
		bindElements( builder );

		SaveableEngine engine = builder.compile();

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


	private String getPath()
	{
		return "src/test/data/org/formulacompiler/tutorials/BonusPerEmployee" + getSpreadsheetExtension();
	}


	private void bindElements( EngineBuilder _builder ) throws CompilerException, NoSuchMethodException
	{
		Spreadsheet sheet = _builder.getSpreadsheet();

		Section binder = _builder.getRootBinder();

		binder.defineInputCell( sheet.getCell( "BonusTotal" ), "bonusTotal" );
		binder.defineInputCell( sheet.getCell( "OvertimeSalaryPerHour" ), "overtimeSalaryPerHour" );

		Range range = sheet.getRange( "Employees" );
		Class inputType = EmployeeBonusData.class;
		Class outputType = EmployeeBonusComputation.class;
		Orientation orient = Orientation.VERTICAL;
		Section employees = binder
				.defineRepeatingSection( range, orient, "employees", inputType, "employees", outputType );

		employees.defineInputCell( sheet.getCell( "BaseSalary" ), "baseSalary" );
		employees.defineInputCell( sheet.getCell( "HoursOvertime" ), "hoursOvertime" );
		employees.defineOutputCell( sheet.getCell( "BonusAmount" ), "bonusAmount" );
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
		BonusComputation /**/comp = _factory.newComputation( data )/**/;
		EmployeeBonusComputation[] /**/empOutputs = comp.employees()/**/;
		for (int i = 0; i < _expectedBonusAmounts.length; i++) {
			/**/assertSame( comp, empOutputs[ i ].parent() );/**/
		}
		// ---- consumeOutputs

	}


	public static Test suite()
	{
		return MultiFormatTestFactory.testSuite( BonusPerEmployee_FullyLinked.class );
	}


	public static interface BonusComputationFactory
	{
		public BonusComputation newComputation( BonusData _data );
	}


	// ---- Outputs
	@ScaledLong( 4 )
	public static interface BonusComputation extends Resettable
	{
		EmployeeBonusComputation[] employees();
	}

	@ScaledLong( 4 )
	public static abstract class EmployeeBonusComputation
	{
		private final BonusComputation parent;

		public EmployeeBonusComputation( EmployeeBonusData _inputs, /**/BonusComputation _parent/**/ )
		{
			super();
			this.parent = _parent;
		}

		public/**/ BonusComputation parent()/**/
		{
			return this.parent;
		}

		public abstract long bonusAmount();
	}
	// ---- Outputs

}
