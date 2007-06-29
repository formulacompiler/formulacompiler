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

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.GregorianCalendar;

import org.formulacompiler.compiler.CallFrame;
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

		builder.getRootBinder().defineInputCell( builder.getSpreadsheet().getCell( _inputCell ),
				new CallFrame( DateInput.class.getMethod( _inputMethod ) ) );
		builder.getRootBinder().defineOutputCell( builder.getSpreadsheet().getCell( _outputCell ),
				new CallFrame( Demarcation.class.getMethod( "isBefore" ) ) );

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

		public DateInput(long _millis)
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


	private static final long JAVA_DIFF = 4343L;
	private static final long EXCEL_DIFF = 4344L;

	public void testDLS() throws Exception
	{
		DLSInput input = new DLSInput();
		Date w = input.inWinter();
		Date s = input.inSummer();
		/*
		 * But Excel and AFC will say 4344 because Excel date values do not handle DLS and time zones!
		 */
		assertEquals( JAVA_DIFF, (s.getTime() - w.getTime()) / 1000 / 3600 );
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
		DLSInput in = new DLSInput();
		DLSOutput out = (DLSOutput) factory.newComputation( in );

		assertEquals( "input", EXCEL_DIFF, out.inputDiff() );
		assertEquals( "CET", EXCEL_DIFF, out.constCETDiff() );
		assertEquals( "local", EXCEL_DIFF, out.constLocalDiff() );
	}

	public class DLSInput
	{

		public Date inWinter()
		{
			Calendar w = new GregorianCalendar( CET );
			w.clear();
			w.set( 1981, 0, 1, 12, 0 );
			return w.getTime();
		}

		public Date inSummer()
		{
			Calendar s = new GregorianCalendar( CET );
			s.clear();
			s.set( 1981, 6, 1, 12, 0 );
			return s.getTime();
		}

	}

	public interface DLSOutput
	{
		long inputDiff();
		long constCETDiff();
		long constLocalDiff();
	}


}
