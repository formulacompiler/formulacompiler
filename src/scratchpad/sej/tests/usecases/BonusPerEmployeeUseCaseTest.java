package sej.tests.usecases;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import sej.Band;
import sej.BandElement;
import sej.CompilerNameSpace;
import sej.ModelError;
import sej.Orientation;
import sej.Spreadsheet;
import sej.ValueType;
import sej.runtime.Computation;
import sej.runtime.Engine;
import sej.tests.usecases.AbstractUseCaseTest;

public class BonusPerEmployeeUseCaseTest extends AbstractUseCaseTest
{

	
	public void testComputeBonusPerEmployee() throws IOException, ModelError, SecurityException, NoSuchMethodException, InvocationTargetException
	{
		runUseCase( "BonusPerEmployee", new UseCase()
		{

			public void defineEngine( Spreadsheet _model, CompilerNameSpace _root ) throws ModelError
			{
				// ---- defineRange
				_root.defineInputCell( "OvertimeRate", ValueType.DOUBLE );
				_root.defineInputCell( "BonusTotal", ValueType.DOUBLE );
				CompilerNameSpace employees = _root.defineBand( "Employees", Orientation.VERTICAL );
				employees.defineInputCell( "Name", ValueType.STRING );
				employees.defineInputCell( "BaseSalary", ValueType.DOUBLE );
				employees.defineInputCell( "Overtime", ValueType.DOUBLE );
				employees.defineOutputCell( "BonusAmount", ValueType.DOUBLE );
				// ---- defineRange
			}


			public void useEngine( Engine _engine ) throws InvocationTargetException
			{
				final String[] names = new String[] { "Ann", "Beth", "Charlie" };
				final double[] salaries = new double[] { 5600, 5400, 5500 };
				final double[] overtimes = new double[] { 20, 15, 0 };
				final double[] expectedBonuses = new double[] { 7232.88, 6739.73, 6027.40 };
				assertBonuses( 50, 20000, names, salaries, overtimes, _engine, expectedBonuses );
			}


			private void assertBonuses( double _overtimeRate, double _bonusTotal, String[] _names, double[] _salaries,
					double[] _overtimes, Engine _engine, double[] _expectedBonuses ) throws InvocationTargetException
			{
				// ---- useRange
				Computation c = _engine.newComputation();
				BandElement root = c.getRootElement();
				root.setDouble( "OvertimeRate", _overtimeRate );
				root.setDouble( "BonusTotal", _bonusTotal );
				Band employees = root.getBand( "Employees" );
				for (int i = 0; i < _names.length; i++) {
					BandElement employee = employees.newElement();
					employee.setString( "Name", _names[ i ] );
					employee.setDouble( "BaseSalary", _salaries[ i ] );
					employee.setDouble( "Overtime", _overtimes[ i ] );
				}
				int i = 0;
				for (BandElement employee : employees) {
					double bonusAmount = employee.getDouble( "BonusAmount" );
					assertEquals( _expectedBonuses[ i++ ], bonusAmount, 0.0001 );
				}
				// ---- useRange
			}

		} );
	}


}
