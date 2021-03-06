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

import org.formulacompiler.spreadsheet.internal.CellInstance;
import org.formulacompiler.spreadsheet.internal.CellWithConstant;
import org.formulacompiler.spreadsheet.internal.RowImpl;
import org.formulacompiler.spreadsheet.internal.SheetImpl;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;
import org.formulacompiler.tests.utils.IgnoreFormat;
import org.formulacompiler.tests.utils.MultiFormat;
import org.formulacompiler.tests.utils.SpreadsheetVerificationRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;


@RunWith( MultiFormat.class )
public class SaveTest
{
	private static final File TEST_FILES_DIR = new File( "src/test/data/org/formulacompiler/tests/spreadsheet/SaveTest" );

	@Rule
	public final SpreadsheetVerificationRule verifier;

	public SaveTest( final String _spreadsheetExtension, final String _templateExtension )
	{
		this.verifier = new SpreadsheetVerificationRule( _spreadsheetExtension, _templateExtension, TEST_FILES_DIR );
	}

	@Test
	public void testNullRow() throws Exception
	{
		final SpreadsheetImpl s = new SpreadsheetImpl();
		final SheetImpl sheet = new SheetImpl( s );
		sheet.getRowList().add( null );
		final RowImpl row = new RowImpl( sheet );
		new CellWithConstant( row, 1 );
		this.verifier.setSpreadsheet( s );
	}

	@Test
	public void testNullCell() throws Exception
	{
		final SpreadsheetImpl s = new SpreadsheetImpl();
		final SheetImpl sheet = new SheetImpl( s );
		final RowImpl row = new RowImpl( sheet );
		row.getCellList().add( null );
		new CellWithConstant( row, 1 );
		this.verifier.setSpreadsheet( s );
	}

	@Test
	public void testEmptyCell() throws Exception
	{
		final SpreadsheetImpl s = new SpreadsheetImpl();
		final SheetImpl sheet = new SheetImpl( s );
		final RowImpl row = new RowImpl( sheet );
		new CellWithConstant( row, null );
		new CellWithConstant( row, 1 );
		this.verifier.setSpreadsheet( s );
	}

	@Test
	@IgnoreFormat( ".xls" )
	public void testCellStyles() throws Exception
	{
		final SpreadsheetImpl s = new SpreadsheetImpl();
		final SheetImpl sheet = new SheetImpl( s );
		final RowImpl row = new RowImpl( sheet );
		final CellInstance cell1 = new CellWithConstant( row, 1 );
		cell1.setStyleName( "Style1" );
		final CellInstance cell2 = new CellWithConstant( row, "2" );
		cell2.setStyleName( "Style2" );
		this.verifier.setSpreadsheet( s );
	}
}
