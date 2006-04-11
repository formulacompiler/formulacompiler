/*
 * Copyright © 2006 by Abacus Research AG, Switzerland.
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
package sej.tests.usecases;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import sej.Compiler;
import sej.CompilerFactory;
import sej.Engine;
import sej.ModelError;
import sej.Settings;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.engine.standard.compiler.StandardCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;
import sej.loader.excel.xml.ExcelXMLLoader;
import junit.framework.TestCase;

abstract class AbstractUseCaseTest extends TestCase
{


	static {
		Settings.setDebugLogEnabled( true );
		ExcelXLSLoader.register();
		ExcelXMLLoader.register();
		StandardCompiler.registerAsDefault();
	}


	protected interface UseCase
	{

		public void defineEngine( Spreadsheet _model, Compiler.Section _root ) throws ModelError, SecurityException,
				NoSuchMethodException;


		public void useEngine( Engine _engine ) throws InvocationTargetException;

	}


	protected final void runUseCase( String _sheetFileName, UseCase _useCase, Class _inputs, Class _outputs )
			throws IOException, ModelError, SecurityException, NoSuchMethodException, InvocationTargetException
	{
		runUseCase( _sheetFileName, _useCase, ".xls", _inputs, _outputs );
		// runUseCase( _sheetFileName, _useCase, ".xml" );
	}


	private final void runUseCase( String _sheetFileName, UseCase _useCase, String _extension, Class _inputs,
			Class _outputs ) throws IOException, ModelError, SecurityException, NoSuchMethodException,
			InvocationTargetException
	{
		Spreadsheet model = SpreadsheetLoader.loadFromFile( "src/test-system/testdata/sej/usecases/"
				+ _sheetFileName + _extension );
		assertNotNull( "Model is null", model );
		runUseCase( model, _useCase, CompilerFactory.newDefaultCompiler( model, _inputs, _outputs ) );
	}


	private final void runUseCase( Spreadsheet _model, UseCase _useCase, Compiler _compiler ) throws ModelError,
			SecurityException, NoSuchMethodException, InvocationTargetException
	{
		_useCase.defineEngine( _model, _compiler.getRoot() );
		Engine engine = _compiler.compileNewEngine();
		_useCase.useEngine( engine );
	}

}
