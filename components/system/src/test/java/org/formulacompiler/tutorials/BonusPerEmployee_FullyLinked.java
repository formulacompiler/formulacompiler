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

import org.formulacompiler.compiler.CompilerException;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.runtime.ScaledLong;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.tutorials.BonusPerEmployee.BonusData;
import org.formulacompiler.tutorials.BonusPerEmployee.BonusDataImpl;
import org.formulacompiler.tutorials.BonusPerEmployee.EmployeeBonusData;
import org.formulacompiler.tutorials.BonusPerEmployee.EmployeeBonusDataImpl;

import junit.framework.TestCase;

@SuppressWarnings( "unchecked" )
public class BonusPerEmployee_FullyLinked extends TestCase
{
	private static final String SHEETPATH = "src/test/data/org/formulacompiler/tutorials/BonusPerEmployee.xls";


	public void testBonusPerEmployee() throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.setNumericType( SpreadsheetCompiler.LONG_SCALE4 );
		builder.loadSpreadsheet( SHEETPATH );
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
