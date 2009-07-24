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
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.formulacompiler.runtime.ComputationMode;
import org.formulacompiler.runtime.Engine;
import org.formulacompiler.runtime.MillisecondsSinceUTC1970;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetBuilder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiNumericTypeTestFactory;

import junit.framework.Test;

public class CompMode extends MultiNumericTypeTestFactory.SpreadsheetNumericTypeTestCase
{
	public static final File PATH = new File( "src/test/data/org/formulacompiler/tutorials" );

	public void testExcelMode() throws Exception
	{
		// ---- excelMode
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		// -omit-
		builder.setNumericType( getNumericType() );
		// -omit-
		builder.loadSpreadsheet( new File( PATH, /**/"DateConversion.xls"/**/ ) );
		builder.setFactoryClass( Factory.class );
		builder.createCellNamesFromRowTitles();
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();
		Output output = factory.newInstance( new Input() );
		// ---- excelMode

		assertExcelDates( output );
	}

	public void testOOoCalcMode() throws Exception
	{
		// ---- calcMode
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		// -omit-
		builder.setNumericType( getNumericType() );
		// -omit-
		builder.loadSpreadsheet( new File( PATH, /**/"DateConversion.ods"/**/ ) );
		builder.setFactoryClass( Factory.class );
		builder.createCellNamesFromRowTitles();
		builder.bindAllByName();
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();
		Output output = factory.newInstance( new Input() );
		// ---- calcMode

		assertOOoCalcDates( output );
	}

	public void testSpreadsheetBuilder() throws Exception
	{
		// ---- forcedCalcMode
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		// -omit-
		builder.setNumericType( getNumericType() );
		// -omit-
		SpreadsheetBuilder b = SpreadsheetCompiler./**/newSpreadsheetBuilder/**/();

		// Input cells
		b.newCell();
		b.nameCell( "Number" );
		SpreadsheetBuilder.CellRef numberCellRef = b.currentCell();
		b.newCell();
		b.nameCell( "Date" );
		SpreadsheetBuilder.CellRef dateCellRef = b.currentCell();
		b.newCell();
		b.nameCell( "DateInMilliseconds" );
		SpreadsheetBuilder.CellRef msCellRef = b.currentCell();

		// Output cells
		b.newCell( b.ref( dateCellRef ) );
		b.nameCell( "NumberFromDate" );
		b.newCell( b.ref( msCellRef ) );
		b.nameCell( "NumberFromMilliseconds" );
		b.newCell( b.ref( numberCellRef ) );
		b.nameCell( "DateFromNumber" );
		b.newCell( b.ref( numberCellRef ) );
		b.nameCell( "MillisecondsFromNumber" );

		builder.setSpreadsheet( b.getSpreadsheet() );
		builder.setFactoryClass( Factory.class );
		builder.createCellNamesFromRowTitles();
		builder.bindAllByName();
		/**/builder.setComputationMode( ComputationMode.OPEN_OFFICE_CALC );/**/
		Engine engine = builder.compile();
		Factory factory = (Factory) engine.getComputationFactory();
		Output output = factory.newInstance( new Input() );
		// ---- forcedCalcMode

		assertOOoCalcDates( output );
	}

	private void assertExcelDates( final Output output )
	{
		// ---- assertExcelDates
		assertEquals( /**/1.0/**/, output.getNumberFromDate() );
		assertEquals( /**/2.0/**/, output.getNumberFromMilliseconds() );
		assertEquals( date( /**/1900, Calendar.JANUARY, 1/**/ ), output.getDateFromNumber() );
		assertEquals( date( /**/1900, Calendar.JANUARY, 1/**/ ).getTime(), output.getMillisecondsFromNumber() );
		// ---- assertExcelDates
	}

	private void assertOOoCalcDates( final Output output )
	{
		// ---- assertOOoCalcDates
		assertEquals( /**/2.0/**/, output.getNumberFromDate() );
		assertEquals( /**/3.0/**/, output.getNumberFromMilliseconds() );
		assertEquals( date( /**/1899, Calendar.DECEMBER, 31/**/ ), output.getDateFromNumber() );
		assertEquals( date( /**/1899, Calendar.DECEMBER, 31/**/ ).getTime(), output.getMillisecondsFromNumber() );
		// ---- assertOOoCalcDates
	}

	private static Date date( final int _year, final int _month, final int _day )
	{
		GregorianCalendar calendar = new GregorianCalendar();
		calendar.clear();
		calendar.set( _year, _month, _day );
		final Date date = calendar.getTime();
		return date;
	}

	public static Test suite()
	{
		return MultiNumericTypeTestFactory.testSuite( CompMode.class );
	}

	public static class Input
	{
		public Double getNumber()
		{
			return 1.0;
		}

		public Date getDate()
		{
			return date( 1900, Calendar.JANUARY, 1 );
		}

		@MillisecondsSinceUTC1970
		public long getDateInMilliseconds()
		{
			return date( 1900, Calendar.JANUARY, 2 ).getTime();
		}
	}

	public static interface Output
	{
		Double getNumberFromDate();

		Double getNumberFromMilliseconds();

		Date getDateFromNumber();

		@MillisecondsSinceUTC1970
		long getMillisecondsFromNumber();
	}

	public static interface Factory
	{
		Output newInstance( Input _input );
	}

}
