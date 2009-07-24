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

package org.formulacompiler.spreadsheet.internal;

import static org.formulacompiler.compiler.internal.expressions.ExpressionBuilder.*;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import org.formulacompiler.compiler.Function;
import org.formulacompiler.runtime.New;
import org.formulacompiler.spreadsheet.Spreadsheet.Cell;
import org.formulacompiler.spreadsheet.Spreadsheet.Range;
import org.formulacompiler.tests.utils.AbstractStandardInputsOutputsTestCase;
import org.formulacompiler.tests.utils.WorksheetBuilderWithBands;


public class SpreadsheetTest extends AbstractStandardInputsOutputsTestCase
{

	public void testGetCellA1() throws Exception
	{
		SpreadsheetImpl wb = new SpreadsheetImpl();
		{
			SheetImpl sheet = new SheetImpl( wb );
			RowImpl r1 = new RowImpl( sheet );
			new CellWithExpression( r1, null );
			new WorksheetBuilderWithBands( sheet );
		}
		assertCell( wb, 0, 0, 0, wb.getCellA1( "A1" ) );
		assertCell( wb, 0, 0, 1, wb.getCellA1( "A2" ) );
		assertCell( wb, 0, 1, 0, wb.getCellA1( "B1" ) );
		assertCell( wb, 0, 1, 1, wb.getCellA1( "B2" ) );
		assertCell( wb, 0, 25, 499, wb.getCellA1( "Z500" ) );
		wb.defineModelRangeName( "A1", (CellRange) wb.getCell( 0, 10, 20 ) );
		assertCell( wb, 0, 0, 0, wb.getCellA1( "A1" ) );
	}

	private void assertCell( SpreadsheetImpl _wb, int _sheet, int _col, int _row, Cell _have )
	{
		Cell want = _wb.getCell( _sheet, _col, _row );
		assertEquals( want.describe(), _have.describe() );
	}


	public void testDescribe() throws Exception
	{
		SpreadsheetImpl w = new SpreadsheetImpl();

		final CellInstance onFirstSheet;
		final CellInstance named;
		{
			SheetImpl s = new SheetImpl( w );

			{
				RowImpl r = new RowImpl( s );
				{
					CellWithExpression c = new CellWithExpression( r, null );
					c.setValue( 13.5 );
					c.setMaxFractionalDigits( 2 );
					w.defineModelRangeName( "Result", c.getCellIndex() );
					named = c;
				}
			}

			new WorksheetBuilderWithBands( s );

			new RowImpl( s );

			{
				RowImpl r = new RowImpl( s );
				CellInstance c1 = new CellWithExpression( r, null );
				CellInstance c2 = new CellWithConstant( r, null );
				w.defineModelRangeName( "Range", CellRange.getCellRange( c1.getCellIndex(), c2.getCellIndex() ) );

				Calendar cal = Calendar.getInstance( TimeZone.getTimeZone( "GMT+1" ) );
				cal.clear();
				cal.set( 1970, 3, 19, 12, 13 );
				cal.set( cal.SECOND, 14 );
				onFirstSheet = new CellWithConstant( r, cal.getTime() );
			}

		}
		{
			SheetImpl s = new SheetImpl( w );
			{
				RowImpl r = new RowImpl( s );
				{
					CellInstance local = new CellWithConstant( r, 2.0 );
					new CellWithExpression( r, fun( Function.SUM, new ExpressionNodeForCell( named ),
							new ExpressionNodeForCell( onFirstSheet ), new ExpressionNodeForCell( local ) ) );
					new CellWithExpression( r, new ExpressionNodeForCell( onFirstSheet ) );
				}
			}
		}

		String description = w.describe();

		assertEqualToFile( "src/test/data/org/formulacompiler/spreadsheet/internal/test-sheet-description.yaml",
				description );
	}

	public void testModelAndUserNames()
	{
		final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
		spreadsheet.getSheetList().add( new SheetImpl( spreadsheet ) );

		final NamesChecker checker = new NamesChecker();

		checker.assertNames( spreadsheet );

		{
			final String name = "modelRange1";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 0, 0 );
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "userRange1";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 1, 0 );
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "modelRange2";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 0, 1 );
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "userRange2";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 1, 1 );
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}
	}


	public void testUserAndModelNames()
	{
		final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
		spreadsheet.getSheetList().add( new SheetImpl( spreadsheet ) );

		final NamesChecker checker = new NamesChecker();

		checker.assertNames( spreadsheet );

		{
			final String name = "userRange1";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 1, 0 );
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "modelRange1";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 0, 0 );
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "userRange2";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 1, 1 );
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "modelRange2";
			final CellIndex cell = new CellIndex( spreadsheet, 0, 0, 1 );
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}
	}

	public void testDifferentModelAndUserNamesForSameCell()
	{
		final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
		spreadsheet.getSheetList().add( new SheetImpl( spreadsheet ) );

		final NamesChecker checker = new NamesChecker();

		checker.assertNames( spreadsheet );

		final CellIndex cell = new CellIndex( spreadsheet, 0, 0, 0 );

		{
			final String name = "modelRange1";
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "userRange1";
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "modelRange2";
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "userRange2";
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}
	}

	public void testDifferentUserAndModelNamesForSameCell()
	{
		final SpreadsheetImpl spreadsheet = new SpreadsheetImpl();
		spreadsheet.getSheetList().add( new SheetImpl( spreadsheet ) );

		final NamesChecker checker = new NamesChecker();

		checker.assertNames( spreadsheet );

		final CellIndex cell = new CellIndex( spreadsheet, 0, 0, 0 );

		{
			final String name = "userRange1";
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "modelRange1";
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "userRange2";
			spreadsheet.defineAdditionalRangeName( name, cell );
			checker.addUserDefinedName( name, cell );
			checker.assertNames( spreadsheet );
		}

		{
			final String name = "modelRange2";
			spreadsheet.defineModelRangeName( name, cell );
			checker.addModelName( name, cell );
			checker.assertNames( spreadsheet );
		}
	}

	private class NamesChecker
	{
		final Map<String, Range> expectedModelRanges = New.map();
		final Map<String, Range> expectedRanges = New.map();
		final Map<Range, Set<String>> expectedModelNames = New.map();
		final Map<Range, Set<String>> expectedNames = New.map();
		final Set<Range> knownRanges = New.set();


		public void assertNames( SpreadsheetImpl _spreadsheet )
		{
			assertEquals( this.expectedModelRanges, _spreadsheet.getModelRangeNames() );
			assertEquals( this.expectedRanges, _spreadsheet.getRangeNames() );
			for (Range range : this.knownRanges) {
				assertEquals( this.expectedModelNames.get( range ), _spreadsheet.getModelNamesFor( range ) );
				assertEquals( this.expectedNames.get( range ), _spreadsheet.getNamesFor( range ) );
			}
		}

		public void addModelName( final String _name, final Range _range )
		{
			this.knownRanges.add( _range );
			assertFalse( this.expectedModelRanges.containsKey( _name ) );
			this.expectedModelRanges.put( _name, _range );
			putRangeName( this.expectedModelNames, _range, _name );
			assertFalse( this.expectedRanges.containsKey( _name ) );
			this.expectedRanges.put( _name, _range );
			putRangeName( this.expectedNames, _range, _name );
		}

		public void addUserDefinedName( final String _name, final Range _range )
		{
			this.knownRanges.add( _range );
			assertFalse( this.expectedRanges.containsKey( _name ) );
			this.expectedRanges.put( _name, _range );
			putRangeName( this.expectedNames, _range, _name );
		}

		private void putRangeName( Map<Range, Set<String>> _namedRanges, Range _ref, String _name )
		{
			final Set<String> existingCellNames = _namedRanges.get( _ref );
			final Set<String> cellNames;
			if (existingCellNames != null) {
				cellNames = existingCellNames;
			}
			else {
				cellNames = New.sortedSet();
				_namedRanges.put( _ref, cellNames );
			}
			assertFalse( cellNames.contains( _name ) );
			cellNames.add( _name );
		}
	}

}
