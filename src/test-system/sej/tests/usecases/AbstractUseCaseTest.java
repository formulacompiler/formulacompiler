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

import sej.Engine;
import sej.EngineBuilder;
import sej.Spreadsheet;
import sej.SpreadsheetBinder;
import sej.internal.EngineBuilderImpl;
import junit.framework.TestCase;

abstract class AbstractUseCaseTest extends TestCase
{


	protected interface UseCase
	{
		public void defineEngine( Spreadsheet _model, SpreadsheetBinder.Section _root ) throws Exception;
		public void useEngine( Engine _engine ) throws Exception;
	}


	protected final void runUseCase( String _sheetFileName, UseCase _useCase, Class _inputs, Class _outputs )
			throws Exception
	{
		runUseCase( _sheetFileName, _useCase, ".xls", _inputs, _outputs );
		// runUseCase( _sheetFileName, _useCase, ".xml" );
	}


	private final void runUseCase( String _sheetFileName, UseCase _useCase, String _extension, Class _inputs,
			Class _outputs ) throws Exception
	{
		EngineBuilder builder = new EngineBuilderImpl();
		builder.loadSpreadsheet( "src/test-system/testdata/sej/usecases/" + _sheetFileName + _extension );
		builder.setInputClass( _inputs );
		builder.setOutputClass( _outputs );
		runUseCase( _useCase, builder );
	}


	private final void runUseCase( UseCase _useCase, EngineBuilder _builder ) throws Exception
	{
		_useCase.defineEngine( _builder.getSpreadsheet(), _builder.getRootBinder() );
		Engine engine = _builder.compile();
		_useCase.useEngine( engine );
	}

}
