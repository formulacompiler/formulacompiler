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

package org.formulacompiler.tests.reference.base;

import java.io.File;
import java.io.FileInputStream;

import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.spreadsheet.SpreadsheetLoader;
import org.formulacompiler.spreadsheet.internal.SpreadsheetImpl;

public class SheetLoadingTestSuite extends AbstractContextTestSuite
{

	public SheetLoadingTestSuite( Context _cx )
	{
		super( _cx );
	}

	@Override
	protected String getOwnName()
	{
		return cx().getSpreadsheetFile().getName();
	}

	@Override
	protected void addTests() throws Exception
	{
		final Context main = cx();
		loadContext( main );
		for (Context variant : main.variants()) {
			loadContext( variant );
		}
	}

	private void loadContext( Context _cx ) throws Exception
	{
		final File file = _cx.getSpreadsheetFile();
		final String name = file.getName();
		final FileInputStream stream = new FileInputStream( file );
		final SpreadsheetLoader.Config cfg = new SpreadsheetLoader.Config();
		cfg.loadAllCellValues = true;
		_cx.setSpreadsheet( (SpreadsheetImpl) SpreadsheetCompiler.loadSpreadsheet( name, stream, cfg ) );
	}

	@Override
	protected void setUp() throws Throwable
	{
		super.setUp();
		cx().getDocumenter().beginFile( cx().getSpreadsheetFileBaseName() );
	}

	@Override
	protected void tearDown() throws Throwable
	{
		cx().getDocumenter().endFile();
		super.tearDown();
	}

}
