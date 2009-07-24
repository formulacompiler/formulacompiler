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

package org.formulacompiler.spreadsheet.internal.excel.xls.loader;

import java.io.File;

import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.utils.AbstractSpreadsheetTestCase;


@SuppressWarnings( "unqualified-field-access" )
public class UnicodeTest extends AbstractSpreadsheetTestCase
{
	private Spreadsheet spreadsheet;

	@Override
	protected void setUp() throws Exception
	{
		final String path = "src/test/data/UnicodeTest.xls";
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( path );
		builder.createCellNamesFromRowTitles();
		spreadsheet = builder.getSpreadsheet();
	}

	public void testYaml() throws Exception
	{
		assertYaml( new File( "src/test/data" ), "UnicodeTest", spreadsheet, "UnicodeTest.xls" );
	}

	public void testBasicLatinSymbols()
	{
		final String expected = " !\"#$%&'()*+,-./:;<=>?@{|}~";
		final Spreadsheet.Cell cell = spreadsheet.getCell( "BasicLatinSymbols" );
		final String actual = (String) cell.getConstantValue();
		assertEquals( expected, actual );
	}

	public void testBasicLatinCharacters()
	{
		final String expected = "ABCDXYZabcdxyz";
		final Spreadsheet.Cell cell = spreadsheet.getCell( "BasicLatinCharacters" );
		final String actual = (String) cell.getConstantValue();
		assertEquals( expected, actual );
	}

	public void testLatin1Symbols()
	{
		final String expected = "\u00A0\u00A1\u00A2\u00A3\u00A4\u00A5\u00A6\u00A7\u00A8\u00A9\u00AA\u00AB\u00AC\u00AD\u00AE\u00AF\u00B0\u00B1\u00B2\u00B3\u00B4\u00B5\u00B6\u00B7\u00B8\u00B9\u00BA\u00BB\u00BC\u00BD\u00BE\u00BF";
		final Spreadsheet.Cell cell = spreadsheet.getCell( "Latin1Symbols" );
		final String actual = (String) cell.getConstantValue();
		assertEquals( expected, actual );
	}

	public void testLatin1Characters()
	{
		final String expected = "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00C6\u00C7\u00DD\u00DE\u00DF\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u00E6\u00E7\u00FD\u00FE\u00FF";
		final Spreadsheet.Cell cell = spreadsheet.getCell( "Latin1Characters" );
		final String actual = (String) cell.getConstantValue();
		assertEquals( expected, actual );
	}

	public void testCyrillicCharacters()
	{
		final String expected = "\u0401\u0404\u0406\u0407\u0410\u0411\u0412\u0413\u0414\u0430\u0431\u0432\u0433\u0434\u0451\u0454\u0456\u0457\u04E8\u04E9";
		final Spreadsheet.Cell cell = spreadsheet.getCell( "CyrillicCharacters" );
		final String actual = (String) cell.getConstantValue();
		assertEquals( expected, actual );
	}

	public void testMixedCharacters()
	{
		final String expected = "\u00C0\u00C1\u00C2\u00C3\u00C4\u00C5\u00E0\u00E1\u00E2\u00E3\u00E4\u00E5\u0410\u0411\u0412\u0413\u0414\u0430\u0431\u0432\u0433\u0434";
		final Spreadsheet.Cell cell = spreadsheet.getCell( "MixedCharacters" );
		final String actual = (String) cell.getConstantValue();
		assertEquals( expected, actual );
	}
}
