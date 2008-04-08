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

package org.formulacompiler.tests.usecases;

import java.io.File;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;
import org.formulacompiler.tests.MultiFormatTestFactory;

abstract class AbstractUseCaseTest extends MultiFormatTestFactory.SpreadsheetFormatTestCase
{
	private static final File SHEET_PATH = new File( "src/test/data/org/formulacompiler/tests/usecases" );


	protected interface UseCase
	{
		public void defineEngine( EngineBuilder _builder, Spreadsheet _model, SpreadsheetBinder.Section _root )
				throws Exception;

		public void useEngine( SaveableEngine _engine ) throws Exception;
	}


	protected final void runUseCase( String _sheetFileName, UseCase _useCase, Class _inputs, Class _outputs )
			throws Exception
	{
		final String sheetFileName = _sheetFileName + getSpreadsheetExtension();
		final EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
		builder.loadSpreadsheet( new File( SHEET_PATH, sheetFileName ) );
		builder.setInputClass( _inputs );
		builder.setOutputClass( _outputs );
		runUseCase( _useCase, builder );
	}


	private final void runUseCase( UseCase _useCase, EngineBuilder _builder ) throws Exception
	{
		_useCase.defineEngine( _builder, _builder.getSpreadsheet(), _builder.getRootBinder() );
		SaveableEngine engine = _builder.compile();
		_useCase.useEngine( engine );
	}

}
