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
package sej.tests.reference;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import sej.EngineBuilder;
import sej.SEJ;
import sej.SaveableEngine;
import sej.internal.logging.Log;
import sej.internal.spreadsheet.SpreadsheetImpl;
import junit.framework.TestCase;

@SuppressWarnings("unchecked")
public abstract class AbstractWorkbookBasedTest extends TestCase
{
	public static final Log LOG = new Log();

	private static final File SPREADSHEET_PATH = new File( "src/test-system/testdata/sej/tests/reference" );
	
	protected final String baseName;
	protected final String spreadsheetName;

	protected static enum NumType {
		DOUBLE, BIGDECIMAL, LONG;
	}


	protected AbstractWorkbookBasedTest()
	{
		super();
		this.baseName = extractBaseNameFrom( getClass().getSimpleName() );
		this.spreadsheetName = this.baseName + ".xls";
	}

	protected AbstractWorkbookBasedTest(String _baseName)
	{
		super();
		this.baseName = _baseName;
		this.spreadsheetName = this.baseName + ".xls";
	}

	private static String extractBaseNameFrom( String _name )
	{
		if (_name.endsWith( "Test" )) {
			return _name.substring( 0, _name.length() - 4 );
		}
		else {
			return _name;
		}
	}

	
	protected static void writeStringTo( String _value, File _target ) throws IOException
	{
		BufferedWriter writer = new BufferedWriter( new FileWriter( _target ) );
		try {
			if (null != _value) writer.write( _value );
		}
		finally {
			writer.close();
		}
	}


	public void testExpressions() throws Exception
	{
		final EngineBuilder eb = SEJ.newEngineBuilder();
		eb.loadSpreadsheet( new File( SPREADSHEET_PATH, this.spreadsheetName ) );
		newSheetRunner( (SpreadsheetImpl) eb.getSpreadsheet() ).run();
	}

	protected abstract AbstractSheetRunner newSheetRunner( SpreadsheetImpl _impl );


	protected void reportTestRun( String _testName )
	{
		LOG.a( _testName ).lf().i();
	}

	protected void reportDefectiveEngine( SaveableEngine _engine, String _testName )
	{
		// overridable
	}

	protected void reportEndOfTestRun( String _testName )
	{
		LOG.o();
	}

	protected final String htmlize( String _text )
	{
		return _text.replace( "&", "&amp;" ).replace( "<", "&lt;" ).replace( ">", "&gt;" );
	}


	protected abstract class AbstractSheetRunner
	{
		protected final SpreadsheetImpl book;

		public AbstractSheetRunner(SpreadsheetImpl _book)
		{
			super();
			this.book = _book;
		}
		
		protected abstract void run() throws Exception;

	}

	protected static enum ValueType {
		NUMBER, STRING, DATE, BOOL;
	}

	protected static final ValueType valueTypeOf( Object _value )
	{
		if (_value instanceof String) return ValueType.STRING;
		if (_value instanceof Date) return ValueType.DATE;
		if (_value instanceof Boolean) return ValueType.BOOL;
		return ValueType.NUMBER;
	}

}
