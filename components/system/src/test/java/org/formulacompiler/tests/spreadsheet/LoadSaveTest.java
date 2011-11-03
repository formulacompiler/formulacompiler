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

package org.formulacompiler.tests.spreadsheet;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.formulacompiler.compiler.internal.Duration;
import org.formulacompiler.compiler.internal.LocalDate;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.SpreadsheetNameCreator;
import org.formulacompiler.spreadsheet.internal.BaseRow;
import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.tests.utils.IgnoreFormat;
import org.formulacompiler.tests.utils.MultiFormat;
import org.formulacompiler.tests.utils.SpreadsheetVerificationRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith( MultiFormat.class )
public class LoadSaveTest
{
	private static final File TEST_FILES_DIR = new File( "src/test/data/org/formulacompiler/tests/spreadsheet/LoadSaveTest" );

	@Rule
	public final SpreadsheetVerificationRule verifier;
	private final String spreadsheetExtension;
	private Spreadsheet spreadsheet;

	public LoadSaveTest( final String _spreadsheetExtension, final String _templateExtension )
	{
		this.spreadsheetExtension = _spreadsheetExtension;
		this.verifier = new SpreadsheetVerificationRule( _spreadsheetExtension, _templateExtension, TEST_FILES_DIR );
	}

	public String getSpreadsheetExtension()
	{
		return this.spreadsheetExtension;
	}

	@Before
	public void setUp() throws Exception
	{
		final File dataFile = new File( TEST_FILES_DIR, this.verifier.getFileName() + getSpreadsheetExtension() );
		final SpreadsheetLoader.Config config = new SpreadsheetLoader.Config();
		config.loadAllCellValues = true;
		this.spreadsheet = SpreadsheetCompiler.loadSpreadsheet( dataFile, config );
		this.verifier.setSpreadsheet( this.spreadsheet );
	}

	@Test
	public void testEmptySheet()
	{
		final Spreadsheet.Sheet[] sheets = this.spreadsheet.getSheets();
		assertEquals( 2, sheets.length );
		final Spreadsheet.Sheet sheet1 = sheets[ 0 ];
		assertEquals( "Sheet1", sheet1.getName() );
		assertEquals( 0, sheet1.getRows().length );
		final Spreadsheet.Sheet sheet2 = sheets[ 1 ];
		assertEquals( "Sheet2", sheet2.getName() );
		assertEquals( 1, sheet2.getRows().length );
	}

	@Test
	public void testEmptyRows()
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row[] rows = sheet.getRows();
		assertEquals( 5, rows.length );
		assertEquals( 0, rows[ 0 ].getCells().length );
		assertEquals( 1, rows[ 1 ].getCells().length );
		assertEquals( 0, rows[ 2 ].getCells().length );
		assertEquals( 0, rows[ 3 ].getCells().length );
		assertEquals( 1, rows[ 4 ].getCells().length );
	}

	@Test
	public void testEmptyCells()
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row row = sheet.getRows()[ 0 ];
		final List<? extends CellInstance> cells = ((BaseRow) row).getCellList();
		assertEquals( 5, cells.size() );
		assertNull( cells.get( 0 ) );
		assertNotNull( cells.get( 1 ) );
		assertNull( cells.get( 2 ) );
		assertNull( cells.get( 3 ) );
		assertNotNull( cells.get( 4 ) );
	}

	@Test
	public void testDataTypes()
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row[] rows = sheet.getRows();
		assertEquals( 7, rows.length );
		assertEquals( 1.25, rows[ 0 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( 0.1234, rows[ 1 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( 99.95, rows[ 2 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( "text", rows[ 3 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( Boolean.TRUE, rows[ 4 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( Boolean.FALSE, rows[ 4 ].getCells()[ 2 ].getConstantValue() );
		assertEquals( new LocalDate( 37126 ), rows[ 5 ].getCells()[ 1 ].getConstantValue() );
		assertEquals( new LocalDate( 39507 + ((12.0 + (46.0 + 47.555 / 60.0) / 60.0) / 24.0) ), rows[ 5 ].getCells()[ 2 ].getConstantValue() );
		assertEquals( new Duration( (23.0 + (12.0 + 23.442 / 60.0) / 60.0) / 24.0 ), rows[ 6 ].getCells()[ 1 ].getConstantValue() );
	}

	@Test
	public void testDataTypesInFormulas()
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row[] rows = sheet.getRows();
		assertEquals( 7, rows.length );
		assertEquals( 1.25, rows[ 0 ].getCells()[ 1 ].getValue() );
		assertEquals( 0.1234, rows[ 1 ].getCells()[ 1 ].getValue() );
		assertEquals( 99.95, rows[ 2 ].getCells()[ 1 ].getValue() );
		assertEquals( "text", rows[ 3 ].getCells()[ 1 ].getValue() );
		assertEquals( Boolean.TRUE, rows[ 4 ].getCells()[ 1 ].getValue() );
		assertEquals( Boolean.FALSE, rows[ 4 ].getCells()[ 2 ].getValue() );
		assertEquals( new LocalDate( 37126 ), rows[ 5 ].getCells()[ 1 ].getValue() );
		assertEquals( new Duration( (23.0 + (12.0 + 23.0 / 60.0) / 60.0) / 24.0 ), rows[ 6 ].getCells()[ 1 ].getValue() );
	}

	@Test
	public void testExpressions() throws Exception
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row[] rows = sheet.getRows();
		assertEquals( "1.21", rows[ 0 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "1.234E21", rows[ 0 ].getCells()[ 2 ].getExpressionText() );
		assertEquals( "\"text\"", rows[ 1 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "\"\u0442\u0435\u043a\u0441\u0442\"", rows[ 1 ].getCells()[ 2 ].getExpressionText() );
		assertEquals( "\"< '&\"#![]>\"", rows[ 1 ].getCells()[ 3 ].getExpressionText() );
		assertEquals( "SUM( 1.0, 2.0, 3.0 )", rows[ 2 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "PRODUCT( $B$1, C1 )", rows[ 3 ].getCells()[ 1 ].getExpressionText() );
	}

	@Test
	public void testOperators() throws Exception
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row[] rows = sheet.getRows();
		assertEquals( 17, rows.length );
		assertEquals( "((B1 + C1) + 10.0)", rows[ 2 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(((-B1) - C1) - 10.0)", rows[ 3 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "((-B1) - (C1 - 10.0))", rows[ 3 ].getCells()[ 2 ].getExpressionText() );
		assertEquals( "((B1 * C1) * 10.0)", rows[ 4 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "((B1 / C1) / 10.0)", rows[ 5 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 / (C1 / 10.0))", rows[ 5 ].getCells()[ 2 ].getExpressionText() );
		assertEquals( "(B1%)", rows[ 6 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "((C1%)%)", rows[ 6 ].getCells()[ 2 ].getExpressionText() );
		assertEquals( "((B1 ^ C1) ^ 4.0)", rows[ 7 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 ^ (C1 ^ 2.0))", rows[ 7 ].getCells()[ 2 ].getExpressionText() );
		assertEquals( "(B1 = C1)", rows[ 8 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 > C1)", rows[ 9 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 < C1)", rows[ 10 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 >= C1)", rows[ 11 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 <= C1)", rows[ 12 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 <> C1)", rows[ 13 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "(B1 & C1 & \"text\")", rows[ 14 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "((B1 + (10.0 * C1)) / (B1 - (C1%)))", rows[ 16 ].getCells()[ 1 ].getExpressionText() );
	}

	@Test
	@IgnoreFormat( ".xls" )
	public void testRangeNames()
	{
		final Map<String, Spreadsheet.Range> definedNames = this.spreadsheet.getRangeNames();
		assertEquals( 6, definedNames.size() );
		final Spreadsheet.Range rangeB2 = definedNames.get( "Cell_B2" );
		assertEquals( "Sheet1!B2", rangeB2.getTopLeft().toString() );
		assertEquals( "Sheet1!B2", rangeB2.getBottomRight().toString() );
		final Spreadsheet.Range range_B_2 = definedNames.get( "Cell_B2_abs" );
		assertEquals( "Sheet1!$B$2", range_B_2.getTopLeft().toString() );
		assertEquals( "Sheet1!$B$2", range_B_2.getBottomRight().toString() );
		final Spreadsheet.Range rangeB2C4 = definedNames.get( "Range_B2C4" );
		assertEquals( "Sheet1!B2", rangeB2C4.getTopLeft().toString() );
		assertEquals( "Sheet1!C4", rangeB2C4.getBottomRight().toString() );
		final Spreadsheet.Range range_B_2_C_4 = definedNames.get( "Range_B2C4_abs" );
		assertEquals( "Sheet1!$B$2", range_B_2_C_4.getTopLeft().toString() );
		assertEquals( "Sheet1!$C$4", range_B_2_C_4.getBottomRight().toString() );
		final Spreadsheet.Range rangeS1B2S3C4 = definedNames.get( "Range_Sheet1B2Sheet3C4_abs" );
		assertEquals( "Sheet1!$B$2", rangeS1B2S3C4.getTopLeft().toString() );
		assertEquals( "Sheet3!$C$4", rangeS1B2S3C4.getBottomRight().toString() );
		final Spreadsheet.Range range_S1_B_2_S3_C_4 = definedNames.get( "Range_Sheet1B2Sheet3C4_abs" );
		assertEquals( "Sheet1!$B$2", range_S1_B_2_S3_C_4.getTopLeft().toString() );
		assertEquals( "Sheet3!$C$4", range_S1_B_2_S3_C_4.getBottomRight().toString() );
	}

	@Test
	@IgnoreFormat( ".xls" )
	public void testRangeNamesUsage() throws Exception
	{
		final Map<String, Spreadsheet.Range> definedNames = this.spreadsheet.getRangeNames();
		assertEquals( 3, definedNames.size() );
		final Spreadsheet.Range rangeB2 = definedNames.get( "Cell" );
		assertEquals( "Sheet1!$B$1", rangeB2.getTopLeft().toString() );
		assertEquals( "Sheet1!$B$1", rangeB2.getBottomRight().toString() );
		final Spreadsheet.Range range_B_2 = definedNames.get( "Range2D" );
		assertEquals( "Sheet1!$B$2", range_B_2.getTopLeft().toString() );
		assertEquals( "Sheet1!$C$4", range_B_2.getBottomRight().toString() );
		final Spreadsheet.Range rangeB2C4 = definedNames.get( "Range3D" );
		assertEquals( "Sheet1!$B$5", rangeB2C4.getTopLeft().toString() );
		assertEquals( "Sheet2!$D$6", rangeB2C4.getBottomRight().toString() );

		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		assertEquals( "SUM( $B$1, $B$2:$C$4, $B$5:Sheet2!$D$6 )", sheet.getRows()[ 0 ].getCells()[ 0 ].getExpressionText() );
		assertEquals( "SUM( Cell, Range2D, Range3D )", sheet.getRows()[ 1 ].getCells()[ 0 ].getExpressionText() );
		assertEquals( "((SUM( Cell ) + SUM( Range2D )) + SUM( Range3D ))", sheet.getRows()[ 2 ].getCells()[ 0 ].getExpressionText() );
	}

	@Test
	public void testTextConstants() throws Exception
	{
		final SpreadsheetNameCreator creator = SpreadsheetCompiler.newSpreadsheetCellNameCreator( this.spreadsheet.getSheets()[ 0 ] );
		creator.createCellNamesFromRowTitles();

		{
			final String expected = " !\"#$%&'()*+,-./:;<=>?@{|}~";
			final Spreadsheet.Cell cell = this.spreadsheet.getCell( "BasicLatinSymbols" );
			final String actual = (String) cell.getConstantValue();
			assertEquals( expected, actual );
		}

		{
			final String expected = "ABCDXYZabcdxyz";
			final Spreadsheet.Cell cell = this.spreadsheet.getCell( "BasicLatinCharacters" );
			final String actual = (String) cell.getConstantValue();
			assertEquals( expected, actual );
		}

		{
			final String expected = "\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF";
			final Spreadsheet.Cell cell = this.spreadsheet.getCell( "Latin1Symbols" );
			final String actual = (String) cell.getConstantValue();
			assertEquals( expected, actual );
		}

		{
			final String expected = "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00FD\u00FE\u00FF";
			final Spreadsheet.Cell cell = this.spreadsheet.getCell( "Latin1Characters" );
			final String actual = (String) cell.getConstantValue();
			assertEquals( expected, actual );
		}

		{
			final String expected = "\u0401\u0404\u0406\u0407\u0410\u0411\u0412\u0413\u0414\u0430\u0431\u0432\u0433\u0434\u0451\u0454\u0456\u0457\u04E8\u04E9";
			final Spreadsheet.Cell cell = this.spreadsheet.getCell( "CyrillicCharacters" );
			final String actual = (String) cell.getConstantValue();
			assertEquals( expected, actual );
		}

		{
			final String expected = "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u0410\u0411\u0412\u0413\u0414\u0430\u0431\u0432\u0433\u0434";
			final Spreadsheet.Cell cell = this.spreadsheet.getCell( "MixedCharacters" );
			final String actual = (String) cell.getConstantValue();
			assertEquals( expected, actual );
		}
	}

	@Test
	@IgnoreFormat( ".xls" )
	public void testIntersections() throws Exception
	{
		final Spreadsheet.Sheet sheet = this.spreadsheet.getSheets()[ 0 ];
		final Spreadsheet.Row[] rows = sheet.getRows();
		assertEquals( "SUM( C1:E4 D2:F8 )", rows[ 0 ].getCells()[ 1 ].getExpressionText() );
		assertEquals( "SUM( C1:E8 D2:G8 D4:E12 E5:H10 )", rows[ 1 ].getCells()[ 1 ].getExpressionText() );
	}
}
