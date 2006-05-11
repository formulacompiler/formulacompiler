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
package sej.tests.serialization;

import java.io.FileOutputStream;
import java.io.OutputStream;

import sej.CallFrame;
import sej.Compiler;
import sej.CompilerFactory;
import sej.NumericType;
import sej.Spreadsheet;
import sej.SpreadsheetLoader;
import sej.engine.standard.compiler.StandardCompiler;
import sej.loader.excel.xls.ExcelXLSLoader;

public abstract class AbstractSerializationTest extends AbstractTestBase
{

	static {
		ExcelXLSLoader.register();
		StandardCompiler.registerAsDefault();
	}


	public void testSerialization() throws Exception
	{
		serializeAndTest();
		deserializeAndTest();
	}


	private void serializeAndTest() throws Exception
	{
		Spreadsheet model = SpreadsheetLoader.loadFromFile( "src/test-system/testdata/sej/serialization/SerializationTest.xls" );
		Compiler compiler = CompilerFactory.newDefaultCompiler( model, Inputs.class, Outputs.class, getNumericType() );
		Compiler.Section root = compiler.getRoot();
		root.defineInputCell( model.getCell( 0, 1, 0 ), new CallFrame( Inputs.class.getMethod( "getA" + getTypeSuffix() ) ) );
		root.defineInputCell( model.getCell( 0, 1, 1 ), new CallFrame( Inputs.class.getMethod( "getB" + getTypeSuffix() ) ) );
		root.defineOutputCell( model.getCell( 0, 1, 2 ), new CallFrame( Outputs.class.getMethod( "getResult" + getTypeSuffix() ) ) );

		computeAndTestResult( compiler.compileNewEngine() );
		
		serialize( compiler );
	}


	protected abstract NumericType getNumericType();


	private void serialize( Compiler _compiler ) throws Exception
	{
		OutputStream outStream = new FileOutputStream( getEngineFile() );
		try {
			_compiler.saveTo( outStream );
		}
		finally {
			outStream.close();
		}
	}

	
}
