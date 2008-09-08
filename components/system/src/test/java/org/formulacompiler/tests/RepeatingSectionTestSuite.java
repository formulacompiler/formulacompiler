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

package org.formulacompiler.tests;

import java.io.File;
import java.util.List;

import org.formulacompiler.compiler.NumericType;
import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.runtime.ComputationFactory;
import org.formulacompiler.runtime.Resettable;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Orientation;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetBinder.Section;
import org.formulacompiler.spreadsheet.internal.CellIndex;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellRange;
import org.formulacompiler.spreadsheet.internal.CellWithExpression;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class RepeatingSectionTestSuite extends AbstractTestSuite
{
	private static final int FIRST_TEST_ROW = 5;
	private static final int NAME_COL = 0;
	private static final int FORMULA_COL = 1;
	private static final int RANGE_NAME_COL = 2;


	static {
		// FIX-ME Debug
		// Settings.LOG_CONSTEVAL.setEnabled( true );
	}

	public static Test suite()
	{
		return new RepeatingSectionTestSuite();
	}


	private SpreadsheetImpl workbook;
	private SheetImpl sheet;
	private List<RowImpl> rows;


	@Override
	@SuppressWarnings( "unqualified-field-access" )
	protected void addTests() throws Exception
	{
		final TestSuite fileSuite = new TestSuite( "SectionTests.xls" );
		this.addTest( fileSuite );

		final String filePath = "src/test/data/org/formulacompiler/tests/" + "SectionTests.xls";
		workbook = (SpreadsheetImpl) SpreadsheetCompiler.loadSpreadsheet( new File( filePath ) );
		sheet = workbook.getSheetList().get( 0 );
		rows = sheet.getRowList();

		for (int iRow = FIRST_TEST_ROW; iRow < rows.size(); iRow++) {
			final RowImpl row = rows.get( iRow );
			final List<CellInstance> cells = row.getCellList();
			if (cells.size() > RANGE_NAME_COL) {
				final CellInstance rangeNameCell = cells.get( RANGE_NAME_COL );
				if (rangeNameCell != null) {
					final Object rangeNameValue = rangeNameCell.getValue();
					if (rangeNameValue instanceof String) {
						final String rangeName = (String) rangeNameValue;
						final CellInstance formulaCell = cells.get( FORMULA_COL );
						final CellInstance nameCell = cells.get( NAME_COL );
						final String testName = "R" + (iRow + 1) + ": " + nameCell.getValue().toString();

						final TestSuite rangeSuite = new RangeTestSuite( testName, formulaCell, rangeName );

						// if (iRow == 74) // FIX-ME Debug
						fileSuite.addTest( rangeSuite );

					}
				}
			}
		}
	}


	private final class RangeTestSuite extends TestSuite
	{
		private final CellInstance formulaCell;
		private final CellRange sectionRange;
		private final CellInstance[][] sectionCells;
		private final CellInstance[][] expectedResultsCells;
		private final Orientation orientation;
		private final CellInstance[][] outerRefCells;


		@SuppressWarnings( "unqualified-field-access" )
		public RangeTestSuite( String _name, CellInstance _formulaCell, String _rangeName )
		{
			super( _name );

			final CellRange expectedResultsRange = namedRange( _rangeName + "_E" );
			final CellRange outerRefsRange = namedRange( _rangeName + "_R" );

			formulaCell = _formulaCell;
			sectionRange = (CellRange) workbook.getRange( _rangeName );
			orientation = (_rangeName.charAt( 0 ) == 'H') ? Orientation.HORIZONTAL : Orientation.VERTICAL;
			sectionCells = extractRangeCells( sectionRange, orientation );
			expectedResultsCells = extractRangeCells( expectedResultsRange, orientation );
			outerRefCells = extractRangeCells( outerRefsRange, orientation );

			addTest( new SectionTestCase( SpreadsheetCompiler.DOUBLE ) );
			addTest( new SectionTestCase( SpreadsheetCompiler.BIGDECIMAL_SCALE8 ) );
			addTest( new SectionTestCase( SpreadsheetCompiler.LONG_SCALE4 ) );
		}


		private final class SectionTestCase extends TestCase
		{
			private final NumericType numericType;


			public SectionTestCase( NumericType _numericType )
			{
				super( RangeTestSuite.this.getName() + " @ " + _numericType );
				this.numericType = _numericType;
			}


			@SuppressWarnings( "unqualified-field-access" )
			@Override
			protected void runTest() throws Throwable
			{
				final EngineBuilder eb = SpreadsheetCompiler.newEngineBuilder();
				eb.setSpreadsheet( workbook );
				eb.setInputClass( Input.class );
				eb.setOutputClass( Output.class );
				eb.setNumericType( numericType );
				final Section rb = eb.getRootBinder();
				rb.defineOutputCell( formulaCell.getCellIndex(), Output.class.getMethod( "result" ) );
				Section sb = rb.defineRepeatingSection( sectionRange, orientation, Input.class.getMethod( "subs" ),
						Input.class, null, null );

				final CellInstance[] templateCells = sectionCells[ 0 ];
				int iCell = 0;
				for (CellInstance c : templateCells) {
					if (!(c instanceof CellWithExpression)) {
						sb.defineInputCell( c.getCellIndex(), Input.class.getMethod( "value", Integer.TYPE ), iCell );
					}
					iCell++;
				}

				SaveableEngine eng = eb.compile();
				// Debug.saveEngine( eng, "/temp/sect.jar" ); // FIX-ME Debug

				ComputationFactory fact = eng.getComputationFactory();

				for (int iRes = 0; iRes < sectionCells.length; iRes++) {
					final Input in = new Input( iRes + 1, sectionCells, outerRefCells );
					final Output out = (Output) fact.newComputation( in );
					final double actual = out.result();
					final double expected = (Double) expectedResultsCells[ iRes ][ 0 ].getValue();
					assertEquals( getName() + "@" + iRes, expected, actual, 0.00001 );
				}
			}

		}

	}


	public static interface Output extends Resettable
	{
		double result();
	}


	public static class Input
	{
		private final double[] values;
		private final Input[] subs;

		public Input( int _len, CellInstance[][] _sectionCells, CellInstance[][] _outerRefCells )
		{
			super();

			this.values = new double[ _outerRefCells.length ];
			for (int i = 0; i < _outerRefCells.length; i++) {
				final CellInstance cell = _outerRefCells[ i ][ 0 ];
				if (cell.getValue() != null) {
					this.values[ i ] = (Double) cell.getValue();
				}
			}

			this.subs = new Input[ _len ];
			for (int i = 0; i < _len; i++) {
				this.subs[ i ] = new Input( _sectionCells[ i ] );
			}
		}

		public Input( CellInstance[] _cells )
		{
			super();
			this.values = new double[ _cells.length ];
			this.subs = null;
			for (int i = 0; i < _cells.length; i++) {
				if (_cells[ i ].getValue() != null) {
					this.values[ i ] = (Double) _cells[ i ].getValue();
				}
			}
		}

		public double value( int _i )
		{
			return this.values[ _i ];
		}

		public Input[] subs()
		{
			return this.subs;
		}

	}


	private CellRange namedRange( String _name )
	{
		return (CellRange) this.workbook.getRangeNames().get( _name );
	}

	private CellInstance[][] extractRangeCells( CellRange _range, Orientation _orientation )
	{
		if (_range == null) return new CellInstance[ 0 ][];

		final CellIndex f = _range.getFrom();
		final CellIndex t = _range.getTo();
		final Orientation other = (_orientation == Orientation.HORIZONTAL) ? Orientation.VERTICAL
				: Orientation.HORIZONTAL;
		final int felt = f.getIndex( _orientation );
		final int telt = t.getIndex( _orientation );
		final int nelt = telt - felt + 1;
		final int fcell = f.getIndex( other );
		final int tcell = t.getIndex( other );
		final int ncell = tcell - fcell + 1;

		final CellInstance[][] r = new CellInstance[ nelt ][];

		for (int ielt = 0; ielt < nelt; ielt++) {
			final CellInstance[] cells = new CellInstance[ ncell ];
			for (int icell = 0; icell < ncell; icell++) {
				switch (_orientation) {
					case HORIZONTAL:
						cells[ icell ] = new CellIndex( this.workbook, 0, felt + ielt, fcell + icell ).getCell();
						break;
					case VERTICAL:
						cells[ icell ] = new CellIndex( this.workbook, 0, fcell + icell, felt + ielt ).getCell();
						break;
				}
			}
			r[ ielt ] = cells;
		}

		return r;
	}

}
