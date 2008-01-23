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
package org.formulacompiler.tests.usecases;

import java.io.File;

import org.formulacompiler.compiler.SaveableEngine;
import org.formulacompiler.spreadsheet.EngineBuilder;
import org.formulacompiler.spreadsheet.Spreadsheet;
import org.formulacompiler.spreadsheet.SpreadsheetBinder;
import org.formulacompiler.spreadsheet.SpreadsheetCompiler;

import junit.framework.TestCase;

abstract class AbstractUseCaseTest extends TestCase
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
		runUseCase( _sheetFileName, _useCase, ".ods", _inputs, _outputs );
		runUseCase( _sheetFileName, _useCase, ".xls", _inputs, _outputs );
	}


	private final void runUseCase( String _sheetFileBaseName, UseCase _useCase, String _extension, Class _inputs,
			Class _outputs ) throws Exception
	{
		final String sheetFileName = _sheetFileBaseName + _extension;
		EngineBuilder builder = SpreadsheetCompiler.newEngineBuilder();
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
