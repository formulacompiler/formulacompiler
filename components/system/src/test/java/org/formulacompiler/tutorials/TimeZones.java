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

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.Computation;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Milliseconds;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

public final class TimeZones extends TestCase
{
	private static final TimeZone EST = TimeZone.getTimeZone( "EST" );
	private static final TimeZone CET = TimeZone.getTimeZone( "CET" );
	private static final TimeZone EET = TimeZone.getTimeZone( "EET" );


	public void testDemarcationDate() throws Exception
	{
		SaveableEngine engine = compileDemarcation( "DateOfBirth", "IsBeforeDate" );
		assertDemarcationDate( engine, TimeZone.getDefault() );
		assertDemarcationDate( engine, EST );
		assertDemarcationDate( engine, CET );
		assertDemarcationDate( engine, EET );
	}

	private void assertDemarcationDate( SaveableEngine _engine, TimeZone _timeZone )
	{
		ComputationFactory factory = _engine.getComputationFactory( new Computation.Config( _timeZone ) );
		assertDemarcationDate( true, 1980, 11, 31, factory, _timeZone );
		assertDemarcationDate( false, 1981, 0, 1, factory, _timeZone );
		assertDemarcationDate( false, 1981, 0, 2, factory, _timeZone );
	}

	private void assertDemarcationDate( boolean _expected, int _y, int _m, int _d, ComputationFactory _factory,
			TimeZone _timeZone )
	{
		// ---- setupDateOfBirth
		Calendar calendar = Calendar.getInstance( _timeZone );
		calendar.clear();
		calendar.set( _y, _m, _d );
		// ---- setupDateOfBirth
		assertDemarcation( _expected, calendar, _factory );
	}


	public void testDemarcationTime() throws Exception
	{
		SaveableEngine engine = compileDemarcation( "time", "TimeOfBirth", "IsBeforeTime" );
		assertDemarcationTime( engine, TimeZone.getDefault() );
		assertDemarcationTime( engine, EST );
		assertDemarcationTime( engine, CET );
		assertDemarcationTime( engine, EET );
	}

	private void assertDemarcationTime( SaveableEngine _engine, TimeZone _timeZone )
	{
		ComputationFactory factory = _engine.getComputationFactory( new Computation.Config( _timeZone ) );
		assertDemarcationTime( true, 11, 59, factory );
		assertDemarcationTime( false, 12, 00, factory );
		assertDemarcationTime( false, 12, 01, factory );
	}

	private void assertDemarcationTime( boolean _expected, int _hour, int _minute, ComputationFactory _factory )
	{
		// ---- setupTime
		long millis = (_hour * 60L + _minute) * 60L * 1000L;
		// ---- setupTime
		assertDemarcation( _expected, millis, _factory, "" + _hour + ":" + _minute );
	}


	public void testDemarcationDateTime() throws Exception
	{
		SaveableEngine engine = compileDemarcation( "DateTimeOfBirth", "IsBeforeDateTime" );
		assertDemarcationDateTime( false, EST, engine );
		assertDemarcationDateTime( false, CET, engine );
		assertDemarcationDateTime( true, EET, engine );
	}

	private void assertDemarcationDateTime( boolean _expected, TimeZone _timeZone, SaveableEngine _engine )
	{
		ComputationFactory factory = _engine.getComputationFactory( new Computation.Config( _timeZone ) );
		Calendar calendar = Calendar.getInstance( _timeZone );
		calendar.clear();
		calendar.set( 1981, 0, 1, 12, 0 );
		assertDemarcation( _expected, calendar, factory );
	}


	private SaveableEngine compileDemarcation( String _inputCell, String _outputCell ) throws Exception
	{
		return compileDemarcation( "date", _inputCell, _outputCell );
	}

	private SaveableEngine compileDemarcation( String _inputMethod, String _inputCell, String _outputCell )
			throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tutorials/TimeZones.xls" );
		builder.setInputClass( DateInput.class );
		builder.setOutputClass( Demarcation.class );

		builder.getRootBinder().defineInputCell( builder.getSpreadsheet().getCell( _inputCell ), _inputMethod );
		builder.getRootBinder().defineOutputCell( builder.getSpreadsheet().getCell( _outputCell ), "isBefore" );

		SaveableEngine engine = builder.compile();
		return engine;
	}

	private void assertDemarcation( boolean _expected, Calendar _calendar, ComputationFactory _factory )
	{
		assertDemarcation( _expected, _calendar.getTime().getTime(), _factory, _calendar.getTimeZone().getDisplayName() );
	}

	private void assertDemarcation( boolean _expected, long _millis, ComputationFactory _factory, String _msg )
	{
		Demarcation computation = (Demarcation) _factory.newComputation( new DateInput( _millis ) );
		assertEquals( _msg, _expected, computation.isBefore() );
	}

	public static class DateInput
	{
		private final long millis;

		public DateInput( long _millis )
		{
			super();
			this.millis = _millis;
		}

		public Date date()
		{
			return new Date( this.millis );
		}

		// ---- timeInput
		@Milliseconds
		public long time()
		{
			return this.millis;
		}
		// ---- timeInput
	}

	public static interface Demarcation
	{
		public boolean isBefore();
	}


	// ---- DLS_Consts
	private static final long JAVA_DIFF_HOURS = 4343L;
	private static final long EXCEL_DIFF_HOURS = 4344L;
	// ---- DLS_Consts

	public void testDLS() throws Exception
	{
		// ---- DLS_Java
		DLSInput input = new DLSInput();
		Date w = input.inWinter();
		Date s = input.inSummer();
		long diffHours = (s.getTime() - w.getTime()) / 1000 / 3600;
		assertEquals( JAVA_DIFF_HOURS, diffHours );
		// ---- DLS_Java
	}

	public void testDLSInSheet() throws Exception
	{
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( "src/test/data/org/formulacompiler/tutorials/TimeZones.xls" );
		builder.setInputClass( DLSInput.class );
		builder.setOutputClass( DLSOutput.class );

		builder.getByNameBinder().inputs().bindAllMethodsToNamedCells();
		builder.getByNameBinder().outputs().bindAllMethodsToNamedCells();

		SaveableEngine engine = builder.compile();
		ComputationFactory factory = engine.getComputationFactory( new Computation.Config( CET ) );
		// ---- DLS_Excel
		DLSInput input = new DLSInput();
		DLSOutput out = (DLSOutput) factory.newComputation( input );
		assertEquals( "input", EXCEL_DIFF_HOURS, out.inputDiff() );
		assertEquals( "CET", EXCEL_DIFF_HOURS, out.constCETDiff() );
		assertEquals( "local", EXCEL_DIFF_HOURS, out.constLocalDiff() );
		// ---- DLS_Excel
	}

	// DO NOT REFORMAT BELOW THIS LINE
	// ---- DLS_Input
	public class DLSInput {

		public Date inWinter() {
			Calendar w = new GregorianCalendar( CET );
			w.clear();
			w.set( 1981, 0, 1, 12, 0 );
			return w.getTime();
		}

		public Date inSummer() {
			Calendar s = new GregorianCalendar( CET );
			s.clear();
			s.set( 1981, 6, 1, 12, 0 );
			return s.getTime();
		}

	}
	// ---- DLS_Input
	// DO NOT REFORMAT ABOVE THIS LINE

	public interface DLSOutput
	{
		long inputDiff();
		long constCETDiff();
		long constLocalDiff();
	}


}
